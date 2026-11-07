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

    // ---------- чтение рабочего дерева ----------

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
            
            // Получаем последний коммит для этого файла
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

    // ---------- запись + коммит ----------

    suspend fun createNote(): String = withContext(Dispatchers.IO) {
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

    // ---------- история ----------

    suspend fun history(noteId: String?): List<CommitInfo> = withContext(Dispatchers.IO) {
        withGit { g ->
            if (noteId != null) {
                commitsOf(g.repository, fileName(noteId))
            } else {
                commitsOf(g.repository, null)
            }
        }
    }

    /** Синхронная версия для использования внутри listNotes */
    private fun historySync(noteId: String): List<CommitInfo> {
        val path = fileName(noteId)
        return try {
            val git = if (File(baseDir, ".git").exists()) Git.open(baseDir)
            else Git.init().setDirectory(baseDir).setInitialBranch("main").call()
            try {
                commitsOf(git.repository, path)
            } finally {
                runCatching { git.close() }
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun commitsOf(repo: Repository, path: String?): List<CommitInfo> {
        val head = repo.resolve("HEAD") ?: return emptyList()
        val walk = RevWalk(repo)
        return try {
            walk.markStart(walk.parseCommit(head))
            walk.setRetainBody(false) // Экономим память
            
            val out = ArrayList<CommitInfo>()
            var count = 0
            for (c in walk) {
                count++
                if (count > 1000) break // Ограничиваем глубину
                
                if (path == null || changedIn(repo, c, path)) {
                    // shortMessage возвращает ByteArray, конвертируем
                    val msgBytes = c.shortMessage
                    val msgStr = if (msgBytes != null) String(msgBytes, Charsets.UTF_8).trim() else ""
                    
                    out += CommitInfo(
                        hash = c.id.name,
                        shortHash = c.id.name.take(7),
                        message = msgStr,
                        author = c.authorIdent?.name ?: "—",
                        date = c.commitTime.toLong() * 1000L
                    )
                }
            }
            out
        } finally {
            walk.dispose()
        }
    }

    private fun changedIn(repo: Repository, c: RevCommit, path: String): Boolean {
        val cur = blobIdAt(repo, c, path)
        if (c.parentCount == 0) return cur != null
        for (p in c.parents) {
            if (blobIdAt(repo, p, path) != cur) return true
        }
        return false
    }

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

    // ---------- откат коммита ----------

    suspend fun revertToCommit(id: String, hash: String): Boolean = withContext(Dispatchers.IO) {
        withGit { g ->
            val repo = g.repository
            val commitId = repo.resolve(hash) ?: return@withGit false
            val walk = RevWalk(repo)
            val bytes: ByteArray? = try {
                val commit = walk.parseCommit(commitId)
                val blob = blobIdAt(repo, commit, fileName(id))
                blob?.let { repo.open(it).bytes }
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
