package com.example.ui.components

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathBuilder
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

/**
 * Unified Iconify (Solar-inspired Rounded Geometric) vector icon set used across the entire app.
 * Every icon renders on a 24x24 viewport with crisp rounded caps/joins or bold filled geometry.
 */
object IconifyIcons {

    private val black = SolidColor(Color.Black)

    private inline fun strokeIcon(
        name: String,
        strokeWidth: Float = 1.85f,
        block: PathBuilder.() -> Unit
    ): ImageVector = ImageVector.Builder(
        name = "iconify:$name",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).path(
        fill = null,
        stroke = black,
        strokeLineWidth = strokeWidth,
        strokeLineCap = StrokeCap.Round,
        strokeLineJoin = StrokeJoin.Round,
        pathFillType = PathFillType.NonZero,
        pathBuilder = block
    ).build()

    private inline fun fillIcon(
        name: String,
        fillType: PathFillType = PathFillType.EvenOdd,
        block: PathBuilder.() -> Unit
    ): ImageVector = ImageVector.Builder(
        name = "iconify:$name",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).path(
        fill = black,
        pathFillType = fillType,
        pathBuilder = block
    ).build()

    private inline fun duotoneIcon(
        name: String,
        secondaryAlpha: Float = 0.36f,
        secondaryBlock: PathBuilder.() -> Unit,
        primaryBlock: PathBuilder.() -> Unit
    ): ImageVector = ImageVector.Builder(
        name = "iconify:$name",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).path(
        fill = black,
        fillAlpha = secondaryAlpha,
        pathFillType = PathFillType.NonZero,
        pathBuilder = secondaryBlock
    ).path(
        fill = black,
        fillAlpha = 1f,
        pathFillType = PathFillType.EvenOdd,
        pathBuilder = primaryBlock
    ).build()

    private fun PathBuilder.circle(cx: Float, cy: Float, r: Float) {
        moveTo(cx - r, cy)
        arcTo(r, r, 0f, isMoreThanHalf = true, isPositiveArc = true, cx + r, cy)
        arcTo(r, r, 0f, isMoreThanHalf = true, isPositiveArc = true, cx - r, cy)
        close()
    }

    private fun PathBuilder.roundedRect(l: Float, t: Float, r: Float, b: Float, rad: Float) {
        moveTo(l + rad, t)
        lineTo(r - rad, t)
        quadTo(r, t, r, t + rad)
        lineTo(r, b - rad)
        quadTo(r, b, r - rad, b)
        lineTo(l + rad, b)
        quadTo(l, b, l, b - rad)
        lineTo(l, t + rad)
        quadTo(l, t, l + rad, t)
        close()
    }

    // --- Navigation Tabs (Solar Bold & Linear) ---

    val HomeBold: ImageVector by lazy {
        fillIcon("solar:home-2-bold") {
            moveTo(12f, 2.5f)
            lineTo(3.4f, 9.2f)
            curveTo(2.8f, 9.7f, 2.5f, 10.4f, 2.5f, 11.2f)
            verticalLineTo(19f)
            curveTo(2.5f, 20.65f, 3.85f, 22f, 5.5f, 22f)
            horizontalLineTo(9.5f)
            verticalLineTo(16.2f)
            curveTo(9.5f, 14.9f, 10.5f, 14f, 12f, 14f)
            curveTo(13.5f, 14f, 14.5f, 14.9f, 14.5f, 16.2f)
            verticalLineTo(22f)
            horizontalLineTo(18.5f)
            curveTo(20.15f, 22f, 21.5f, 20.65f, 21.5f, 19f)
            verticalLineTo(11.2f)
            curveTo(21.5f, 10.4f, 21.2f, 9.7f, 20.6f, 9.2f)
            close()
        }
    }

    val HomeLinear: ImageVector by lazy {
        strokeIcon("solar:home-2-linear") {
            moveTo(3f, 10.2f)
            lineTo(12f, 3f)
            lineTo(21f, 10.2f)
            verticalLineTo(19f)
            curveTo(21f, 20.4f, 19.9f, 21.5f, 18.5f, 21.5f)
            horizontalLineTo(5.5f)
            curveTo(4.1f, 21.5f, 3f, 20.4f, 3f, 19f)
            close()
            moveTo(9.5f, 21.5f)
            verticalLineTo(16f)
            curveTo(9.5f, 14.9f, 10.4f, 14f, 11.5f, 14f)
            horizontalLineTo(12.5f)
            curveTo(13.6f, 14f, 14.5f, 14.9f, 14.5f, 16f)
            verticalLineTo(21.5f)
        }
    }

    val FolderBold: ImageVector by lazy {
        fillIcon("solar:folder-with-files-bold") {
            moveTo(2.5f, 6.5f)
            curveTo(2.5f, 4.85f, 3.85f, 3.5f, 5.5f, 3.5f)
            horizontalLineTo(9.3f)
            curveTo(10.1f, 3.5f, 10.85f, 3.85f, 11.35f, 4.45f)
            lineTo(12.6f, 6f)
            horizontalLineTo(18.5f)
            curveTo(20.15f, 6f, 21.5f, 7.35f, 21.5f, 9f)
            verticalLineTo(17.5f)
            curveTo(21.5f, 19.15f, 20.15f, 20.5f, 18.5f, 20.5f)
            horizontalLineTo(5.5f)
            curveTo(3.85f, 20.5f, 2.5f, 19.15f, 2.5f, 17.5f)
            close()
        }
    }

    val FolderLinear: ImageVector by lazy {
        strokeIcon("solar:folder-linear") {
            moveTo(3f, 7f)
            curveTo(3f, 5.35f, 4.35f, 4f, 6f, 4f)
            horizontalLineTo(9.4f)
            curveTo(10.2f, 4f, 10.9f, 4.35f, 11.4f, 4.95f)
            lineTo(12.6f, 6.5f)
            horizontalLineTo(18f)
            curveTo(19.65f, 6.5f, 21f, 7.85f, 21f, 9.5f)
            verticalLineTo(17f)
            curveTo(21f, 18.65f, 19.65f, 20f, 18f, 20f)
            horizontalLineTo(6f)
            curveTo(4.35f, 20f, 3f, 18.65f, 3f, 17f)
            close()
        }
    }

    val ToolsBold: ImageVector by lazy {
        fillIcon("solar:widget-4-bold") {
            roundedRect(3f, 3f, 10.8f, 10.8f, 2.8f)
            roundedRect(13.2f, 3f, 21f, 10.8f, 2.8f)
            roundedRect(3f, 13.2f, 10.8f, 21f, 2.8f)
            circle(17.1f, 17.1f, 3.9f)
        }
    }

    val ToolsLinear: ImageVector by lazy {
        strokeIcon("solar:widget-4-linear") {
            roundedRect(3.2f, 3.2f, 10.6f, 10.6f, 2.4f)
            roundedRect(13.4f, 3.2f, 20.8f, 10.6f, 2.4f)
            roundedRect(3.2f, 13.4f, 10.6f, 20.8f, 2.4f)
            circle(17.1f, 17.1f, 3.6f)
        }
    }

