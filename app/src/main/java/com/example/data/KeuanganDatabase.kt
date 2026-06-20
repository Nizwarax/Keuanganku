package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [UserEntity::class, CategoryEntity::class, TransactionEntity::class],
    version = 1,
    exportSchema = false
)
abstract class KeuanganDatabase : RoomDatabase() {

    abstract fun dao(): KeuanganDao

    companion object {
        @Volatile
        private var INSTANCE: KeuanganDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): KeuanganDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    KeuanganDatabase::class.java,
                    "keuangan_ku_database"
                )
                    .addCallback(KeuanganDatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class KeuanganDatabaseCallback(
        private val scope: CoroutineScope
    ) : Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    populateDefaultCategories(database.dao())
                }
            }
        }

        private suspend fun populateDefaultCategories(dao: KeuanganDao) {
            // Default Income categories (Pemasukan)
            val incomes = listOf(
                CategoryEntity(name = "Gaji", type = "Pemasukan", icon = "💵"),
                CategoryEntity(name = "Investasi", type = "Pemasukan", icon = "📈"),
                CategoryEntity(name = "Bonus & Hadiah", type = "Pemasukan", icon = "🎁"),
                CategoryEntity(name = "Penjualan", type = "Pemasukan", icon = "🤝"),
                CategoryEntity(name = "Lainnya", type = "Pemasukan", icon = "🪙")
            )

            // Default Expense categories (Pengeluaran)
            val expenses = listOf(
                CategoryEntity(name = "Makanan & Minuman", type = "Pengeluaran", icon = "🍔"),
                CategoryEntity(name = "Belanja Harian", type = "Pengeluaran", icon = "🛍️"),
                CategoryEntity(name = "Transportasi", type = "Pengeluaran", icon = "🚗"),
                CategoryEntity(name = "Keperluan Rumah", type = "Pengeluaran", icon = "🏠"),
                CategoryEntity(name = "Kesehatan", type = "Pengeluaran", icon = "💊"),
                CategoryEntity(name = "Hiburan", type = "Pengeluaran", icon = "🎬"),
                CategoryEntity(name = "Tagihan & Pulsa", type = "Pengeluaran", icon = "⚡"),
                CategoryEntity(name = "Pendidikan", type = "Pengeluaran", icon = "📚"),
                CategoryEntity(name = "Lainnya", type = "Pengeluaran", icon = "🍽️")
            )

            incomes.forEach { dao.insertCategory(it) }
            expenses.forEach { dao.insertCategory(it) }

            // Pre-seed an initial default user
            val defaultUser = UserEntity(
                name = "Kawan Keuangan",
                email = "user@keuanganku.com",
                isDarkMode = false,
                isReminderActive = false,
                reminderTime = "20:00"
            )
            dao.saveUser(defaultUser)
        }
    }
}
