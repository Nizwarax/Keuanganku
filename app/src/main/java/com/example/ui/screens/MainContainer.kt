package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.TransactionEntity
import com.example.ui.KeuanganViewModel
import com.example.ui.theme.GreenIncome
import com.example.ui.theme.RedExpense
import java.util.*

@Composable
fun MainContainer(
    viewModel: KeuanganViewModel,
    onLogoutRequested: () -> Unit
) {
    val context = LocalContext.current
    val allCategories by viewModel.allCategories.collectAsState()

    var selectedTabIdx by remember { mutableStateOf(0) }
    
    // Dialog Add/Edit states
    var showAddEditDialog by remember { mutableStateOf(false) }
    var editingTransaction by remember { mutableStateOf<TransactionEntity?>(null) } // null = Add, object = Edit

    // Transaction form inputs
    var txTitle by remember { mutableStateOf("") }
    var txAmount by remember { mutableStateOf("") }
    var txType by remember { mutableStateOf("Pengeluaran") } // "Pemasukan" vs "Pengeluaran"
    var txCategory by remember { mutableStateOf("") }
    var txNotes by remember { mutableStateOf("") }
    var txCapitalCost by remember { mutableStateOf("") }

    // Synchronize category selection when type changes
    val displayedCategories = remember(allCategories, txType) {
        allCategories.filter { it.type == txType }
    }

    LaunchedEffect(displayedCategories) {
        if (displayedCategories.isNotEmpty()) {
            txCategory = displayedCategories.first().name
        }
    }

    // Helper functions to open Add/Edit dialogs
    fun openAddDialog() {
        editingTransaction = null
        txTitle = ""
        txAmount = ""
        txType = "Pengeluaran"
        txNotes = ""
        txCapitalCost = ""
        if (displayedCategories.isNotEmpty()) {
            txCategory = displayedCategories.first().name
        }
        showAddEditDialog = true
    }

    fun openEditDialog(tx: TransactionEntity) {
        editingTransaction = tx
        txTitle = tx.title
        txAmount = tx.amount.toLong().toString()
        txType = tx.type
        txCategory = tx.category
        txNotes = tx.notes
        txCapitalCost = if (tx.capitalCost > 0) tx.capitalCost.toLong().toString() else ""
        showAddEditDialog = true
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            @OptIn(ExperimentalMaterial3Api::class)
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "KeuanganKu",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                actions = {
                    // Profile button to quickly log out or clean data
                    IconButton(onClick = onLogoutRequested) {
                        Icon(
                            imageVector = Icons.Default.Logout,
                            contentDescription = "Keluar",
                            tint = RedExpense.copy(alpha = 0.8f)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        floatingActionButton = {
            // High contrast central Floating Action button specifically for record insertions
            FloatingActionButton(
                onClick = { openAddDialog() },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = CircleShape
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Tambah Transaksi",
                    modifier = Modifier.size(28.dp)
                )
            }
        },
        bottomBar = {
            // Elegant M3 compliant Navigation Bar
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp,
                windowInsets = WindowInsets.navigationBars
            ) {
                NavigationBarItem(
                    selected = selectedTabIdx == 0,
                    onClick = { selectedTabIdx = 0 },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Beranda") },
                    label = { Text("Beranda", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                )
                NavigationBarItem(
                    selected = selectedTabIdx == 1,
                    onClick = { selectedTabIdx = 1 },
                    icon = { Icon(Icons.Default.ReceiptLong, contentDescription = "Transaksi") },
                    label = { Text("Transaksi", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                )
                NavigationBarItem(
                    selected = selectedTabIdx == 2,
                    onClick = { selectedTabIdx = 2 },
                    icon = { Icon(Icons.Default.ShowChart, contentDescription = "Statistik") },
                    label = { Text("Statistik", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                )
                NavigationBarItem(
                    selected = selectedTabIdx == 3,
                    onClick = { selectedTabIdx = 3 },
                    icon = { Icon(Icons.Default.Assessment, contentDescription = "Laporan") },
                    label = { Text("Laporan", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                )
                NavigationBarItem(
                    selected = selectedTabIdx == 4,
                    onClick = { selectedTabIdx = 4 },
                    icon = { Icon(Icons.Default.Settings, contentDescription = "Pengaturan") },
                    label = { Text("Pengaturan", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Render active sub-layouts
            when (selectedTabIdx) {
                0 -> DashboardTab(
                    viewModel = viewModel,
                    onNavigateToTransactions = { selectedTabIdx = 1 },
                    onEditTransactionRequested = { tx -> openEditDialog(tx) }
                )
                1 -> TransaksiTab(
                    viewModel = viewModel,
                    onEditTransactionRequested = { tx -> openEditDialog(tx) }
                )
                2 -> StatistikTab(viewModel = viewModel)
                3 -> LaporanTab(viewModel = viewModel)
                4 -> SistemTab(viewModel = viewModel)
            }
        }
    }

    // --- Unified Create or Edit Transaction Drawer Modal Dialog ---
    if (showAddEditDialog) {
        Dialog(onDismissRequest = { showAddEditDialog = false }) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    val isEditing = editingTransaction != null
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isEditing) "Edit Transaksi" else "Tambah Transaksi",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        IconButton(onClick = { showAddEditDialog = false }) {
                            Icon(Icons.Default.Close, contentDescription = "Tutup")
                        }
                    }

                    // Select Income vs Expense Tab
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.background, shape = RoundedCornerShape(12.dp))
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Button(
                            onClick = { txType = "Pengeluaran" },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (txType == "Pengeluaran") RedExpense.copy(alpha = 0.85f) else Color.Transparent,
                                contentColor = if (txType == "Pengeluaran") Color.White else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(vertical = 10.dp)
                        ) {
                            Text("Pengeluaran", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }

                        Button(
                            onClick = { txType = "Pemasukan" },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (txType == "Pemasukan") GreenIncome.copy(alpha = 0.85f) else Color.Transparent,
                                contentColor = if (txType == "Pemasukan") Color.White else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(vertical = 10.dp)
                        ) {
                            Text("Pemasukan", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }

                    // Input Title
                    OutlinedTextField(
                        value = txTitle,
                        onValueChange = { txTitle = it },
                        label = { Text("Judul Transaksi") },
                        placeholder = { Text("e.g., Gaji Bulanan / Beli Kopi") },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    // Input Amount
                    OutlinedTextField(
                        value = txAmount,
                        onValueChange = { txAmount = it },
                        label = { Text(if (txType == "Pemasukan") "Harga Jual / Omset (Rupiah)" else "Jumlah (Rupiah)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        placeholder = { Text("e.g., 50000") },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    // Input Capital Cost (Only for Pemasukan / Sales)
                    AnimatedVisibility(visible = txType == "Pemasukan") {
                        OutlinedTextField(
                            value = txCapitalCost,
                            onValueChange = { txCapitalCost = it },
                            label = { Text("Harga Modal / COGS (Rupiah)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            placeholder = { Text("e.g., 30000") },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }

                    // Category scroll list
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Kategori",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )

                        if (displayedCategories.isEmpty()) {
                            Text(
                                text = "Mohon buat kategori baru di tab Pengaturan!",
                                fontSize = 11.sp,
                                color = RedExpense,
                                fontWeight = FontWeight.Medium
                            )
                        } else {
                            LazyRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(displayedCategories) { cat ->
                                    val isSelected = txCategory == cat.name
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(
                                                if (isSelected) MaterialTheme.colorScheme.primary 
                                                else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                                            )
                                            .clickable { txCategory = cat.name }
                                            .padding(horizontal = 14.dp, vertical = 8.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Text(text = cat.icon, fontSize = 14.sp)
                                            Text(
                                                text = cat.name,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onPrimaryContainer
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Input Notes
                    OutlinedTextField(
                        value = txNotes,
                        onValueChange = { txNotes = it },
                        label = { Text("Catatan Tambahan (Opsional)") },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 2
                    )

                    // Action Save/Update Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = { showAddEditDialog = false },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Batal")
                        }

                        Button(
                            onClick = {
                                if (txTitle.isBlank() || txAmount.isBlank()) {
                                    Toast.makeText(context, "Lengkapi judul & nominal uang!", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }
                                val amountVal = txAmount.toDoubleOrNull()
                                if (amountVal == null || amountVal <= 0) {
                                    Toast.makeText(context, "Masukkan nominal angka yang valid!", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }
                                if (txCategory.isBlank()) {
                                    Toast.makeText(context, "Pilih kategori!", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }

                                 val capitalCostVal = if (txType == "Pemasukan") (txCapitalCost.toDoubleOrNull() ?: 0.0) else 0.0

                                if (isEditing) {
                                    viewModel.editTransaction(
                                        id = editingTransaction!!.id,
                                        title = txTitle,
                                        amount = amountVal,
                                        type = txType,
                                        category = txCategory,
                                        dateMillis = editingTransaction!!.dateMillis,
                                        notes = txNotes,
                                        capitalCost = capitalCostVal
                                    )
                                    Toast.makeText(context, "Transaksi berhasil diperbarui!", Toast.LENGTH_SHORT).show()
                                } else {
                                    viewModel.addTransaction(
                                        title = txTitle,
                                        amount = amountVal,
                                        type = txType,
                                        category = txCategory,
                                        dateMillis = System.currentTimeMillis(),
                                        notes = txNotes,
                                        capitalCost = capitalCostVal
                                    )
                                    Toast.makeText(context, "Transaksi dicatat!", Toast.LENGTH_SHORT).show()
                                }
                                showAddEditDialog = false
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Text(
                                text = if (isEditing) "Perbarui" else "Simpan",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}
