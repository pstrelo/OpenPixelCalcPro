package com.google.pixel.calculator.ui.easteregg

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.pixel.calculator.R
import com.google.pixel.calculator.ui.theme.QuicksandFontFamily

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EasterEggScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current
    val scrollState = rememberScrollState()

    BackHandler {
        onBack()
    }

    val openLink = { url: String ->
        try {
            uriHandler.openUri(url)
        } catch (_: Exception) {
            Toast.makeText(context, "Impossibile aprire il link", Toast.LENGTH_SHORT).show()
        }
    }

    val backgroundColor = Color(0xFF111215)
    val cardBackground = Color(0xFF1C1E24)
    val cardBorder = Color(0xFF2C2F3A)
    val textPrimary = Color(0xFFF1F2F6)
    val textSecondary = Color(0xFFA6ABB8)
    val accentBlue = Color(0xFFA8C7FA)

    Scaffold(
        containerColor = backgroundColor,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = backgroundColor,
                    scrolledContainerColor = backgroundColor
                ),
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Indietro",
                            tint = textPrimary
                        )
                    }
                },
                title = {
                    Text(
                        text = "Easter Egg",
                        fontFamily = QuicksandFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 20.sp,
                        color = textPrimary
                    )
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Badge Easter Egg sbloccato
            Surface(
                color = Color(0xFF1D2433),
                shape = CircleShape,
                border = BorderStroke(1.dp, Color(0xFF323F5D)),
                modifier = Modifier.padding(bottom = 20.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = accentBlue,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "26 + 04 • Easter Egg sbloccato",
                        fontFamily = QuicksandFontFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = 13.sp,
                        color = accentBlue
                    )
                }
            }

            // Foto / Illustrazione Autore con cornice elegante
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(208.dp)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(Color(0xFF2B3A5A), Color.Transparent)
                        ),
                        shape = CircleShape
                    )
            ) {
                Image(
                    painter = painterResource(id = R.drawable.easter_egg_author),
                    contentDescription = "Lorenzo Trinati",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(196.dp)
                        .clip(CircleShape)
                        .border(2.5.dp, Color(0xFF384766), CircleShape)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Nome
            Text(
                text = "Lorenzo Trinati",
                fontFamily = QuicksandFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 26.sp,
                color = textPrimary,
                letterSpacing = 0.5.sp
            )

            // Ruolo / Sottotitolo
            Text(
                text = "Creatore dell'applicazione",
                fontFamily = QuicksandFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 15.sp,
                color = accentBlue,
                modifier = Modifier.padding(top = 4.dp, bottom = 18.dp)
            )

            // Scheda con descrizione rielaborata e sintetica
            Surface(
                color = cardBackground,
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, cardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Il progetto nasce dall'esigenza di migliorare la calcolatrice stock di Google Pixel. Nato per uso personale, ho voluto condividerlo con tutti nella speranza che sia utile e possa continuare a crescere ed evolversi.",
                        fontFamily = QuicksandFontFamily,
                        fontWeight = FontWeight.Normal,
                        fontSize = 15.sp,
                        lineHeight = 22.sp,
                        color = Color(0xFFD6DAE3),
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Sezione Link Social
            Text(
                text = "CONTATTI & PROGETTI",
                fontFamily = QuicksandFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 12.sp,
                letterSpacing = 1.2.sp,
                color = textSecondary,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 6.dp, bottom = 12.dp)
            )

            // Pulsante Instagram
            SocialButton(
                iconRes = R.drawable.ic_instagram,
                title = "Instagram",
                handle = "@lorenzo.trinati",
                onClick = { openLink("https://www.instagram.com/lorenzo.trinati") }
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Pulsante GitHub
            SocialButton(
                iconRes = R.drawable.ic_github,
                title = "GitHub",
                handle = "trinatilorenzo",
                onClick = { openLink("https://github.com/trinatilorenzo") }
            )

            Spacer(modifier = Modifier.height(30.dp))

            // Piccolo footer
            Text(
                text = "Pixel Calculator • Made with care",
                fontFamily = QuicksandFontFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 12.sp,
                color = Color(0xFF6B7080)
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun SocialButton(
    iconRes: Int,
    title: String,
    handle: String,
    onClick: () -> Unit
) {
    Surface(
        color = Color(0xFF181A20),
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, Color(0xFF2A2D37)),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(Color(0xFF242731), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = iconRes),
                    contentDescription = title,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    fontFamily = QuicksandFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    color = Color(0xFFF1F2F6)
                )
                Text(
                    text = handle,
                    fontFamily = QuicksandFontFamily,
                    fontWeight = FontWeight.Normal,
                    fontSize = 13.sp,
                    color = Color(0xFFA6ABB8)
                )
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                contentDescription = null,
                tint = Color(0xFF757A89),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
