package com.google.pixel.calculator.ui.toolbox

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.pixel.calculator.R
import com.google.pixel.calculator.core.utils.RulerUtils
import com.google.pixel.calculator.ui.ActiveScreen
import com.google.pixel.calculator.ui.theme.CalculatorTheme
import com.google.pixel.calculator.ui.theme.QuicksandFontFamily
import kotlin.math.roundToInt

enum class RulerUnit(val label: String, val shortLabel: String, val labelRes: Int = 0) {
    CM("Centimetri (cm)", "cm", R.string.ruler_unit_cm),
    MM("Millimetri (mm)", "mm", R.string.ruler_unit_mm),
    INCH("Pollici (in)", "in", R.string.ruler_unit_inch)
}

/**
 * Digital Ruler screen with clean, minimal layout:
 * - Left vertical graduated lane with ticks and numbers.
 * - Horizontal dashed measurement line spanning the display.
 * - Floating pill badge in the center of the line showing exact value (e.g. "5.00 cm").
 * - Dynamic color consistency with active theme / layout colors.
 * - Physical dimensional accuracy based on device display DPI.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RulerView(
    onBack: () -> Unit,
    onNavigate: (ActiveScreen) -> Unit = {}
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val density = LocalDensity.current
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE

    BackHandler { onBack() }

    val rawDpi = remember { RulerUtils.getRawScreenDpi(context) }
    val effectiveYdpi = rawDpi.second

    // Unit and display settings
    var selectedUnit by rememberSaveable { mutableStateOf(RulerUnit.CM) }
    var showMmTicks by rememberSaveable { mutableStateOf(false) }
    var useClassicYellow by rememberSaveable { mutableStateOf(false) }

    // Slider position in pixels. Defaults to 5.00 cm
    var sliderY by rememberSaveable { mutableFloatStateOf(-1f) }

    // Layout dimensions
    val zeroY = with(density) { 32.dp.toPx() }
    val rulerWidth = 84.dp
    val rulerWidthPx = with(density) { rulerWidth.toPx() }
    val bottomToolbarClearance = with(density) { if (isLandscape) 56.dp.toPx() else 96.dp.toPx() }

    LaunchedEffect(effectiveYdpi, zeroY) {
        if (sliderY < 0f) {
            val initial5cmPx = RulerUtils.cmToPixels(5.0f, effectiveYdpi)
            sliderY = zeroY + initial5cmPx
        }
    }

    val isDark = CalculatorTheme.colors.isDark
    val backgroundColor = CalculatorTheme.colors.background
    val rulerLaneColor = MaterialTheme.colorScheme.surfaceContainer ?: if (isDark) Color(0xFF1A1C21) else Color(0xFFF1F2F6)
    val dividerColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    val onSurfaceVariantColor = MaterialTheme.colorScheme.onSurfaceVariant

    val defaultLayoutAccent = CalculatorTheme.colors.equalsContainer.takeIf { it != Color.Unspecified }
        ?: if (isDark) Color(0xFFFFD153) else Color(0xFFE5A800)
    val defaultLayoutAccentContent = CalculatorTheme.colors.equalsContent.takeIf { it != Color.Unspecified }
        ?: Color(0xFF1C1500)

    val accentContainer = if (useClassicYellow) Color(0xFFFFD153) else defaultLayoutAccent
    val accentContent = if (useClassicYellow) Color(0xFF1C1500) else defaultLayoutAccentContent

    // Measured value computation
    val distancePx = (sliderY - zeroY).coerceAtLeast(0f)
    val measuredText = remember(distancePx, effectiveYdpi, selectedUnit) {
        when (selectedUnit) {
            RulerUnit.CM -> {
                val cm = RulerUtils.pixelsToCm(distancePx, effectiveYdpi)
                "%.2f cm".format(cm)
            }
            RulerUnit.MM -> {
                val mm = RulerUtils.pixelsToMm(distancePx, effectiveYdpi)
                "%.1f mm".format(mm)
            }
            RulerUnit.INCH -> {
                val inches = RulerUtils.pixelsToInches(distancePx, effectiveYdpi)
                "%.2f in".format(inches)
            }
        }
    }

    // High performance text measurer for Canvas
    val textMeasurer = rememberTextMeasurer()
    val numberStyle = TextStyle(
        fontSize = 12.sp,
        fontWeight = FontWeight.Medium,
        fontFamily = QuicksandFontFamily,
        color = onSurfaceVariantColor
    )

    // Pre-measure numbers 0..30 to eliminate allocation during drag
    val premeasuredNumbers = remember(onSurfaceVariantColor) {
        (0..30).associateWith { num ->
            textMeasurer.measure(
                text = AnnotatedString("$num"),
                style = numberStyle
            )
        }
    }

    Scaffold(
        containerColor = backgroundColor,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.ruler_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = onSurfaceColor
                    )
                },
                navigationIcon = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(start = 4.dp)
                    ) {
                        IconButton(onClick = onBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(R.string.back),
                                tint = onSurfaceColor
                            )
                        }
                        IconButton(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                showMmTicks = !showMmTicks
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Straighten,
                                contentDescription = if (showMmTicks) {
                                    stringResource(R.string.hide_mm_ticks)
                                } else {
                                    stringResource(R.string.show_mm_ticks)
                                },
                                tint = if (showMmTicks) accentContainer else onSurfaceColor.copy(alpha = 0.65f)
                            )
                        }
                    }
                },
                actions = {
                    // Quick unit toggle chip (cm / mm / in)
                    FilterChip(
                        selected = false,
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            selectedUnit = when (selectedUnit) {
                                RulerUnit.CM -> RulerUnit.MM
                                RulerUnit.MM -> RulerUnit.INCH
                                RulerUnit.INCH -> RulerUnit.CM
                            }
                        },
                        label = {
                            Text(
                                text = selectedUnit.shortLabel,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.labelMedium
                            )
                        },
                        shape = RoundedCornerShape(percent = 50),
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh ?: Color(0xFF282A30),
                            labelColor = onSurfaceColor
                        ),
                        modifier = Modifier.padding(end = 4.dp)
                    )

                    // Options dropdown menu
                    var showMenu by remember { mutableStateOf(false) }
                    Box {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = stringResource(R.string.more_options),
                                tint = onSurfaceColor
                            )
                        }
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        if (showMmTicks) {
                                            stringResource(R.string.hide_mm_ticks)
                                        } else {
                                            stringResource(R.string.show_mm_ticks)
                                        }
                                    )
                                },
                                onClick = {
                                    showMmTicks = !showMmTicks
                                    showMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        if (useClassicYellow) {
                                            stringResource(R.string.use_layout_accent_color)
                                        } else {
                                            stringResource(R.string.use_classic_yellow_color)
                                        }
                                    )
                                },
                                onClick = {
                                    useClassicYellow = !useClassicYellow
                                    showMenu = false
                                }
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = backgroundColor,
                    titleContentColor = onSurfaceColor,
                    navigationIconContentColor = onSurfaceColor
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Interactive gesture area
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(zeroY, effectiveYdpi, bottomToolbarClearance) {
                        awaitEachGesture {
                            val down = awaitFirstDown(requireUnconsumed = false)
                            val maxY = size.height - bottomToolbarClearance
                            val targetY = down.position.y.coerceIn(zeroY, maxY)
                            sliderY = targetY

                            val pxPerCm = RulerUtils.cmToPixels(1f, effectiveYdpi)
                            var lastCm = ((targetY - zeroY) / pxPerCm).toInt()

                            while (true) {
                                val event = awaitPointerEvent()
                                val change = event.changes.firstOrNull { it.id == down.id } ?: break
                                if (!change.pressed) break
                                change.consume()

                                val currentY = change.position.y.coerceIn(zeroY, maxY)
                                sliderY = currentY

                                val currentCm = ((currentY - zeroY) / pxPerCm).toInt()
                                if (currentCm != lastCm) {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    lastCm = currentCm
                                }
                            }
                        }
                    }
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val width = size.width
                    val height = size.height
                    val maxY = height - bottomToolbarClearance

                    // 1. Left vertical lane
                    drawRect(
                        color = rulerLaneColor,
                        topLeft = Offset(0f, 0f),
                        size = Size(rulerWidthPx, height)
                    )

                    // 2. Vertical lane divider
                    drawLine(
                        color = dividerColor,
                        start = Offset(rulerWidthPx, 0f),
                        end = Offset(rulerWidthPx, height),
                        strokeWidth = 1.dp.toPx()
                    )

                    // 3. Graduated ticks and numbers
                    val cmTickLength = 48.dp.toPx()
                    val halfCmTickLength = 28.dp.toPx()
                    val mmTickLength = 16.dp.toPx()

                    if (selectedUnit == RulerUnit.INCH) {
                        val inStepPx = RulerUtils.inchesToPixels(1f, effectiveYdpi)
                        val maxInches = ((maxY - zeroY) / inStepPx).toInt() + 1

                        for (i in 0..maxInches) {
                            val y = zeroY + (i * inStepPx)
                            if (y > maxY + inStepPx) break

                            drawLine(
                                color = onSurfaceColor,
                                start = Offset(0f, y),
                                end = Offset(cmTickLength, y),
                                strokeWidth = 2.2.dp.toPx()
                            )

                            premeasuredNumbers[i]?.let { layoutResult ->
                                val textY = y - (layoutResult.size.height / 2f)
                                val textX = 56.dp.toPx()
                                drawText(
                                    textLayoutResult = layoutResult,
                                    topLeft = Offset(textX, textY)
                                )
                            }

                            val yHalf = y + (inStepPx / 2f)
                            if (yHalf <= maxY) {
                                drawLine(
                                    color = onSurfaceVariantColor.copy(alpha = 0.75f),
                                    start = Offset(0f, yHalf),
                                    end = Offset(halfCmTickLength, yHalf),
                                    strokeWidth = 1.6.dp.toPx()
                                )
                            }

                            if (showMmTicks) {
                                val yQuarter1 = y + (inStepPx / 4f)
                                val yQuarter3 = y + (3f * inStepPx / 4f)
                                if (yQuarter1 <= maxY) {
                                    drawLine(
                                        color = onSurfaceVariantColor.copy(alpha = 0.4f),
                                        start = Offset(0f, yQuarter1),
                                        end = Offset(mmTickLength, yQuarter1),
                                        strokeWidth = 1.2.dp.toPx()
                                    )
                                }
                                if (yQuarter3 <= maxY) {
                                    drawLine(
                                        color = onSurfaceVariantColor.copy(alpha = 0.4f),
                                        start = Offset(0f, yQuarter3),
                                        end = Offset(mmTickLength, yQuarter3),
                                        strokeWidth = 1.2.dp.toPx()
                                    )
                                }
                            }
                        }
                    } else {
                        val cmStepPx = RulerUtils.cmToPixels(1f, effectiveYdpi)
                        val halfCmStepPx = cmStepPx / 2f
                        val mmStepPx = cmStepPx / 10f
                        val maxCm = ((maxY - zeroY) / cmStepPx).toInt() + 1

                        for (cm in 0..maxCm) {
                            val y = zeroY + (cm * cmStepPx)
                            if (y > maxY + cmStepPx) break

                            drawLine(
                                color = onSurfaceColor,
                                start = Offset(0f, y),
                                end = Offset(cmTickLength, y),
                                strokeWidth = 2.2.dp.toPx()
                            )

                            premeasuredNumbers[cm]?.let { layoutResult ->
                                val textY = y - (layoutResult.size.height / 2f)
                                val textX = 56.dp.toPx()
                                drawText(
                                    textLayoutResult = layoutResult,
                                    topLeft = Offset(textX, textY)
                                )
                            }

                            val yHalf = y + halfCmStepPx
                            if (yHalf <= maxY) {
                                drawLine(
                                    color = onSurfaceVariantColor.copy(alpha = 0.75f),
                                    start = Offset(0f, yHalf),
                                    end = Offset(halfCmTickLength, yHalf),
                                    strokeWidth = 1.6.dp.toPx()
                                )
                            }

                            if (showMmTicks) {
                                for (mm in 1..9) {
                                    if (mm == 5) continue
                                    val yMm = y + (mm * mmStepPx)
                                    if (yMm <= maxY) {
                                        drawLine(
                                            color = onSurfaceVariantColor.copy(alpha = 0.4f),
                                            start = Offset(0f, yMm),
                                            end = Offset(mmTickLength, yMm),
                                            strokeWidth = 1.1.dp.toPx()
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // 4. Dashed horizontal measurement line
                    if (sliderY >= zeroY) {
                        drawLine(
                            color = accentContainer,
                            start = Offset(0f, sliderY),
                            end = Offset(width, sliderY),
                            strokeWidth = 2.6.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(
                                floatArrayOf(12.dp.toPx(), 7.dp.toPx()),
                                0f
                            ),
                            cap = StrokeCap.Round
                        )
                    }
                }

                // 5. Central floating badge with measurement reading
                if (sliderY >= zeroY) {
                    val badgeHeight = 38.dp
                    val badgeHalfHeightPx = with(density) { (badgeHeight / 2).toPx() }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .offset {
                                IntOffset(
                                    x = 0,
                                    y = (sliderY - badgeHalfHeightPx).roundToInt()
                                )
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Surface(
                            shape = RoundedCornerShape(percent = 50),
                            color = accentContainer,
                            contentColor = accentContent,
                            shadowElevation = 4.dp,
                            modifier = Modifier
                                .shadow(4.dp, shape = CircleShape)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    selectedUnit = when (selectedUnit) {
                                        RulerUnit.CM -> RulerUnit.MM
                                        RulerUnit.MM -> RulerUnit.INCH
                                        RulerUnit.INCH -> RulerUnit.CM
                                    }
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 24.dp, vertical = 9.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = measuredText,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = accentContent,
                                    letterSpacing = 0.2.sp
                                )
                            }
                        }
                    }
                }
            }

            // Floating bottom toolbar
            PixelFloatingToolBar(
                currentScreen = ActiveScreen.TOOLBOX_RULER,
                onNavigate = onNavigate,
                modifier = Modifier.align(Alignment.BottomEnd),
                alignRight = true
            )
        }
    }
}
