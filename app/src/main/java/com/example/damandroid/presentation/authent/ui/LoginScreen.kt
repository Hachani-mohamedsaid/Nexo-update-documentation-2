package com.example.damandroid

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.damandroid.api.AuthRepository
import com.example.damandroid.auth.RememberMeStore
import com.example.damandroid.ui.theme.AuthScreenPalette
import com.example.damandroid.ui.theme.DamAndroidTheme
import com.example.damandroid.ui.theme.LocalThemeController
import com.example.damandroid.ui.theme.ThemeController
import com.example.damandroid.ui.theme.rememberAppThemeColors
import com.example.damandroid.ui.theme.rememberAuthScreenPalette
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    onLogin: () -> Unit,
    onSignUpClick: () -> Unit,
    onForgotPasswordClick: () -> Unit,
    logoResId: Int = R.drawable.nexo_logo,
    onGoogleSignInRequest: ((GoogleSignInAccount?) -> Unit) -> Unit = {},
    googleSignInHelper: com.example.damandroid.auth.GoogleSignInHelper? = null,
    facebookSignInHelper: com.example.damandroid.auth.FacebookSignInHelper? = null,
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showVerificationPrompt by remember { mutableStateOf(false) }
    var verificationMessage by remember { mutableStateOf<String?>(null) }
    var isResending by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val rememberMeStore = remember(context) { RememberMeStore(context.applicationContext) }
    var rememberMe by remember { mutableStateOf(rememberMeStore.isRememberMeEnabled()) }

    val authRepository = remember(context) { AuthRepository(context.applicationContext) }
    val coroutineScope = rememberCoroutineScope()
    val themeController = LocalThemeController.current
    val appTheme = rememberAppThemeColors(themeController.isDarkMode)
    val palette = rememberAuthScreenPalette(appTheme)

    LaunchedEffect(Unit) {
        if (rememberMe) {
            rememberMeStore.getEmail()?.let { savedEmail ->
                email = savedEmail
            }
            rememberMeStore.getPassword()?.let { savedPassword ->
                password = savedPassword
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(palette.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Logo Section
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(bottom = 32.dp)
            ) {
                // White rounded square with logo
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White)
                ) {
                    Image(
                        painter = painterResource(id = logoResId),
                        contentDescription = "NEXO Logo",
                        modifier = Modifier
                            .size(56.dp)
                            .align(Alignment.Center),
                        contentScale = ContentScale.Fit
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Welcome Back",
                    color = Color.Black,
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Sign in to continue your fitness journey",
                    color = Color(0xFF6B7280),
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center
                )
            }

            // Login Form Card - White rounded rectangle
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 384.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp)
                ) {

                        // Email Input
                        Column(
                            modifier = Modifier.padding(bottom = 16.dp)
                        ) {
                            Text(
                                text = "Email",
                                color = Color(0xFF1F2937),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
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
                                        tint = Color(0xFF6B7280),
                                        modifier = Modifier.size(20.dp)
                                    )
                                },
                                singleLine = true,
                                textStyle = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.Black,
                                    unfocusedTextColor = Color.Black,
                                    focusedPlaceholderColor = Color(0xFF9CA3AF),
                                    unfocusedPlaceholderColor = Color(0xFF9CA3AF),
                                    focusedBorderColor = Color(0xFFE5E7EB),
                                    unfocusedBorderColor = Color(0xFFE5E7EB),
                                    cursorColor = Color.Black,
                                    focusedContainerColor = Color(0xFFF9FAFB),
                                    unfocusedContainerColor = Color(0xFFF9FAFB),
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp)
                            )
                        }

                        // Password Input
                        Column(
                            modifier = Modifier.padding(bottom = 8.dp)
                        ) {
                            Text(
                                text = "Password",
                                color = Color(0xFF1F2937),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            OutlinedTextField(
                                value = password,
                                onValueChange = { password = it },
                                placeholder = {
                                    Text(
                                        "Enter your password",
                                        color = Color(0xFF9CA3AF)
                                    )
                                },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.Lock,
                                        contentDescription = null,
                                        tint = Color(0xFF6B7280),
                                        modifier = Modifier.size(20.dp)
                                    )
                                },
                                trailingIcon = {
                                    IconButton(onClick = { showPassword = !showPassword }) {
                                        Icon(
                                            imageVector = if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                            contentDescription = null,
                                            tint = Color(0xFF6B7280),
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                },
                                singleLine = true,
                                visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.Black,
                                    unfocusedTextColor = Color.Black,
                                    focusedPlaceholderColor = Color(0xFF9CA3AF),
                                    unfocusedPlaceholderColor = Color(0xFF9CA3AF),
                                    focusedBorderColor = Color(0xFFE5E7EB),
                                    unfocusedBorderColor = Color(0xFFE5E7EB),
                                    cursorColor = Color.Black,
                                    focusedContainerColor = Color(0xFFF9FAFB),
                                    unfocusedContainerColor = Color(0xFFF9FAFB),
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp)
                            )
                        }

                        // Remember Me Checkbox
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp, bottom = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = rememberMe,
                                onCheckedChange = { 
                                    rememberMe = it
                                    if (!it) {
                                        // Si l'utilisateur décoche, supprimer les credentials sauvegardés
                                        rememberMeStore.setEnabled(false)
                                    }
                                },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = Color.Black,
                                    uncheckedColor = Color(0xFF9CA3AF),
                                    checkmarkColor = Color.White
                                )
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Remember me",
                                color = Color(0xFF1F2937),
                                fontSize = 14.sp,
                                modifier = Modifier.clickable { 
                                    rememberMe = !rememberMe
                                    if (!rememberMe) {
                                        rememberMeStore.setEnabled(false)
                                    }
                                }
                            )
                        }

                        // Error Message
                        if (errorMessage != null) {
                            Text(
                                text = errorMessage!!,
                                color = palette.errorText,
                                fontSize = 12.sp,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp, bottom = 4.dp)
                            )
                        }

                        if (showVerificationPrompt) {
                            VerificationPromptCard(
                                palette = palette,
                                email = email.trim(),
                                isResending = isResending,
                                statusMessage = verificationMessage,
                                onResend = {
                                    if (email.trim().isBlank()) {
                                        verificationMessage = "Enter your email first"
                                        return@VerificationPromptCard
                                    }
                                    verificationMessage = null
                                    coroutineScope.launch {
                                        isResending = true
                                        val result = authRepository.sendVerificationEmail(email.trim())
                                        isResending = false
                                        verificationMessage = when (result) {
                                            is AuthRepository.PasswordResetResult.Success -> result.message
                                            is AuthRepository.PasswordResetResult.Error -> result.message
                                        }
                                    }
                                }
                            )
                        }

                        // Forgot Password (right-aligned)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(onClick = onForgotPasswordClick) {
                                Text(
                                    "Forgot password?",
                                    color = Color.Black,
                                    fontSize = 14.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Sign In Button - White with black text and shadow
                        Button(
                            onClick = {
                                if (email.isBlank() || password.isBlank()) {
                                    errorMessage = "Please fill in all fields"
                                    return@Button
                                }
                                isLoading = true
                                errorMessage = null
                                showVerificationPrompt = false
                                verificationMessage = null
                                coroutineScope.launch {
                                    val result = authRepository.login(
                                        email = email.trim(),
                                        password = password,
                                        rememberMe = rememberMe
                                    )
                                    isLoading = false
                                    when (result) {
                                        is AuthRepository.AuthResult.Success -> {
                                            showVerificationPrompt = false
                                            if (rememberMe) {
                                                rememberMeStore.saveCredentials(email.trim(), password)
                                            } else {
                                                rememberMeStore.setEnabled(false)
                                            }
                                            onLogin()
                                        }
                                        is AuthRepository.AuthResult.Error -> {
                                            errorMessage = result.message
                                            showVerificationPrompt = result.message.contains("Email not verified", ignoreCase = true)
                                            if (!showVerificationPrompt) {
                                                verificationMessage = null
                                            }
                                        }
                                    }
                                }
                            },
                            enabled = !isLoading,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.White,
                                disabledContainerColor = Color.White.copy(alpha = 0.7f)
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
                                    "Sign In",
                                    color = Color.Black,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Divider - Small circular badge with "or"
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = Color(0xFFF3F4F6),
                                modifier = Modifier.padding(horizontal = 12.dp)
                            ) {
                                Text(
                                    "or",
                                    color = Color(0xFF6B7280),
                                    fontSize = 14.sp,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Social Login Buttons
                        SocialButton(
                            text = "Continue with Google",
                            iconResId = R.drawable.google,
                            onClick = {
                                onGoogleSignInRequest { account ->
                                    if (account != null) {
                                        // Handle successful Google Sign-In
                                        coroutineScope.launch {
                                            isLoading = true
                                            errorMessage = null
                                            
                                            try {
                                                val email = account.email ?: ""
                                                val name = account.displayName ?: ""
                                                val idToken = account.idToken
                                                val photoUrl = account.photoUrl?.toString()
                                                
                                                android.util.Log.d("LoginScreen", "Google Sign-In successful: $email")
                                                
                                                // Send to backend API
                                                val result = authRepository.loginWithGoogle(
                                                    email = email,
                                                    name = name,
                                                    idToken = idToken,
                                                    photoUrl = photoUrl
                                                )
                                                
                                                isLoading = false
                                                
                                                when (result) {
                                                    is AuthRepository.AuthResult.Success -> {
                                                        // TODO: Save token to SharedPreferences or secure storage
                                                        android.util.Log.d("LoginScreen", "Google login backend successful")
                                                        onLogin()
                                                    }
                                                    is AuthRepository.AuthResult.Error -> {
                                                        errorMessage = result.message
                                                        android.util.Log.e("LoginScreen", "Google login backend error: ${result.message}")
                                                    }
                                                }
                                            } catch (e: Exception) {
                                                isLoading = false
                                                errorMessage = "Error connecting to server: ${e.message}"
                                                android.util.Log.e("LoginScreen", "Exception in Google login: ${e.message}", e)
                                            }
                                        }
                                    } else {
                                        // Google Sign-In failed or cancelled
                                        android.util.Log.e("LoginScreen", "Google Sign-In failed or cancelled")
                                        coroutineScope.launch {
                                            errorMessage = "Google Sign-In failed. Please check your Google account configuration or try again."
                                        }
                                    }
                                }
                            },
                            palette = palette,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp)
                        )

                        SocialButton(
                            text = "Continue with Facebook",
                            iconResId = R.drawable.fb,
                            onClick = {
                                coroutineScope.launch {
                                    isLoading = true
                                    errorMessage = null
                                    
                                    try {
                                        val loginResult = facebookSignInHelper?.login() 
                                            ?: throw IllegalStateException("FacebookSignInHelper not provided")
                                        
                                        when (loginResult) {
                                            is com.example.damandroid.auth.FacebookSignInHelper.FacebookLoginResult.Success -> {
                                                val email = loginResult.email ?: ""
                                                val name = loginResult.name ?: ""
                                                val userId = loginResult.userId
                                                val accessToken = loginResult.accessToken.token
                                                val photoUrl = loginResult.photoUrl
                                                
                                                android.util.Log.d("LoginScreen", "Facebook Sign-In successful: $email")
                                                
                                                // Send to backend API
                                                val result = authRepository.loginWithFacebook(
                                                    email = email,
                                                    name = name,
                                                    userId = userId,
                                                    accessToken = accessToken,
                                                    photoUrl = photoUrl
                                                )
                                                
                                                isLoading = false
                                                
                                                when (result) {
                                                    is AuthRepository.AuthResult.Success -> {
                                                        // TODO: Save token to SharedPreferences or secure storage
                                                        android.util.Log.d("LoginScreen", "Facebook login backend successful")
                                                        onLogin()
                                                    }
                                                    is AuthRepository.AuthResult.Error -> {
                                                        errorMessage = result.message
                                                        android.util.Log.e("LoginScreen", "Facebook login backend error: ${result.message}")
                                                    }
                                                }
                                            }
                                            is com.example.damandroid.auth.FacebookSignInHelper.FacebookLoginResult.Cancelled -> {
                                                isLoading = false
                                                android.util.Log.d("LoginScreen", "Facebook Sign-In cancelled")
                                            }
                                        }
                                    } catch (e: Exception) {
                                        isLoading = false
                                        errorMessage = "Error connecting to Facebook: ${e.message}"
                                        android.util.Log.e("LoginScreen", "Exception in Facebook login: ${e.message}", e)
                                    }
                                }
                            },
                            palette = palette,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

            Spacer(modifier = Modifier.height(24.dp))

            // Sign Up Link
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Don't have an account? ",
                    color = Color(0xFF6B7280),
                    fontSize = 14.sp
                )
                Text(
                    text = "Sign Up",
                    color = Color.Black,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { onSignUpClick() }
                )
            }
        }
    }
}

