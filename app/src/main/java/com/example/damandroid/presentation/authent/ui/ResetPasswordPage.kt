package com.example.damandroid

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.damandroid.api.AuthRepository
import kotlinx.coroutines.launch

@Composable
fun ResetPasswordPage(
    onBackToLogin: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var isEmailSent by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    val context = LocalContext.current
    val authRepository = remember(context) { AuthRepository(context.applicationContext) }
    val coroutineScope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFE8D5F2), // Light purple top
                        Color(0xFFF0E5F5)  // Lighter purple bottom
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(200.dp))
            if (!isEmailSent) {
                // Mail Icon Badge - Rounded square (squircle) with shadow
                Card(
                    modifier = Modifier.size(100.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        // Purple gradient envelope icon
                        EnvelopeIcon(
                            modifier = Modifier.size(50.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(40.dp))

                // Headline
                Text(
                    text = "Reset Password",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    letterSpacing = (-0.5).sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Subtitle
                Text(
                    text = "Enter your email address and we'll send you a link to reset your password",
                    fontSize = 15.sp,
                    color = Color(0xFF9CA3AF),
                    lineHeight = 22.sp, // 1.5 line height
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .padding(bottom = 50.dp)
                )

                // Main Content Card - White rounded rectangle
                Card(
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .widthIn(max = 340.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White.copy(alpha = 0.95f)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(28.dp)
                    ) {
                        // Email Address Label
                        Text(
                            text = "Email Address",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.Black,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        // Email Input Field
                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            placeholder = {
                                Text(
                                    "your@email.com",
                                    color = Color(0xFF9CA3AF)
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Email,
                                    contentDescription = null,
                                    tint = Color(0xFF9CA3AF),
                                    modifier = Modifier.size(20.dp)
                                )
                            },
                            singleLine = true,
                            textStyle = MaterialTheme.typography.bodyMedium.copy(
                                fontSize = 16.sp,
                                color = Color(0xFF6366F1) // Blue/purple for entered text
                            ),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color(0xFF6366F1),
                                unfocusedTextColor = Color(0xFF6366F1),
                                focusedPlaceholderColor = Color(0xFF9CA3AF),
                                unfocusedPlaceholderColor = Color(0xFF9CA3AF),
                                focusedBorderColor = Color(0xFFE5E7EB),
                                unfocusedBorderColor = Color(0xFFE5E7EB),
                                cursorColor = Color(0xFF6366F1),
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White,
                            ),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                        )

                        // Error Message
                        if (errorMessage != null) {
                            Text(
                                text = errorMessage!!,
                                color = Color(0xFFEF4444),
                                fontSize = 12.sp,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp, bottom = 8.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Send Reset Link Button
                        Button(
                            onClick = {
                                if (email.isBlank()) {
                                    errorMessage = "Please enter your email address"
                                    return@Button
                                }
                                isLoading = true
                                errorMessage = null
                                coroutineScope.launch {
                                    val result = authRepository.forgotPassword(email.trim())
                                    isLoading = false
                                    when (result) {
                                        is AuthRepository.PasswordResetResult.Success -> {
                                            isEmailSent = true
                                        }
                                        is AuthRepository.PasswordResetResult.Error -> {
                                            errorMessage = result.message
                                        }
                                    }
                                }
                            },
                            enabled = !isLoading,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.White
                            ),
                            elevation = ButtonDefaults.buttonElevation(
                                defaultElevation = 2.dp,
                                pressedElevation = 1.dp
                            )
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = Color.Black,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text(
                                    "Send Reset Link",
                                    color = Color.Black,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    letterSpacing = (-0.3).sp
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(40.dp))

                // Back to Sign In Link
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Remember your password? ",
                        color = Color(0xFF9CA3AF),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Normal
                    )
                    Text(
                        text = "Back to Sign In",
                        color = Color.Black,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.clickable { onBackToLogin() }
                    )
                }

                Spacer(modifier = Modifier.height(40.dp))
            } else {
                // Success State - Simplified design
                Spacer(modifier = Modifier.height(200.dp))
                
                // Success Icon Badge with shadow
                Card(
                    modifier = Modifier.size(100.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(50.dp),
                            tint = Color(0xFF2ECC71)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(40.dp))

                Text(
                    text = "Check Your Email",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    letterSpacing = (-0.5).sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "We've sent a password reset link to",
                    fontSize = 15.sp,
                    color = Color(0xFF9CA3AF),
                    lineHeight = 22.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .padding(bottom = 8.dp)
                )

                Text(
                    text = email,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(50.dp))

                // Success Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .widthIn(max = 340.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White.copy(alpha = 0.95f)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Click the link in the email to reset your password. If you don't see the email, check your spam folder.",
                            color = Color(0xFF6B7280),
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(bottom = 24.dp)
                        )

                        // Back to Sign In Button
                        Button(
                            onClick = {
                                isEmailSent = false
                                onBackToLogin()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.White
                            ),
                            elevation = ButtonDefaults.buttonElevation(
                                defaultElevation = 2.dp,
                                pressedElevation = 1.dp
                            )
                        ) {
                            Text(
                                "Back to Sign In",
                                color = Color.Black,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.SemiBold,
                                letterSpacing = (-0.3).sp
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Resend Link
                        Text(
                            text = "Didn't receive the email? Send again",
                            color = Color(0xFF8B5CF6),
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    if (email.isNotBlank() && !isLoading) {
                                        isLoading = true
                                        errorMessage = null
                                        coroutineScope.launch {
                                            val result = authRepository.forgotPassword(email.trim())
                                            isLoading = false
                                            when (result) {
                                                is AuthRepository.PasswordResetResult.Success -> {
                                                    // Email sent again
                                                }
                                                is AuthRepository.PasswordResetResult.Error -> {
                                                    errorMessage = result.message
                                                    isEmailSent = false
                                                }
                                            }
                                        }
                                    }
                                }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}

/**
 * Envelope icon with purple gradient - Mail icon for reset password
 */
@Composable
private fun EnvelopeIcon(
    modifier: Modifier = Modifier
) {
    Canvas(
        modifier = modifier
    ) {
        val width = size.width
        val height = size.height
        
        // Create purple gradient
        val gradient = Brush.linearGradient(
            colors = listOf(
                Color(0xFF8B5CF6), // Purple
                Color(0xFFA78BFA)  // Lighter purple
            )
        )
        
        // Draw envelope base (rectangle)
        val basePath = Path().apply {
            moveTo(width * 0.15f, height * 0.3f)
            lineTo(width * 0.15f, height * 0.85f)
            lineTo(width * 0.85f, height * 0.85f)
            lineTo(width * 0.85f, height * 0.3f)
            close()
        }
        
        // Draw envelope flap (triangle)
        val flapPath = Path().apply {
            moveTo(width * 0.15f, height * 0.3f)
            lineTo(width / 2f, height * 0.5f)
            lineTo(width * 0.85f, height * 0.3f)
            close()
        }
        
        // Fill envelope base with gradient
        drawPath(
            path = basePath,
            brush = gradient
        )
        
        // Fill envelope flap with gradient (slightly darker)
        drawPath(
            path = flapPath,
            brush = gradient
        )
    }
}

