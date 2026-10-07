package com.google.pixel.calculator.ui.toolbox

import android.Manifest
import android.graphics.Paint
import android.graphics.Typeface
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.pixel.calculator.core.sensors.CompassLocationData
import com.google.pixel.calculator.core.sensors.CompassLocationManager
import com.google.pixel.calculator.core.sensors.CompassSensorManager
import com.google.pixel.calculator.ui.ActiveScreen
import com.google.pixel.calculator.ui.theme.CalculatorTheme
import kotlin.math.cos
import kotlin.math.sin

/**
 * Schermata della Bussola Digitale Google Pixel in Material 3 Expressive.
 * - Stile grafico fedele con quadrante ad alta precisione, tacche a 360°, ago sfaccettato 3D bicolore.
 * - Coordinate GPS (Latitudine, Longitudine) e Altitudine in basso con formattazione DMS / Decimale.
 * - Menu con 3 punti in alto con avvio della modalità di ricalibrazione a figura di 8.
 * - Barra di navigazione strumenti unificata M3.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompassView(
    onBack: () -> Unit,
    onNavigate: (ActiveScreen) -> Unit = {}
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val clipboardManager = LocalClipboardManager.current
    val density = LocalDensity.current

    // Gestori Sensori e Posizione
    val compassManager = remember { CompassSensorManager(context) }
    val locationManager = remember { CompassLocationManager(context) }

    val compassData by compassManager.getCompassFlow().collectAsState(
        initial = CompassSensorManager.CompassData(0f, 0f, "N")
    )
    val locationData by locationManager.locationFlow.collectAsState()

    // Permessi Posizione
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            locationManager.startListening()
        }
    }

    DisposableEffect(Unit) {
        if (locationManager.hasPermission()) {
            locationManager.startListening()
        }
        onDispose {
            locationManager.stopListening()
        }
    }

    // Stati UI
    var showMenu by remember { mutableStateOf(false) }
    var showCalibrationDialog by remember { mutableStateOf(false) }
    var useDecimalFormat by remember { mutableStateOf(false) }

    // Dialog di Ricalibrazione a Figura di 8
    if (showCalibrationDialog) {
        CompassCalibrationDialog(
            compassData = compassData,
            onDismiss = { showCalibrationDialog = false }
        )
    }

    val isDark = CalculatorTheme.colors.isDark
    val isLandscape = LocalConfiguration.current.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    // Intestazione minimalista con solo icona indietro e menu a 3 puntini
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Torna alla calcolatrice",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                actions = {
                    // Menu con 3 punti in alto
                    Box {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "Opzioni bussola",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Ricalibra bussola") },
                                onClick = {
                                    showMenu = false
                                    showCalibrationDialog = true
                                },
                                leadingIcon = {
                                    @Suppress("DEPRECATION")
                                    Icon(Icons.Default.RotateRight, contentDescription = null)
                                }
                            )

                            DropdownMenuItem(
                                text = {
                                    Text(if (locationData.hasPermission) "Aggiorna posizione GPS" else "Attiva posizione GPS")
                                },
                                onClick = {
                                    showMenu = false
                                    if (locationData.hasPermission) {
                                        locationManager.startListening()
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    } else {
                                        locationPermissionLauncher.launch(
                                            arrayOf(
                                                Manifest.permission.ACCESS_FINE_LOCATION,
                                                Manifest.permission.ACCESS_COARSE_LOCATION
                                            )
                                        )
                                    }
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.LocationOn, contentDescription = null)
                                }
                            )

                            DropdownMenuItem(
                                text = {
                                    Text(if (useDecimalFormat) "Usa formato DMS (G°M'S\")" else "Usa formato decimale")
                                },
                                onClick = {
                                    showMenu = false
                                    useDecimalFormat = !useDecimalFormat
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.Tune, contentDescription = null)
                                }
                            )

                            HorizontalDivider()

                            DropdownMenuItem(
                                text = { Text("Copia coordinate") },
                                onClick = {
                                    showMenu = false
                                    val coordsText = "${locationData.latitudeDms} ${locationData.latitudeCardinal}, ${locationData.longitudeDms} ${locationData.longitudeCardinal} - ${locationData.altitudeFormatted}"
                                    clipboardManager.setText(AnnotatedString(coordsText))
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.ContentCopy, contentDescription = null)
                                }
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    scrolledContainerColor = Color.Transparent
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (isLandscape) {
                // Layout Orizzontale (Landscape)
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Colonna Sinistra: Località, Angolo e Schede Coordinate
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CompassHeader(
                            locationName = locationData.locationName,
                            hasPermission = locationData.hasPermission,
                            azimuthDegrees = compassData.azimuthDegrees,
                            cardinalDirection = compassData.cardinalDirection,
                            onRequestPermission = {
                                locationPermissionLauncher.launch(
                                    arrayOf(
                                        Manifest.permission.ACCESS_FINE_LOCATION,
                                        Manifest.permission.ACCESS_COARSE_LOCATION
                                    )
                                )
                            }
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        CompassCoordinateCards(
                            locationData = locationData,
                            useDecimalFormat = useDecimalFormat,
                            onCardClick = {
                                val coordsText = "${locationData.latitudeDms} ${locationData.latitudeCardinal}, ${locationData.longitudeDms} ${locationData.longitudeCardinal} - Alt: ${locationData.altitudeFormatted}"
                                clipboardManager.setText(AnnotatedString(coordsText))
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            }
                        )
                    }

                    // Colonna Destra: Quadrante Bussola
                    Box(
                        modifier = Modifier
                            .weight(1.2f)
                            .fillMaxHeight(),
                        contentAlignment = Alignment.Center
                    ) {
                        PixelCompassDial(
                            azimuth = compassData.azimuthDegrees,
                            isDark = isDark,
                            dialSize = 250.dp
                        )
                    }
                }
            } else {
                // Layout Verticale (Portrait)
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // In Alto: Località + Azimut 0°N
                    CompassHeader(
                        locationName = locationData.locationName,
                        hasPermission = locationData.hasPermission,
                        azimuthDegrees = compassData.azimuthDegrees,
                        cardinalDirection = compassData.cardinalDirection,
                        onRequestPermission = {
                            locationPermissionLauncher.launch(
                                arrayOf(
                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.ACCESS_COARSE_LOCATION
                                )
                            )
                        }
                    )

                    // Al Centro: Quadrante della Bussola Google Pixel
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        PixelCompassDial(
                            azimuth = compassData.azimuthDegrees,
                            isDark = isDark,
                            dialSize = 310.dp
                        )
                    }

                    // Suggerimento di allineamento se il telefono è troppo inclinato
                    AnimatedVisibility(
                        visible = !compassData.isLevel,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        Surface(
                            shape = RoundedCornerShape(percent = 50),
                            color = MaterialTheme.colorScheme.surfaceContainerHigh,
                            modifier = Modifier.padding(bottom = 8.dp)
                        ) {
                            Text(
                                text = "Tieni il telefono in piano per la massima precisione",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                            )
                        }
                    }

                    // In Basso: Schede Coordinate e Altitudine
                    CompassCoordinateCards(
                        locationData = locationData,
                        useDecimalFormat = useDecimalFormat,
                        onCardClick = {
                            val coordsText = "${locationData.latitudeDms} ${locationData.latitudeCardinal}, ${locationData.longitudeDms} ${locationData.longitudeCardinal} - Alt: ${locationData.altitudeFormatted}"
                            clipboardManager.setText(AnnotatedString(coordsText))
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        },
                        modifier = Modifier.padding(bottom = 84.dp)
                    )
                }
            }

            // Barra strumenti fluttuante Material 3 coerente con il resto dell'app
            PixelFloatingToolBar(
                currentScreen = ActiveScreen.TOOLBOX_COMPASS,
                onNavigate = onNavigate,
                modifier = Modifier.align(Alignment.BottomStart)
            )
        }
    }
}

/**
 * Sezione di Intestazione con località e grande lettura dei gradi e direzione cardinale (es. 0°N).
 */
