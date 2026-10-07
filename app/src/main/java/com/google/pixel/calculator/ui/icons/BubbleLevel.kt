package com.google.pixel.calculator.ui.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

private var _bubbleLevel: ImageVector? = null

/**
 * Custom Material Symbols icon representing a spirit / bubble level (vial with central bubble and indicator marks).
 */
val BubbleLevel: ImageVector
    get() {
        if (_bubbleLevel != null) {
            return _bubbleLevel!!
        }
        _bubbleLevel = ImageVector.Builder(
            name = "bubble_level",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            // Tubo esterno sagomato
            path(
                fill = SolidColor(Color.Black),
                fillAlpha = 1f,
                pathFillType = PathFillType.EvenOdd
            ) {
                // Rettangolo arrotondato esterno
                moveTo(7f, 7f)
                lineTo(17f, 7f)
                curveTo(19.76f, 7f, 22f, 9.24f, 22f, 12f)
                curveTo(22f, 14.76f, 19.76f, 17f, 17f, 17f)
                lineTo(7f, 17f)
                curveTo(4.24f, 17f, 2f, 14.76f, 2f, 12f)
                curveTo(2f, 9.24f, 4.24f, 7f, 7f, 7f)
                close()

                // Apertura fiala interna
                moveTo(17f, 9f)
                lineTo(7f, 9f)
                curveTo(5.34f, 9f, 4f, 10.34f, 4f, 12f)
                curveTo(4f, 13.66f, 5.34f, 15f, 7f, 15f)
                lineTo(17f, 15f)
                curveTo(18.66f, 15f, 20f, 13.66f, 20f, 12f)
                curveTo(20f, 10.34f, 18.66f, 9f, 17f, 9f)
                close()
            }
            // Bolla centrale d'aria
            path(
                fill = SolidColor(Color.Black),
                fillAlpha = 1f
            ) {
                moveTo(12f, 10.2f)
                curveTo(13f, 10.2f, 13.8f, 11f, 13.8f, 12f)
                curveTo(13.8f, 13f, 13f, 13.8f, 12f, 13.8f)
                curveTo(11f, 13.8f, 10.2f, 13f, 10.2f, 12f)
                curveTo(10.2f, 11f, 11f, 10.2f, 12f, 10.2f)
                close()
            }
            // Reference indicator tick marks for the bubble
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 1.2f,
                strokeLineCap = StrokeCap.Round
            ) {
                moveTo(9f, 8.8f)
                lineTo(9f, 15.2f)
                moveTo(15f, 8.8f)
                lineTo(15f, 15.2f)
            }
        }.build()
        return _bubbleLevel!!
    }
