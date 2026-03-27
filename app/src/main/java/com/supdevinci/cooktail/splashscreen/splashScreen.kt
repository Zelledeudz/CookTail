package com.supdevinci.cooktail.splashscreen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.decode.GifDecoder
import com.supdevinci.cooktail.R
import com.supdevinci.cooktail.navigation.Routes
import com.supdevinci.cooktail.viewmodel.IngredientState
import com.supdevinci.cooktail.viewmodel.IngredientViewModel

@Composable
fun SplashScreen(navController: NavHostController) {
    val viewModel: IngredientViewModel = viewModel()
    val currentState by viewModel.etat.collectAsState()
    val context = LocalContext.current
    
    val imageLoader = remember {
        ImageLoader.Builder(context)
            .components { add(GifDecoder.Factory()) }
            .build()
    }

    LaunchedEffect(Unit) {
        viewModel.fetchIngredient()
    }

    // On calcule la progression à afficher en fonction de l'état réel du ViewModel
    val displayProgress = remember(currentState) {
        when (val state = currentState) {
            is IngredientState.Loading -> state.progress
            is IngredientState.Success -> 1f
            else -> 0f
        }
    }

    LaunchedEffect(currentState) {
        if (currentState is IngredientState.Success) {
            navController.navigate(Routes.INGREDIENTS) {
                popUpTo(Routes.SPLASH) { inclusive = true }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            AsyncImage(
                model = R.raw.gifrigolo,
                contentDescription = "Loading Animation",
                imageLoader = imageLoader,
                modifier = Modifier.size(280.dp),
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Barre de progression synchronisée avec la progression réelle (bloquée à 99% tant que pas fini)
            LinearProgressIndicator(
                progress = { displayProgress },
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Préparation de la carte... ${(displayProgress * 100).toInt()}%",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
            )
        }
    }
}