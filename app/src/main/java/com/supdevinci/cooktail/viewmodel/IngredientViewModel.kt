package com.supdevinci.cooktail.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.supdevinci.cooktail.data.local.CocktailDatabase
import com.supdevinci.cooktail.data.local.entities.IngredientEntity
import com.supdevinci.cooktail.data.remote.RetrofitInstance
import com.supdevinci.cooktail.model.Ingredient
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class IngredientViewModel(application: Application) : AndroidViewModel(application) {
    private val ingredientDao = CocktailDatabase.getInstance(application).ingredientDao()
    
    private val _etat = MutableStateFlow<IngredientState>(IngredientState.Idle)
    val etat: StateFlow<IngredientState> = _etat

    fun fetchIngredient() {
        viewModelScope.launch {
            try {
                // 1. On regarde s'il y a déjà des ingrédients dans Room
                val localIngredients = ingredientDao.getAllIngredients()
                
                if (localIngredients.isNotEmpty()) {
                    Log.d("INGREDIENT_FLOW", "Chargement depuis la base locale : ${localIngredients.size} ingrédients trouvés")
                    val ingredients = localIngredients.map { Ingredient(strIngredient1 = it.name, strIngredient = it.name) }
                    _etat.value = IngredientState.Success(ingredients)
                    return@launch
                }

                // 2. Si Room est vide, on fait les appels API
                val startId = 1
                val endId = 616
                val totalSteps = endId - startId + 1
                
                Log.d("INGREDIENT_FLOW", "Base vide, récupération via API (IDs $startId à $endId)...")
                val allIngredients = mutableListOf<Ingredient>()
                
                _etat.value = IngredientState.Loading(0f)

                for (id in startId..endId) {
                    try {
                        // Délai de 100ms pour éviter l'erreur 429 (Too Many Requests)
                        delay(100)
                        val response = RetrofitInstance.api.getIngredientById(id.toString())
                        val ingredient = response.ingredients?.firstOrNull()
                        
                        if (ingredient != null) {
                            val name = ingredient.displayName
                            if (!name.isNullOrBlank()) {
                                allIngredients.add(ingredient)
                            }
                        }
                    } catch (e: Exception) {
                        Log.e("INGREDIENT_FETCH", "Erreur pour l'ID $id: ${e.message}")
                    } finally {
                        // Progression réelle, mais limitée à 0.99f tant qu'on n'a pas fini de tout traiter (Room compris)
                        val realProgress = (id - startId + 1).toFloat() / totalSteps
                        val cappedProgress = if (realProgress >= 1f) 0.99f else realProgress
                        _etat.value = IngredientState.Loading(cappedProgress)
                    }
                }

                if (allIngredients.isEmpty()) {
                    _etat.value = IngredientState.Error("Aucun ingrédient trouvé")
                } else {
                    // 3. On sauvegarde dans Room pour la prochaine fois
                    val entities = allIngredients.map { IngredientEntity(name = it.displayName) }
                    ingredientDao.insertAll(entities)
                    
                    val sortedIngredients = allIngredients.sortedBy { it.displayName }
                    // On passe enfin à Success (ce qui libérera le SplashScreen)
                    _etat.value = IngredientState.Success(sortedIngredients)
                }

            } catch (e: Exception) {
                Log.e("INGREDIENT_ERROR", "Erreur globale: ${e.message}")
                _etat.value = IngredientState.Error(
                    message = e.message ?: "Erreur inconnue"
                )
            }
        }
    }
}