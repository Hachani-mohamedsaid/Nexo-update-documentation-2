package com.example.damandroid

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.damandroid.api.AuthRepository
import com.example.damandroid.api.LocationRepository
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.example.damandroid.api.CityLocation
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.filled.ArrowDropUp

@Composable
fun SignUpPage(
    onSignUp: () -> Unit,
    onLoginClick: () -> Unit,
    logoResId: Int = R.drawable.nexo_logo,
    onGoogleSignInRequest: ((GoogleSignInAccount?) -> Unit) -> Unit = {},
    googleSignInHelper: com.example.damandroid.auth.GoogleSignInHelper? = null,
    facebookSignInHelper: com.example.damandroid.auth.FacebookSignInHelper? = null,
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    var showConfirmPassword by remember { mutableStateOf(false) }
    var acceptTerms by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    // Location suggestions
    var locationSuggestions by remember { mutableStateOf<List<CityLocation>>(emptyList()) }
    var showLocationSuggestions by remember { mutableStateOf(false) }
    var isSearchingLocation by remember { mutableStateOf(false) }
    
    val context = LocalContext.current
    val authRepository = remember(context) { AuthRepository(context.applicationContext) }
    val locationRepository = remember { LocationRepository() }
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
            Spacer(modifier = Modifier.height(60.dp))

            // App Logo Badge - Rounded square (squircle)
            Card(
                modifier = Modifier.size(90.dp),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    // NEXO Logo - Image resource
                    Image(
                        painter = painterResource(id = logoResId),
                        contentDescription = "NEXO Logo",
                        modifier = Modifier.size(50.dp),
                        contentScale = ContentScale.Fit
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Headline
            Text(
                text = "Join NEXO",
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                letterSpacing = (-0.5).sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Subtitle
            Text(
                text = "Create your account and start connecting",
                fontSize = 15.sp,
                color = Color(0xFFA0A0A0),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 35.dp)
            )

            // Main Form Card - White rounded rectangle
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.94f)
                    .widthIn(max = 380.dp),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White.copy(alpha = 0.9f)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp)
                ) {
                        // Full Name Field
                        Column(
                            modifier = Modifier.padding(bottom = 20.dp)
                        ) {
                            Text(
                                text = "Full Name",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.Black,
                                modifier = Modifier.padding(bottom = 10.dp)
                            )
                            OutlinedTextField(
                                value = name,
                                onValueChange = { name = it },
                                placeholder = {
                                    Text(
                                        "Your name",
                                        color = Color(0xFFC0C0C0),
                                        fontSize = 15.sp
                                    )
                                },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.Person,
                                        contentDescription = null,
                                        tint = Color(0xFFC0C0C0),
                                        modifier = Modifier.size(20.dp)
                                    )
                                },
                                singleLine = true,
                                textStyle = MaterialTheme.typography.bodyMedium.copy(fontSize = 15.sp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.Black,
                                    unfocusedTextColor = Color.Black,
                                    focusedPlaceholderColor = Color(0xFFC0C0C0),
                                    unfocusedPlaceholderColor = Color(0xFFC0C0C0),
                                    focusedBorderColor = Color(0xFFE5E7EB),
                                    unfocusedBorderColor = Color(0xFFE5E7EB),
                                    cursorColor = Color.Black,
                                    focusedContainerColor = Color.White,
                                    unfocusedContainerColor = Color.White,
                                ),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp)
                            )
                        }

                        // Email Field
                        Column(
                            modifier = Modifier.padding(bottom = 20.dp)
                        ) {
                            Text(
                                text = "Email",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.Black,
                                modifier = Modifier.padding(bottom = 10.dp)
                            )
                            OutlinedTextField(
                                value = email,
                                onValueChange = { email = it },
                                placeholder = {
                                    Text(
                                        "your@email.com",
                                        color = Color(0xFFC0C0C0),
                                        fontSize = 15.sp
                                    )
                                },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.Email,
                                        contentDescription = null,
                                        tint = Color(0xFFC0C0C0),
                                        modifier = Modifier.size(20.dp)
                                    )
                                },
                                singleLine = true,
                                textStyle = MaterialTheme.typography.bodyMedium.copy(fontSize = 15.sp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.Black,
                                    unfocusedTextColor = Color.Black,
                                    focusedPlaceholderColor = Color(0xFFC0C0C0),
                                    unfocusedPlaceholderColor = Color(0xFFC0C0C0),
                                    focusedBorderColor = Color(0xFFE5E7EB),
                                    unfocusedBorderColor = Color(0xFFE5E7EB),
                                    cursorColor = Color.Black,
                                    focusedContainerColor = Color.White,
                                    unfocusedContainerColor = Color.White,
                                ),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp)
                            )
                        }

                        // Location Field with suggestions
                        Column(
                            modifier = Modifier
                                .padding(bottom = 20.dp)
                                .fillMaxWidth()
                        ) {
                            Text(
                                text = "Location (State/Province)",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.Black,
                                modifier = Modifier.padding(bottom = 10.dp)
                            )
                            
                            Box {
                                OutlinedTextField(
                                    value = location,
                                    onValueChange = { newValue ->
                                        location = newValue
                                        showLocationSuggestions = newValue.isNotEmpty()
                                        
                                        // Debounce search
                                        coroutineScope.launch {
                                            delay(300) // Wait 300ms after user stops typing
                                            if (location == newValue && newValue.length >= 2) {
                                                isSearchingLocation = true
                                                val suggestions = locationRepository.searchCities(newValue)
                                                locationSuggestions = suggestions
                                                isSearchingLocation = false
                                                showLocationSuggestions = suggestions.isNotEmpty()
                                            } else if (newValue.length < 2) {
                                                locationSuggestions = emptyList()
                                                showLocationSuggestions = false
                                            }
                                        }
                                    },
                                    placeholder = {
                                        Text(
                                            "Enter your (State/Province)",
                                            color = Color(0xFFC0C0C0),
                                            fontSize = 15.sp
                                        )
                                    },
                                    leadingIcon = {
                                        Icon(
                                            Icons.Default.LocationOn,
                                            contentDescription = null,
                                            tint = Color(0xFFC0C0C0),
                                            modifier = Modifier.size(20.dp)
                                        )
                                    },
                                    trailingIcon = {
                                        if (isSearchingLocation) {
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(20.dp),
                                                strokeWidth = 2.dp,
                                                color = Color(0xFF9CA3AF)
                                            )
                                        } else if (showLocationSuggestions && locationSuggestions.isNotEmpty()) {
                                            Icon(
                                                Icons.Default.ArrowDropUp,
                                                contentDescription = null,
                                                tint = Color(0xFF9CA3AF)
                                            )
                                        }
                                    },
                                    singleLine = true,
                                    textStyle = MaterialTheme.typography.bodyMedium.copy(fontSize = 15.sp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = Color.Black,
                                        unfocusedTextColor = Color.Black,
                                        focusedPlaceholderColor = Color(0xFFC0C0C0),
                                        unfocusedPlaceholderColor = Color(0xFFC0C0C0),
                                        focusedBorderColor = Color(0xFFE5E7EB),
                                        unfocusedBorderColor = Color(0xFFE5E7EB),
                                        cursorColor = Color.Black,
                                        focusedContainerColor = Color.White,
                                        unfocusedContainerColor = Color.White,
                                    ),
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(50.dp)
                                )
                                
                                // Dropdown suggestions
                                if (showLocationSuggestions && locationSuggestions.isNotEmpty()) {
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 52.dp),
                                        shape = RoundedCornerShape(14.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = Color.White
                                        ),
                                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                                    ) {
                                        LazyColumn(
                                            modifier = Modifier.heightIn(max = 200.dp)
                                        ) {
                                            items(locationSuggestions) { city ->
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .clickable {
                                                            location = city.getDisplayName()
                                                            showLocationSuggestions = false
                                                        }
                                                        .padding(horizontal = 16.dp, vertical = 12.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Icon(
                                                        Icons.Default.LocationOn,
                                                        contentDescription = null,
                                                        tint = Color(0xFF8B5CF6),
                                                        modifier = Modifier.size(20.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(12.dp))
                                                    Text(
                                                        text = city.getDisplayName(),
                                                        color = Color.Black,
                                                        fontSize = 14.sp
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Password Field
                        Column(
                            modifier = Modifier.padding(bottom = 20.dp)
                        ) {
                            Text(
                                text = "Password",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.Black,
                                modifier = Modifier.padding(bottom = 10.dp)
                            )
                            OutlinedTextField(
                                value = password,
                                onValueChange = { password = it },
                                placeholder = {
                                    Text(
                                        "Create a password",
                                        color = Color(0xFFC0C0C0),
                                        fontSize = 15.sp
                                    )
                                },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.Lock,
                                        contentDescription = null,
                                        tint = Color(0xFFC0C0C0),
                                        modifier = Modifier.size(20.dp)
                                    )
                                },
                                trailingIcon = {
                                    IconButton(onClick = { showPassword = !showPassword }) {
                                        Icon(
                                            imageVector = if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                            contentDescription = null,
                                            tint = Color(0xFF9CA3AF),
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                },
                                singleLine = true,
                                visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                                textStyle = MaterialTheme.typography.bodyMedium.copy(fontSize = 15.sp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.Black,
                                    unfocusedTextColor = Color.Black,
                                    focusedPlaceholderColor = Color(0xFFC0C0C0),
                                    unfocusedPlaceholderColor = Color(0xFFC0C0C0),
                                    focusedBorderColor = Color(0xFFE5E7EB),
                                    unfocusedBorderColor = Color(0xFFE5E7EB),
                                    cursorColor = Color.Black,
                                    focusedContainerColor = Color.White,
                                    unfocusedContainerColor = Color.White,
                                ),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp)
                            )
                        }

                        // Confirm Password Field
                        Column(
                            modifier = Modifier.padding(bottom = 20.dp)
                        ) {
                            Text(
                                text = "Confirm Password",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.Black,
                                modifier = Modifier.padding(bottom = 10.dp)
                            )
                            OutlinedTextField(
                                value = confirmPassword,
                                onValueChange = { confirmPassword = it },
                                placeholder = {
                                    Text(
                                        "Confirm your password",
                                        color = Color(0xFFC0C0C0),
                                        fontSize = 15.sp
                                    )
                                },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.Lock,
                                        contentDescription = null,
                                        tint = Color(0xFFC0C0C0),
                                        modifier = Modifier.size(20.dp)
                                    )
                                },
                                trailingIcon = {
                                    IconButton(onClick = { showConfirmPassword = !showConfirmPassword }) {
                                        Icon(
                                            imageVector = if (showConfirmPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                            contentDescription = null,
                                            tint = Color(0xFF9CA3AF),
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                },
                                singleLine = true,
                                visualTransformation = if (showConfirmPassword) VisualTransformation.None else PasswordVisualTransformation(),
                                textStyle = MaterialTheme.typography.bodyMedium.copy(fontSize = 15.sp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.Black,
                                    unfocusedTextColor = Color.Black,
                                    focusedPlaceholderColor = Color(0xFFC0C0C0),
                                    unfocusedPlaceholderColor = Color(0xFFC0C0C0),
                                    focusedBorderColor = Color(0xFFE5E7EB),
                                    unfocusedBorderColor = Color(0xFFE5E7EB),
                                    cursorColor = Color.Black,
                                    focusedContainerColor = Color.White,
                                    unfocusedContainerColor = Color.White,
                                ),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp)
                            )
                        }

                        // Terms and Conditions Checkbox
                        Row(
                            modifier = Modifier.padding(bottom = 24.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = acceptTerms,
                                onCheckedChange = { acceptTerms = it },
                                modifier = Modifier.size(20.dp),
                                colors = CheckboxDefaults.colors(
                                    checkedColor = Color(0xFF8B5CF6),
                                    uncheckedColor = Color(0xFFD1D5DB),
                                    checkmarkColor = Color.White
                                )
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "I agree to the Terms of Service and Privacy Policy",
                                color = Color(0xFF9CA3AF),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Normal,
                                lineHeight = 18.sp // 1.4 line height
                            )
                        }

                        // Error Message
                        if (errorMessage != null) {
                            Text(
                                text = errorMessage!!,
                                color = Color(0xFFEF4444),
                                fontSize = 12.sp,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 8.dp)
                            )
                        }

                        // Create Account Button
                        Button(
                            onClick = {
                                if (name.isBlank() || email.isBlank() || location.isBlank() || password.isBlank() || confirmPassword.isBlank()) {
                                    errorMessage = "Please fill in all fields"
                                    return@Button
                                }
                                if (password != confirmPassword) {
                                    errorMessage = "Passwords do not match"
                                    return@Button
                                }
                                if (password.length < 6) {
                                    errorMessage = "Password must be at least 6 characters long"
                                    return@Button
                                }
                                if (!acceptTerms) {
                                    errorMessage = "Please accept the terms and conditions"
                                    return@Button
                                }
                                isLoading = true
                                errorMessage = null
                                coroutineScope.launch {
                                    val result = authRepository.register(
                                        email = email.trim(),
                                        password = password,
                                        name = name.trim(),
                                        location = location.trim()
                                    )
                                    isLoading = false
                                    when (result) {
                                        is AuthRepository.AuthResult.Success -> {
                                            if (result.token.isEmpty()) {
                                                onLoginClick()
                                            } else {
                                                onSignUp()
                                            }
                                        }
                                        is AuthRepository.AuthResult.Error -> {
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
                                    color = Color(0xFF4B5563),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text(
                                    "Create Account",
                                    color = Color(0xFF4B5563),
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    letterSpacing = (-0.3).sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Alternative Sign Up Text
                        Text(
                            text = "or sign up with",
                            fontSize = 14.sp,
                            color = Color(0xFFB0B0B0),
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp)
                        )

                        // Social Sign Up Buttons
                        SocialButton(
                            text = "Continue with Google",
                            iconResId = R.drawable.google,
                            onClick = {
                                onGoogleSignInRequest { account ->
                                    if (account != null) {
                                        coroutineScope.launch {
                                            isLoading = true
                                            errorMessage = null
                                            
                                            try {
                                                val email = account.email ?: ""
                                                val name = account.displayName ?: ""
                                                val idToken = account.idToken
                                                val photoUrl = account.photoUrl?.toString()
                                                
                                                val result = authRepository.loginWithGoogle(
                                                    email = email,
                                                    name = name,
                                                    idToken = idToken,
                                                    photoUrl = photoUrl
                                                )
                                                
                                                isLoading = false
                                                
                                                when (result) {
                                                    is AuthRepository.AuthResult.Success -> {
                                                        onSignUp()
                                                    }
                                                    is AuthRepository.AuthResult.Error -> {
                                                        errorMessage = result.message
                                                    }
                                                }
                                            } catch (e: Exception) {
                                                isLoading = false
                                                errorMessage = "Error connecting to server: ${e.message}"
                                            }
                                        }
                                    }
                                }
                            },
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
                                                        onSignUp()
                                                    }
                                                    is AuthRepository.AuthResult.Error -> {
                                                        errorMessage = result.message
                                                    }
                                                }
                                            }
                                            is com.example.damandroid.auth.FacebookSignInHelper.FacebookLoginResult.Cancelled -> {
                                                isLoading = false
                                            }
                                        }
                                    } catch (e: Exception) {
                                        isLoading = false
                                        errorMessage = "Error connecting to Facebook: ${e.message}"
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Login Link
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Already have an account? ",
                    color = Color(0xFF9CA3AF),
                    fontSize = 14.sp
                )
                Text(
                    text = "Sign In",
                    color = Color.Black,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { onLoginClick() }
                )
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
private fun SocialButton(
    text: String,
    iconResId: Int,
    onClick: () -> Unit,
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

/**
 * NEXO Logo Icon - Circular arrangement of stylized "Z" letters
 */
@Composable
private fun NexoLogoIcon(
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
        val radius = width * 0.32f
        
        // Draw 8 stylized "Z" letters in a circle pattern
        for (i in 0 until 8) {
            val angleDegrees = (i * 45f - 90f)
            val angleRad = angleDegrees * (kotlin.math.PI.toFloat() / 180f)
            
            // Position of the center of each "Z"
            val zCenterX = centerX + radius * kotlin.math.cos(angleRad)
            val zCenterY = centerY + radius * kotlin.math.sin(angleRad)
            
            // "Z" dimensions
            val zWidth = width * 0.12f
            val zHeight = width * 0.10f
            val strokeWidth = width * 0.025f // Thick, blocky lines
            
            // Rotate the "Z" to point outward from center (90 degrees added to point outward)
            val rotationAngle = angleRad + kotlin.math.PI.toFloat() / 2f
            val cosRot = kotlin.math.cos(rotationAngle)
            val sinRot = kotlin.math.sin(rotationAngle)
            
            // Helper function to rotate a point around zCenter
            fun rotatePoint(x: Float, y: Float): Pair<Float, Float> {
                val dx = x - zCenterX
                val dy = y - zCenterY
                val rotatedX = dx * cosRot - dy * sinRot + zCenterX
                val rotatedY = dx * sinRot + dy * cosRot + zCenterY
                return Pair(rotatedX, rotatedY)
            }
            
            // Define "Z" shape points (before rotation)
            val topLeft = Pair(zCenterX - zWidth / 2f, zCenterY - zHeight / 2f)
            val topRight = Pair(zCenterX + zWidth / 2f, zCenterY - zHeight / 2f)
            val bottomLeft = Pair(zCenterX - zWidth / 2f, zCenterY + zHeight / 2f)
            val bottomRight = Pair(zCenterX + zWidth / 2f, zCenterY + zHeight / 2f)
            
            // Rotate all points
            val (topLeftRot, topLeftRotY) = rotatePoint(topLeft.first, topLeft.second)
            val (topRightRot, topRightRotY) = rotatePoint(topRight.first, topRight.second)
            val (bottomLeftRot, bottomLeftRotY) = rotatePoint(bottomLeft.first, bottomLeft.second)
            val (bottomRightRot, bottomRightRotY) = rotatePoint(bottomRight.first, bottomRight.second)
            
            // Draw "Z" shape with rotated coordinates
            val zPath = Path().apply {
                // Top horizontal line
                moveTo(topLeftRot, topLeftRotY)
                lineTo(topRightRot, topRightRotY)
                
                // Diagonal line (top-right to bottom-left)
                lineTo(bottomLeftRot, bottomLeftRotY)
                
                // Bottom horizontal line
                lineTo(bottomRightRot, bottomRightRotY)
            }
            
            // Draw the "Z" with thick, rounded strokes
            drawPath(
                path = zPath,
                color = color,
                style = Stroke(
                    width = strokeWidth,
                    cap = androidx.compose.ui.graphics.StrokeCap.Round,
                    join = androidx.compose.ui.graphics.StrokeJoin.Round
                )
            )
        }
    }
}

