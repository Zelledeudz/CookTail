package com.supdevinci.cooktail

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.supdevinci.cooktail.data.local.entities.CocktailEntity
import com.supdevinci.cooktail.model.Cocktail
import com.supdevinci.cooktail.model.Ingredient
import com.supdevinci.cooktail.navigation.CocktailNavHost
import com.supdevinci.cooktail.navigation.Routes
import com.supdevinci.cooktail.ui.theme.CookTailTheme
import com.supdevinci.cooktail.viewmodel.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CookTailTheme {
                MainNavigation()
            }
        }
    }
}

@Composable
fun MainNavigation() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    Scaffold(
        bottomBar = {
            val currentRoute = currentDestination?.route
            if (currentRoute != null && !currentRoute.startsWith("detail")) {
                NavigationBar {
                    NavigationBarItem(
                        selected = currentDestination.hierarchy.any { it.route == Routes.INGREDIENTS || it.route?.startsWith("search_results") == true },
                        onClick = {
                            navController.navigate(Routes.INGREDIENTS) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(Icons.Default.Search, contentDescription = "Chercher") },
                        label = { Text("Chercher") }
                    )
                    NavigationBarItem(
                        selected = currentDestination.hierarchy.any { it.route == Routes.FAVORITES },
                        onClick = {
                            navController.navigate(Routes.FAVORITES) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(Icons.Default.Favorite, contentDescription = "Favoris") },
                        label = { Text("Favoris") }
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            CocktailNavHost(navController = navController)
        }
    }
}

@Composable
fun IngredientsPage(onShowCocktails: (List<String>) -> Unit) {
    val viewModel: IngredientViewModel = viewModel()
    val state by viewModel.etat.collectAsStateWithLifecycle()
    var selectedIngredients by remember { mutableStateOf<Set<String>>(emptySet()) }
    var searchQuery by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        viewModel.fetchIngredient()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Ingrédients", style = MaterialTheme.typography.headlineMedium)

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Rechercher un ingrédient...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Default.Clear, contentDescription = "Effacer")
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        when (state) {
            is IngredientState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }

            is IngredientState.Success -> {
                val allIngredients = (state as IngredientState.Success).data
                val filteredIngredients = allIngredients.filter {
                    it.strIngredient1?.contains(searchQuery, ignoreCase = true) == true
                }

                Column(modifier = Modifier.weight(1f)) {
                    LazyColumn {
                        items(filteredIngredients) { ingredient ->
                            val name = ingredient.strIngredient1 ?: ""
                            IngredientCheckItem(
                                name = name,
                                isSelected = selectedIngredients.contains(name),
                                onSelect = { isSelected ->
                                    selectedIngredients = if (isSelected) {
                                        selectedIngredients + name
                                    } else {
                                        selectedIngredients - name
                                    }
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { onShowCocktails(selectedIngredients.toList()) },
                    enabled = selectedIngredients.isNotEmpty(),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Chercher les cocktails (${selectedIngredients.size} sélectionnés)")
                }
            }

            is IngredientState.Error -> {
                Text("Erreur de chargement")
                Button(onClick = { viewModel.fetchIngredient() }) {
                    Text("Réessayer")
                }
            }

            else -> {}
        }
    }
}

@Composable
fun IngredientCheckItem(name: String, isSelected: Boolean, onSelect: (Boolean) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onSelect(!isSelected) },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = isSelected,
                onCheckedChange = onSelect,
                modifier = Modifier.padding(end = 16.dp)
            )
            Text(
                text = name,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            )
        }
    }
}