    val SettingsBold: ImageVector by lazy {
        fillIcon("solar:settings-bold") {
            moveTo(10.2f, 2.6f)
            horizontalLineTo(13.8f)
            lineTo(14.5f, 4.8f)
            curveTo(15.2f, 5.05f, 15.85f, 5.42f, 16.4f, 5.9f)
            lineTo(18.6f, 5.1f)
            lineTo(20.4f, 8.2f)
            lineTo(18.7f, 9.8f)
            curveTo(18.85f, 10.52f, 18.92f, 11.25f, 18.9f, 12f)
            curveTo(18.92f, 12.75f, 18.85f, 13.48f, 18.7f, 14.2f)
            lineTo(20.4f, 15.8f)
            lineTo(18.6f, 18.9f)
            lineTo(16.4f, 18.1f)
            curveTo(15.85f, 18.58f, 15.2f, 18.95f, 14.5f, 19.2f)
            lineTo(13.8f, 21.4f)
            horizontalLineTo(10.2f)
            lineTo(9.5f, 19.2f)
            curveTo(8.8f, 18.95f, 8.15f, 18.58f, 7.6f, 18.1f)
            lineTo(5.4f, 18.9f)
            lineTo(3.6f, 15.8f)
            lineTo(5.3f, 14.2f)
            curveTo(5.15f, 13.48f, 5.08f, 12.75f, 5.1f, 12f)
            curveTo(5.08f, 11.25f, 5.15f, 10.52f, 5.3f, 9.8f)
            lineTo(3.6f, 8.2f)
            lineTo(5.4f, 5.1f)
            lineTo(7.6f, 5.9f)
            curveTo(8.15f, 5.42f, 8.8f, 5.05f, 9.5f, 4.8f)
            close()
            circle(12f, 12f, 3.2f)
        }
    }

    val SettingsLinear: ImageVector by lazy {
        strokeIcon("solar:settings-linear") {
            circle(12f, 12f, 3f)
            moveTo(19.4f, 15f)
            arcTo(1.65f, 1.65f, 0f, false, false, 19.73f, 16.82f)
            lineTo(19.79f, 16.88f)
            arcTo(2f, 2f, 0f, true, true, 16.96f, 19.71f)
            lineTo(16.9f, 19.65f)
            arcTo(1.65f, 1.65f, 0f, false, false, 15.08f, 19.32f)
            arcTo(1.65f, 1.65f, 0f, false, false, 14.08f, 20.83f)
            verticalLineTo(21f)
            arcTo(2f, 2f, 0f, true, true, 10.08f, 21f)
            verticalLineTo(20.91f)
            arcTo(1.65f, 1.65f, 0f, false, false, 9f, 19.4f)
            arcTo(1.65f, 1.65f, 0f, false, false, 7.18f, 19.73f)
            lineTo(7.12f, 19.79f)
            arcTo(2f, 2f, 0f, true, true, 4.29f, 16.96f)
            lineTo(4.35f, 16.9f)
            arcTo(1.65f, 1.65f, 0f, false, false, 4.68f, 15.08f)
            arcTo(1.65f, 1.65f, 0f, false, false, 3.17f, 14.08f)
            horizontalLineTo(3f)
            arcTo(2f, 2f, 0f, true, true, 3f, 10.08f)
            horizontalLineTo(3.09f)
            arcTo(1.65f, 1.65f, 0f, false, false, 4.6f, 9f)
            arcTo(1.65f, 1.65f, 0f, false, false, 4.27f, 7.18f)
            lineTo(4.21f, 7.12f)
            arcTo(2f, 2f, 0f, true, true, 7.04f, 4.29f)
            lineTo(7.1f, 4.35f)
            arcTo(1.65f, 1.65f, 0f, false, false, 8.92f, 4.68f)
            horizontalLineTo(9f)
            arcTo(1.65f, 1.65f, 0f, false, false, 10f, 3.17f)
            verticalLineTo(3f)
            arcTo(2f, 2f, 0f, true, true, 14f, 3f)
            verticalLineTo(3.09f)
            arcTo(1.65f, 1.65f, 0f, false, false, 15f, 4.6f)
            arcTo(1.65f, 1.65f, 0f, false, false, 16.82f, 4.27f)
            lineTo(16.88f, 4.21f)
            arcTo(2f, 2f, 0f, true, true, 19.71f, 7.04f)
            lineTo(19.65f, 7.1f)
            arcTo(1.65f, 1.65f, 0f, false, false, 19.32f, 8.92f)
            verticalLineTo(9f)
            arcTo(1.65f, 1.65f, 0f, false, false, 20.83f, 10f)
            horizontalLineTo(21f)
            arcTo(2f, 2f, 0f, true, true, 21f, 14f)
            horizontalLineTo(20.91f)
            arcTo(1.65f, 1.65f, 0f, false, false, 19.4f, 15f)
            close()
        }
    }

    // --- Common Chrome & Navigation ---

    val Search: ImageVector by lazy {
        strokeIcon("solar:magnifer-linear") {
            circle(11f, 11f, 7.2f)
            moveTo(16.4f, 16.4f)
            lineTo(20.6f, 20.6f)
        }
    }

    val Close: ImageVector by lazy {
        strokeIcon("solar:close-linear", strokeWidth = 2.0f) {
            moveTo(6f, 6f)
            lineTo(18f, 18f)
            moveTo(18f, 6f)
            lineTo(6f, 18f)
        }
    }

    val ArrowLeft: ImageVector by lazy {
        strokeIcon("solar:arrow-left-linear") {
            moveTo(19f, 12f)
            horizontalLineTo(5f)
            moveTo(11f, 6f)
            lineTo(5f, 12f)
            lineTo(11f, 18f)
        }
    }

    val ChevronRight: ImageVector by lazy {
        strokeIcon("solar:alt-arrow-right-linear") {
            moveTo(9f, 5.5f)
            lineTo(15.5f, 12f)
            lineTo(9f, 18.5f)
        }
    }

    val ChevronLeft: ImageVector by lazy {
        strokeIcon("solar:alt-arrow-left-linear") {
            moveTo(15f, 5.5f)
            lineTo(8.5f, 12f)
            lineTo(15f, 18.5f)
        }
    }

    val ChevronDown: ImageVector by lazy {
        strokeIcon("solar:alt-arrow-down-linear") {
            moveTo(5.5f, 9f)
            lineTo(12f, 15.5f)
            lineTo(18.5f, 9f)
        }
    }

    val Tune: ImageVector by lazy {
        strokeIcon("solar:tuning-2-linear") {
            moveTo(4f, 7f)
            horizontalLineTo(12f)
            moveTo(18f, 7f)
            horizontalLineTo(20f)
            circle(15f, 7f, 2.5f)
            moveTo(4f, 17f)
            horizontalLineTo(7f)
            moveTo(13f, 17f)
            horizontalLineTo(20f)
            circle(10f, 17f, 2.5f)
        }
    }

