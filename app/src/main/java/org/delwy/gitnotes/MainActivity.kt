package org.delwy.gitnotes

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import org.delwy.gitnotes.ui.GitNotesViewModel
import org.delwy.gitnotes.ui.screens.EditorScreen
import org.delwy.gitnotes.ui.screens.HistoryScreen
import org.delwy.gitnotes.ui.screens.NotesListScreen
import org.delwy.gitnotes.ui.theme.GitNotesTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GitNotesRoot()
        }
    }
}

private object Route {
    const val LIST = "list"
    const val EDITOR = "editor/{noteId}"
    const val HISTORY = "history/{noteId}"
    fun editor(id: String) = "editor/$id"
    fun history(id: String?) = "history/${id ?: "global"}"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GitNotesRoot() {
    var darkTheme by remember { mutableStateOf(true) }
    val dynamicColor = remember { mutableStateOf(true) }
    var showMenu by remember { mutableStateOf(false) }
    var showAbout by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()
    val nav = rememberNavController()
    val vm: GitNotesViewModel = viewModel()

    GitNotesTheme(darkTheme = darkTheme, dynamicColor = dynamicColor.value) {
        AppNav(nav, vm, onMenu = { showMenu = true })

        if (showMenu) {
            MenuSheet(
                sheetState = sheetState,
                darkTheme = darkTheme,
                onDismiss = { showMenu = false },
                onToggleTheme = { darkTheme = !darkTheme },
                onGlobalHistory = { showMenu = false; nav.navigate(Route.history(null)) },
                onAbout = { showMenu = false; showAbout = true }
            )
        }

        if (showAbout) {
            AlertDialog(
                onDismissRequest = { showAbout = false },
                confirmButton = { TextButton(onClick = { showAbout = false }) { Text("Ок") } },
                title = { Text("GitNotes") },
                text = {
                    Text(
                        "Заметки с git‑историей на JGit.\n" +
                            "Каждая заметка — файл в рабочем дереве репозитория;\n" +
                            "коммиты, история и откат — настоящие git‑операции.\n\n" +
                            "Material 3 Expressive + Monet."
                    )
                }
            )
        }
    }
}

@Composable
private fun AppNav(nav: NavHostController, vm: GitNotesViewModel, onMenu: () -> Unit) {
    NavHost(navController = nav, startDestination = Route.LIST) {
        composable(Route.LIST) {
            NotesListScreen(
                vm = vm,
                onOpenNote = { nav.navigate(Route.editor(it)) },
                onOpenHistory = { nav.navigate(Route.history(it)) },
                onOpenGlobalHistory = { nav.navigate(Route.history(null)) },
                onMenu = onMenu
            )
        }
        composable(
            Route.EDITOR,
            arguments = listOf(navArgument("noteId") { type = NavType.StringType })
        ) { back ->
            val id = back.arguments?.getString("noteId").orEmpty()
            EditorScreen(
                vm = vm,
                noteId = id,
                onBack = { nav.popBackStack() },
                onOpenHistory = { nav.navigate(Route.history(it)) },
                onMenu = onMenu
            )
        }
        composable(
            Route.HISTORY,
            arguments = listOf(navArgument("noteId") { type = NavType.StringType })
        ) { back ->
            val raw = back.arguments?.getString("noteId")
            val id = if (raw == "global") null else raw
            HistoryScreen(
                vm = vm,
                noteId = id,
                onBack = { nav.popBackStack() },
                onMenu = onMenu
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MenuSheet(
    sheetState: SheetState,
    darkTheme: Boolean,
    onDismiss: () -> Unit,
    onToggleTheme: () -> Unit,
    onGlobalHistory: () -> Unit,
    onAbout: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
    ) {
        Column(Modifier.fillMaxWidth().padding(bottom = 24.dp)) {
            MenuRow("Глобальная история", onGlobalHistory)
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            MenuRow(if (darkTheme) "Светлая тема" else "Тёмная тема", onToggleTheme)
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            MenuRow("О приложении", onAbout)
        }
    }
}

@Composable
private fun MenuRow(label: String, onClick: () -> Unit) {
    TextButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            label,
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}
