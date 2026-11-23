package com.electrosathi.app.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.electrosathi.app.R
import com.electrosathi.app.ui.theme.BrandCyan
import kotlinx.coroutines.delay

/**
 * Brand splash (Batch 4): the chip-and-bolt logo over a deep-navy glow,
 * the two-tone wordmark and the "Interest to Profession" tagline.
 * Shows briefly on every start, then hands over to onboarding
 * (first run) or straight to the main app.
 */
@Composable
fun SplashScreen(onFinished: () -> Unit) {
    LaunchedEffect(Unit) {
        delay(2200)
        onFinished()
    }
    Box(
        modifier = Modifier.fillMaxSize().background(
            Brush.verticalGradient(
                listOf(Color(0xFF0A0E1C), Color(0xFF0D1B3E), Color(0xFF0A0E1C))
            )
        ),
        contentAlignment = Alignment.Center
    ) {
        // Soft circuit-glow blobs behind the logo.
        Box(
            Modifier.size(320.dp).offset(x = (-120).dp, y = (-280).dp)
                .background(
                    Brush.radialGradient(
                        listOf(BrandCyan.copy(alpha = 0.16f), Color.Transparent)
                    ),
                    CircleShape
                )
        )
        Box(
            Modifier.size(320.dp).offset(x = 130.dp, y = 300.dp)
                .background(
                    Brush.radialGradient(
                        listOf(Color(0xFF7C3AED).copy(alpha = 0.14f), Color.Transparent)
                    ),
                    CircleShape
                )
        )
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Image(
                painter = painterResource(R.drawable.logo_mark),
                contentDescription = "ElectroSathi logo",
                modifier = Modifier.size(168.dp)
            )
            Spacer(Modifier.height(22.dp))
            Text(
                buildAnnotatedString {
                    withStyle(
                        SpanStyle(color = Color.White, fontWeight = FontWeight.Bold)
                    ) { append("Electro") }
                    withStyle(
                        SpanStyle(color = BrandCyan, fontWeight = FontWeight.Bold)
                    ) { append("Sathi") }
                },
                fontSize = 38.sp
            )
            Spacer(Modifier.height(10.dp))
            Text(
                "INTEREST TO PROFESSION",
                fontSize = 12.sp,
                letterSpacing = 3.sp,
                color = Color(0xFF9AA3B8)
            )
        }
        CircularProgressIndicator(
            color = BrandCyan,
            strokeWidth = 2.5.dp,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 54.dp)
                .size(26.dp)
        )
    }
}
