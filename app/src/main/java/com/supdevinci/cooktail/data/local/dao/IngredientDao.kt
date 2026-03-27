package com.supdevinci.cooktail.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.supdevinci.cooktail.data.local.entities.IngredientEntity

@Dao
interface IngredientDao {
    @Query("SELECT * FROM IngredientEntity ORDER BY name ASC")
    suspend fun getAllIngredients(): List<IngredientEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(ingredients: List<IngredientEntity>)

    @Query("SELECT COUNT(*) FROM IngredientEntity")
    suspend fun getCount(): Int
}