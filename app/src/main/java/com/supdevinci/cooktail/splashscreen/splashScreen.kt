package com.supdevinci.cooktail.splashscreen

import androidx.compose.animation.*
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
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.decode.GifDecoder
import com.supdevinci.cooktail.R
import com.supdevinci.cooktail.navigation.Routes
import com.supdevinci.cooktail.viewmodel.IngredientState
import com.supdevinci.cooktail.viewmodel.IngredientViewModel
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(navController: NavHostController) {
    val viewModel: IngredientViewModel = viewModel()
    val currentState by viewModel.etat.collectAsState()
    val context = LocalContext.current
    
    val facts = remember {
        listOf(
            "Le mot “cocktail” viendrait du terme anglais “cock tail” (queue de coq), car les premières boissons mélangées étaient aussi colorées que les plumes d’un coq.",
            "Le célèbre Mojito était déjà consommé au XVIe siècle par des marins pour prévenir les maladies grâce au citron vert riche en vitamine C.",
            "Le Bloody Mary est souvent considéré comme un “remède contre la gueule de bois”… même si ça ne soigne pas vraiment !",
            "Le Martini de James Bond est célèbre pour être commandé “shaken, not stirred” (secoué, pas mélangé)… contrairement à la méthode classique.",
            "La Piña Colada est la boisson nationale de Porto Rico depuis 1978.",
            "Le record du plus grand cocktail au monde est détenu par un Margarita de plus de 32 000 litres !",
            "Le terme 'On the rocks' vient de l'époque où l'on utilisait de vraies pierres froides de rivière pour refroidir les boissons sans les diluer."
        )
    }

    var currentFactIndex by remember { mutableIntStateOf(0) }

    val imageLoader = remember {
        ImageLoader.Builder(context)
            .components { add(GifDecoder.Factory()) }
            .build()
    }

    LaunchedEffect(Unit) {
        viewModel.fetchIngredient()
        // Changement de fait toutes le 5 secondes
        while(true) {
            delay(5000)
            currentFactIndex = (currentFactIndex + 1) % facts.size
        }
    }

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
        Column(
            modifier = Modifier.padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AsyncImage(
                model = R.raw.gifrigolo,
                contentDescription = "Loading Animation",
                imageLoader = imageLoader,
                modifier = Modifier.size(280.dp),
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            LinearProgressIndicator(
                progress = { displayProgress },
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Préparation de la carte... ${(displayProgress * 100).toInt()}%",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Affichage animé des "Le saviez-vous ?"
            Column(
                modifier = Modifier.height(100.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Le saviez-vous ?",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(8.dp))

                AnimatedContent(
                    targetState = facts[currentFactIndex],
                    transitionSpec = {
                        fadeIn() + slideInVertically { it } togetherWith fadeOut() + slideOutVertically { -it }
                    },
                    label = "FactAnimation"
                ) { text ->
                    Text(
                        text = text,
                        style = MaterialTheme.typography.bodySmall,
                        fontStyle = FontStyle.Italic,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = "La première configuration peut prendre un peu de temps, merci de votre patience ! 🍸",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
                textAlign = TextAlign.Center
            )
        }
    }
}