@Composable
private fun SocialButton(
    text: String,
    iconResId: Int,
    onClick: () -> Unit,
    palette: AuthScreenPalette,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(48.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.White
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 1.dp,
            pressedElevation = 0.dp
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = iconResId),
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                contentScale = ContentScale.Fit
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = text,
                color = Color.Black,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun VerificationPromptCard(
    palette: AuthScreenPalette,
    email: String,
    isResending: Boolean,
    statusMessage: String?,
    onResend: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp),
        colors = CardDefaults.cardColors(containerColor = palette.cardSurface),
        border = BorderStroke(2.dp, palette.glassBorder),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier.size(40.dp),
                    color = palette.linkText.copy(alpha = 0.15f),
                    shape = CircleShape
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.MailOutline,
                            contentDescription = null,
                            tint = palette.primaryText,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "Verify your email",
                        color = palette.primaryText,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "We sent a verification link to $email. Please verify to continue.",
                        color = palette.secondaryText,
                        fontSize = 13.sp
                    )
                }
            }

            Button(
                onClick = onResend,
                enabled = !isResending,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = palette.linkText,
                    contentColor = Color.White
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
            ) {
                if (isResending) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Resend verification email", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                }
            }

            statusMessage?.let { message ->
                Text(
                    text = message,
                    color = palette.primaryText,
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Preview(
    name = "Login Screen - Light Mode",
    showBackground = true,
    showSystemUi = true,
    device = "spec:width=411dp,height=891dp,dpi=420,isRound=false,chinSize=0dp,orientation=portrait"
)
@Composable
fun LoginScreenPreview() {
    val controller = ThemeController(isDarkMode = false) { }
    androidx.compose.runtime.CompositionLocalProvider(LocalThemeController provides controller) {
        DamAndroidTheme(darkTheme = controller.isDarkMode) {
            LoginScreen(
                onLogin = { },
                onSignUpClick = { },
                onForgotPasswordClick = { }
            )
        }
    }
}

@Preview(
    name = "Login Screen - Dark Mode",
    showBackground = true,
    showSystemUi = true,
    device = "spec:width=411dp,height=891dp,dpi=420,isRound=false,chinSize=0dp,orientation=portrait"
)
@Composable
fun LoginScreenDarkPreview() {
    val controller = ThemeController(isDarkMode = true) { }
    androidx.compose.runtime.CompositionLocalProvider(LocalThemeController provides controller) {
        DamAndroidTheme(darkTheme = controller.isDarkMode) {
            LoginScreen(
                onLogin = { },
                onSignUpClick = { },
                onForgotPasswordClick = { }
            )
        }
    }
}
