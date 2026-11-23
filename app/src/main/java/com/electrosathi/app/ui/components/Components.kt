package com.electrosathi.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.electrosathi.app.ui.theme.BannerGradient
import com.electrosathi.app.ui.theme.BrandCyan
import com.electrosathi.app.ui.theme.TileBorderGradient

// ---------------------------------------------------------------------------
// Section accent colors — the PrepFusion-style identity strip.
// Each app section owns one color, used consistently everywhere.
// ---------------------------------------------------------------------------
val AccentStudy = Color(0xFF3B82F6)    // blue
val AccentExams = Color(0xFF22C55E)    // green
val AccentProjects = Color(0xFFF59E0B) // amber
val AccentCareer = Color(0xFF14B8A6)    // teal
val AccentLibrary = Color(0xFFA855F7)  // purple
val AccentVolt = Color(0xFFEC4899)     // pink
val AccentHack = Color(0xFFF97316)     // orange
val AccentSkill = Color(0xFFEAB308)    // yellow

/**
 * Small uppercase blue micro-label, PrepFusion style.
 * Example: Eyebrow("By destination")
 */
@Composable
fun Eyebrow(text: String, modifier: Modifier = Modifier) {
    Text(
        text.uppercase(),
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = modifier.padding(horizontal = 16.dp).padding(top = 12.dp, bottom = 4.dp)
    )
}

/**
 * The signature card: 18dp rounded, hairline border, colored identity strip
 * on the left, tinted icon chip, title + subtitle, optional pill badge.
 * Tapping navigates when onClick is provided.
 */
@Composable
fun AccentCard(
    title: String,
    subtitle: String,
    accent: Color,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    badge: String? = null,
    onClick: (() -> Unit)? = null
) {
    Card(
        onClick = { onClick?.invoke() },
        enabled = onClick != null,
        modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Identity strip
            Box(
                modifier = Modifier.width(4.dp).height(52.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(accent)
            )
            Spacer(Modifier.width(12.dp))
            // Tinted icon chip
            Box(
                modifier = Modifier.size(46.dp)
                    .clip(RoundedCornerShape(13.dp))
                    .background(accent.copy(alpha = 0.16f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = accent,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(2.dp))
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2
                )
            }
            if (badge != null) {
                Spacer(Modifier.width(8.dp))
                AssistChip(onClick = { }, label = { Text(badge) })
            }
        }
    }
}

/**
 * Countdown-style deadline tile — the PrepFusion countdown widget,
 * adapted for internship/exam deadlines.
 */
@Composable
fun DeadlineTile(label: String, date: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth().padding(16.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        border = BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
        )
    ) {
        Column(Modifier.padding(20.dp)) {
            Text(
                "DEADLINE",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(6.dp))
            Text(date, style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(4.dp))
            Text(label, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

// ---------------------------------------------------------------------------
// Batch 4 — components for the new Home, from Ashish's reference designs.
// ---------------------------------------------------------------------------

/**
 * Circular progress ring with the percentage in the middle —
 * the "Continue Learning" card's 68% ring.
 */
@Composable
fun ProgressRing(
    progress: Float,
    modifier: Modifier = Modifier,
    size: Dp = 64.dp,
    strokeWidth: Dp = 6.dp,
    color: Color = BrandCyan,
    trackColor: Color = Color.White.copy(alpha = 0.14f)
) {
    val pct = progress.coerceIn(0f, 1f)
    Box(modifier = modifier.size(size), contentAlignment = Alignment.Center) {
        Canvas(Modifier.size(size)) {
            drawArc(
                color = trackColor,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
            )
            drawArc(
                color = color,
                startAngle = -90f,
                sweepAngle = 360f * pct,
                useCenter = false,
                style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
            )
        }
        Text(
            "${(pct * 100).toInt()}%",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

/**
 * Home shortcut tile: rounded square, gradient border, tinted icon chip,
 * label underneath the icon — Study / Exams / Projects / Career / …
 */
@Composable
fun GradientTile(
    title: String,
    icon: ImageVector,
    accent: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(108.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.5.dp, TileBorderGradient, RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier.size(52.dp)
                    .clip(RoundedCornerShape(15.dp))
                    .background(accent.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = accent, modifier = Modifier.size(28.dp))
            }
            Spacer(Modifier.height(10.dp))
            Text(
                title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

/**
 * Exam-countdown card with a gradient background. When [days] is known it
 * shows the huge day count ("84 days left"); when the exam's date text is
 * all we have, [detail] is shown prominently instead — never a made-up date.
 */
@Composable
fun CountdownCard(
    name: String,
    days: Long?,
    detail: String,
    brush: Brush,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(118.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(brush)
            .clickable(onClick = onClick)
            .padding(16.dp)
    ) {
        Column {
            Text(
                name.uppercase(),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                maxLines = 1
            )
            Spacer(Modifier.height(8.dp))
            if (days != null) {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        days.toString(),
                        fontSize = 42.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        if (days == 1L) "day left" else "days left",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.88f),
                        modifier = Modifier.padding(bottom = 7.dp)
                    )
                }
            } else {
                Text(
                    detail,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White,
                    maxLines = 3
                )
            }
        }
    }
}

/**
 * The gradient promo banner (GATE 2027): headline, one-line subtitle,
 * rocket medallion and a dark pill button.
 */
@Composable
fun PromoBanner(
    title: String,
    subtitle: String,
    buttonText: String,
    onButtonClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(BannerGradient)
            .padding(20.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(
                    title,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.9f)
                )
                Spacer(Modifier.height(14.dp))
                Surface(
                    onClick = onButtonClick,
                    shape = RoundedCornerShape(50),
                    color = Color(0xFF131C3F),
                    contentColor = Color.White
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 9.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            buttonText,
                            style = MaterialTheme.typography.labelLarge,
                            maxLines = 1
                        )
                        Spacer(Modifier.width(4.dp))
                        Icon(
                            Icons.Filled.KeyboardArrowRight,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
            Spacer(Modifier.width(12.dp))
            Box(
                modifier = Modifier.size(64.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.16f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Filled.RocketLaunch,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(34.dp)
                )
            }
        }
    }
}

/**
 * The visual search pill under Home's greeting: cyan outline, search icon,
 * hint text, mic icon. Tapping it is wired by the caller (Batch 4 Part 1
 * sends the user to Study; a real search backend is a later batch).
 */
@Composable
fun PillSearchBar(
    hint: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clip(RoundedCornerShape(50))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .border(1.dp, BrandCyan.copy(alpha = 0.55f), RoundedCornerShape(50))
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.Filled.Search,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.width(10.dp))
        Text(
            hint,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f),
            maxLines = 1
        )
        Icon(
            Icons.Filled.Mic,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