    val Sort: ImageVector by lazy {
        strokeIcon("solar:sort-from-top-to-bottom-linear") {
            moveTo(4f, 7f)
            horizontalLineTo(13f)
            moveTo(4f, 12f)
            horizontalLineTo(11f)
            moveTo(4f, 17f)
            horizontalLineTo(9f)
            moveTo(17f, 6f)
            verticalLineTo(18f)
            moveTo(14f, 15f)
            lineTo(17f, 18f)
            lineTo(20f, 15f)
        }
    }

    val GridView: ImageVector by lazy {
        strokeIcon("solar:widget-linear") {
            roundedRect(3.5f, 3.5f, 10.2f, 10.2f, 2.2f)
            roundedRect(13.8f, 3.5f, 20.5f, 10.2f, 2.2f)
            roundedRect(3.5f, 13.8f, 10.2f, 20.5f, 2.2f)
            roundedRect(13.8f, 13.8f, 20.5f, 20.5f, 2.2f)
        }
    }

    val ListView: ImageVector by lazy {
        strokeIcon("solar:list-linear") {
            circle(5f, 7f, 1.1f)
            moveTo(9f, 7f)
            horizontalLineTo(20f)
            circle(5f, 12f, 1.1f)
            moveTo(9f, 12f)
            horizontalLineTo(20f)
            circle(5f, 17f, 1.1f)
            moveTo(9f, 17f)
            horizontalLineTo(20f)
        }
    }

    // --- Desk & Productivity ---

    val Sparkles: ImageVector by lazy {
        fillIcon("solar:stars-minimalistic-bold") {
            moveTo(10f, 3f)
            curveTo(10.5f, 7.2f, 11.8f, 8.5f, 16f, 9f)
            curveTo(11.8f, 9.5f, 10.5f, 10.8f, 10f, 15f)
            curveTo(9.5f, 10.8f, 8.2f, 9.5f, 4f, 9f)
            curveTo(8.2f, 8.5f, 9.5f, 7.2f, 10f, 3f)
            close()
            moveTo(18f, 13.5f)
            curveTo(18.3f, 15.8f, 19f, 16.5f, 21.3f, 16.8f)
            curveTo(19f, 17.1f, 18.3f, 17.8f, 18f, 20.1f)
            curveTo(17.7f, 17.8f, 17f, 17.1f, 14.7f, 16.8f)
            curveTo(17f, 16.5f, 17.7f, 15.8f, 18f, 13.5f)
            close()
        }
    }

    val EditNote: ImageVector by lazy {
        strokeIcon("solar:pen-new-square-linear") {
            moveTo(11f, 4f)
            horizontalLineTo(6.5f)
            curveTo(4.85f, 4f, 3.5f, 5.35f, 3.5f, 7f)
            verticalLineTo(17.5f)
            curveTo(3.5f, 19.15f, 4.85f, 20.5f, 6.5f, 20.5f)
            horizontalLineTo(17f)
            curveTo(18.65f, 20.5f, 20f, 19.15f, 20f, 17.5f)
            verticalLineTo(13f)
            moveTo(18.5f, 3.5f)
            lineTo(20.5f, 5.5f)
            lineTo(12f, 14f)
            horizontalLineTo(9.5f)
            verticalLineTo(11.5f)
            close()
        }
    }

    val StickyNote: ImageVector by lazy {
        strokeIcon("solar:document-text-linear") {
            moveTo(6.5f, 3.5f)
            horizontalLineTo(14.5f)
            lineTo(19f, 8f)
            verticalLineTo(18f)
            curveTo(19f, 19.65f, 17.65f, 21f, 16f, 21f)
            horizontalLineTo(6.5f)
            curveTo(4.85f, 21f, 3.5f, 19.65f, 3.5f, 18f)
            verticalLineTo(6.5f)
            curveTo(3.5f, 4.85f, 4.85f, 3.5f, 6.5f, 3.5f)
            close()
            moveTo(14f, 3.5f)
            verticalLineTo(8f)
            horizontalLineTo(19f)
            moveTo(7.5f, 12.5f)
            horizontalLineTo(15f)
            moveTo(7.5f, 16.5f)
            horizontalLineTo(12.5f)
        }
    }

    val Checklist: ImageVector by lazy {
        strokeIcon("solar:checklist-minimalistic-linear") {
            moveTo(3.5f, 7.5f)
            lineTo(5.3f, 9.3f)
            lineTo(8.5f, 5.8f)
            moveTo(11.5f, 7.5f)
            horizontalLineTo(20.5f)
            moveTo(3.5f, 13.5f)
            lineTo(5.3f, 15.3f)
            lineTo(8.5f, 11.8f)
            moveTo(11.5f, 13.5f)
            horizontalLineTo(20.5f)
            moveTo(11.5f, 18.5f)
            horizontalLineTo(20.5f)
            circle(5.5f, 18.5f, 1.3f)
        }
    }

    val ShoppingBag: ImageVector by lazy {
        strokeIcon("solar:bag-4-linear") {
            moveTo(4.5f, 8.5f)
            horizontalLineTo(19.5f)
            lineTo(18.4f, 19.2f)
            curveTo(18.25f, 20.2f, 17.4f, 21f, 16.3f, 21f)
            horizontalLineTo(7.7f)
            curveTo(6.6f, 21f, 5.75f, 20.2f, 5.6f, 19.2f)
            close()
            moveTo(8.5f, 8.5f)
            verticalLineTo(6.5f)
            curveTo(8.5f, 4.55f, 10.05f, 3f, 12f, 3f)
            curveTo(13.95f, 3f, 15.5f, 4.55f, 15.5f, 6.5f)
            verticalLineTo(8.5f)
        }
    }

    val Mic: ImageVector by lazy {
        strokeIcon("solar:microphone-3-linear") {
            roundedRect(9f, 3f, 15f, 13f, 3f)
            moveTo(5.5f, 11f)
            curveTo(5.5f, 14.6f, 8.4f, 17.5f, 12f, 17.5f)
            curveTo(15.6f, 17.5f, 18.5f, 14.6f, 18.5f, 11f)
            moveTo(12f, 17.5f)
            verticalLineTo(21f)
            moveTo(9f, 21f)
            horizontalLineTo(15f)
        }
    }

    val Camera: ImageVector by lazy {
        strokeIcon("solar:camera-linear") {
            moveTo(3f, 8.5f)
            curveTo(3f, 7.1f, 4.1f, 6f, 5.5f, 6f)
            horizontalLineTo(7.5f)
            lineTo(9f, 4f)
            horizontalLineTo(15f)
            lineTo(16.5f, 6f)
            horizontalLineTo(18.5f)
            curveTo(19.9f, 6f, 21f, 7.1f, 21f, 8.5f)
            verticalLineTo(17.5f)
            curveTo(21f, 18.9f, 19.9f, 20f, 18.5f, 20f)
            horizontalLineTo(5.5f)
            curveTo(4.1f, 20f, 3f, 18.9f, 3f, 17.5f)
            close()
            circle(12f, 13f, 3.2f)
        }
    }

    val MindMap: ImageVector by lazy {
        strokeIcon("solar:diagram-up-linear") {
            circle(6f, 12f, 2.5f)
            circle(18f, 6.5f, 2.3f)
            circle(18f, 17.5f, 2.3f)
            moveTo(8.3f, 11f)
            lineTo(15.8f, 7.3f)
            moveTo(8.3f, 13f)
            lineTo(15.8f, 16.7f)
        }
    }

