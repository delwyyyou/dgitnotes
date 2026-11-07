package org.delwy.gitnotes.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

/** Роли кнопок, повторяющие цветовую семантику референса. */
enum class BtnRole { NEUTRAL, OPERATOR, ACTION, PRIMARY }

@Composable
fun btnColors(role: BtnRole): Pair<Color, Color> = when (role) {
    BtnRole.NEUTRAL -> MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.onSurface
    BtnRole.OPERATOR -> MaterialTheme.colorScheme.secondaryContainer to MaterialTheme.colorScheme.onSecondaryContainer
    BtnRole.ACTION -> MaterialTheme.colorScheme.tertiaryContainer to MaterialTheme.colorScheme.onTertiaryContainer
    BtnRole.PRIMARY -> MaterialTheme.colorScheme.primary to MaterialTheme.colorScheme.onPrimary
}

/** Большая «сквиркл»‑кнопка в стиле референса. */
@Composable
fun GitButton(
    text: String,
    role: BtnRole,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val (bg, fg) = btnColors(role)
    val shape = RoundedCornerShape(28.dp)
    Box(
        modifier = modifier
            .clip(shape)
            .background(if (enabled) bg else bg.copy(alpha = 0.4f))
            .clickable(
                enabled = enabled,
                interactionSource = remember { MutableInteractionSource() },
                indication = rememberRipple(color = fg)
            ) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (enabled) fg else fg.copy(alpha = 0.5f),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

/** Строка‑«дисплей» как большое правое число в калькуляторе. */
@Composable
fun DisplayBand(text: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 10.dp),
        contentAlignment = Alignment.CenterRight
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.displaySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/** Гамбургер из трёх штрихов — ровно как в референсе (без иконочных библиотек). */
@Composable
fun Hamburger(onClick: () -> Unit, modifier: Modifier = Modifier) {
    val line = MaterialTheme.colorScheme.onSurface
    Box(
        modifier = modifier
            .size(48.dp)
            .clip(RoundedCornerShape(50))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = rememberRipple(color = line)
            ) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            repeat(3) {
                Box(
                    Modifier
                        .width(22.dp)
                        .height(2.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(line)
                )
            }
        }
    }
}

/** Нижняя панель действий — ряд равных «сквиркл»‑кнопок. */
@Composable
fun ActionBar(vararg buttons: @Composable (Modifier) -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        buttons.forEach { b -> b(Modifier.weight(1f).height(72.dp)) }
    }
}

/** Небольшой чип для хэша коммита (роль ACTION/слива). */
@Composable
fun HashChip(hash: String?, modifier: Modifier = Modifier) {
    val bg = MaterialTheme.colorScheme.tertiaryContainer
    val fg = MaterialTheme.colorScheme.onTertiaryContainer
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(bg)
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(
            text = hash ?: "нет коммитов",
            color = fg,
            style = MaterialTheme.typography.labelMedium,
            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
        )
    }
}