@Composable
private fun CompassHeader(
    locationName: String,
    hasPermission: Boolean,
    azimuthDegrees: Float,
    cardinalDirection: String,
    onRequestPermission: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Riga Località con Pin (cliccabile per richiedere il permesso se non presente)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier
                .clickable(
                    enabled = !hasPermission,
                    onClick = onRequestPermission
                )
                .padding(horizontal = 12.dp, vertical = 4.dp)
        ) {
            Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = null,
                tint = if (hasPermission) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = locationName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(2.dp))

        // Grande visualizzazione numerica dei gradi e direzione (es. 0°N)
        val headingText = "${azimuthDegrees.toInt()}°$cardinalDirection"
        Text(
            text = headingText,
            style = MaterialTheme.typography.displayMedium,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onSurface,
            letterSpacing = (-0.5).sp
        )
    }
}

/**
 * Quadrante della Bussola fedele allo stile Google Pixel dell'immagine:
 * - Lunetta circolare ad alta definizione con tacche ogni 2°, 10°, 30°.
 * - Indicatore triangolare rosso in alto (0° orientamento dispositivo).
 * - Rotazione coordinata con azimut magnetico.
 * - Numeri dei gradi e punti cardinali N, E, S, W.
 * - Ago magnetico bicolore (Nord Rosso con sfaccettatura 3D, Sud Blu con sfaccettatura 3D, perno centrale).
 */
