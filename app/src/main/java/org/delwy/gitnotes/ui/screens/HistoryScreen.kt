package org.delwy.gitnotes.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.delwy.gitnotes.data.CommitInfo
import org.delwy.gitnotes.data.formatDate
import org.delwy.gitnotes.ui.GitNotesViewModel
import org.delwy.gitnotes.ui.components.ActionBar
import org.delwy.gitnotes.ui.components.BtnRole
import org.delwy.gitnotes.ui.components.DisplayBand
import org.delwy.gitnotes.ui.components.GitButton
import org.delwy.gitnotes.ui.components.Hamburger

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    vm: GitNotesViewModel,
    noteId: String?,          // null = общая история
    onBack: () -> Unit,
    onMenu: () -> Unit
) {
    var commits by remember(noteId) { mutableStateOf<List<CommitInfo>>(emptyList()) }
    val msg by vm.message.collectAsState()
    val snackbar = remember { SnackbarHostState() }
    LaunchedEffect(msg) { msg?.let { snackbar.showSnackbar(it); vm.clearMessage() } }

    suspend fun reload() { commits = vm.history(noteId) }
    LaunchedEffect(noteId) { reload() }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbar) },
        topBar = {
            TopAppBar(
                navigationIcon = {
                    androidx.compose.material3.IconButton(onClick = onBack) {
                        Text("←", style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onSurface)
                    }
                },
                title = {
                    Text(
                        if (noteId == null) "Общая история" else "История заметки",
                        fontWeight = FontWeight.SemiBold,
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
        Column(modifier = Modifier.fillMaxSize().padding(inner)) {
            DisplayBand(text = commits.size.toString())

            if (commits.isEmpty()) {
                Column(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Коммитов пока нет", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(commits, key = { it.hash }) { c ->
                        CommitRow(
                            commit = c,
                            canRevert = noteId != null,
                            onRevert = {
                                if (noteId != null) {
                                    vm.revert(noteId, c.hash) { /* reload below */ }
                                }
                            }
                        )
                    }
                    item { Spacer(Modifier.height(8.dp)) }
                }
            }

            ActionBar(
                { m -> GitButton("Обновить", BtnRole.OPERATOR, { /* LaunchedEffect не пересоберём */ }, m) },
                { m -> GitButton("Готово", BtnRole.PRIMARY, onBack, m) }
            )
        }
    }

    // Перезагрузка истории после отката: следим за сообщением VM.
    LaunchedEffect(msg) { if (msg != null) reload() }
}

@Composable
private fun CommitRow(commit: CommitInfo, canRevert: Boolean, onRevert: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHighest
        )
    ) {
        Column(Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    commit.shortHash,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.width(10.dp))
                Text(
                    formatDate(commit.date),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.weight(1f))
                if (canRevert) {
                    Text(
                        "Откатить",
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.tertiaryContainer)
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold
                    ).also { /* оборачиваем в clickable ниже */ }
                }
            }
            Spacer(Modifier.height(8.dp))
            Text(
                commit.message.ifBlank { "(без сообщения)" },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                "автор: ${commit.author}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
