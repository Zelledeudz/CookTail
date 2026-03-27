package com.supdevinci.cooktail.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.supdevinci.cooktail.*
import com.supdevinci.cooktail.splashscreen.SplashScreen

object Routes {
    const val SPLASH = "splash"
    const val INGREDIENTS = "ingredients"
    const val SEARCH_RESULTS = "search_results/{ingredients}"
    const val DETAIL = "detail/{cocktailId}"
    const val FAVORITES = "favorites"
    
    fun searchResults(ingredients: String) = "search_results/$ingredients"
    fun detail(id: String) = "detail/$id"
}

@Composable
fun CocktailNavHost(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Routes.SPLASH
    ) {
        composable(Routes.SPLASH) {
            SplashScreen(navController)
        }

        composable(Routes.INGREDIENTS) {
            IngredientsPage(
                onShowCocktails = { ingredients ->
                    val ingredientsString = ingredients.joinToString(",")
                    navController.navigate(Routes.searchResults(ingredientsString))
                }
            )
        }
        
        composable(
            route = Routes.SEARCH_RESULTS,
            arguments = listOf(navArgument("ingredients") { type = NavType.StringType })
        ) { backStackEntry ->
            val ingredientsString = backStackEntry.arguments?.getString("ingredients") ?: ""
            val ingredients = ingredientsString.split(",").filter { it.isNotEmpty() }
            CocktailsByIngredientsPage(
                ingredients = ingredients,
                onBack = { navController.popBackStack() },
                onCocktailClick = { id ->
                    navController.navigate(Routes.detail(id))
                }
            )
        }
        
        composable(Routes.FAVORITES) {
            FavoritesPage(
                onCocktailClick = { id ->
                    navController.navigate(Routes.detail(id))
                }
            )
        }
        
        composable(
            route = Routes.DETAIL,
            arguments = listOf(navArgument("cocktailId") { type = NavType.StringType })
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getString("cocktailId") ?: ""
            CocktailDetailPage(
                cocktailId = id,
                onBack = { navController.popBackStack() }
            )
        }
    }
}