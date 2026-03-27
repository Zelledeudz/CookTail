package com.supdevinci.cooktail.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.supdevinci.cooktail.data.local.dao.CocktailDao
import com.supdevinci.cooktail.data.local.dao.IngredientDao
import com.supdevinci.cooktail.data.local.entities.CocktailEntity
import com.supdevinci.cooktail.data.local.entities.IngredientEntity

@Database(entities = [CocktailEntity::class, IngredientEntity::class], version = 3, exportSchema = false)
@TypeConverters(Converters::class)
abstract class CocktailDatabase : RoomDatabase() {

    abstract fun cocktailDao(): CocktailDao
    abstract fun ingredientDao(): IngredientDao

    companion object {
        @Volatile
        private var INSTANCE: CocktailDatabase? = null

        fun getInstance(context: Context): CocktailDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    CocktailDatabase::class.java,
                    "cocktail_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}