package com.supdevinci.cooktail.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "IngredientEntity")
data class IngredientEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String?
)