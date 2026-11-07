private fun commitsOf(repo: Repository, path: String?): List<CommitInfo> {
    val head = repo.resolve("HEAD") ?: return emptyList()
    val walk = RevWalk(repo)
    return try {
        walk.markStart(walk.parseCommit(head))
        walk.setRetainBody(false)

        val out = ArrayList<CommitInfo>()
        var count = 0
        for (c in walk) {
            count++
            if (count > 1000) break

            if (path == null || changedIn(repo, c, path)) {
                out += CommitInfo(
                    hash = c.id.name,
                    shortHash = c.id.name.take(7),
                    message = c.shortMessage?.trim() ?: "",
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
