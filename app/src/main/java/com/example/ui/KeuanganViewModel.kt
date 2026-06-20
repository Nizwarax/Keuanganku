package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class KeuanganViewModel(
    application: Application,
    private val repository: KeuanganRepository
) : AndroidViewModel(application) {

    // --- State Streams ---
    val currentUser = repository.userFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    val allTransactions = repository.allTransactions.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val allCategories = repository.allCategories.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // --- Filter & Search States ---
    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _selectedTypeFilter = MutableStateFlow("Semua") // "Semua", "Pemasukan", "Pengeluaran"
    val selectedTypeFilter = _selectedTypeFilter.asStateFlow()

    private val _selectedCategoryFilter = MutableStateFlow("Semua")
    val selectedCategoryFilter = _selectedCategoryFilter.asStateFlow()

    // Date filters represented as epoch millis at start & end of day
    private val _startDateFilter = MutableStateFlow<Long?>(null)
    val startDateFilter = _startDateFilter.asStateFlow()

    private val _endDateFilter = MutableStateFlow<Long?>(null)
    val endDateFilter = _endDateFilter.asStateFlow()

    // Combined filtered transactions
    val filteredTransactions = combine(
        allTransactions,
        _searchQuery,
        _selectedTypeFilter,
        _selectedCategoryFilter
    ) { txs, query, typeFilter, catFilter ->
        val start = _startDateFilter.value
        val end = _endDateFilter.value
        txs.filter { tx ->
            val matchesQuery = tx.title.contains(query, ignoreCase = true) || tx.notes.contains(query, ignoreCase = true)
            val matchesType = typeFilter == "Semua" || tx.type == typeFilter
            val matchesCategory = catFilter == "Semua" || tx.category == catFilter
            val matchesStart = start == null || tx.dateMillis >= start
            val matchesEnd = end == null || tx.dateMillis <= end
            matchesQuery && matchesType && matchesCategory && matchesStart && matchesEnd
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Dashboard Summary Calculations ---
    val totalIncome = allTransactions.map { txs ->
        txs.filter { it.type == "Pemasukan" }.sumOf { it.amount }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val totalExpense = allTransactions.map { txs ->
        txs.filter { it.type == "Pengeluaran" }.sumOf { it.amount }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val currentBalance = combine(totalIncome, totalExpense) { income, expense ->
        income - expense
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    // --- Statistics and Chart Data Structures ---
    val categoryExpBreakdown = allTransactions.map { txs ->
        txs.filter { it.type == "Pengeluaran" }
            .groupBy { it.category }
            .mapValues { entry -> entry.value.sumOf { it.amount } }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    val categoryIncBreakdown = allTransactions.map { txs ->
        txs.filter { it.type == "Pemasukan" }
            .groupBy { it.category }
            .mapValues { entry -> entry.value.sumOf { it.amount } }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    // Grouping transactions by date for trends over key periods
    val monthlyTrend = allTransactions.map { txs ->
        val df = SimpleDateFormat("MMM yyyy", Locale("id", "ID"))
        txs.sortedBy { it.dateMillis }
            .groupBy { df.format(Date(it.dateMillis)) }
            .mapValues { entry ->
                val income = entry.value.filter { it.type == "Pemasukan" }.sumOf { it.amount }
                val expense = entry.value.filter { it.type == "Pengeluaran" }.sumOf { it.amount }
                Pair(income, expense)
            }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    // --- Shop Profit / Margin Calculations ---
    val totalProductCapital = allTransactions.map { txs ->
        txs.filter { it.type == "Pemasukan" }.sumOf { it.capitalCost }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val grossProfit = combine(totalIncome, totalProductCapital) { income, productCapital ->
        income - productCapital
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val netProfit = combine(grossProfit, totalExpense) { gross, expense ->
        gross - expense
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val grossMargin = combine(totalIncome, grossProfit) { income, gross ->
        if (income > 0) (gross / income) * 100.0 else 0.0
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val netMargin = combine(totalIncome, netProfit) { income, net ->
        if (income > 0) (net / income) * 100.0 else 0.0
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    // --- Setters / User Actions ---
    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setFilters(type: String, category: String, start: Long?, end: Long?) {
        _selectedTypeFilter.value = type
        _selectedCategoryFilter.value = category
        _startDateFilter.value = start
        _endDateFilter.value = end
    }

    fun resetFilters() {
        _searchQuery.value = ""
        _selectedTypeFilter.value = "Semua"
        _selectedCategoryFilter.value = "Semua"
        _startDateFilter.value = null
        _endDateFilter.value = null
    }

    // --- Transaction CRUD Methods ---
    fun addTransaction(title: String, amount: Double, type: String, category: String, dateMillis: Long, notes: String, capitalCost: Double = 0.0) {
        viewModelScope.launch {
            val tx = TransactionEntity(
                title = title,
                amount = amount,
                type = type,
                category = category,
                dateMillis = dateMillis,
                notes = notes,
                capitalCost = capitalCost
            )
            repository.insertTransaction(tx)
        }
    }

    fun editTransaction(id: Int, title: String, amount: Double, type: String, category: String, dateMillis: Long, notes: String, capitalCost: Double = 0.0) {
        viewModelScope.launch {
            val tx = TransactionEntity(
                id = id,
                title = title,
                amount = amount,
                type = type,
                category = category,
                dateMillis = dateMillis,
                notes = notes,
                capitalCost = capitalCost
            )
            repository.updateTransaction(tx)
        }
    }

    fun deleteTransaction(tx: TransactionEntity) {
        viewModelScope.launch {
            repository.deleteTransaction(tx)
        }
    }

    // --- Category CRUD Methods ---
    fun addCategory(name: String, type: String, icon: String) {
        viewModelScope.launch {
            repository.insertCategory(CategoryEntity(name = name, type = type, icon = icon))
        }
    }

    fun deleteCategory(category: CategoryEntity) {
        viewModelScope.launch {
            repository.deleteCategory(category)
        }
    }

    // --- Settings, Preferences & Custom Backup ---
    fun toggleDarkMode(enabled: Boolean) {
        viewModelScope.launch {
            val user = repository.getUser() ?: UserEntity(name = "Kawan Keuangan", email = "user@keuanganku.com")
            repository.saveUser(user.copy(isDarkMode = enabled))
        }
    }

    fun updateStoreCapital(capital: Double) {
        viewModelScope.launch {
            val user = repository.getUser() ?: UserEntity(name = "Kawan Keuangan", email = "user@keuanganku.com")
            repository.saveUser(user.copy(storeCapital = capital))
        }
    }

    fun toggleReminder(enabled: Boolean) {
        viewModelScope.launch {
            val user = repository.getUser() ?: UserEntity(name = "Kawan Keuangan", email = "user@keuanganku.com")
            repository.saveUser(user.copy(isReminderActive = enabled))
        }
    }

    fun setReminderTime(time: String) {
        viewModelScope.launch {
            val user = repository.getUser() ?: UserEntity(name = "Kawan Keuangan", email = "user@keuanganku.com")
            repository.saveUser(user.copy(reminderTime = time))
        }
    }

    fun updateUserProfile(name: String, email: String, avatarUrl: String = "") {
        viewModelScope.launch {
            val user = repository.getUser() ?: UserEntity(name = name, email = email)
            repository.saveUser(user.copy(name = name, email = email, avatarUrl = avatarUrl))
        }
    }

    // Backup & Restore Wrapper Tasks
    fun exportBackup(onExported: (String) -> Unit) {
        viewModelScope.launch {
            val backupData = repository.exportBackupJson()
            onExported(backupData)
        }
    }

    fun importBackup(jsonString: String, onCompleted: (Boolean) -> Unit) {
        viewModelScope.launch {
            val success = repository.importBackupJson(jsonString)
            onCompleted(success)
        }
    }
}

class KeuanganViewModelFactory(
    private val application: Application,
    private val repository: KeuanganRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(KeuanganViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return KeuanganViewModel(application, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
