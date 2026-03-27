package com.supdevinci.cooktail.model

import com.google.gson.annotations.SerializedName

data class Ingredient(
    @SerializedName("strIngredient1") val strIngredient1: String?,
    @SerializedName("strIngredient") val strIngredient: String?
) {
    // Helper to get the name regardless of which field the API used
    val displayName: String? get() = strIngredient ?: strIngredient1
}