    val FolderOpen: ImageVector by lazy {
        strokeIcon("solar:folder-open-linear") {
            moveTo(3f, 8f)
            curveTo(3f, 6.35f, 4.35f, 5f, 6f, 5f)
            horizontalLineTo(9.4f)
            lineTo(11.3f, 7.2f)
            horizontalLineTo(18f)
            curveTo(19.2f, 7.2f, 20.1f, 8f, 20.4f, 9f)
            moveTo(3f, 18.5f)
            lineTo(5.2f, 11.2f)
            curveTo(5.5f, 10.3f, 6.3f, 9.8f, 7.2f, 9.8f)
            horizontalLineTo(19.6f)
            curveTo(20.8f, 9.8f, 21.6f, 10.9f, 21.2f, 12f)
            lineTo(19.2f, 18.2f)
            curveTo(18.9f, 19.1f, 18.1f, 19.7f, 17.1f, 19.7f)
            horizontalLineTo(4.5f)
            curveTo(3.5f, 19.7f, 2.7f, 19.2f, 3f, 18.5f)
            close()
        }
    }

    private fun ImageVector.Builder.addFlatFlaticonFileSheet(
        bodyColor: Color,
        foldColor: Color
    ) {
        // Solid flat rounded sheet with 45-degree cut top-right dog-ear corner (matches file-types-21 flat style)
        path(fill = SolidColor(bodyColor)) {
            moveTo(7.4f, 0.5f)
            horizontalLineTo(19.0f)
            lineTo(28.0f, 9.5f)
            verticalLineTo(28.1f)
            curveTo(28.0f, 29.98f, 26.48f, 31.5f, 24.6f, 31.5f)
            horizontalLineTo(7.4f)
            curveTo(5.52f, 31.5f, 4.0f, 29.98f, 4.0f, 28.1f)
            verticalLineTo(3.9f)
            curveTo(4.0f, 2.02f, 5.52f, 0.5f, 7.4f, 0.5f)
            close()
        }
        // Lighter tinted top-right folded corner triangle
        path(fill = SolidColor(foldColor)) {
            moveTo(19.0f, 0.5f)
            lineTo(28.0f, 9.5f)
            horizontalLineTo(19.0f)
            close()
        }
    }

    private fun PathBuilder.addBoldFilledP(x0: Float) {
        moveTo(x0, 17.5f)
        lineTo(x0 + 2.95f, 17.5f)
        curveTo(x0 + 5.15f, 17.5f, x0 + 5.15f, 22.4f, x0 + 2.95f, 22.4f)
        lineTo(x0 + 1.8f, 22.4f)
        lineTo(x0 + 1.8f, 25.3f)
        lineTo(x0, 25.3f)
        close()
        // Counter hole
        moveTo(x0 + 1.8f, 19.0f)
        lineTo(x0 + 2.8f, 19.0f)
        curveTo(x0 + 3.55f, 19.0f, x0 + 3.55f, 20.9f, x0 + 2.8f, 20.9f)
        lineTo(x0 + 1.8f, 20.9f)
        close()
    }

    private fun PathBuilder.addBoldFilledD(x0: Float) {
        moveTo(x0, 17.5f)
        lineTo(x0 + 2.45f, 17.5f)
        curveTo(x0 + 6.15f, 17.5f, x0 + 6.15f, 25.3f, x0 + 2.45f, 25.3f)
        lineTo(x0, 25.3f)
        close()
        // Counter hole
        moveTo(x0 + 1.8f, 19.05f)
        lineTo(x0 + 2.35f, 19.05f)
        curveTo(x0 + 4.3f, 19.05f, x0 + 4.3f, 23.75f, x0 + 2.35f, 23.75f)
        lineTo(x0 + 1.8f, 23.75f)
        close()
    }

    // --- File Type Icons (Flaticon file-types-21 flat solid-color folded-corner style) ---

    val FilePdf: ImageVector by lazy {
        ImageVector.Builder(
            name = "flaticon:file-types-21:pdf",
            defaultWidth = 32.dp,
            defaultHeight = 32.dp,
            viewportWidth = 32f,
            viewportHeight = 32f
        ).apply {
            addFlatFlaticonFileSheet(
                bodyColor = Color(0xFFE72325),
                foldColor = Color(0xFFF37476)
            )
            path(
                fill = SolidColor(Color.White),
                pathFillType = PathFillType.EvenOdd
            ) {
                addBoldFilledP(7.75f)
                addBoldFilledD(13.25f)
                // F
                moveTo(19.65f, 17.5f)
                lineTo(24.25f, 17.5f)
                lineTo(24.25f, 19.05f)
                lineTo(21.45f, 19.05f)
                lineTo(21.45f, 20.7f)
                lineTo(23.85f, 20.7f)
                lineTo(23.85f, 22.25f)
                lineTo(21.45f, 22.25f)
                lineTo(21.45f, 25.3f)
                lineTo(19.65f, 25.3f)
                close()
            }
        }.build()
    }

    val FileWord: ImageVector by lazy {
        ImageVector.Builder(
            name = "flaticon:file-types-21:doc",
            defaultWidth = 32.dp,
            defaultHeight = 32.dp,
            viewportWidth = 32f,
            viewportHeight = 32f
        ).apply {
            addFlatFlaticonFileSheet(
                bodyColor = Color(0xFF2878E5),
                foldColor = Color(0xFF7CB2F4)
            )
            path(
                fill = SolidColor(Color.White),
                pathFillType = PathFillType.NonZero
            ) {
                // Bold W centered on sheet
                moveTo(9.2f, 13.8f)
                lineTo(11.4f, 13.8f)
                lineTo(12.8f, 21.3f)
                lineTo(15.05f, 15.6f)
                lineTo(16.95f, 15.6f)
                lineTo(19.2f, 21.3f)
                lineTo(20.6f, 13.8f)
                lineTo(22.8f, 13.8f)
                lineTo(20.4f, 25.2f)
                lineTo(18.2f, 25.2f)
                lineTo(16.0f, 19.2f)
                lineTo(13.8f, 25.2f)
                lineTo(11.6f, 25.2f)
                close()
            }
        }.build()
    }

    val FileExcel: ImageVector by lazy {
        ImageVector.Builder(
            name = "flaticon:file-types-21:xls",
            defaultWidth = 32.dp,
            defaultHeight = 32.dp,
            viewportWidth = 32f,
            viewportHeight = 32f
        ).apply {
            addFlatFlaticonFileSheet(
                bodyColor = Color(0xFF21A366),
                foldColor = Color(0xFF76D0A5)
            )
            path(
                fill = SolidColor(Color.White),
                pathFillType = PathFillType.NonZero
            ) {
                // Bold X centered on sheet
                moveTo(10.8f, 13.8f)
                lineTo(13.5f, 13.8f)
                lineTo(21.2f, 25.2f)
                lineTo(18.5f, 25.2f)
                close()
                moveTo(18.5f, 13.8f)
                lineTo(21.2f, 13.8f)
                lineTo(13.5f, 25.2f)
                lineTo(10.8f, 25.2f)
                close()
            }
        }.build()
    }