@Composable
private fun PixelCompassDial(
    azimuth: Float,
    isDark: Boolean,
    dialSize: androidx.compose.ui.unit.Dp,
    modifier: Modifier = Modifier
) {
    val dialBg = if (isDark) Color(0xFF0D0F14) else Color(0xFFFFFFFF)
    val outerRingColor = if (isDark) Color(0xFF1E293B) else Color(0xFFE2E8F0)
    val tickMajorColor = if (isDark) Color(0xFF38BDF8) else Color(0xFF2563EB)
    val tickMinorColor = if (isDark) Color(0xFF1E3A8A) else Color(0xFF93C5FD)
    val textColorArgb = if (isDark) 0xFFF1F5F9.toInt() else 0xFF0F172A.toInt()
    val subTextColorArgb = if (isDark) 0xFF94A3B8.toInt() else 0xFF64748B.toInt()
    val redAccent = Color(0xFFEF4444)
    val redDarkAccent = Color(0xFFB91C1C)
    val blueAccent = Color(0xFF3B82F6)
    val blueDarkAccent = Color(0xFF1D4ED8)

    Box(
        modifier = modifier
            .size(dialSize)
            .background(color = dialBg, shape = CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val radius = size.width / 2f - 12.dp.toPx()

            // 1. Lunetta esterna
            drawCircle(
                color = outerRingColor,
                radius = radius,
                center = center,
                style = Stroke(width = 2.dp.toPx())
            )

            // Anello interno sottile di precisione
            drawCircle(
                color = outerRingColor.copy(alpha = 0.5f),
                radius = radius * 0.76f,
                center = center,
                style = Stroke(width = 1.dp.toPx())
            )

            // 2. Elementi che ruotano con l'azimut inverso (-azimuth)
            rotate(-azimuth, pivot = center) {
                // Tacche perimetrali ogni 2 gradi
                for (deg in 0 until 360 step 2) {
                    val isCardinal = deg % 90 == 0
                    val isMajor = deg % 30 == 0
                    val isMedium = deg % 10 == 0

                    val tickLen = when {
                        isCardinal -> 14.dp.toPx()
                        isMajor -> 10.dp.toPx()
                        isMedium -> 6.5.dp.toPx()
                        else -> 3.5.dp.toPx()
                    }

                    val tickWidth = when {
                        isCardinal -> 2.5.dp.toPx()
                        isMajor -> 1.8.dp.toPx()
                        isMedium -> 1.2.dp.toPx()
                        else -> 0.8.dp.toPx()
                    }

                    val tickColor = when {
                        deg == 0 -> redAccent
                        isMajor -> tickMajorColor
                        isMedium -> tickMajorColor.copy(alpha = 0.8f)
                        else -> tickMinorColor.copy(alpha = 0.65f)
                    }

                    rotate(deg.toFloat(), pivot = center) {
                        drawLine(
                            color = tickColor,
                            start = Offset(center.x, center.y - radius),
                            end = Offset(center.x, center.y - radius + tickLen),
                            strokeWidth = tickWidth,
                            cap = StrokeCap.Round
                        )
                    }
                }

                // Disegno etichette con canvas nativo per rendering ad altissime prestazioni
                drawIntoCanvas { canvas ->
                    val nativePaint = Paint().apply {
                        isAntiAlias = true
                        textAlign = Paint.Align.CENTER
                    }

                    // Punti Cardinali (N, E, S, W)
                    val cardinals = listOf(
                        Pair(0, "N"),
                        Pair(90, "E"),
                        Pair(180, "S"),
                        Pair(270, "W")
                    )

                    val cardinalDist = radius * 0.60f
                    nativePaint.textSize = 20.sp.toPx()
                    nativePaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)

                    for ((deg, label) in cardinals) {
                        nativePaint.color = if (deg == 0) redAccent.toArgb() else textColorArgb
                        val rad = Math.toRadians((deg - 90).toDouble())
                        val lx = center.x + (cardinalDist * cos(rad)).toFloat()
                        val ly = center.y + (cardinalDist * sin(rad)).toFloat()
                        val baseline = ly - (nativePaint.descent() + nativePaint.ascent()) / 2f
                        canvas.nativeCanvas.drawText(label, lx, baseline, nativePaint)
                    }

                    // Numeri dei gradi (30, 60, 120, 150, 210, 240, 300, 330)
                    nativePaint.textSize = 10.sp.toPx()
                    nativePaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                    nativePaint.color = subTextColorArgb

                    val degreeDist = radius * 0.84f
                    val degreeAngles = listOf(30, 60, 120, 150, 210, 240, 300, 330)
                    for (deg in degreeAngles) {
                        val rad = Math.toRadians((deg - 90).toDouble())
                        val lx = center.x + (degreeDist * cos(rad)).toFloat()
                        val ly = center.y + (degreeDist * sin(rad)).toFloat()
                        val baseline = ly - (nativePaint.descent() + nativePaint.ascent()) / 2f
                        canvas.nativeCanvas.drawText(deg.toString(), lx, baseline, nativePaint)
                    }
                }

                // 3. Ago Magnetico Bicolore Sfaccettato (3D Diamond Needle)
                val needleLength = radius * 0.72f
                val needleHalfWidth = 13.dp.toPx()

                // Punta Nord (Rosso Material)
                val northTip = Offset(center.x, center.y - needleLength)
                val leftWaist = Offset(center.x - needleHalfWidth, center.y)
                val rightWaist = Offset(center.x + needleHalfWidth, center.y)

                // Sfaccettatura sinistra Nord (colore vivido)
                val northLeftFacet = Path().apply {
                    moveTo(center.x, center.y)
                    lineTo(leftWaist.x, leftWaist.y)
                    lineTo(northTip.x, northTip.y)
                    close()
                }
                drawPath(northLeftFacet, color = redAccent)

                // Sfaccettatura destra Nord (colore ombreggiato)
                val northRightFacet = Path().apply {
                    moveTo(center.x, center.y)
                    lineTo(rightWaist.x, rightWaist.y)
                    lineTo(northTip.x, northTip.y)
                    close()
                }
                drawPath(northRightFacet, color = redDarkAccent)

                // Punta Sud (Blu Material)
                val southTip = Offset(center.x, center.y + needleLength)

                // Sfaccettatura sinistra Sud (colore vivido)
                val southLeftFacet = Path().apply {
                    moveTo(center.x, center.y)
                    lineTo(leftWaist.x, leftWaist.y)
                    lineTo(southTip.x, southTip.y)
                    close()
                }
                drawPath(southLeftFacet, color = blueAccent)

                // Sfaccettatura destra Sud (colore ombreggiato)
                val southRightFacet = Path().apply {
                    moveTo(center.x, center.y)
                    lineTo(rightWaist.x, rightWaist.y)
                    lineTo(southTip.x, southTip.y)
                    close()
                }
                drawPath(southRightFacet, color = blueDarkAccent)

                // 4. Perno centrale (Hub dell'ago)
                val hubOuterRadius = 14.dp.toPx()
                val hubInnerRadius = 8.dp.toPx()
                val hubCoreRadius = 4.dp.toPx()

                drawCircle(
                    color = if (isDark) Color(0xFF94A3B8) else Color(0xFFCBD5E1),
                    radius = hubOuterRadius,
                    center = center,
                    style = Stroke(width = 3.dp.toPx())
                )
                drawCircle(
                    color = if (isDark) Color(0xFF1E293B) else Color(0xFFF1F5F9),
                    radius = hubInnerRadius,
                    center = center
                )
                drawCircle(
                    color = if (isDark) Color(0xFF38BDF8) else Color(0xFF2563EB),
                    radius = hubCoreRadius,
                    center = center
                )
            }

            // 5. Indicatore triangolare fisso in alto a 0° (Marker di mira sul telaio)
            val topPointerLength = 9.dp.toPx()
            val topPointerWidth = 8.dp.toPx()
            val topPointer = Path().apply {
                moveTo(center.x, center.y - radius - 2.dp.toPx())
                lineTo(center.x - topPointerWidth / 2f, center.y - radius - 2.dp.toPx() - topPointerLength)
                lineTo(center.x + topPointerWidth / 2f, center.y - radius - 2.dp.toPx() - topPointerLength)
                close()
            }
            drawPath(topPointer, color = redAccent)
        }
    }
}

/**
 * Tre schede informative in basso per Latitudine, Altitudine e Longitudine
 * perfettamente allineate allo stile mostrato nell'immagine di riferimento.
 */
@Composable
private fun CompassCoordinateCards(
    locationData: CompassLocationData,
    useDecimalFormat: Boolean,
    onCardClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val latValue = if (useDecimalFormat) "%.4f°".format(locationData.latitude) else locationData.latitudeDms
    val lonValue = if (useDecimalFormat) "%.4f°".format(locationData.longitude) else locationData.longitudeDms

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onCardClick),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Scheda Sinistra: Latitudine (es. 40°5'35" NL)
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainer
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            modifier = Modifier.weight(1f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 14.dp, horizontal = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = latValue,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = locationData.latitudeCardinal,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Elemento Centrale: Altitudine (es. ↓ 991m)
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainer
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            modifier = Modifier.weight(0.85f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 14.dp, horizontal = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowDownward,
                        contentDescription = "Altitudine",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = locationData.altitudeFormatted,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "ALT",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Scheda Destra: Longitudine (es. 120°2'1" EL)
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainer
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            modifier = Modifier.weight(1f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 14.dp, horizontal = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = lonValue,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = locationData.longitudeCardinal,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
