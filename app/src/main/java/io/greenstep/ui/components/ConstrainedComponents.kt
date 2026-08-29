package io.greenstep.ui.components

/**
 * Pill/label overflow guard: always use maxLines=1 + TextOverflow.Ellipsis + softWrap=false
 * for chips/tabs/buttons and widthIn(max=160.dp) + weight(1f) in Row to prevent
 * pill shape breakage on long locale (e.g. German) or accessibility large font.
 * Use PillChip/PillFilterChip/PillSuggestionChip/ConstrainedText/ConstrainedButton instead of raw components.
 */

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun ConstrainedText(
    text: String,
    modifier: Modifier = Modifier,
    maxLines: Int = 1,
    softWrap: Boolean = false,
    overflow: TextOverflow = TextOverflow.Ellipsis,
    style: androidx.compose.ui.text.TextStyle? = null,
) {
    if (style != null) {
        Text(
            text = text,
            modifier = modifier,
            maxLines = maxLines,
            softWrap = softWrap,
            overflow = overflow,
            style = style,
        )
    } else {
        Text(
            text = text,
            modifier = modifier,
            maxLines = maxLines,
            softWrap = softWrap,
            overflow = overflow,
        )
    }
}

@Composable
fun PillChip(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leadingIcon: @Composable (() -> Unit)? = null,
) {
    AssistChip(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.widthIn(max = 160.dp),
        label = {
            Text(
                text = label,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                softWrap = false,
                modifier = Modifier.widthIn(max = 160.dp),
            )
        },
        leadingIcon = leadingIcon,
    )
}

@Composable
fun PillSuggestionChip(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: @Composable (() -> Unit)? = null,
) {
    SuggestionChip(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.widthIn(max = 160.dp),
        label = {
            Text(
                text = label,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                softWrap = false,
                modifier = Modifier.widthIn(max = 160.dp),
            )
        },
        icon = icon,
    )
}

@Composable
fun PillFilterChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leadingIcon: @Composable (() -> Unit)? = null,
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.widthIn(max = 160.dp),
        label = {
            Text(
                text = label,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                softWrap = false,
                modifier = Modifier.widthIn(max = 160.dp),
            )
        },
        leadingIcon = leadingIcon,
    )
}

@Composable
fun ConstrainedButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable RowScope.() -> Unit = {
        Text(
            text = text,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            softWrap = false,
        )
    },
) {
    Button(
        onClick = onClick,
        modifier = modifier.widthIn(max = 280.dp),
        enabled = enabled,
        content = content,
    )
}

@Preview(showBackground = true, name = "PillChip long label")
@Composable
private fun PillChipLongPreview() {
    Row {
        PillChip(label = "Sehr langer Filtername Verlauf Vorschau", onClick = {}, modifier = Modifier.weight(1f, fill = false))
        PillSuggestionChip(label = "AssistChip mit extrem langem Text Beispiel", onClick = {}, modifier = Modifier.weight(1f, fill = false))
    }
}

@Preview(showBackground = true, name = "ConstrainedText long")
@Composable
private fun ConstrainedTextLongPreview() {
    Row {
        ConstrainedText(
            text = "Sehr langer Text der nicht umbrechen darf und mit Ellipsis endet",
            modifier = Modifier.weight(1f),
        )
    }
}

@Preview(showBackground = true, name = "Button long label")
@Composable
private fun ConstrainedButtonLongPreview() {
    Row {
        ConstrainedButton(text = "Sehr langer Button Text mit Ellipsis Verhalten", onClick = {}, modifier = Modifier.weight(1f, fill = false))
    }
}
