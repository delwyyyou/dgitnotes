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

    fun clearMessage() { _message.value = null }

    fun refresh() = viewModelScope.launch {
        try {
            _notes.value = repo.listNotes()
        } catch (e: Exception) {
            _message.value = "Ошибка чтения: ${e.message}"
        }
    }

    suspend fun loadContent(id: String): String = repo.readNote(id)

    fun createNote(onCreated: (String) -> Unit) = viewModelScope.launch {
        try {
            val id: String = repo.createNote()
            refresh()
            onCreated(id)
        } catch (e: Exception) {
            _message.value = "Не удалось создать: ${e.message}"
        }
    }

    fun commitNote(id: String, content: String, message: String) = viewModelScope.launch {
        try {
            val ok: Boolean = repo.commitNote(id, content, message)
            _message.value = if (ok) "Коммит создан" else "Нет изменений для коммита"
            refresh()
        } catch (e: Exception) {
            _message.value = "Ошибка коммита: ${e.message}"
        }
    }

    fun deleteNote(id: String, onDone: () -> Unit) = viewModelScope.launch {
        try {
            repo.deleteNote(id)
            refresh()
            onDone()
        } catch (e: Exception) {
            _message.value = "Ошибка удаления: ${e.message}"
        }
    }

    suspend fun history(noteId: String?): List<CommitInfo> =
        try {
            repo.history(noteId)
        } catch (e: Exception) {
            emptyList()
        }

    fun revert(id: String, hash: String, onDone: () -> Unit) = viewModelScope.launch {
        try {
            val ok: Boolean = repo.revertToCommit(id, hash)
            _message.value = if (ok) "Выполнен откат к ${hash.take(7)}" else "Коммит не найден"
            refresh()
            onDone()
        } catch (e: Exception) {
            _message.value = "Ошибка отката: ${e.message}"
        }
    }
}
