package org.delwy.gitnotes.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.delwy.gitnotes.data.CommitInfo
import org.delwy.gitnotes.data.GitNotesRepository
import org.delwy.gitnotes.data.NoteSummary
import java.io.File

class GitNotesViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = GitNotesRepository(File(app.filesDir, "gitnotes_repo"))

    private val _notes = MutableStateFlow<List<NoteSummary>>(emptyList())
    val notes: StateFlow<List<NoteSummary>> = _notes.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    init { refresh() }

    fun refresh() = viewModelScope.launch {
        _notes.value = runCatching { repo.listNotes() }
            .onFailure { _message.value = "Ошибка чтения: ${it.message}" }
            .getOrDefault(emptyList())
    }

    fun clearMessage() { _message.value = null }

    suspend fun loadContent(id: String): String = repo.readNote(id)

    fun createNote(onCreated: (String) -> Unit) = viewModelScope.launch {
        val id = runCatching { repo.createNote() }
            .onFailure { _message.value = "Не удалось создать: ${it.message}" }
            .getOrNull()
        if (id != null) { refresh(); onCreated(id) }
    }

    fun commitNote(id: String, content: String, message: String) = viewModelScope.launch {
        val ok = runCatching { repo.commitNote(id, content, message) }
            .onFailure { _message.value = "Ошибка коммита: ${it.message}" }
            .getOrDefault(false)
        _message.value = if (ok) "Коммит создан" else "Нет изменений для коммита"
        refresh()
    }

    fun deleteNote(id: String, onDone: () -> Unit) = viewModelScope.launch {
        runCatching { repo.deleteNote(id) }
            .onFailure { _message.value = "Ошибка удаления: ${it.message}" }
        refresh(); onDone()
    }

    suspend fun history(noteId: String?): List<CommitInfo> =
        runCatching { repo.history(noteId) }.getOrDefault(emptyList())

    fun revert(id: String, hash: String, onDone: () -> Unit) = viewModelScope.launch {
        val ok = runCatching { repo.revertToCommit(id, hash) }
            .onFailure { _message.value = "Ошибка отката: ${it.message}" }
            .getOrDefault(false)
        _message.value = if (ok) "Выполнен откат к ${hash.take(7)}" else "Коммит не найден"
        refresh(); onDone()
    }
}
