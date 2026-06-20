package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val email: String,
    val isDarkMode: Boolean = false,
    val isReminderActive: Boolean = false,
    val reminderTime: String = "20:00",
    val avatarUrl: String = "",
    val storeCapital: Double = 0.0
) : Serializable

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val type: String, // "Pemasukan" (Income) or "Pengeluaran" (Expense)
    val icon: String // Emoji representation
) : Serializable

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val amount: Double,
    val type: String, // "Pemasukan" or "Pengeluaran"
    val category: String,
    val dateMillis: Long = System.currentTimeMillis(),
    val notes: String = "",
    val capitalCost: Double = 0.0
) : Serializable

@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val capitalCost: Double,
    val sellPrice: Double,
    val emoji: String = "📦",
    val stock: Int = 999
) : Serializable

