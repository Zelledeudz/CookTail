package com.supdevinci.cooktail.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.supdevinci.cooktail.data.local.CocktailDatabase
import com.supdevinci.cooktail.data.local.entities.CocktailEntity
import com.supdevinci.cooktail.model.Cocktail
import com.supdevinci.cooktail.data.remote.RetrofitInstance
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Date

class CocktailByIngredientViewModel(application: Application) : AndroidViewModel(application) {
    private val cocktailDatabase = CocktailDatabase.getInstance(application)
    private val favoriteCocktailDao = cocktailDatabase.cocktailDao()

    private val _etat = MutableStateFlow<CocktailByIngredientState>(CocktailByIngredientState.Idle)
    val etat: StateFlow<CocktailByIngredientState> = _etat

    val favoriteIds: StateFlow<Set<Int>> = favoriteCocktailDao.getFavoriteCocktails()
        .map { list -> list.map { it.id }.toSet() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    fun fetchCocktailsByIngredients(selectedIngredients: List<String>) {
        if (selectedIngredients.isEmpty()) {
            _etat.value = CocktailByIngredientState.Idle
            return
        }

        viewModelScope.launch {
            _etat.value = CocktailByIngredientState.Loading
            try {
                var commonCocktails: List<Cocktail>? = null

                // On boucle sur chaque ingrédient sélectionné
                for (ingredient in selectedIngredients) {
                    try {
                        val response = RetrofitInstance.api.getCocktailsByIngredient(ingredient)
                        val drinks = response.drinks ?: emptyList()

                        if (commonCocktails == null) {
                            // Premier ingrédient : on prend toute sa liste
                            commonCocktails = drinks
                        } else {
                            // Ingrédients suivants : on fait l'INTERSECTION
                            // On ne garde que les cocktails déjà présents dans commonCocktails
                            commonCocktails = commonCocktails.filter { existing ->
                                drinks.any { new -> new.idDrink == existing.idDrink }
                            }
                        }

                        // Si à un moment l'intersection est vide, inutile de continuer
                        if (commonCocktails?.isEmpty() == true) break
                        
                    } catch (e: Exception) {
                        Log.e("COCKTAIL_FILTER", "Erreur pour $ingredient: ${e.message}")
                    }
                }

                val finalResult = commonCocktails ?: emptyList()

                if (finalResult.isEmpty()) {
                    _etat.value = CocktailByIngredientState.Error("Aucun cocktail ne contient tous ces ingrédients simultanément.")
                } else {
                    _etat.value = CocktailByIngredientState.Success(finalResult)
                }

            } catch (e: Exception) {
                Log.e("COCKTAIL_FILTER_ERROR", e.message ?: "Erreur inconnue")
                _etat.value = CocktailByIngredientState.Error("Erreur lors de la recherche")
            }
        }
    }

    fun toggleFavorite(cocktail: Cocktail) {
        viewModelScope.launch {
            val id = cocktail.idDrink.toIntOrNull() ?: return@launch
            val existing = favoriteCocktailDao.getCocktailById(id)

            if (existing != null) {
                favoriteCocktailDao.update(existing.copy(isFavorite = !existing.isFavorite, updatedAt = Date()))
            } else {
                val entity = CocktailEntity(
                    id = id,
                    name = cocktail.strDrink ?: "Unknown",
                    imageUrl = cocktail.strDrinkThumb,
                    isFavorite = true,
                    createdAt = Date(),
                    updatedAt = null,
                    deletedAt = null
                )
                favoriteCocktailDao.insert(entity)
            }
        }
    }
}

sealed class CocktailByIngredientState {
    object Idle : CocktailByIngredientState()
    object Loading : CocktailByIngredientState()
    data class Success(val data: List<Cocktail>) : CocktailByIngredientState()
    data class Error(val message: String) : CocktailByIngredientState()
}