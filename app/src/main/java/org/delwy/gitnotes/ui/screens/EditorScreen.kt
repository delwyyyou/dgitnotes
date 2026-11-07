package org.delwy.gitnotes.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.Capitalization
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import org.delwy.gitnotes.ui.GitNotesViewModel
import org.delwy.gitnotes.ui.components.ActionBar
import org.delwy.gitnotes.ui.components.BtnRole
import org.delwy.gitnotes.ui.components.DisplayBand
import org.delwy.gitnotes.ui.components.GitButton
import org.delwy.gitnotes.ui.components.Hamburger

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorScreen(
    vm: GitNotesViewModel,
    noteId: String,
    onBack: () -> Unit,
    onOpenHistory: (String) -> Unit,
    onMenu: () -> Unit
) {
    var content by remember(noteId) { mutableStateOf("") }
    var original by remember(noteId) { mutableStateOf("") }
    LaunchedEffect(noteId) {
        val c = vm.loadContent(noteId)
        content = c; original = c
    }
    val dirty = content != original
    val msg by vm.message.collectAsState()
    val snackbar = remember { SnackbarHostState() }
    LaunchedEffect(msg) { msg?.let { snackbar.showSnackbar(it); vm.clearMessage() } }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbar) },
        topBar = {
            TopAppBar(
                navigationIcon = {
                    Text(
                        "←",
                        modifier = Modifier
                            .padding(start = 16.dp)
                            .clip(RoundedCornerShape(50))
                            .background(Color.Transparent)
                            .padding(8.dp),
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    ).also { /* клик обрабатывается ниже через Box‑обёртку не нужен */ }
                },
                title = {
                    Text(
                        if (dirty) "● правка" else "заметка",
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                actions = { Hamburger(onClick = onMenu) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { inner ->
        Column(modifier = Modifier.fillMaxSize().padding(inner)) {
            DisplayBand(text = content.length.toString())

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(28.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainerHighest)
            ) {
                TextField(
                    value = content,
                    onValueChange = { content = it },
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState()),
                    placeholder = { Text("Начните писать…", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                    textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface),
                    keyboardOptions = KeyboardOptions(
                        capitalization = Capitalization.Sentences,
                        imeAction = ImeAction.Newline
                    ),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        cursorColor = MaterialTheme.colorScheme.primary
                    )
                )
            }

            ActionBar(
                { m ->
                    GitButton("AC", BtnRole.ACTION, {
                        content = original
                    }, m, enabled = dirty)
                },
                { m ->
                    GitButton("Удалить", BtnRole.ACTION, {
                        vm.deleteNote(noteId, onBack)
                    }, m)
                },
                { m ->
                    GitButton("История", BtnRole.OPERATOR, { onOpenHistory(noteId) }, m)
                },
                { m ->
                    GitButton("✓ Коммит", BtnRole.PRIMARY, {
                        val firstLine = content.lineSequence().firstOrNull { it.isNotBlank() }?.trim()?.take(50)
                        vm.commitNote(noteId, content, firstLine ?: "Правка заметки")
                        original = content
                    }, m, enabled = dirty)
                }
            )
        }
    }
}
