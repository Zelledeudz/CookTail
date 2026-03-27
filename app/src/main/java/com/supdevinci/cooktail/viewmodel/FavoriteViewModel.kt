package com.supdevinci.cooktail.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.supdevinci.cooktail.data.local.CocktailDatabase
import com.supdevinci.cooktail.data.local.entities.CocktailEntity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Date

class FavoriteViewModel(application: Application) : AndroidViewModel(application) {
    private val cocktailDao = CocktailDatabase.getInstance(application).cocktailDao()

    val favorites: StateFlow<List<CocktailEntity>> = cocktailDao.getFavoriteCocktails()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun toggleFavorite(entity: CocktailEntity) {
        viewModelScope.launch {
            cocktailDao.update(entity.copy(isFavorite = !entity.isFavorite, updatedAt = Date()))
        }
    }
}