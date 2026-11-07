package org.delwy.gitnotes.data

import org.eclipse.jgit.api.Git
import org.eclipse.jgit.api.errors.EmptyCommitException
import org.eclipse.jgit.lib.ObjectId
import org.eclipse.jgit.lib.Repository
import org.eclipse.jgit.revwalk.RevCommit
import org.eclipse.jgit.revwalk.RevWalk
import org.eclipse.jgit.treewalk.TreeWalk
import org.eclipse.jgit.treewalk.filter.PathFilter
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

class GitNotesRepository(private val baseDir: File) {

    private val mutex = Mutex()
    private val authorName = "GitNotes"
    private val authorMail = "gitnotes@local"

    private fun fileName(id: String) = "$id.md"

    private suspend fun <T> withGit(block: (Git) -> T): T = withContext(Dispatchers.IO) {
        mutex.withLock {
            if (!baseDir.exists()) baseDir.mkdirs()
            val dotGit = File(baseDir, ".git")
            val git = if (dotGit.exists()) Git.open(baseDir)
            else Git.init().setDirectory(baseDir).setInitialBranch("main").call()
            try {
                block(git)
            } finally {
                runCatching { git.close() }
            }
        }
    }

    private fun <T> withGitSync(block: (Git) -> T): T {
        if (!baseDir.exists()) baseDir.mkdirs()
        val dotGit = File(baseDir, ".git")
        val git = if (dotGit.exists()) Git.open(baseDir)
        else Git.init().setDirectory(baseDir).setInitialBranch("main").call()
        return try {
            block(git)
        } finally {
            runCatching { git.close() }
        }
    }

    // ---------- ЧТЕНИЕ ----------

    suspend fun listNotes(): List<NoteSummary> = withContext(Dispatchers.IO) {
        if (!baseDir.exists()) return@withContext emptyList()
        val files = baseDir.listFiles()?.filter { it.isFile && it.name.endsWith(".md") }
            ?.sortedByDescending { it.lastModified() } ?: emptyList()

        files.map { f ->
            val id = f.name.removeSuffix(".md")
            val text = runCatching { f.readText() }.getOrDefault("")
            val lines = text.lineSequence().filter { it.isNotBlank() }.toList()
            val title = lines.firstOrNull()?.trim()?.take(60) ?: "Без названия"
            val snippet = lines.drop(1).joinToString(" ").trim().take(120)

            val lastCommit = runCatching {
                historySync(id).firstOrNull()
            }.getOrNull()

            NoteSummary(
                id = id,
                title = title,
                snippet = snippet,
                lastHash = lastCommit?.shortHash,
                lastDate = lastCommit?.date ?: 0L
            )
        }
    }

    suspend fun readNote(id: String): String = withContext(Dispatchers.IO) {
        val f = File(baseDir, fileName(id))
        if (f.exists()) runCatching { f.readText() }.getOrDefault("") else ""
    }

    // ---------- ЗАПИСЬ + КОММИТ ----------

    suspend fun createNote(): String = withContext(Dispatchers.IO) {
        if (!baseDir.exists()) baseDir.mkdirs()
        val id = java.util.UUID.randomUUID().toString()
        File(baseDir, fileName(id)).writeText("")
        withGit { g ->
            g.add().addFilepattern(fileName(id)).call()
            g.commit()
                .setMessage("Создание заметки")
                .setAuthor(authorName, authorMail)
                .setCommitter(authorName, authorMail)
                .setAllowEmpty(true)
                .call()
        }
        id
    }

    suspend fun commitNote(id: String, content: String, message: String): Boolean =
        withContext(Dispatchers.IO) {
            if (!baseDir.exists()) baseDir.mkdirs()
            File(baseDir, fileName(id)).writeText(content)
            try {
                withGit { g ->
                    g.add().addFilepattern(fileName(id)).call()
                    g.commit()
                        .setMessage(message.ifBlank { "Правка заметки" })
                        .setAuthor(authorName, authorMail)
                        .setCommitter(authorName, authorMail)
                        .call()
                }
                true
            } catch (_: EmptyCommitException) {
                false
            }
        }

    suspend fun deleteNote(id: String) = withContext(Dispatchers.IO) {
        withGit { g ->
            g.rm().addFilepattern(fileName(id)).call()
            g.commit()
                .setMessage("Удаление заметки")
                .setAuthor(authorName, authorMail)
                .setCommitter(authorName, authorMail)
                .call()
        }
    }

    // ---------- ИСТОРИЯ (git log -- path) ----------

    suspend fun history(noteId: String?): List<CommitInfo> = withContext(Dispatchers.IO) {
        withGit { g ->
            commitsOf(g, noteId?.let { fileName(it) })
        }
    }

    private fun historySync(noteId: String): List<CommitInfo> {
        val path = fileName(noteId)
        return withGitSync { g ->
            commitsOf(g, path)
        }
    }

    private fun commitsOf(git: Git, path: String?): List<CommitInfo> {
        val repo = git.repository
        val head = repo.resolve("HEAD") ?: return emptyList()

        val logCmd = git.log().add(head)
        if (path != null) logCmd.addPath(path)
        logCmd.setMaxCount(1000)

        val out = ArrayList<CommitInfo>()
        try {
            for (c in logCmd.call()) {
                out += CommitInfo(
                    hash = c.id.name,
                    shortHash = c.id.name.take(7),
                    message = c.shortMessage?.trim() ?: "",
                    author = c.authorIdent?.name ?: "—",
                    date = c.commitTime.toLong() * 1000L
                )
            }
        } catch (_: Exception) {
            // возвращаем то, что успели собрать
        }
        return out
    }

    // ---------- ОТКАТ ----------

    private fun blobIdAt(repo: Repository, c: RevCommit, path: String): ObjectId? {
        val tw = TreeWalk(repo)
        return try {
            tw.addTree(c.tree)
            tw.isRecursive = true
            tw.setFilter(PathFilter.create(path))
            if (tw.next()) tw.getObjectId(0) else null
        } finally {
            tw.close()
        }
    }

    suspend fun revertToCommit(id: String, hash: String): Boolean = withContext(Dispatchers.IO) {
        withGit { g ->
            val repo = g.repository
            val commitId = repo.resolve(hash) ?: return@withGit false

            val walk = RevWalk(repo)
            val bytes: ByteArray? = try {
                val commit = walk.parseCommit(commitId)
                val blob = blobIdAt(repo, commit, fileName(id))
                blob?.let { oid -> repo.open(oid).bytes }
            } finally {
                walk.dispose()
            }

            if (bytes != null) {
                File(baseDir, fileName(id)).writeBytes(bytes)
                g.add().addFilepattern(fileName(id)).call()
            } else {
                g.rm().addFilepattern(fileName(id)).call()
            }
            g.commit()
                .setMessage("Откат ${fileName(id)} к ${hash.take(7)}")
                .setAuthor(authorName, authorMail)
                .setCommitter(authorName, authorMail)
                .call()
            true
        }
    }
}
