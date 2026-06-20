package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.text.style.TextAlign
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
import com.example.data.ProductEntity
import androidx.compose.ui.text.style.TextOverflow
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

    // Cashier (Kasir) & Product Catalog states
    val allProducts by viewModel.allProducts.collectAsState()
    var pemasukanMode by remember { mutableStateOf("Manual") } // "Manual" vs "Kasir"
    var cartItems by remember { mutableStateOf<Map<ProductEntity, Int>>(emptyMap()) } // Map of Product -> Qty
    var showManageCatalogDialog by remember { mutableStateOf(false) }

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
        cartItems = emptyMap()
        pemasukanMode = "Manual"
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
        pemasukanMode = "Manual"
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
                    .heightIn(max = 580.dp)
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

                    if (editingTransaction == null && txType == "Pemasukan") {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.background, shape = RoundedCornerShape(12.dp))
                                .padding(4.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Button(
                                onClick = { pemasukanMode = "Manual" },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (pemasukanMode == "Manual") MaterialTheme.colorScheme.primary else Color.Transparent,
                                    contentColor = if (pemasukanMode == "Manual") MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(vertical = 8.dp)
                            ) {
                                Text("Pencatatan Manual", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }

                            Button(
                                onClick = { pemasukanMode = "Kasir" },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (pemasukanMode == "Kasir") GreenIncome.copy(alpha = 0.85f) else Color.Transparent,
                                    contentColor = if (pemasukanMode == "Kasir") Color.White else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(vertical = 8.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(Icons.Default.Storefront, contentDescription = null, modifier = Modifier.size(13.dp))
                                    Text("Mode Kasir 🛒", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                }
                            }
                        }
                    }

                    if (txType == "Pengeluaran" || isEditing || pemasukanMode == "Manual") {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f, fill = false)
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Input Title
                            OutlinedTextField(
                                value = txTitle,
                                onValueChange = { txTitle = it },
                                label = { Text("Judul Transaksi") },
                                placeholder = { Text(if (txType == "Pemasukan") "e.g., Penjualan Toko / Gaji" else "e.g., Beli Kopi / Bayar Listrik") },
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

                            // ONLY show advanced details (categories and notes) for Pengeluaran / Expense
                            if (txType == "Pengeluaran") {
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
                            }
                        }

                        // Action Save/Update Buttons (placed layout-wise below scrollable Column)
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

                                    // Resolve category fallback for Pemasukan Manual if chosen list is hidden
                                    var finalCategory = txCategory
                                    if (txType == "Pemasukan" && finalCategory.isBlank()) {
                                        if (displayedCategories.isNotEmpty()) {
                                            finalCategory = displayedCategories.first().name
                                        } else {
                                            finalCategory = "Penjualan"
                                        }
                                    } else if (txType == "Pengeluaran" && finalCategory.isBlank()) {
                                        Toast.makeText(context, "Pilih kategori!", Toast.LENGTH_SHORT).show()
                                        return@Button
                                    }

                                    val capitalCostVal = 0.0 // No manual capital cost for simple Pemasukan Manual

                                    if (isEditing) {
                                        viewModel.editTransaction(
                                            id = editingTransaction!!.id,
                                            title = txTitle,
                                            amount = amountVal,
                                            type = txType,
                                            category = finalCategory,
                                            dateMillis = editingTransaction!!.dateMillis,
                                            notes = if (txType == "Pemasukan") "" else txNotes,
                                            capitalCost = capitalCostVal
                                        )
                                        Toast.makeText(context, "Transaksi berhasil diperbarui!", Toast.LENGTH_SHORT).show()
                                    } else {
                                        viewModel.addTransaction(
                                            title = txTitle,
                                            amount = amountVal,
                                            type = txType,
                                            category = finalCategory,
                                            dateMillis = System.currentTimeMillis(),
                                            notes = if (txType == "Pemasukan") "" else txNotes,
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
                    } else {
                        // --- BEAUTIFUL MODERN COSY CASHIER INTERFACE ---
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Pilih Produk Belanja Toko",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )

                            TextButton(
                                onClick = { showManageCatalogDialog = true },
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp)
                            ) {
                                Icon(Icons.Default.Settings, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Kelola Katalog ⚙️", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        // Product live query filter search bar
                        var productSearchQuery by remember { mutableStateOf("") }
                        OutlinedTextField(
                            value = productSearchQuery,
                            onValueChange = { productSearchQuery = it },
                            placeholder = { Text("Cari nama produk...") },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp)) },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = MaterialTheme.colorScheme.background,
                                unfocusedContainerColor = MaterialTheme.colorScheme.background
                            )
                        )

                        val filteredProducts = remember(allProducts, productSearchQuery) {
                            allProducts.filter { it.name.contains(productSearchQuery, ignoreCase = true) }
                        }

                        if (filteredProducts.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 160.dp, max = 220.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Icon(
                                        imageVector = Icons.Default.Storefront,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                                        modifier = Modifier.size(40.dp)
                                    )
                                    Text(
                                        text = "Katalog kosong.\nSilakan buat produk toko terlebih dahulu!",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                        textAlign = TextAlign.Center
                                    )
                                    Button(
                                        onClick = { showManageCatalogDialog = true },
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Text("Kelola Katalog / Tambah Item", fontSize = 11.sp)
                                    }
                                }
                            }
                        } else {
                            // Scrollable Product list in Dialog Column
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 240.dp)
                            ) {
                                androidx.compose.foundation.lazy.LazyColumn(
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    items(filteredProducts) { prod ->
                                        val qtyInCart = cartItems[prod] ?: 0
                                        Card(
                                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background),
                                            shape = RoundedCornerShape(16.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(10.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    modifier = Modifier.weight(1f)
                                                ) {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(42.dp)
                                                            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f), shape = RoundedCornerShape(10.dp)),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Text(prod.emoji, fontSize = 22.sp)
                                                    }
                                                    Spacer(modifier = Modifier.width(10.dp))
                                                    Column(modifier = Modifier.weight(1f)) {
                                                        Text(
                                                            text = prod.name,
                                                            fontSize = 12.sp,
                                                            fontWeight = FontWeight.ExtraBold,
                                                            color = MaterialTheme.colorScheme.onSurface,
                                                            maxLines = 1,
                                                            overflow = TextOverflow.Ellipsis
                                                        )
                                                        Text(
                                                            text = "Jual: ${formatRupiah(prod.sellPrice)}",
                                                            fontSize = 11.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = GreenIncome
                                                        )
                                                        Text(
                                                            text = "Modal: ${formatRupiah(prod.capitalCost)} | Stok: ${prod.stock}",
                                                            fontSize = 10.sp,
                                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                                        )
                                                    }
                                                }

                                                // Quantity Selector Row
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                                ) {
                                                    IconButton(
                                                        onClick = {
                                                            if (qtyInCart > 0) {
                                                                val temp = cartItems.toMutableMap()
                                                                if (qtyInCart == 1) {
                                                                    temp.remove(prod)
                                                                } else {
                                                                    temp[prod] = qtyInCart - 1
                                                                }
                                                                cartItems = temp
                                                            }
                                                        },
                                                        modifier = Modifier
                                                            .size(28.dp)
                                                            .background(MaterialTheme.colorScheme.surface, shape = CircleShape)
                                                    ) {
                                                        Text("-", fontWeight = FontWeight.Black, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
                                                    }

                                                    Text(
                                                        text = qtyInCart.toString(),
                                                        fontSize = 13.sp,
                                                        fontWeight = FontWeight.ExtraBold,
                                                        modifier = Modifier.padding(horizontal = 6.dp)
                                                    )

                                                    IconButton(
                                                        onClick = {
                                                            val temp = cartItems.toMutableMap()
                                                            temp[prod] = qtyInCart + 1
                                                            cartItems = temp
                                                        },
                                                        modifier = Modifier
                                                            .size(28.dp)
                                                            .background(MaterialTheme.colorScheme.primaryContainer, shape = CircleShape)
                                                    ) {
                                                        Text("+", fontWeight = FontWeight.Black, fontSize = 14.sp, color = MaterialTheme.colorScheme.onPrimaryContainer)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))

                        // Selected summaries in checkout Cart
                        val totalQtyVal = cartItems.values.sum()
                        val totalSellPriceVal = cartItems.entries.sumOf { it.key.sellPrice * it.value }
                        val totalCapitalCostVal = cartItems.entries.sumOf { it.key.capitalCost * it.value }
                        val estProfitVal = totalSellPriceVal - totalCapitalCostVal

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Total Keranjang Belanja (${totalQtyVal} item)",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                                )
                                Text(
                                    text = formatRupiah(totalSellPriceVal),
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Black,
                                    color = GreenIncome
                                )
                            }
                            
                            if (totalQtyVal > 0) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(GreenIncome.copy(alpha = 0.1f))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "Untung Bersih: ${formatRupiah(estProfitVal)}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = GreenIncome
                                    )
                                }
                            }
                        }

                        // Optional cashier checkout notes input
                        var cashierNotesInput by remember { mutableStateOf("") }
                        OutlinedTextField(
                            value = cashierNotesInput,
                            onValueChange = { cashierNotesInput = it },
                            label = { Text("Catatan / Nama Pelanggan (Opsional)") },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth(),
                            maxLines = 1,
                            singleLine = true
                        )

                        // Action dialog buttons for Checkout
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
                                    if (cartItems.isEmpty() || cartItems.values.all { it <= 0 }) {
                                        Toast.makeText(context, "Keranjang kasir masih kosong!", Toast.LENGTH_SHORT).show()
                                        return@Button
                                    }
                                    viewModel.checkoutProducts(cartItems, cashierNotesInput)
                                    Toast.makeText(context, "Transaksi penjualan kasir dicatat!", Toast.LENGTH_SHORT).show()
                                    showAddEditDialog = false
                                },
                                enabled = totalQtyVal > 0,
                                modifier = Modifier.weight(1.5f),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = GreenIncome)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(Icons.Default.ShoppingCart, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Text("Checkout (Bayar)", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showManageCatalogDialog) {
        ManageCatalogDialog(
            viewModel = viewModel,
            onDismiss = { showManageCatalogDialog = false }
        )
    }
}

