package com.subreax.reaction.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.*
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.subreax.reaction.ui.theme.ReactionTheme

@Composable
fun LayoutTest() {
    Text("Hi there", modifier = Modifier.firstBaselineToTop(24.dp))
}

fun Modifier.firstBaselineToTop(
    firstBaselineToTop: Dp
) = layout { measurable, constraints ->
    val placeable = measurable.measure(constraints)
    check(placeable[FirstBaseline] != AlignmentLine.Unspecified)
    val firstBaseline = placeable[FirstBaseline]

    val offset = firstBaselineToTop.roundToPx() - firstBaseline
    val height = placeable.height + offset
    layout(placeable.width, height) {
        placeable.placeRelative(0, offset)
    }
}


@Preview
@Composable
fun LayoutTestPreview() {
    ReactionTheme {
        Surface {
            LayoutTest()
        }
    }
}



@Composable
fun MyBasicColumn(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Layout(modifier = modifier, content = content) { measurables, constraints ->
        val placeables = measurables.map {
            it.measure(constraints)
        }

        val width = placeables.maxOf { it.width }

        var height = 0
        placeables.forEach {
            height += it.height
        }

        layout(width, height) {
            var yPosition = 0

            placeables.forEach { placeable ->
                placeable.placeRelative(x = 0, y = yPosition)
                yPosition += placeable.height
            }
        }
    }
}

@Preview
@Composable
fun MyBasicColumnPreview() {
    Surface {
        MyBasicColumn {
            Text("My basic column")
            Text("places items")
            Text("vertically.")
            Text("We've done it by hand!")
        }
    }
}