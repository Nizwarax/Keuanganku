package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.TransactionEntity
import com.example.ui.KeuanganViewModel
import com.example.ui.theme.GreenIncome
import com.example.ui.theme.RedExpense
import java.util.*

@Composable
fun TransaksiTab(
    viewModel: KeuanganViewModel,
    onEditTransactionRequested: (TransactionEntity) -> Unit
) {
    val filteredTransactions by viewModel.filteredTransactions.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedType by viewModel.selectedTypeFilter.collectAsState()
    val selectedCategory by viewModel.selectedCategoryFilter.collectAsState()
    val allCategories by viewModel.allCategories.collectAsState()

    var showFilterSheet by remember { mutableStateOf(false) }
    var selectedDetailTx by remember { mutableStateOf<TransactionEntity?>(null) }

    val categoriesList = remember(allCategories) {
        listOf("Semua") + allCategories.map { it.name }.distinct()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Search and Filter Bar at the Top
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Search Field
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.updateSearchQuery(it) },
                    placeholder = { Text("Cari transaksi...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Cari") },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.updateSearchQuery("") }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear")
                            }
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                    )
                )

                // Filter Toggle Button
                IconButton(
                    onClick = { showFilterSheet = true },
                    modifier = Modifier
                        .size(54.dp)
                        .background(
                            MaterialTheme.colorScheme.primaryContainer,
                            shape = RoundedCornerShape(12.dp)
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.FilterList,
                        contentDescription = "Filter",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            // Quick Filters Scrollable (Transaction Types)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val types = listOf("Semua", "Pemasukan", "Pengeluaran")
                types.forEach { type ->
                    val isSelected = selectedType == type
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            viewModel.setFilters(
                                type = type,
                                category = selectedCategory,
                                start = viewModel.startDateFilter.value,
                                end = viewModel.endDateFilter.value
                            )
                        },
                        label = { Text(type) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                        )
                    )
                }
            }
        }

        // Active State Filters badge
        if (selectedCategory != "Semua" || viewModel.startDateFilter.value != null || viewModel.endDateFilter.value != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Filter aktif: " + 
                            (if (selectedCategory != "Semua") "Kategori: $selectedCategory " else "") +
                            (if (viewModel.startDateFilter.value != null) "Rentang Tanggal " else ""),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )
                TextButton(
                    onClick = { viewModel.resetFilters() },
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text("Reset", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Transactions List View
        if (filteredTransactions.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text("🔍", fontSize = 56.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Transaksi Tidak Ditemukan",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Coba ubah kata kunci pencarian atau filter Anda.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredTransactions) { tx ->
                    Box(modifier = Modifier.clickable { selectedDetailTx = tx }) {
                        TransactionRowItem(
                            tx = tx,
                            onEdit = { onEditTransactionRequested(tx) },
                            onDelete = { viewModel.deleteTransaction(tx) }
                        )
                    }
                }
            }
        }
    }

    // --- Elegant Custom Dialog for Search Filters ---
    if (showFilterSheet) {
        Dialog(onDismissRequest = { showFilterSheet = false }) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(18.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Filter Transaksi",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        IconButton(onClick = { showFilterSheet = false }) {
                            Icon(Icons.Default.Close, contentDescription = "Tutup")
                        }
                    }

                    // Category Selector Label
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Kategori",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(categoriesList) { cat ->
                                val isSelected = selectedCategory == cat
                                FilterChip(
                                    selected = isSelected,
                                    onClick = {
                                        viewModel.setFilters(
                                            type = selectedType,
                                            category = cat,
                                            start = viewModel.startDateFilter.value,
                                            end = viewModel.endDateFilter.value
                                        )
                                    },
                                    label = { Text(cat) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                    )
                                )
                            }
                        }
                    }

                    // Fast Date Range Selection Options
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Rentang Waktu Cepat",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val times = listOf("Hari Ini", "Minggu Ini", "Bulan Ini")
                            times.forEach { timeName ->
                                Button(
                                    onClick = {
                                        val cal = Calendar.getInstance()
                                        val endMillis = cal.timeInMillis
                                        when (timeName) {
                                            "Hari Ini" -> cal.set(Calendar.HOUR_OF_DAY, 0)
                                            "Minggu Ini" -> cal.add(Calendar.DAY_OF_YEAR, -7)
                                            "Bulan Ini" -> cal.add(Calendar.MONTH, -1)
                                        }
                                        cal.set(Calendar.MINUTE, 0)
                                        val startMillis = cal.timeInMillis
                                        viewModel.setFilters(
                                            type = selectedType,
                                            category = selectedCategory,
                                            start = startMillis,
                                            end = endMillis
                                        )
                                        showFilterSheet = false
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                                    ),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.weight(1f),
                                    contentPadding = PaddingValues(vertical = 8.dp)
                                ) {
                                    Text(timeName, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    // Reset and Close triggers
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                viewModel.resetFilters()
                                showFilterSheet = false
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Reset Semua")
                        }
                        Button(
                            onClick = { showFilterSheet = false },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Terapkan")
                        }
                    }
                }
            }
        }
    }

    // --- Elegant Detail Dialog Trigger ---
    if (selectedDetailTx != null) {
        val tx = selectedDetailTx!!
        Dialog(onDismissRequest = { selectedDetailTx = null }) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .background(
                                if (tx.type == "Pemasukan") GreenIncome.copy(alpha = 0.15f)
                                else RedExpense.copy(alpha = 0.15f),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (tx.type == "Pemasukan") "📥" else "📤",
                            fontSize = 32.sp
                        )
                    }

                    Text(
                        text = tx.title,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = formatRupiah(tx.amount),
                        fontSize = 26.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (tx.type == "Pemasukan") GreenIncome else RedExpense,
                        textAlign = TextAlign.Center
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))

                    // Key details table
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        detailRowItem(label = "Jenis", value = tx.type)
                        detailRowItem(label = "Kategori", value = tx.category)
                        detailRowItem(label = "Tanggal", value = formatDate(tx.dateMillis))
                        
                        if (tx.type == "Pemasukan") {
                            val untung = tx.amount - tx.capitalCost
                            val marginTx = if (tx.amount > 0) (untung / tx.amount) * 100 else 0.0
                            detailRowItem(label = "Harga Jual (Pemasukan)", value = formatRupiah(tx.amount))
                            detailRowItem(label = "Harga Modal (COGS)", value = formatRupiah(tx.capitalCost))
                            detailRowItem(label = "Keuntungan Bersih", value = formatRupiah(untung))
                            detailRowItem(label = "Margin Produk", value = "${String.format("%.1f", marginTx)}%")
                        }
                        
                        detailRowItem(label = "Catatan", value = if (tx.notes.isBlank()) "-" else tx.notes)
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = { selectedDetailTx = null },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Tutup", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun detailRowItem(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
            modifier = Modifier.padding(end = 16.dp)
        )
        Text(
            text = value,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1f)
        )
    }
}
