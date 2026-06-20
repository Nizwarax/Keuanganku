package com.example.data

import kotlinx.coroutines.flow.Flow
import org.json.JSONArray
import org.json.JSONObject

class KeuanganRepository(private val dao: KeuanganDao) {

    // --- User Repo Methods ---
    val userFlow: Flow<UserEntity?> = dao.getUserFlow()

    suspend fun getUser(): UserEntity? = dao.getUserSync()

    suspend fun saveUser(user: UserEntity) = dao.saveUser(user)

    suspend fun updateUser(user: UserEntity) = dao.updateUser(user)


    // --- Category Repo Methods ---
    val allCategories: Flow<List<CategoryEntity>> = dao.getAllCategoriesFlow()

    fun getCategoriesByType(type: String): Flow<List<CategoryEntity>> = dao.getCategoriesByTypeFlow(type)

    suspend fun getAllCategoriesSync(): List<CategoryEntity> = dao.getAllCategoriesSync()

    suspend fun insertCategory(category: CategoryEntity) = dao.insertCategory(category)

    suspend fun deleteCategory(category: CategoryEntity) = dao.deleteCategory(category)


    // --- Transaction Repo Methods ---
    val allTransactions: Flow<List<TransactionEntity>> = dao.getAllTransactionsFlow()

    suspend fun getTransactionById(id: Int): TransactionEntity? = dao.getTransactionById(id)

    suspend fun insertTransaction(transaction: TransactionEntity) = dao.insertTransaction(transaction)

    suspend fun updateTransaction(transaction: TransactionEntity) = dao.updateTransaction(transaction)

    suspend fun deleteTransaction(transaction: TransactionEntity) = dao.deleteTransaction(transaction)

    suspend fun clearTransactions() = dao.clearTransactions()


    // --- Product Repo Methods ---
    val allProducts: Flow<List<ProductEntity>> = dao.getAllProductsFlow()

    suspend fun insertProduct(product: ProductEntity) = dao.insertProduct(product)

    suspend fun updateProduct(product: ProductEntity) = dao.updateProduct(product)

    suspend fun deleteProduct(product: ProductEntity) = dao.deleteProduct(product)

    suspend fun clearProducts() = dao.clearProducts()


    // --- Backup & Restore Methods ---
    suspend fun exportBackupJson(): String {
        val categories = dao.getAllCategoriesSync()
        val transactionsList = dao.getAllTransactionsSync()

        val backupObj = JSONObject()
        
        // Export Categories
        val categoriesArr = JSONArray()
        for (cat in categories) {
            val catObj = JSONObject()
            catObj.put("name", cat.name)
            catObj.put("type", cat.type)
            catObj.put("icon", cat.icon)
            categoriesArr.put(catObj)
        }
        backupObj.put("categories", categoriesArr)

        // Export Transactions
        val transactionsArr = JSONArray()
        for (tx in transactionsList) {
            val txObj = JSONObject()
            txObj.put("title", tx.title)
            txObj.put("amount", tx.amount)
            txObj.put("type", tx.type)
            txObj.put("category", tx.category)
            txObj.put("dateMillis", tx.dateMillis)
            txObj.put("notes", tx.notes)
            txObj.put("capitalCost", tx.capitalCost)
            transactionsArr.put(txObj)
        }
        backupObj.put("transactions", transactionsArr)

        return backupObj.toString(4)
    }

    suspend fun importBackupJson(jsonString: String): Boolean {
        return try {
            val backupObj = JSONObject(jsonString)
            
            // Restore categories (if any)
            if (backupObj.has("categories")) {
                val categoriesArr = backupObj.getJSONArray("categories")
                // Let's clear categories first if backup has entries
                if (categoriesArr.length() > 0) {
                    dao.clearCategories()
                    for (i in 0 until categoriesArr.length()) {
                        val catObj = categoriesArr.getJSONObject(i)
                        dao.insertCategory(
                            CategoryEntity(
                                name = catObj.getString("name"),
                                type = catObj.getString("type"),
                                icon = catObj.getString("icon")
                            )
                        )
                    }
                }
            }

            // Restore transactions (if any)
            if (backupObj.has("transactions")) {
                val transactionsArr = backupObj.getJSONArray("transactions")
                if (transactionsArr.length() > 0) {
                    dao.clearTransactions()
                    for (i in 0 until transactionsArr.length()) {
                        val txObj = transactionsArr.getJSONObject(i)
                        dao.insertTransaction(
                            TransactionEntity(
                                title = txObj.getString("title"),
                                amount = txObj.getDouble("amount"),
                                type = txObj.getString("type"),
                                category = txObj.getString("category"),
                                dateMillis = txObj.getLong("dateMillis"),
                                notes = txObj.optString("notes", ""),
                                capitalCost = txObj.optDouble("capitalCost", 0.0)
                            )
                        )
                    }
                }
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
