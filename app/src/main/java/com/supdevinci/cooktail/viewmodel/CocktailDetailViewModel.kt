package com.supdevinci.cooktail.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.supdevinci.cooktail.data.remote.RetrofitInstance
import com.supdevinci.cooktail.model.Cocktail
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.*

class CocktailDetailViewModel : ViewModel() {
    private val _etat = MutableStateFlow<CocktailDetailState>(CocktailDetailState.Idle)
    val etat: StateFlow<CocktailDetailState> = _etat

    fun fetchCocktailById(id: String) {
        viewModelScope.launch {
            _etat.value = CocktailDetailState.Loading
            try {
                val response = RetrofitInstance.api.getCocktailById(id)
                val cocktail = response.drinks?.firstOrNull()
                if (cocktail != null) {
                    val ingredients = getIngredientsAndMeasures(cocktail)
                    _etat.value = CocktailDetailState.Success(cocktail, ingredients)
                } else {
                    _etat.value = CocktailDetailState.Error("Cocktail non trouvé")
                }
            } catch (e: Exception) {
                _etat.value = CocktailDetailState.Error(e.message ?: "Erreur inconnue")
            }
        }
    }

    private fun getIngredientsAndMeasures(cocktail: Cocktail): List<Pair<String, String?>> {
        val list = mutableListOf<Pair<String, String?>>()
        val cocktailClass = Cocktail::class.java

        for (i in 1..15) {
            val ingredientField = cocktailClass.getDeclaredField("strIngredient$i")
            ingredientField.isAccessible = true
            val ingredient = ingredientField.get(cocktail) as? String

            if (!ingredient.isNullOrBlank()) {
                val measureField = cocktailClass.getDeclaredField("strMeasure$i")
                measureField.isAccessible = true
                val rawMeasure = measureField.get(cocktail) as? String
                
                // On convertit d'abord les oz en cl, puis on traduit le reste
                val processedMeasure = translateMeasure(convertOzToCl(rawMeasure))
                list.add(ingredient to processedMeasure)
            }
        }
        return list
    }

    private fun convertOzToCl(measure: String?): String? {
        if (measure == null) return null

        // Regex pour capturer les nombres (entiers, décimaux ou fractions) suivis de "oz"
        val ozRegex = """([\d.]+(?:/[\d.]+)?)\s*(oz|oz\.)""".toRegex(RegexOption.IGNORE_CASE)
        val matchResult = ozRegex.find(measure)

        return if (matchResult != null) {
            val valueStr = matchResult.groupValues[1]
            val ozValue = parseValue(valueStr)
            if (ozValue != null && ozValue > 0) {
                val clValue = ozValue * 2.95735 // 1 oz = 2.95735 cl
                val formattedValue = String.format(Locale.US, "%.1f cl", clValue)
                measure.replace(matchResult.value, formattedValue)
            } else {
                measure
            }
        } else {
            measure
        }
    }

    private fun translateMeasure(measure: String?): String? {
        if (measure == null) return null
        return measure
            .replace(" dashes", " traits", ignoreCase = true)
            .replace(" dash", " trait", ignoreCase = true)
            .replace(" drops", " gouttes", ignoreCase = true)
            .replace(" drop", " goutte", ignoreCase = true)
            .replace(" teaspoons", " cuilleres a cafe", ignoreCase = true)
            .replace(" teaspoon", " cuillere a cafe", ignoreCase = true)
            .replace(" tsp", " cuillere a cafe", ignoreCase = true)
            .replace(" tablespoons", " cuilleres a soupe", ignoreCase = true)
            .replace(" tablespoon", " cuillere a soupe", ignoreCase = true)
            .replace(" tblsp", " cuillere a soupe", ignoreCase = true)
            .replace(" tbsp", " cuillere a soupe", ignoreCase = true)
            .replace(" cups", " tasses", ignoreCase = true)
            .replace(" cup", " tasse", ignoreCase = true)
            .replace(" splash", " trait", ignoreCase = true)
            .replace(" oz", " cl", ignoreCase = true)
            .replace(" ml", " ml", ignoreCase = true)
            .replace(" pint hard", " Pinte dur", ignoreCase = true)
            .replace(" pint sweet or dry", " Pinte doux ou sec", ignoreCase = true)
            .replace(" pint", " Pinte", ignoreCase = true)
            .replace(" bottle", " Bouteille", ignoreCase = true)
            .replace(" Juice of", " Jus de", ignoreCase = true)
            .replace(" Chilled", " Glacé", ignoreCase = true)
            .replace(" a little bit of", " un peu de", ignoreCase = true)
            .replace(" handful", " poignée", ignoreCase = true)
            .trim()
    }

    private fun parseValue(valueStr: String): Double? {
        return try {
            when {
                // Fraction comme "1/2", "3/4"
                valueStr.contains("/") -> {
                    val parts = valueStr.split("/")
                    if (parts.size == 2) {
                        val numerator = parts[0].trim().toDoubleOrNull() ?: return null
                        val denominator = parts[1].trim().toDoubleOrNull() ?: return null
                        if (denominator != 0.0) numerator / denominator else null
                    } else null
                }
                // Nombre décimal ou entier
                else -> valueStr.toDoubleOrNull()
            }
        } catch (e: Exception) {
            null
        }
    }
}

sealed class CocktailDetailState {
    object Idle : CocktailDetailState()
    object Loading : CocktailDetailState()
    data class Success(val cocktail: Cocktail, val ingredients: List<Pair<String, String?>>) : CocktailDetailState()
    data class Error(val message: String) : CocktailDetailState()
}