    val FilePpt: ImageVector by lazy {
        ImageVector.Builder(
            name = "flaticon:file-types-21:ppt",
            defaultWidth = 32.dp,
            defaultHeight = 32.dp,
            viewportWidth = 32f,
            viewportHeight = 32f
        ).apply {
            addFlatFlaticonFileSheet(
                bodyColor = Color(0xFFF16821),
                foldColor = Color(0xFFF8AC82)
            )
            path(
                fill = SolidColor(Color.White),
                pathFillType = PathFillType.EvenOdd
            ) {
                // Bold P centered on sheet
                moveTo(11.8f, 13.8f)
                lineTo(16.6f, 13.8f)
                curveTo(21.4f, 13.8f, 21.4f, 21.0f, 16.6f, 21.0f)
                lineTo(14.3f, 21.0f)
                lineTo(14.3f, 25.2f)
                lineTo(11.8f, 25.2f)
                close()
                // Counter hole
                moveTo(14.3f, 15.9f)
                lineTo(16.3f, 15.9f)
                curveTo(18.5f, 15.9f, 18.5f, 18.9f, 16.3f, 18.9f)
                lineTo(14.3f, 18.9f)
                close()
            }
        }.build()
    }

    val FileNote: ImageVector by lazy {
        ImageVector.Builder(
            name = "flaticon:file-types-21:note",
            defaultWidth = 32.dp,
            defaultHeight = 32.dp,
            viewportWidth = 32f,
            viewportHeight = 32f
        ).apply {
            addFlatFlaticonFileSheet(
                bodyColor = Color(0xFFF5A623),
                foldColor = Color(0xFFFAD38B)
            )
            path(
                fill = SolidColor(Color.White),
                pathFillType = PathFillType.NonZero
            ) {
                // Bold N centered on sheet
                moveTo(10.8f, 13.8f)
                lineTo(13.3f, 13.8f)
                lineTo(18.8f, 21.6f)
                lineTo(18.8f, 13.8f)
                lineTo(21.2f, 13.8f)
                lineTo(21.2f, 25.2f)
                lineTo(18.7f, 25.2f)
                lineTo(13.2f, 17.4f)
                lineTo(13.2f, 25.2f)
                lineTo(10.8f, 25.2f)
                close()
            }
        }.build()
    }

    val LockFile: ImageVector by lazy {
        ImageVector.Builder(
            name = "flaticon:file-types-21:locked",
            defaultWidth = 32.dp,
            defaultHeight = 32.dp,
            viewportWidth = 32f,
            viewportHeight = 32f
        ).apply {
            addFlatFlaticonFileSheet(
                bodyColor = Color(0xFF6745D6),
                foldColor = Color(0xFFAC98EE)
            )
            // Bold white padlock body with keyhole cutout
            path(
                fill = SolidColor(Color.White),
                pathFillType = PathFillType.EvenOdd
            ) {
                roundedRect(10.8f, 19.2f, 21.2f, 27.0f, 1.8f)
                circle(16.0f, 22.8f, 1.15f)
            }
            // Padlock shackle
            path(
                fill = null,
                stroke = SolidColor(Color.White),
                strokeLineWidth = 1.9f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(13.0f, 19.3f)
                verticalLineTo(16.8f)
                curveTo(13.0f, 15.0f, 14.3f, 13.7f, 16.0f, 13.7f)
                curveTo(17.7f, 13.7f, 19.0f, 15.0f, 19.0f, 16.8f)
                verticalLineTo(19.3f)
            }
        }.build()
    }

    val FilePdfLinear: ImageVector by lazy {
        strokeIcon("solar:file-text-linear") {
            moveTo(6f, 3f)
            horizontalLineTo(14.5f)
            lineTo(19f, 7.5f)
            verticalLineTo(18.5f)
            curveTo(19f, 19.9f, 17.9f, 21f, 16.5f, 21f)
            horizontalLineTo(6.5f)
            curveTo(5.1f, 21f, 4f, 19.9f, 4f, 18.5f)
            verticalLineTo(5.5f)
            curveTo(4f, 4.1f, 5.1f, 3f, 6.5f, 3f)
            close()
            moveTo(8f, 12f)
            horizontalLineTo(15f)
            moveTo(8f, 16f)
            horizontalLineTo(13f)
        }
    }

    val Lock: ImageVector by lazy {
        fillIcon("solar:lock-keyhole-bold") {
            moveTo(7.5f, 10f)
            verticalLineTo(7.5f)
            curveTo(7.5f, 5f, 9.5f, 3f, 12f, 3f)
            curveTo(14.5f, 3f, 16.5f, 5f, 16.5f, 7.5f)
            verticalLineTo(10f)
            horizontalLineTo(17.5f)
            curveTo(19.15f, 10f, 20.5f, 11.35f, 20.5f, 13f)
            verticalLineTo(18.5f)
            curveTo(20.5f, 20.15f, 19.15f, 21.5f, 17.5f, 21.5f)
            horizontalLineTo(6.5f)
            curveTo(4.85f, 21.5f, 3.5f, 20.15f, 3.5f, 18.5f)
            verticalLineTo(13f)
            curveTo(3.5f, 11.35f, 4.85f, 10f, 6.5f, 10f)
            close()
            moveTo(9.5f, 10f)
            horizontalLineTo(14.5f)
            verticalLineTo(7.5f)
            curveTo(14.5f, 6.1f, 13.4f, 5f, 12f, 5f)
            curveTo(10.6f, 5f, 9.5f, 6.1f, 9.5f, 7.5f)
            close()
            circle(12f, 15.5f, 1.6f)
        }
    }

    val LockOpen: ImageVector by lazy {
        strokeIcon("solar:lock-keyhole-unlocked-linear") {
            roundedRect(4.5f, 10.5f, 19.5f, 20.5f, 3f)
            moveTo(8f, 10.5f)
            verticalLineTo(7.2f)
            curveTo(8f, 4.9f, 9.8f, 3f, 12f, 3f)
            curveTo(13.8f, 3f, 15.4f, 4.2f, 15.9f, 5.9f)
            circle(12f, 15.5f, 1.4f)
        }
    }

    val Share: ImageVector by lazy {
        strokeIcon("solar:share-linear") {
            circle(17.5f, 5.5f, 2.5f)
            circle(6.5f, 12f, 2.5f)
            circle(17.5f, 18.5f, 2.5f)
            moveTo(8.7f, 10.7f)
            lineTo(15.3f, 6.8f)
            moveTo(8.7f, 13.3f)
            lineTo(15.3f, 17.2f)
        }
    }

    val OpenInFull: ImageVector by lazy {
        strokeIcon("solar:maximize-linear") {
            moveTo(14.5f, 4f)
            horizontalLineTo(20f)
            verticalLineTo(9.5f)
            moveTo(20f, 4f)
            lineTo(14f, 10f)
            moveTo(9.5f, 20f)
            horizontalLineTo(4f)
            verticalLineTo(14.5f)
            moveTo(4f, 20f)
            lineTo(10f, 14f)
        }
    }

