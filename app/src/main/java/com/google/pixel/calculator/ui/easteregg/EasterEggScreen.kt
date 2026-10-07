package com.google.pixel.calculator.ui.easteregg

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
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
            Toast.makeText(context, context.getString(R.string.cannot_open_link), Toast.LENGTH_SHORT).show()
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
                            contentDescription = stringResource(R.string.back),
                            tint = textPrimary
                        )
                    }
                },
                title = {}
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp, vertical = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Full image shown with natural proportions and no frame
            Image(
                painter = painterResource(id = R.drawable.easter_egg_author),
                contentDescription = "Lorenzo Trinati",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .heightIn(min = 170.dp, max = 215.dp)
                    .aspectRatio(842f / 1024f)
                    .padding(bottom = 8.dp)
            )

            // Name
            Text(
                text = "Lorenzo Trinati",
                fontFamily = QuicksandFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp,
                color = textPrimary,
                letterSpacing = 0.5.sp
            )

            // Role / Subtitle
            Text(
                text = stringResource(R.string.easter_egg_role),
                fontFamily = QuicksandFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
                color = accentBlue,
                modifier = Modifier.padding(top = 2.dp, bottom = 12.dp)
            )

            // Card with concise description
            Surface(
                color = cardBackground,
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, cardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(R.string.easter_egg_bio),
                        fontFamily = QuicksandFontFamily,
                        fontWeight = FontWeight.Normal,
                        fontSize = 14.sp,
                        lineHeight = 20.sp,
                        color = Color(0xFFD6DAE3),
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Social links section
            Text(
                text = stringResource(R.string.easter_egg_contacts),
                fontFamily = QuicksandFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 11.sp,
                letterSpacing = 1.2.sp,
                color = textSecondary,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 4.dp, bottom = 8.dp)
            )

            // Instagram button
            SocialButton(
                iconRes = R.drawable.ic_instagram,
                title = "Instagram",
                handle = "@lorenzotrinati",
                onClick = { openLink("https://www.instagram.com/lorenzotrinati") }
            )

            Spacer(modifier = Modifier.height(8.dp))

            // GitHub button
            SocialButton(
                iconRes = R.drawable.ic_github,
                title = "GitHub",
                handle = "trinatilorenzo",
                onClick = { openLink("https://github.com/trinatilorenzo") }
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Footer
            Text(
                text = "Made with care",
                fontFamily = QuicksandFontFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 12.sp,
                color = Color(0xFF6B7080)
            )

            Spacer(modifier = Modifier.height(12.dp))
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
                .padding(horizontal = 16.dp, vertical = 10.dp),
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
