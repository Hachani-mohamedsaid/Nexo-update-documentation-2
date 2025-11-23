package com.example.damandroid

import androidx.compose.foundation.Canvas
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import com.example.damandroid.ui.theme.LocalThemeController
import com.example.damandroid.ui.theme.rememberAppThemeColors

data class OnboardingStep(
    val title: String,
    val subtitle: String,
    val imageUrl: String,
    val icon: ImageVector
)

@Composable
fun OnboardingScreens(
    onComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var currentStep by remember { mutableStateOf(0) }
    val themeController = LocalThemeController.current
    val appTheme = rememberAppThemeColors(themeController.isDarkMode)

    val steps = listOf(
        OnboardingStep(
            title = "Welcome to NEXO",
            subtitle = "Connect with people who love sports as much as you do",
            imageUrl = "https://images.unsplash.com/photo-1571019613454-1cb2f99b2d8b?w=1080&q=80",
            icon = Icons.Default.FitnessCenter
        ),
        OnboardingStep(
            title = "Find Your Sport Partners",
            subtitle = "Discover nearby activities and join sessions with like-minded people",
            imageUrl = "https://images.unsplash.com/photo-1534438327276-14e5300c3a48?w=1080&q=80",
            icon = Icons.Default.Group
        ),
        OnboardingStep(
            title = "Stay Active Together",
            subtitle = "Create your own activities or join existing ones near you",
            imageUrl = "https://images.unsplash.com/photo-1576678927484-cc907957088c?w=1080&q=80",
            icon = Icons.Default.LocationOn
        )
    )

    val currentStepData = steps[currentStep]

    Box(
        modifier = modifier
            .fillMaxSize()
    ) {
        // Full-screen background image
        SubcomposeAsyncImage(
            model = currentStepData.imageUrl,
            contentDescription = currentStepData.title,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            loading = {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFFE5E7EB))
                )
            },
            error = {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFFE5E7EB))
                )
            },
            success = {
                SubcomposeAsyncImageContent()
            }
        )

        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Skip button - top right corner (not shown on final screen)
            if (currentStep < steps.size - 1) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 20.dp, end = 20.dp),
                    contentAlignment = Alignment.TopEnd
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color(0xFFF5F5F5)) // Light gray background
                            .clickable { onComplete() }
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = "Skip",
                            color = Color(0xFF808080), // Medium gray text
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Centered icon - large circular white badge
            Box(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(bottom = 200.dp)
            ) {
                // Large circular white badge with shadow
                Box(
                    modifier = Modifier
                        .size(140.dp)
                        .clip(CircleShape)
                        .background(Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    when (currentStep) {
                        0 -> {
                            // Screen 1: Purple heartbeat icon
                            HeartbeatIcon(
                                color = Color(0xFF8B5CF6), // Purple
                                modifier = Modifier.size(64.dp)
                            )
                        }
                        1 -> {
                            // Screen 2: Hot pink people/community icon
                            PeopleGroupIcon(
                                color = Color(0xFFE91E63), // Hot pink
                                modifier = Modifier.size(64.dp)
                            )
                        }
                        2 -> {
                            // Screen 3: Turquoise key icon
                            KeyIcon(
                                color = Color(0xFF1DE9B6), // Turquoise
                                modifier = Modifier.size(64.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Bottom Card - White rounded rectangle with gradient fade
            val cardGradient = when (currentStep) {
                0 -> Brush.verticalGradient(
                    colors = listOf(
                        Color.White,
                        Color(0xFFF3E5F5) // Light purple fade
                    )
                )
                1 -> Brush.verticalGradient(
                    colors = listOf(
                        Color.White,
                        Color(0xFFFFE5EC) // Light pink fade
                    )
                )
                2 -> Brush.verticalGradient(
                    colors = listOf(
                        Color.White,
                        Color(0xFFE0F7F4) // Light turquoise fade
                    )
                )
                else -> Brush.verticalGradient(
                    colors = listOf(Color.White, Color.White)
                )
            }
            
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 0.dp),
                shape = RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.Transparent
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(cardGradient)
                ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 32.dp)
                        .padding(top = 32.dp, bottom = 25.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Title - Large bold black text
                    Text(
                        text = currentStepData.title,
                        fontSize = 30.sp, // 28-32px range
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        textAlign = TextAlign.Center,
                        lineHeight = 36.sp, // 1.2 line height
                        letterSpacing = (-0.5).sp,
                        modifier = Modifier.padding(top = 40.dp, bottom = 15.dp)
                    )

                    // Subtitle - Light gray text
                    Text(
                        text = currentStepData.subtitle,
                        fontSize = 16.sp,
                        color = Color(0xFF9E9E9E), // Light gray
                        textAlign = TextAlign.Center,
                        lineHeight = 24.sp, // 1.5 line height
                        modifier = Modifier.padding(bottom = 25.dp)
                    )

                    // Progress indicators - 3 dots (pill shape when active)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.padding(bottom = 30.dp)
                    ) {
                        steps.forEachIndexed { index, _ ->
                            val isActive = index == currentStep
                            val activeColor = when (index) {
                                0 -> Color(0xFF8B5CF6) // Purple
                                1 -> Color(0xFFE91E63) // Hot pink
                                2 -> Color(0xFF1DE9B6) // Turquoise
                                else -> Color(0xFFE0E0E0)
                            }
                            
                            Box(
                                modifier = Modifier
                                    .height(8.dp)
                                    .width(if (isActive) 24.dp else 8.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(
                                        if (isActive) {
                                            activeColor
                                        } else {
                                            Color(0xFFE0E0E0)
                                        }
                                    )
                            )
                        }
                    }

                    // Button - "Next" or "Get Started"
                    val isLastStep = currentStep == steps.size - 1
                    val buttonText = if (isLastStep) "Get Started" else "Next"
                    val buttonIconColor = if (isLastStep) Color(0xFF1DE9B6) else Color.Black
                    
                    Button(
                        onClick = {
                            if (currentStep < steps.size - 1) {
                                currentStep++
                            } else {
                                onComplete()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth(0.85f)
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White
                        ),
                        shape = RoundedCornerShape(28.dp),
                        elevation = ButtonDefaults.buttonElevation(
                            defaultElevation = 2.dp,
                            pressedElevation = 1.dp
                        )
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = buttonText,
                                color = Color.Black,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            if (isLastStep) {
                                // Turquoise circular arrow icon
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(buttonIconColor),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ChevronRight,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp),
                                        tint = Color.White
                                    )
                                }
                            } else {
                                Icon(
                                    imageVector = Icons.Default.ChevronRight,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp),
                                    tint = Color.Black
                                )
                            }
                        }
                    }
                }
                }
            }
        }
    }
}

