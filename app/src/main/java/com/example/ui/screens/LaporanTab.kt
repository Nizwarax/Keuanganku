package com.example.ui.screens

import android.content.Context
import android.content.Intent
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.TransactionEntity
import com.example.ui.KeuanganViewModel
import com.example.ui.theme.GreenIncome
import com.example.ui.theme.RedExpense
import java.io.File
import java.util.*

@Composable
fun LaporanTab(viewModel: KeuanganViewModel) {
    val context = LocalContext.current
    val allTransactions by viewModel.allTransactions.collectAsState()
    
    // Period state: 0 = Hari Ini, 1 = Minggu Ini, 2 = Bulan Ini, 3 = Tahun Ini
    var selectedPeriodIdx by remember { mutableStateOf(1) }
    val periods = listOf("Hari Ini", "Minggu Ini", "Bulan Ini", "Tahun Ini")

    val filteredList = remember(allTransactions, selectedPeriodIdx) {
        val cal = Calendar.getInstance()
        val currentMillis = cal.timeInMillis
        when (selectedPeriodIdx) {
            0 -> cal.set(Calendar.HOUR_OF_DAY, 0) // start of today
            1 -> cal.add(Calendar.DAY_OF_YEAR, -7) // last 7 days
            2 -> cal.add(Calendar.MONTH, -1) // last 30 days
            3 -> cal.add(Calendar.YEAR, -1) // last 365 days
        }
        cal.set(Calendar.MINUTE, 0)
        val cutoffMillis = cal.timeInMillis

        allTransactions.filter { it.dateMillis >= cutoffMillis }
    }

    val totalIncome = remember(filteredList) {
        filteredList.filter { it.type == "Pemasukan" }.sumOf { it.amount }
    }

    val totalExpense = remember(filteredList) {
        filteredList.filter { it.type == "Pengeluaran" }.sumOf { it.amount }
    }

    val totalCapital = remember(filteredList) {
        filteredList.filter { it.type == "Pemasukan" }.sumOf { it.capitalCost }
    }

    val grossProfit = totalIncome - totalCapital
    val netProfit = grossProfit - totalExpense
    val netMargin = if (totalIncome > 0) (netProfit / totalIncome) * 100.0 else 0.0

    // CSV and PDF Mock generation algorithms
    fun shareReportAsCsv() {
        val csvHeader = "ID,Tanggal,Judul,Kategori,Tipe,Nominal,Harga Modal,Untung Bersih,Catatan\n"
        val csvRows = filteredList.mapIndexed { idx, tx ->
            val capital = if (tx.type == "Pemasukan") tx.capitalCost else 0.0
            val untung = if (tx.type == "Pemasukan") (tx.amount - tx.capitalCost) else -tx.amount
            "${idx + 1},${formatDate(tx.dateMillis)},\"${tx.title}\",\"${tx.category}\",${tx.type},${tx.amount},${capital},${untung},\"${tx.notes}\""
        }.joinToString("\n")
        
        val csvPayload = csvHeader + csvRows
        
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, csvPayload)
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, "Ekspor Laporan Excel (CSV)")
        context.startActivity(shareIntent)
    }

    fun shareReportAsTextPdf() {
        val reportTitle = "=== LAPORAN KEUANGANKU (${periods[selectedPeriodIdx].uppercase()}) ===\n" +
                "Diekspor pada: ${formatDate(System.currentTimeMillis())}\n\n" +
                "RINGKASAN TOKO:\n" +
                "- Total Omset Penjualan: ${formatRupiah(totalIncome)}\n" +
                "- Total Modal Produk (COGS): ${formatRupiah(totalCapital)}\n" +
                "- Untung Kotor: ${formatRupiah(grossProfit)}\n" +
                "- Biaya Operasional (Pengeluaran): ${formatRupiah(totalExpense)}\n" +
                "- Untung/Rugi Bersih: ${formatRupiah(netProfit)}\n" +
                "- Margin Keuntungan Bersih: ${String.format("%.1f", netMargin)}%\n\n" +
                "RIWAYAT TRANSAKSI:\n" +
                "-----------------------------------------\n"
        
        val rows = filteredList.mapIndexed { idx, tx ->
            val capitalDetails = if (tx.type == "Pemasukan") {
                "\n   Harga Modal: ${formatRupiah(tx.capitalCost)} | Untung: ${formatRupiah(tx.amount - tx.capitalCost)}"
            } else ""
            "${idx + 1}. [${formatDate(tx.dateMillis)}] ${tx.title} (${tx.category})\n   [${tx.type}] -> ${formatRupiah(tx.amount)}${capitalDetails}\n   Catatan: ${if (tx.notes.isBlank()) "-" else tx.notes}"
        }.joinToString("\n\n")

        val reportPayload = reportTitle + rows + "\n\nTerima kasih telah menggunakan KeuanganKu."
        
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, reportPayload)
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, "Ekspor Laporan PDF (Dokumen Teks)")
        context.startActivity(shareIntent)
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Period Segment Selector
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Pilih Periode Laporan",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        periods.forEachIndexed { idx, name ->
                            val isSelected = selectedPeriodIdx == idx
                            Button(
                                onClick = { selectedPeriodIdx = idx },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                                    contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onPrimaryContainer
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(vertical = 8.dp)
                            ) {
                                Text(
                                    text = name.substringBefore(" "),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        // Financial Summary cards for the active Period
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Ringkasan Aliran Dana - ${periods[selectedPeriodIdx]}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    summaryRowItem(label = "Total Pemasukan", amount = totalIncome, color = GreenIncome)
                    Spacer(modifier = Modifier.height(10.dp))
                    summaryRowItem(label = "Total Modal COGS (Harga Beli)", amount = totalCapital, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    Spacer(modifier = Modifier.height(10.dp))
                    summaryRowItem(label = "Untung Kotor (Gross Profit)", amount = grossProfit, color = GreenIncome)
                    Spacer(modifier = Modifier.height(10.dp))
                    summaryRowItem(label = "Biaya Operasional (Pengeluaran)", amount = totalExpense, color = RedExpense)
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "UNTUNG / RUGI BERSIH",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                            Text(
                                text = "Margin Bersih: ${String.format("%.1f", netMargin)}%",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                            )
                        }
                        Text(
                            text = formatRupiah(netProfit),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (netProfit >= 0) GreenIncome else RedExpense
                        )
                    }
                }
            }
        }

        // PDF & Excel Trigger Area
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = { shareReportAsTextPdf() },
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(Icons.Default.Share, contentDescription = "Export PDF")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Ekspor PDF", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Button(
                    onClick = { shareReportAsCsv() },
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(Icons.Default.Share, contentDescription = "Export Excel")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Ekspor Excel", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // List Header label
        item {
            Text(
                text = "Rincian Transaksi Periodik",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        // Active listing of transactions during period
        if (filteredList.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Tidak ada riwayat untuk periode ini.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                    )
                }
            }
        } else {
            items(filteredList) { tx ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = tx.title,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${tx.category}   •   ${formatDate(tx.dateMillis)}",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                        }
                        Text(
                            text = "${if (tx.type == "Pemasukan") "+" else "-"} ${formatRupiah(tx.amount)}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (tx.type == "Pemasukan") GreenIncome else RedExpense
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun summaryRowItem(label: String, amount: Double, color: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
        Text(
            text = formatRupiah(amount),
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}
