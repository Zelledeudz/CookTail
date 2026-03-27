package com.supdevinci.cooktail.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "CocktailEntity")
data class CocktailEntity(
    @PrimaryKey val id: Int,
    val name: String,
    val imageUrl: String?,
    val isFavorite: Boolean,
    val createdAt: Date,
    val updatedAt: Date?,
    val deletedAt: Date?
)