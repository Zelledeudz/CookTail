package com.supdevinci.cooktail.viewmodel

import com.supdevinci.cooktail.model.Cocktail
import com.supdevinci.cooktail.model.Ingredient

sealed interface CocktailState {
    object Loading : CocktailState
    data class Success(val data: List<Cocktail>) : CocktailState
    data class Error(val message: String) : CocktailState

}