    val Delete: ImageVector by lazy {
        strokeIcon("solar:trash-bin-trash-linear") {
            moveTo(4.5f, 7f)
            horizontalLineTo(19.5f)
            moveTo(9f, 7f)
            verticalLineTo(4.8f)
            curveTo(9f, 4.1f, 9.6f, 3.5f, 10.3f, 3.5f)
            horizontalLineTo(13.7f)
            curveTo(14.4f, 3.5f, 15f, 4.1f, 15f, 4.8f)
            verticalLineTo(7f)
            moveTo(6.5f, 7f)
            lineTo(7.4f, 18.2f)
            curveTo(7.55f, 19.8f, 8.85f, 21f, 10.4f, 21f)
            horizontalLineTo(13.6f)
            curveTo(15.15f, 21f, 16.45f, 19.8f, 16.6f, 18.2f)
            lineTo(17.5f, 7f)
        }
    }

    val Rename: ImageVector by lazy {
        strokeIcon("solar:pen-2-linear") {
            moveTo(14.5f, 4.5f)
            lineTo(19.5f, 9.5f)
            moveTo(4f, 20f)
            lineTo(5.2f, 14.8f)
            lineTo(16.2f, 3.8f)
            curveTo(17f, 3f, 18.2f, 3f, 19f, 3.8f)
            lineTo(20.2f, 5f)
            curveTo(21f, 5.8f, 21f, 7f, 20.2f, 7.8f)
            lineTo(9.2f, 18.8f)
            close()
        }
    }

    // --- Tools ---

    val Layers: ImageVector by lazy {
        strokeIcon("solar:layers-linear") {
            moveTo(12f, 3.5f)
            lineTo(3.5f, 8f)
            lineTo(12f, 12.5f)
            lineTo(20.5f, 8f)
            close()
            moveTo(3.5f, 12.2f)
            lineTo(12f, 16.7f)
            lineTo(20.5f, 12.2f)
            moveTo(3.5f, 16.4f)
            lineTo(12f, 20.9f)
            lineTo(20.5f, 16.4f)
        }
    }

    val Scissors: ImageVector by lazy {
        strokeIcon("solar:scissors-linear") {
            circle(6.5f, 6.5f, 2.7f)
            circle(6.5f, 17.5f, 2.7f)
            moveTo(8.7f, 8.4f)
            lineTo(19.5f, 18.5f)
            moveTo(8.7f, 15.6f)
            lineTo(19.5f, 5.5f)
        }
    }

    val Compress: ImageVector by lazy {
        strokeIcon("solar:minimize-square-3-linear") {
            moveTo(4f, 10f)
            horizontalLineTo(10f)
            verticalLineTo(4f)
            moveTo(10f, 10f)
            lineTo(3.5f, 3.5f)
            moveTo(20f, 14f)
            horizontalLineTo(14f)
            verticalLineTo(20f)
            moveTo(14f, 14f)
            lineTo(20.5f, 20.5f)
        }
    }

    val RotateRight: ImageVector by lazy {
        strokeIcon("solar:restart-linear") {
            moveTo(19.5f, 11.5f)
            curveTo(19.5f, 7.35f, 16.15f, 4f, 12f, 4f)
            curveTo(7.85f, 4f, 4.5f, 7.35f, 4.5f, 11.5f)
            curveTo(4.5f, 15.65f, 7.85f, 19f, 12f, 19f)
            curveTo(14.8f, 19f, 17.2f, 17.5f, 18.5f, 15.2f)
            moveTo(19.5f, 6.5f)
            verticalLineTo(11.5f)
            horizontalLineTo(14.5f)
        }
    }

    val ExtractPages: ImageVector by lazy {
        strokeIcon("solar:documents-linear") {
            roundedRect(7f, 3.5f, 19f, 16.5f, 2.5f)
            moveTo(4.5f, 7.5f)
            verticalLineTo(18f)
            curveTo(4.5f, 19.65f, 5.85f, 21f, 7.5f, 21f)
            horizontalLineTo(15.5f)
        }
    }

    val Scanner: ImageVector by lazy {
        strokeIcon("solar:scanner-linear") {
            moveTo(4f, 7.5f)
            verticalLineTo(5.5f)
            curveTo(4f, 4.4f, 4.9f, 3.5f, 6f, 3.5f)
            horizontalLineTo(8.5f)
            moveTo(15.5f, 3.5f)
            horizontalLineTo(18f)
            curveTo(19.1f, 3.5f, 20f, 4.4f, 20f, 5.5f)
            verticalLineTo(7.5f)
            moveTo(4f, 16.5f)
            verticalLineTo(18.5f)
            curveTo(4f, 19.6f, 4.9f, 20.5f, 6f, 20.5f)
            horizontalLineTo(8.5f)
            moveTo(15.5f, 20.5f)
            horizontalLineTo(18f)
            curveTo(19.1f, 20.5f, 20f, 19.6f, 20f, 18.5f)
            verticalLineTo(16.5f)
            moveTo(3f, 12f)
            horizontalLineTo(21f)
        }
    }

    val GalleryAdd: ImageVector by lazy {
        strokeIcon("solar:gallery-add-linear") {
            roundedRect(3.5f, 4f, 20.5f, 20f, 3f)
            circle(8.5f, 9f, 1.5f)
            moveTo(4f, 17f)
            lineTo(9.5f, 12.2f)
            lineTo(13.5f, 15.8f)
            lineTo(16.2f, 13.5f)
            lineTo(20f, 17f)
        }
    }

    val ImageEdit: ImageVector by lazy {
        strokeIcon("solar:gallery-edit-linear") {
            moveTo(12f, 4.5f)
            horizontalLineTo(6.5f)
            curveTo(4.85f, 4.5f, 3.5f, 5.85f, 3.5f, 7.5f)
            verticalLineTo(17.5f)
            curveTo(3.5f, 19.15f, 4.85f, 20.5f, 6.5f, 20.5f)
            horizontalLineTo(16.5f)
            curveTo(18.15f, 20.5f, 19.5f, 19.15f, 19.5f, 17.5f)
            verticalLineTo(12f)
            moveTo(4f, 17f)
            lineTo(9f, 12.5f)
            lineTo(13f, 16f)
            moveTo(15f, 5.5f)
            lineTo(18.5f, 9f)
            moveTo(14f, 10f)
            lineTo(19.5f, 4.5f)
        }
    }

    val Watermark: ImageVector by lazy {
        strokeIcon("solar:shield-check-linear") {
            moveTo(12f, 3f)
            lineTo(4.5f, 6.2f)
            verticalLineTo(11.5f)
            curveTo(4.5f, 16.3f, 7.7f, 20.4f, 12f, 21.5f)
            curveTo(16.3f, 20.4f, 19.5f, 16.3f, 19.5f, 11.5f)
            verticalLineTo(6.2f)
            close()
            moveTo(8.5f, 12.2f)
            lineTo(11f, 14.5f)
            lineTo(15.8f, 9.5f)
        }
    }

    // --- PDF Viewer, Audio & Formatting ---