/**
 * Heartbeat/Pulse icon - ECG waveform pattern (Screen 1)
 */
@Composable
private fun HeartbeatIcon(
    color: Color,
    modifier: Modifier = Modifier
) {
    Canvas(
        modifier = modifier
    ) {
        val width = size.width
        val height = size.height
        val strokeWidth = 4.dp.toPx()
        
        // ECG waveform path - heartbeat pattern
        val path = Path().apply {
            // Start from left
            moveTo(0f, height * 0.5f)
            
            // Small spike up
            lineTo(width * 0.15f, height * 0.3f)
            
            // Drop down
            lineTo(width * 0.2f, height * 0.7f)
            
            // Small spike up
            lineTo(width * 0.25f, height * 0.4f)
            
            // Flat line
            lineTo(width * 0.35f, height * 0.4f)
            
            // Big spike up (heartbeat)
            lineTo(width * 0.4f, height * 0.1f)
            lineTo(width * 0.45f, height * 0.1f)
            lineTo(width * 0.5f, height * 0.4f)
            
            // Drop down
            lineTo(width * 0.55f, height * 0.9f)
            
            // Small spike up
            lineTo(width * 0.6f, height * 0.5f)
            
            // Flat line
            lineTo(width * 0.7f, height * 0.5f)
            
            // Small spike up
            lineTo(width * 0.75f, height * 0.3f)
            
            // Drop down
            lineTo(width * 0.8f, height * 0.7f)
            
            // Small spike up
            lineTo(width * 0.85f, height * 0.4f)
            
            // End flat
            lineTo(width, height * 0.4f)
        }
        
        drawPath(
            path = path,
            color = color,
            style = Stroke(
                width = strokeWidth,
                cap = androidx.compose.ui.graphics.StrokeCap.Round,
                join = androidx.compose.ui.graphics.StrokeJoin.Round
            )
        )
    }
}

