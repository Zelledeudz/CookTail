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
    private val cocktailDao = CocktailDatabase.getInstance(application).cocktailDao()

    private val _etat = MutableStateFlow<CocktailByIngredientState>(CocktailByIngredientState.Idle)
    val etat: StateFlow<CocktailByIngredientState> = _etat

    val favoriteIds: StateFlow<Set<Int>> = cocktailDao.getFavoriteCocktails()
        .map { list -> list.map { it.id }.toSet() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    fun fetchCocktailsByIngredients(ingredients: List<String>) {
        if (ingredients.isEmpty()) return

        viewModelScope.launch {
            _etat.value = CocktailByIngredientState.Loading
            try {
                val cocktails = mutableListOf<Cocktail>()

                for (ingredient in ingredients) {
                    try {
                        val response = RetrofitInstance.api.getCocktailsByIngredient(ingredient)
                        if (response.drinks != null) {
                            cocktails.addAll(response.drinks)
                        }
                    } catch (e: Exception) {
                        Log.e("COCKTAIL_BY_INGREDIENT", "Erreur pour $ingredient: ${e.message}")
                    }
                }

                if (cocktails.isEmpty()) {
                    _etat.value = CocktailByIngredientState.Error("Aucun cocktail trouvé")
                } else {
                    // On garde uniquement des cocktails uniques par ID
                    val uniqueCocktails = cocktails.distinctBy { it.idDrink }
                    _etat.value = CocktailByIngredientState.Success(uniqueCocktails)
                }

            } catch (e: Exception) {
                Log.e("COCKTAIL_BY_INGREDIENT_ERROR", e.message ?: "Erreur inconnue")
                _etat.value = CocktailByIngredientState.Error(
                    message = e.message ?: "Erreur inconnue"
                )
            }
        }
    }

    fun toggleFavorite(cocktail: Cocktail) {
        viewModelScope.launch {
            val id = cocktail.idDrink.toIntOrNull() ?: return@launch
            val existing = cocktailDao.getCocktailById(id)

            if (existing != null) {
                if (existing.isFavorite) {
                    cocktailDao.update(existing.copy(isFavorite = false, updatedAt = Date()))
                } else {
                    cocktailDao.update(existing.copy(isFavorite = true, updatedAt = Date()))
                }
            } else {
                // Créer une nouvelle entrée
                val entity = CocktailEntity(
                    id = id,
                    name = cocktail.strDrink ?: "Unknown",
                    imageUrl = cocktail.strDrinkThumb,
                    isFavorite = true,
                    createdAt = Date(),
                    updatedAt = null,
                    deletedAt = null
                )
                cocktailDao.insert(entity)
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