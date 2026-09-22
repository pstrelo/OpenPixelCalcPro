package com.google.pixel.calculator.ui.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

/**
 * Icona Material Symbols Outlined 'square_foot' (squadretta da disegno per il Selettore Strumenti).
 * Fonte: https://fonts.gstatic.com/render/v1/Material+Symbols+Outlined/24dp/square_foot.kt?var=opsz,wght,FILL,GRAD,ROND@24,400,0,0,50
 */
val SquareFoot: ImageVector
    get() {
        if (_squareFoot != null) {
            return _squareFoot!!
        }
        _squareFoot = ImageVector.Builder(
            name = "square_foot",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            path(
                fill = SolidColor(Color.Black),
                fillAlpha = 1f,
                stroke = null,
                strokeAlpha = 1f,
                strokeLineWidth = 1f,
                strokeLineCap = StrokeCap.Butt,
                strokeLineJoin = StrokeJoin.Bevel,
                strokeLineMiter = 1f,
                pathFillType = PathFillType.NonZero,
            ) {
                moveTo(5.2f, 21f)
                quadTo(4.28f, 21f, 3.64f, 20.36f)
                reflectiveQuadTo(3f, 18.8f)
                verticalLineTo(5.1f)
                quadTo(3f, 4.38f, 3.68f, 4.09f)
                reflectiveQuadTo(4.85f, 4.3f)
                lineTo(7.1f, 6.55f)
                lineTo(5.75f, 7.9f)
                lineToRelative(0.7f, 0.7f)
                lineTo(7.8f, 7.25f)
                lineToRelative(2.6f, 2.6f)
                lineTo(9.05f, 11.2f)
                lineToRelative(0.7f, 0.7f)
                lineTo(11.1f, 10.55f)
                lineToRelative(2.6f, 2.6f)
                lineTo(12.35f, 14.5f)
                lineToRelative(0.7f, 0.7f)
                lineTo(14.4f, 13.85f)
                lineToRelative(2.6f, 2.6f)
                lineTo(15.65f, 17.8f)
                lineToRelative(0.7f, 0.7f)
                lineTo(17.7f, 17.15f)
                lineToRelative(2f, 2f)
                quadToRelative(0.5f, 0.5f, 0.21f, 1.18f)
                reflectiveQuadTo(18.9f, 21f)
                horizontalLineTo(5.2f)
                close()
                moveTo(6f, 18f)
                horizontalLineToRelative(8.3f)
                lineTo(6f, 9.7f)
                verticalLineTo(18f)
                close()
            }
        }.build()
        return _squareFoot!!
    }

private var _squareFoot: ImageVector? = null
