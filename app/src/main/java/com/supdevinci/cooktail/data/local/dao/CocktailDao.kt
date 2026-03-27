package com.supdevinci.cooktail.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.supdevinci.cooktail.data.local.entities.CocktailEntity
import kotlinx.coroutines.flow.Flow
import java.util.Date

@Dao
interface CocktailDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(cocktail: CocktailEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(cocktails: List<CocktailEntity>)

    @Update
    suspend fun update(cocktail: CocktailEntity)

    @Delete
    suspend fun delete(cocktail: CocktailEntity)

    @Query("UPDATE CocktailEntity SET deletedAt = :deletedDate WHERE id = :id")
    suspend fun softDelete(id: Int, deletedDate: Date)

    @Query("SELECT * FROM CocktailEntity WHERE deletedAt IS NULL")
    fun getAllVisibleCocktails(): Flow<List<CocktailEntity>>

    @Query("SELECT * FROM CocktailEntity WHERE isFavorite = 1 AND deletedAt IS NULL")
    fun getFavoriteCocktails(): Flow<List<CocktailEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM CocktailEntity WHERE id = :id AND isFavorite = 1 AND deletedAt IS NULL)")
    fun isFavorite(id: Int): Flow<Boolean>

    @Query("SELECT * FROM CocktailEntity WHERE id = :id LIMIT 1")
    suspend fun getCocktailById(id: Int): CocktailEntity?
}