/**
 * People/Community icon - Three stylized person figures (Screen 2)
 */
@Composable
private fun PeopleGroupIcon(
    color: Color,
    modifier: Modifier = Modifier
) {
    Canvas(
        modifier = modifier
    ) {
        val width = size.width
        val height = size.height
        val centerX = width / 2f
        val centerY = height / 2f
        
        // Draw three person silhouettes
        // Left person (slightly smaller, behind)
        drawCircle(
            color = color,
            radius = width * 0.12f,
            center = androidx.compose.ui.geometry.Offset(centerX - width * 0.2f, centerY - height * 0.15f)
        )
        drawRoundRect(
            color = color,
            topLeft = androidx.compose.ui.geometry.Offset(centerX - width * 0.28f, centerY - height * 0.05f),
            size = androidx.compose.ui.geometry.Size(width * 0.16f, height * 0.35f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(width * 0.08f, height * 0.08f)
        )
        
        // Center person (larger, in front)
        drawCircle(
            color = color,
            radius = width * 0.15f,
            center = androidx.compose.ui.geometry.Offset(centerX, centerY - height * 0.2f)
        )
        drawRoundRect(
            color = color,
            topLeft = androidx.compose.ui.geometry.Offset(centerX - width * 0.12f, centerY - height * 0.05f),
            size = androidx.compose.ui.geometry.Size(width * 0.24f, height * 0.4f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(width * 0.12f, height * 0.12f)
        )
        
        // Right person (slightly smaller, behind)
        drawCircle(
            color = color,
            radius = width * 0.12f,
            center = androidx.compose.ui.geometry.Offset(centerX + width * 0.2f, centerY - height * 0.15f)
        )
        drawRoundRect(
            color = color,
            topLeft = androidx.compose.ui.geometry.Offset(centerX + width * 0.12f, centerY - height * 0.05f),
            size = androidx.compose.ui.geometry.Size(width * 0.16f, height * 0.35f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(width * 0.08f, height * 0.08f)
        )
    }
}

/**
 * Key icon - Simple key silhouette (Screen 3)
 */
@Composable
private fun KeyIcon(
    color: Color,
    modifier: Modifier = Modifier
) {
    Canvas(
        modifier = modifier
    ) {
        val width = size.width
        val height = size.height
        val centerX = width / 2f
        val centerY = height / 2f
        val keyHeadRadius = width * 0.12f
        val keyHeadCenterX = centerX - width * 0.15f
        
        // Fill key head circle (circular part)
        drawCircle(
            color = color,
            radius = keyHeadRadius,
            center = Offset(keyHeadCenterX, centerY)
        )
        
        // Draw key shaft and teeth
        val path = Path().apply {
            // Start from right edge of key head
            moveTo(keyHeadCenterX + keyHeadRadius, centerY)
            
            // Key shaft (horizontal line to right)
            lineTo(centerX + width * 0.25f, centerY)
            
            // Key teeth (L-shaped at end)
            lineTo(centerX + width * 0.25f, centerY + height * 0.15f)
            lineTo(centerX + width * 0.15f, centerY + height * 0.15f)
            lineTo(centerX + width * 0.15f, centerY)
        }
        
        drawPath(
            path = path,
            color = color,
            style = Stroke(
                width = 6.dp.toPx(),
                cap = androidx.compose.ui.graphics.StrokeCap.Round,
                join = androidx.compose.ui.graphics.StrokeJoin.Round
            )
        )
    }
}

