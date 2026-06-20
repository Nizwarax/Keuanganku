package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.KeuanganViewModel
import com.example.ui.theme.GreenIncome
import com.example.ui.theme.RedExpense

@Composable
fun StatistikTab(viewModel: KeuanganViewModel) {
    var isExpenseView by remember { mutableStateOf(true) }
    
    val categoryExpBreakdown by viewModel.categoryExpBreakdown.collectAsState()
    val categoryIncBreakdown by viewModel.categoryIncBreakdown.collectAsState()
    val monthlyTrend by viewModel.monthlyTrend.collectAsState()

    val activeBreakdown = if (isExpenseView) categoryExpBreakdown else categoryIncBreakdown
    val activeColorTheme = if (isExpenseView) RedExpense else GreenIncome

    val chartColorsList = listOf(
        Color(0xFF2EC4B6), Color(0xFFFF9F1C), Color(0xFFE71D36), 
        Color(0xFF011627), Color(0xFF3F51B5), Color(0xFF9C27B0),
        Color(0xFFE91E63), Color(0xFF4CAF50), Color(0xFF00BCD4)
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Toggle view
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(12.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Button(
                    onClick = { isExpenseView = true },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isExpenseView) RedExpense.copy(alpha = 0.85f) else Color.Transparent,
                        contentColor = if (isExpenseView) Color.White else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(vertical = 12.dp)
                ) {
                    Text("Pengeluaran", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }

                Button(
                    onClick = { isExpenseView = false },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (!isExpenseView) GreenIncome.copy(alpha = 0.85f) else Color.Transparent,
                        contentColor = if (!isExpenseView) Color.White else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(vertical = 12.dp)
                ) {
                    Text("Pemasukan", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
        }

        // Section 1: Pie Chart (Category Distribution)
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (isExpenseView) "Distribusi Pengeluaran" else "Distribusi Pemasukan",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.align(Alignment.Start)
                    )
                    Spacer(modifier = Modifier.height(24.dp))

                    if (activeBreakdown.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("📉", fontSize = 48.sp)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Belum Ada Data",
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                )
                            }
                        }
                    } else {
                        val total = activeBreakdown.values.sum()
                        val slices = remember(activeBreakdown) {
                            activeBreakdown.entries.toList()
                        }

                        // Drawing our Pie Chart Canvas
                        Canvas(
                            modifier = Modifier
                                .size(160.dp)
                        ) {
                            var startAngle = 0f
                            slices.forEachIndexed { idx, entry ->
                                val percent = entry.value / total
                                val sweepAngle = (percent * 360f).toFloat()
                                drawArc(
                                    color = chartColorsList[idx % chartColorsList.size],
                                    startAngle = startAngle,
                                    sweepAngle = sweepAngle,
                                    useCenter = true,
                                    size = Size(size.width, size.height)
                                )
                                startAngle += sweepAngle
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Custom dynamic Legend List
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            slices.forEachIndexed { idx, entry ->
                                val color = chartColorsList[idx % chartColorsList.size]
                                val percent = (entry.value / total) * 100
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(12.dp)
                                                .clip(CircleShape)
                                                .background(color)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = entry.key,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                    Text(
                                        text = "${String.format("%.1f", percent)}% (${formatRupiah(entry.value)})",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Section 2: Bar Chart (Pemasukan vs Pengeluaran per Bulan)
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Text(
                        text = "Arus Pemasukan vs Pengeluaran",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(24.dp))

                    if (monthlyTrend.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(140.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Menunggu data bulanan...",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                        }
                    } else {
                        val trends = remember(monthlyTrend) {
                            monthlyTrend.entries.toList().takeLast(4) // Show last 4 months
                        }

                        val maxVal = remember(trends) {
                            trends.flatMap { listOf(it.value.first, it.value.second) }.maxOrNull() ?: 1.0
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            trends.forEach { trend ->
                                val inc = trend.value.first
                                val exp = trend.value.second

                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Bottom,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.Bottom,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        // Pemasukan Bar (Green)
                                        val incHeightRatio = (inc / maxVal).coerceIn(0.05, 1.0)
                                        Box(
                                            modifier = Modifier
                                                .width(16.dp)
                                                .fillMaxHeight(incHeightRatio.toFloat())
                                                .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                                .background(GreenIncome)
                                        )

                                        // Pengeluaran Bar (Red)
                                        val expHeightRatio = (exp / maxVal).coerceIn(0.05, 1.0)
                                        Box(
                                            modifier = Modifier
                                                .width(16.dp)
                                                .fillMaxHeight(expHeightRatio.toFloat())
                                                .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                                .background(RedExpense)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = trend.key,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Indicators for Bar Chart Color Meanings
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(GreenIncome))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Pemasukan", fontSize = 11.sp, modifier = Modifier.padding(end = 16.dp))

                            Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(RedExpense))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Pengeluaran", fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }
}
