package org.delwy.gitnotes.data

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class NoteSummary(
    val id: String,          // = имя файла без расширения
    val title: String,
    val snippet: String,
    val lastHash: String?,   // short hash последнего коммита по этому пути
    val lastDate: Long       // epoch millis, 0 если коммитов нет
)

data class CommitInfo(
    val hash: String,
    val shortHash: String,
    val message: String,
    val author: String,
    val date: Long
)

private val fmt = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
fun formatDate(ms: Long): String =
    if (ms <= 0L) "—" else fmt.format(Date(ms))
