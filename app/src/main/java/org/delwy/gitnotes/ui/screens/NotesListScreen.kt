package org.delwy.gitnotes.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.delwy.gitnotes.ui.GitNotesViewModel
import org.delwy.gitnotes.ui.components.ActionBar
import org.delwy.gitnotes.ui.components.BtnRole
import org.delwy.gitnotes.ui.components.DisplayBand
import org.delwy.gitnotes.ui.components.GitButton
import org.delwy.gitnotes.ui.components.Hamburger
import org.delwy.gitnotes.ui.components.NoteCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesListScreen(
    vm: GitNotesViewModel,
    onOpenNote: (String) -> Unit,
    onOpenHistory: (String) -> Unit,
    onOpenGlobalHistory: () -> Unit,
    onMenu: () -> Unit
) {
    val notes by vm.notes.collectAsState()
    val msg by vm.message.collectAsState()
    val snackbar = remember { SnackbarHostState() }

    LaunchedEffect(msg) {
        msg?.let { snackbar.showSnackbar(it); vm.clearMessage() }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbar) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Заметки",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                actions = { Hamburger(onClick = onMenu) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { inner ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner)
        ) {
            // Большая правая «строка дисплея» — количество заметок.
            DisplayBand(text = notes.size.toString())

            if (notes.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Пока пусто.\nНажмите «＋ Заметка».",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(notes, key = { it.id }) { n ->
                        NoteCard(
                            note = n,
                            onOpen = { onOpenNote(n.id) },
                            onHistory = { onOpenHistory(n.id) }
                        )
                    }
                    item { Spacer(Modifier.height(8.dp)) }
                }
            }

            ActionBar(
                { m -> GitButton("История всех", BtnRole.OPERATOR, onOpenGlobalHistory, m) },
                { m ->
                    GitButton(
                        "＋ Заметка",
                        BtnRole.PRIMARY,
                        { vm.createNote(onOpenNote) },
                        m
                    )
                }
            )
        }
    }
}