    val BookmarkBold: ImageVector by lazy {
        fillIcon("solar:bookmark-bold") {
            moveTo(6f, 4.5f)
            curveTo(6f, 3.4f, 6.9f, 2.5f, 8f, 2.5f)
            horizontalLineTo(16f)
            curveTo(17.1f, 2.5f, 18f, 3.4f, 18f, 4.5f)
            verticalLineTo(20.6f)
            curveTo(18f, 21.4f, 17.1f, 21.8f, 16.4f, 21.3f)
            lineTo(12f, 18f)
            lineTo(7.6f, 21.3f)
            curveTo(6.9f, 21.8f, 6f, 21.4f, 6f, 20.6f)
            close()
        }
    }

    val BookmarkLinear: ImageVector by lazy {
        strokeIcon("solar:bookmark-linear") {
            moveTo(6.5f, 5f)
            curveTo(6.5f, 3.9f, 7.4f, 3f, 8.5f, 3f)
            horizontalLineTo(15.5f)
            curveTo(16.6f, 3f, 17.5f, 3.9f, 17.5f, 5f)
            verticalLineTo(20.5f)
            lineTo(12f, 16.8f)
            lineTo(6.5f, 20.5f)
            close()
        }
    }

    val Highlight: ImageVector by lazy {
        strokeIcon("solar:highlighter-linear") {
            moveTo(9f, 15.5f)
            lineTo(15.5f, 9f)
            lineTo(19f, 12.5f)
            lineTo(12.5f, 19f)
            horizontalLineTo(7f)
            lineTo(5.5f, 17.5f)
            close()
            moveTo(15.5f, 9f)
            lineTo(17.8f, 4.5f)
            lineTo(20.5f, 7.2f)
            lineTo(19f, 12.5f)
            moveTo(4f, 21f)
            horizontalLineTo(20f)
        }
    }

    val DrawPen: ImageVector by lazy {
        strokeIcon("solar:pen-linear") {
            moveTo(4f, 20f)
            lineTo(5.5f, 14.5f)
            lineTo(15.5f, 4.5f)
            curveTo(16.4f, 3.6f, 17.8f, 3.6f, 18.7f, 4.5f)
            lineTo(19.5f, 5.3f)
            curveTo(20.4f, 6.2f, 20.4f, 7.6f, 19.5f, 8.5f)
            lineTo(9.5f, 18.5f)
            close()
        }
    }

    val TextTitle: ImageVector by lazy {
        strokeIcon("solar:text-bold-linear", strokeWidth = 2.0f) {
            moveTo(5f, 5.5f)
            horizontalLineTo(19f)
            moveTo(12f, 5.5f)
            verticalLineTo(19f)
            moveTo(9f, 19f)
            horizontalLineTo(15f)
        }
    }

    val Signature: ImageVector by lazy {
        strokeIcon("solar:pen-nib-linear") {
            moveTo(3.5f, 18.5f)
            curveTo(5.5f, 15f, 7.5f, 14.5f, 9f, 17.5f)
            curveTo(10.5f, 20.2f, 12.5f, 15f, 14.5f, 16.5f)
            curveTo(16f, 17.8f, 17.5f, 16f, 20.5f, 16f)
            moveTo(13.5f, 11.5f)
            lineTo(18.5f, 4.5f)
            lineTo(20.5f, 6.2f)
            lineTo(15.5f, 13.2f)
            close()
        }
    }

    val ZoomIn: ImageVector by lazy {
        strokeIcon("solar:magnifer-zoom-in-linear") {
            circle(11f, 11f, 7f)
            moveTo(16.2f, 16.2f)
            lineTo(20.5f, 20.5f)
            moveTo(11f, 8f)
            verticalLineTo(14f)
            moveTo(8f, 11f)
            horizontalLineTo(14f)
        }
    }

    val ZoomOut: ImageVector by lazy {
        strokeIcon("solar:magnifer-zoom-out-linear") {
            circle(11f, 11f, 7f)
            moveTo(16.2f, 16.2f)
            lineTo(20.5f, 20.5f)
            moveTo(8f, 11f)
            horizontalLineTo(14f)
        }
    }

    val FormatBold: ImageVector by lazy {
        strokeIcon("solar:text-bold", strokeWidth = 2.2f) {
            moveTo(7f, 5f)
            horizontalLineTo(13f)
            curveTo(15f, 5f, 16.5f, 6.4f, 16.5f, 8.2f)
            curveTo(16.5f, 10f, 15f, 11.5f, 13f, 11.5f)
            horizontalLineTo(7f)
            close()
            moveTo(7f, 11.5f)
            horizontalLineTo(14f)
            curveTo(16.2f, 11.5f, 17.8f, 13.1f, 17.8f, 15.2f)
            curveTo(17.8f, 17.4f, 16.2f, 19f, 14f, 19f)
            horizontalLineTo(7f)
            close()
        }
    }

    val FormatItalic: ImageVector by lazy {
        strokeIcon("solar:text-italic", strokeWidth = 2.0f) {
            moveTo(10f, 5f)
            horizontalLineTo(17f)
            moveTo(7f, 19f)
            horizontalLineTo(14f)
            moveTo(13.5f, 5f)
            lineTo(10.5f, 19f)
        }
    }

    val Undo: ImageVector by lazy {
        strokeIcon("solar:undo-left-linear") {
            moveTo(8f, 7f)
            lineTo(4f, 11f)
            lineTo(8f, 15f)
            moveTo(4f, 11f)
            horizontalLineTo(14.5f)
            curveTo(17.5f, 11f, 20f, 13.4f, 20f, 16.2f)
            curveTo(20f, 17.8f, 19.2f, 19f, 17.5f, 19.5f)
        }
    }

    val Play: ImageVector by lazy {
        fillIcon("solar:play-bold") {
            moveTo(7.2f, 5.2f)
            curveTo(7.2f, 4.1f, 8.4f, 3.4f, 9.4f, 4f)
            lineTo(19.2f, 10.6f)
            curveTo(20.1f, 11.2f, 20.1f, 12.6f, 19.2f, 13.2f)
            lineTo(9.4f, 19.8f)
            curveTo(8.4f, 20.4f, 7.2f, 19.7f, 7.2f, 18.6f)
            close()
        }
    }

    val Pause: ImageVector by lazy {
        fillIcon("solar:pause-bold") {
            roundedRect(6f, 4.5f, 10f, 19.5f, 1.8f)
            roundedRect(14f, 4.5f, 18f, 19.5f, 1.8f)
        }
    }

    val Stop: ImageVector by lazy {
        fillIcon("solar:stop-bold") {
            roundedRect(5.5f, 5.5f, 18.5f, 18.5f, 3.2f)
        }
    }

    val Waveform: ImageVector by lazy {
        strokeIcon("solar:soundwave-linear", strokeWidth = 2.0f) {
            moveTo(4f, 10f)
            verticalLineTo(14f)
            moveTo(8f, 7f)
            verticalLineTo(17f)
            moveTo(12f, 4f)
            verticalLineTo(20f)
            moveTo(16f, 7.5f)
            verticalLineTo(16.5f)
            moveTo(20f, 10f)
            verticalLineTo(14f)
        }
    }

