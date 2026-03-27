package com.supdevinci.cooktail.viewmodel

import com.supdevinci.cooktail.model.Ingredient

sealed interface IngredientState {
    object Idle : IngredientState
    data class Loading(val progress: Float) : IngredientState
    data class Success(val data: List<Ingredient>) : IngredientState
    data class Error(val message: String) : IngredientState
}