// --- BEAUTIFUL PRODUCT REGISTRY AND CATALOG SERVICE ---
@Composable
fun ManageCatalogDialog(
    viewModel: KeuanganViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val allProducts by viewModel.allProducts.collectAsState()
    
    var isFormOpen by remember { mutableStateOf(false) }
    var editingProduct by remember { mutableStateOf<ProductEntity?>(null) } // null = Add, non-null = Edit
    
    var prodName by remember { mutableStateOf("") }
    var prodCapital by remember { mutableStateOf("") }
    var prodSell by remember { mutableStateOf("") }
    var prodStock by remember { mutableStateOf("999") }
    var prodEmoji by remember { mutableStateOf("☕") }
    
    val emojiList = listOf("☕", "🥐", "🍩", "🧋", "💧", "🍕", "🍔", "🍟", "🍰", "🍘", "🧁", "🍪", "🥤", "🧂", "📦", "🛍️", "🧴", "🧼", "✏️", "📚", "👕", "🔋", "🍬", "🍭")

    fun resetForm() {
        editingProduct = null
        prodName = ""
        prodCapital = ""
        prodSell = ""
        prodStock = "999"
        prodEmoji = "☕"
        isFormOpen = false
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
                .padding(4.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Katalog Produk Toko",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Tutup")
                    }
                }

                // Expand Form Trigger Button
                if (!isFormOpen) {
                    Button(
                        onClick = { 
                            resetForm()
                            isFormOpen = true 
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Tambah Produk Baru", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                } else {
                    // Form expansion
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = if (editingProduct == null) "Tambah Produk ke Toko" else "Edit Detail Produk",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )

                            // Name
                            OutlinedTextField(
                                value = prodName,
                                onValueChange = { prodName = it },
                                label = { Text("Nama Produk") },
                                placeholder = { Text("e.g., Kopi Gula Aren") },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // Capital Cost
                                OutlinedTextField(
                                    value = prodCapital,
                                    onValueChange = { prodCapital = it },
                                    label = { Text("Harga Modal") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.weight(1f),
                                    singleLine = true
                                )
                                // Sell Price
                                OutlinedTextField(
                                    value = prodSell,
                                    onValueChange = { prodSell = it },
                                    label = { Text("Harga Jual") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.weight(1f),
                                    singleLine = true
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // Stock
                                OutlinedTextField(
                                    value = prodStock,
                                    onValueChange = { prodStock = it },
                                    label = { Text("Stok Toko") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.weight(1f),
                                    singleLine = true
                                )

                                // Chosen Emoji Badge
                                Box(
                                    modifier = Modifier
                                        .size(54.dp)
                                        .background(MaterialTheme.colorScheme.primaryContainer, shape = RoundedCornerShape(10.dp))
                                        .align(Alignment.CenterVertically),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(prodEmoji, fontSize = 24.sp)
                                }
                            }

                            // Emoji Selection list
                            Text("Pilih Icon Produk:", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                items(emojiList) { emo ->
                                    val isSelected = prodEmoji == emo
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(CircleShape)
                                            .background(if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                            .clickable { prodEmoji = emo },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(emo, fontSize = 16.sp)
                                    }
                                }
                            }

                            // Form action buttons
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { resetForm() },
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Batal")
                                }

                                Button(
                                    onClick = {
                                        if (prodName.isBlank() || prodCapital.isBlank() || prodSell.isBlank()) {
                                            Toast.makeText(context, "Semua kolom harga & nama harus diisi!", Toast.LENGTH_SHORT).show()
                                            return@Button
                                        }
                                        val capVal = prodCapital.toDoubleOrNull()
                                        val sellVal = prodSell.toDoubleOrNull()
                                        val stockVal = prodStock.toIntOrNull() ?: 999
                                        if (capVal == null || sellVal == null) {
                                            Toast.makeText(context, "Masukkan nominal harga angka yang valid!", Toast.LENGTH_SHORT).show()
                                            return@Button
                                        }

                                        if (editingProduct == null) {
                                            viewModel.addProduct(prodName, capVal, sellVal, prodEmoji, stockVal)
                                            Toast.makeText(context, "Produk ditambahkan ke katalog!", Toast.LENGTH_SHORT).show()
                                        } else {
                                            viewModel.editProduct(editingProduct!!.id, prodName, capVal, sellVal, prodEmoji, stockVal)
                                            Toast.makeText(context, "Produk berhasil diperbarui!", Toast.LENGTH_SHORT).show()
                                        }
                                        resetForm()
                                    },
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Simpan")
                                }
                            }
                        }
                    }
                }

                // Product items list
                Text(
                    text = "Daftar Produk (${allProducts.size})",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )

                Box(modifier = Modifier.weight(1f)) {
                    if (allProducts.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("Belum ada produk. Tambahkan produk pertama Anda di atas!", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                        }
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(allProducts) { prod ->
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier
                                                .weight(1f)
                                                .clickable {
                                                    // Load into edit form
                                                    editingProduct = prod
                                                    prodName = prod.name
                                                    prodCapital = prod.capitalCost.toLong().toString()
                                                    prodSell = prod.sellPrice.toLong().toString()
                                                    prodStock = prod.stock.toString()
                                                    prodEmoji = prod.emoji
                                                    isFormOpen = true
                                                }
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(36.dp)
                                                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f), shape = RoundedCornerShape(8.dp)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(prod.emoji, fontSize = 20.sp)
                                            }
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Column {
                                                Text(prod.name, fontSize = 12.sp, fontWeight = FontWeight.Black)
                                                Text(
                                                    "Jual: ${formatRupiah(prod.sellPrice)} | Modal: ${formatRupiah(prod.capitalCost)}",
                                                    fontSize = 11.sp,
                                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                                )
                                                Text("Stok: ${prod.stock} unit", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
                                            }
                                        }

                                        IconButton(
                                            onClick = {
                                                viewModel.deleteProduct(prod)
                                                Toast.makeText(context, "${prod.name} dihapus dari katalog.", Toast.LENGTH_SHORT).show()
                                            }
                                        ) {
                                            Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = RedExpense.copy(alpha = 0.8f))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