    val Check: ImageVector by lazy {
        strokeIcon("solar:check-linear", strokeWidth = 2.4f) {
            moveTo(5f, 12.5f)
            lineTo(9.8f, 17.2f)
            lineTo(19f, 7f)
        }
    }

    val CheckCircle: ImageVector by lazy {
        fillIcon("solar:check-circle-bold") {
            circle(12f, 12f, 9.5f)
            moveTo(8.2f, 12.3f)
            lineTo(10.8f, 14.9f)
            lineTo(15.8f, 9.4f)
        }
    }

    val CleanSweep: ImageVector by lazy {
        strokeIcon("solar:broom-linear") {
            moveTo(15.5f, 3.5f)
            lineTo(11f, 11f)
            moveTo(6.5f, 12f)
            lineTo(14f, 16.5f)
            lineTo(10.5f, 20.5f)
            lineTo(3.5f, 16.5f)
            close()
        }
    }

    val Palette: ImageVector by lazy {
        strokeIcon("solar:pallete-2-linear") {
            moveTo(12f, 3f)
            curveTo(7f, 3f, 3f, 7f, 3f, 12f)
            curveTo(3f, 17f, 7f, 21f, 11.5f, 21f)
            curveTo(12.9f, 21f, 13.8f, 20f, 13.8f, 18.8f)
            curveTo(13.8f, 18.1f, 13.4f, 17.5f, 13.4f, 16.8f)
            curveTo(13.4f, 15.8f, 14.2f, 15f, 15.2f, 15f)
            horizontalLineTo(17.2f)
            curveTo(19.3f, 15f, 21f, 13.3f, 21f, 11.2f)
            curveTo(21f, 6.7f, 17f, 3f, 12f, 3f)
            close()
            circle(7.8f, 10.5f, 1.1f)
            circle(10.5f, 7.2f, 1.1f)
            circle(15f, 7.6f, 1.1f)
        }
    }

    val Sun: ImageVector by lazy {
        strokeIcon("solar:sun-2-linear") {
            circle(12f, 12f, 4.2f)
            moveTo(12f, 2.8f)
            verticalLineTo(4.8f)
            moveTo(12f, 19.2f)
            verticalLineTo(21.2f)
            moveTo(2.8f, 12f)
            horizontalLineTo(4.8f)
            moveTo(19.2f, 12f)
            horizontalLineTo(21.2f)
            moveTo(5.5f, 5.5f)
            lineTo(7f, 7f)
            moveTo(17f, 17f)
            lineTo(18.5f, 18.5f)
            moveTo(18.5f, 5.5f)
            lineTo(17f, 7f)
            moveTo(7f, 17f)
            lineTo(5.5f, 18.5f)
        }
    }

    val VolumeLoud: ImageVector by lazy {
        strokeIcon("solar:volume-loud-linear") {
            moveTo(4f, 9.5f)
            horizontalLineTo(7.8f)
            lineTo(13f, 5f)
            verticalLineTo(19f)
            lineTo(7.8f, 14.5f)
            horizontalLineTo(4f)
            close()
            moveTo(16.5f, 9f)
            curveTo(17.7f, 10.6f, 17.7f, 13.4f, 16.5f, 15f)
            moveTo(19.2f, 6.8f)
            curveTo(21.2f, 9.6f, 21.2f, 14.4f, 19.2f, 17.2f)
        }
    }

    val PlayCircle: ImageVector by lazy {
        strokeIcon("solar:play-circle-linear") {
            circle(12f, 12f, 9.2f)
            moveTo(10f, 8.8f)
            lineTo(15.6f, 12f)
            lineTo(10f, 15.2f)
            close()
        }
    }

    val Translate: ImageVector by lazy {
        strokeIcon("solar:globus-linear") {
            circle(12f, 12f, 9f)
            moveTo(3.5f, 12f)
            horizontalLineTo(20.5f)
            moveTo(12f, 3f)
            curveTo(14.8f, 6f, 14.8f, 18f, 12f, 21f)
            moveTo(12f, 3f)
            curveTo(9.2f, 6f, 9.2f, 18f, 12f, 21f)
        }
    }

    val Code: ImageVector by lazy {
        strokeIcon("solar:code-square-linear") {
            roundedRect(3.5f, 3.5f, 20.5f, 20.5f, 3.5f)
            moveTo(9.5f, 9.2f)
            lineTo(6.8f, 12f)
            lineTo(9.5f, 14.8f)
            moveTo(14.5f, 9.2f)
            lineTo(17.2f, 12f)
            lineTo(14.5f, 14.8f)
        }
    }

    val Plus: ImageVector by lazy {
        strokeIcon("solar:add-circle-linear") {
            circle(12f, 12f, 9f)
            moveTo(12f, 8.2f)
            verticalLineTo(15.8f)
            moveTo(8.2f, 12f)
            horizontalLineTo(15.8f)
        }
    }

    val Contrast: ImageVector by lazy {
        strokeIcon("solar:contrast-linear") {
            circle(12f, 12f, 9f)
            moveTo(12f, 3f)
            verticalLineTo(21f)
            moveTo(12f, 7f)
            lineTo(16.5f, 11.5f)
            moveTo(12f, 12f)
            lineTo(18f, 18f)
        }
    }

    val Droplet: ImageVector by lazy {
        strokeIcon("solar:droplet-linear") {
            moveTo(12f, 3f)
            curveTo(12f, 3f, 5.5f, 10.2f, 5.5f, 14.5f)
            curveTo(5.5f, 18.1f, 8.4f, 21f, 12f, 21f)
            curveTo(15.6f, 21f, 18.5f, 18.1f, 18.5f, 14.5f)
            curveTo(18.5f, 10.2f, 12f, 3f, 12f, 3f)
            close()
        }
    }

    val FlipHorizontal: ImageVector by lazy {
        strokeIcon("solar:mirror-left-linear") {
            moveTo(12f, 3.5f)
            verticalLineTo(20.5f)
            moveTo(8.5f, 7f)
            lineTo(4f, 17f)
            horizontalLineTo(8.5f)
            close()
            moveTo(15.5f, 7f)
            lineTo(20f, 17f)
            horizontalLineTo(15.5f)
            close()
        }
    }

    val Save: ImageVector by lazy {
        strokeIcon("solar:diskette-linear") {
            moveTo(4.5f, 4f)
            horizontalLineTo(16.5f)
            lineTo(20f, 7.5f)
            verticalLineTo(18.5f)
            curveTo(20f, 19.6f, 19.1f, 20.5f, 18f, 20.5f)
            horizontalLineTo(6f)
            curveTo(4.9f, 20.5f, 4f, 19.6f, 4f, 18.5f)
            verticalLineTo(4.5f)
            close()
            roundedRect(8f, 4f, 14.5f, 8.5f, 0.8f)
            roundedRect(7.5f, 13.5f, 16.5f, 20.5f, 1.2f)
        }
    }

    val MoreVert: ImageVector by lazy {
        fillIcon("solar:menu-dots-bold") {
            circle(12f, 5.5f, 1.75f)
            circle(12f, 12f, 1.75f)
            circle(12f, 18.5f, 1.75f)
        }
    }
}
