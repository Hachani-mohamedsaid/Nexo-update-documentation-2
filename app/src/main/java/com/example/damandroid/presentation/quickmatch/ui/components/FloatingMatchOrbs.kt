package com.example.damandroid.presentation.quickmatch.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.damandroid.presentation.quickmatch.ui.model.QuickMatchThemeColors

@Composable
fun FloatingMatchOrbs(
    colors: QuickMatchThemeColors,
    modifier: Modifier = Modifier
) {
    val orbGradients = colors.orbGradients
    val orb1 = orbGradients.getOrElse(0) { listOf(Color.Transparent, Color.Transparent) }
    val orb2 = orbGradients.getOrElse(1) { listOf(Color.Transparent, Color.Transparent) }
    val orb3 = orbGradients.getOrElse(2) { listOf(Color.Transparent, Color.Transparent) }
    val orb4 = orbGradients.getOrElse(3) { listOf(Color.Transparent, Color.Transparent) }

    Box(modifier = modifier) {
        Box(
            modifier = Modifier
                .offset(x = 40.dp, y = 80.dp)
                .size(128.dp)
                .background(
                    Brush.radialGradient(colors = orb1),
                    shape = CircleShape
                )
                .blur(48.dp)
        )
        Box(
            modifier = Modifier
                .offset(x = (-40).dp, y = (-160).dp)
                .size(160.dp)
                .background(
                    Brush.radialGradient(colors = orb2),
                    shape = CircleShape
                )
                .blur(48.dp)
        )
        Box(
            modifier = Modifier
                .offset(x = (-20).dp, y = 40.dp)
                .size(96.dp)
                .background(
                    Brush.radialGradient(colors = orb3),
                    shape = CircleShape
                )
                .blur(32.dp)
        )
        Box(
            modifier = Modifier
                .offset(x = 20.dp, y = (-100).dp)
                .size(80.dp)
                .background(
                    Brush.radialGradient(colors = orb4),
                    shape = CircleShape
                )
                .blur(32.dp)
        )
    }
}