@Composable
fun CocktailsByIngredientsPage(
    ingredients: List<String>, 
    onBack: () -> Unit,
    onCocktailClick: (String) -> Unit
) {
    val viewModel: CocktailByIngredientViewModel = viewModel()
    val state by viewModel.etat.collectAsStateWithLifecycle()
    val favoriteIds by viewModel.favoriteIds.collectAsStateWithLifecycle()

    LaunchedEffect(ingredients) {
        viewModel.fetchCocktailsByIngredients(ingredients)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        IconButton(onClick = onBack) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour")
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            "Résultats pour :",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.secondary
        )
        Text(
            ingredients.joinToString(", "),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        when (state) {
            is CocktailByIngredientState.Idle -> {
                Text("Sélectionnez des ingrédients")
            }

            is CocktailByIngredientState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }

            is CocktailByIngredientState.Success -> {
                val cocktails = (state as CocktailByIngredientState.Success).data

                LazyColumn {
                    items(cocktails) { cocktail ->
                        val isFav = favoriteIds.contains(cocktail.idDrink.toIntOrNull())
                        CocktailCard(
                            cocktail = cocktail,
                            isFavorite = isFav,
                            onToggleFavorite = { viewModel.toggleFavorite(cocktail) },
                            onClick = { onCocktailClick(cocktail.idDrink) }
                        )
                    }
                }
            }

            is CocktailByIngredientState.Error -> {
                val error = state as CocktailByIngredientState.Error
                Text("Erreur: ${error.message}")
                Button(onClick = { viewModel.fetchCocktailsByIngredients(ingredients) }) {
                    Text("Réessayer")
                }
            }
        }
    }
}

@Composable
fun FavoritesPage(onCocktailClick: (String) -> Unit) {
    val viewModel: FavoriteViewModel = viewModel()
    val favorites by viewModel.favorites.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Mes Favoris", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)

        Spacer(modifier = Modifier.height(16.dp))

        if (favorites.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("🍸", fontSize = 64.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "Aucun favori pour le moment",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        } else {
            LazyColumn {
                items(favorites) { entity ->
                    FavoriteCocktailCard(
                        entity = entity,
                        onToggleFavorite = { viewModel.toggleFavorite(entity) },
                        onClick = { onCocktailClick(entity.id.toString()) }
                    )
                }
            }
        }
    }
}

@Composable
fun CocktailDetailPage(cocktailId: String, onBack: () -> Unit) {
    val viewModel: CocktailDetailViewModel = viewModel()
    val state by viewModel.etat.collectAsStateWithLifecycle()

    LaunchedEffect(cocktailId) {
        viewModel.fetchCocktailById(cocktailId)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        IconButton(onClick = onBack) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour")
        }

        when (state) {
            is CocktailDetailState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            is CocktailDetailState.Success -> {
                val data = state as CocktailDetailState.Success
                val cocktail = data.cocktail
                val ingredients = data.ingredients
                
                AsyncImage(
                    model = cocktail.strDrinkThumb,
                    contentDescription = cocktail.strDrink,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .clip(RoundedCornerShape(16.dp)),
                    contentScale = ContentScale.Crop
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = cocktail.strDrink ?: "Inconnu",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Ingrédients",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(8.dp))

                ingredients.forEach { (ingredient, measure) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "• $ingredient", style = MaterialTheme.typography.bodyLarge)
                        if (measure != null) {
                            Text(
                                text = measure, 
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Instructions",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(8.dp))

                val instructions = cocktail.strInstructionsFR ?: cocktail.strInstructions ?: "Aucune instruction disponible."
                Text(
                    text = instructions,
                    style = MaterialTheme.typography.bodyLarge,
                    lineHeight = 24.sp
                )
                
                Spacer(modifier = Modifier.height(32.dp))
            }
            is CocktailDetailState.Error -> {
                Text("Erreur: ${(state as CocktailDetailState.Error).message}")
            }
            else -> {}
        }
    }
}

@Composable
fun CocktailCard(
    cocktail: Cocktail,
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (cocktail.strDrinkThumb != null) {
                AsyncImage(
                    model = cocktail.strDrinkThumb,
                    contentDescription = cocktail.strDrink,
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .clip(RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("🍸")
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    cocktail.strDrink ?: "Cocktail",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            IconButton(onClick = onToggleFavorite) {
                Icon(
                    imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "Favori",
                    tint = if (isFavorite) Color.Red else LocalContentColor.current
                )
            }
        }
    }
}

@Composable
fun FavoriteCocktailCard(
    entity: CocktailEntity,
    onToggleFavorite: () -> Unit,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (entity.imageUrl != null) {
                AsyncImage(
                    model = entity.imageUrl,
                    contentDescription = entity.name,
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .clip(RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("🍸")
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    entity.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            IconButton(onClick = onToggleFavorite) {
                Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = "Retirer des favoris",
                    tint = Color.Red
                )
            }
        }
    }
}