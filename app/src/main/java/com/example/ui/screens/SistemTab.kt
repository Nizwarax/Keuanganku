package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.CategoryEntity
import com.example.ui.KeuanganViewModel
import com.example.ui.theme.GreenIncome
import com.example.ui.theme.RedExpense

@Composable
fun SistemTab(viewModel: KeuanganViewModel) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    
    val currentUser by viewModel.currentUser.collectAsState()
    val allCategories by viewModel.allCategories.collectAsState()

    var activeSubTabIdx by remember { mutableStateOf(0) } // 0 = Kategori, 1 = Profil & Pengaturan
    
    // Create new category form states
    var catName by remember { mutableStateOf("") }
    var catType by remember { mutableStateOf("Pengeluaran") } // "Pemasukan" vs "Pengeluaran"
    var catIcon by remember { mutableStateOf("🏷️") }
    var showEmojiPicker by remember { mutableStateOf(false) }

    // Backup restore states
    var showImportDialog by remember { mutableStateOf(false) }
    var importJsonStr by remember { mutableStateOf("") }

    // Profile edited values
    var profileName by remember { mutableStateOf("") }
    var profileEmail by remember { mutableStateOf("") }

    // Sync profile fields with local user entities once loaded
    LaunchedEffect(currentUser) {
        currentUser?.let {
            profileName = it.name
            profileEmail = it.email
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Toggle Subtabs
        TabRow(
            selectedTabIndex = activeSubTabIdx,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary
        ) {
            Tab(
                selected = activeSubTabIdx == 0,
                onClick = { activeSubTabIdx = 0 },
                text = { Text("Kelola Kategori", fontWeight = FontWeight.Bold, fontSize = 14.sp) }
            )
            Tab(
                selected = activeSubTabIdx == 1,
                onClick = { activeSubTabIdx = 1 },
                text = { Text("Profil & Pengaturan", fontWeight = FontWeight.Bold, fontSize = 14.sp) }
            )
        }

        if (activeSubTabIdx == 0) {
            // --- KELOLA KATEGORI ---
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header Add form
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(20.dp),
                        elevation = CardDefaults.cardElevation(2.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "Tambah Kategori Baru",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Emoji Icon Box
                                Box(
                                    modifier = Modifier
                                        .size(54.dp)
                                        .background(
                                            MaterialTheme.colorScheme.primaryContainer,
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                        .clickable { showEmojiPicker = true },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = catIcon, fontSize = 28.sp)
                                }

                                // Category Name String Field
                                OutlinedTextField(
                                    value = catName,
                                    onValueChange = { catName = it },
                                    placeholder = { Text("Nama Kategori...") },
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.weight(1f),
                                    singleLine = true
                                )
                            }

                            // Type selection row (segmented buttons)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = { catType = "Pengeluaran" },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (catType == "Pengeluaran") RedExpense.copy(alpha = 0.8f) else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                                        contentColor = if (catType == "Pengeluaran") Color.White else MaterialTheme.colorScheme.onPrimaryContainer
                                    ),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Pengeluaran")
                                }

                                Button(
                                    onClick = { catType = "Pemasukan" },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (catType == "Pemasukan") GreenIncome.copy(alpha = 0.8f) else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                                        contentColor = if (catType == "Pemasukan") Color.White else MaterialTheme.colorScheme.onPrimaryContainer
                                    ),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Pemasukan")
                                }
                            }

                            Button(
                                onClick = {
                                    if (catName.isBlank()) {
                                        Toast.makeText(context, "Nama kategori tidak boleh kosong!", Toast.LENGTH_SHORT).show()
                                        return@Button
                                    }
                                    viewModel.addCategory(catName, catType, catIcon)
                                    catName = ""
                                    Toast.makeText(context, "Kategori ditambahkan!", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Simpan Kategori", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // Categories listing
                item {
                    Text(
                        text = "Kategori Pengeluaran saya",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }

                val expenseCats = allCategories.filter { it.type == "Pengeluaran" }
                if (expenseCats.isEmpty()) {
                    item { Text("Belum ada kategori pengeluaran.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)) }
                } else {
                    items(expenseCats) { cat ->
                        CategoryCardRow(cat = cat, onDelete = { viewModel.deleteCategory(cat) })
                    }
                }

                item {
                    Text(
                        text = "Kategori Pemasukan saya",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }

                val incomeCats = allCategories.filter { it.type == "Pemasukan" }
                if (incomeCats.isEmpty()) {
                    item { Text("Belum ada kategori pemasukan.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)) }
                } else {
                    items(incomeCats) { cat ->
                        CategoryCardRow(cat = cat, onDelete = { viewModel.deleteCategory(cat) })
                    }
                }
            }
        } else {
            // --- PROFIL & PENGATURAN ---
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Section 1: Edit Profile Card
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(20.dp),
                        elevation = CardDefaults.cardElevation(2.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "Ubah Profil Pengguna",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            OutlinedTextField(
                                value = profileName,
                                onValueChange = { profileName = it },
                                label = { Text("Nama Pengguna") },
                                leadingIcon = { Icon(Icons.Default.Person, contentDescription = "Nama") },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            )

                            OutlinedTextField(
                                value = profileEmail,
                                onValueChange = { profileEmail = it },
                                label = { Text("Email Pengguna") },
                                leadingIcon = { Icon(Icons.Default.Email, contentDescription = "Email") },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            )

                            Button(
                                onClick = {
                                    if (profileName.isBlank() || profileEmail.isBlank()) {
                                        Toast.makeText(context, "Lengkapi nama & email!", Toast.LENGTH_SHORT).show()
                                        return@Button
                                    }
                                    val currentAvatar = currentUser?.avatarUrl ?: "😎"
                                    viewModel.updateUserProfile(profileName, profileEmail, currentAvatar)
                                    Toast.makeText(context, "Profil berhasil disimpan!", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Perbarui Profil", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // Section 2: Choose Avatar Emoji
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(20.dp),
                        elevation = CardDefaults.cardElevation(2.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Text(
                                text = "Pilih Emoji Avatar",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            val avatars = listOf("😎", "🤖", "🪙", "👩", "👨", "🦁", "🐼", "🦊", "👑", "🚀")
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                avatars.forEach { emoji ->
                                    val isSelected = currentUser?.avatarUrl == emoji
                                    Box(
                                        modifier = Modifier
                                            .size(34.dp)
                                            .clip(CircleShape)
                                            .background(if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent)
                                            .clickable {
                                                viewModel.updateUserProfile(
                                                    currentUser?.name ?: "User",
                                                    currentUser?.email ?: "email",
                                                    emoji
                                                )
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(text = emoji, fontSize = 20.sp)
                                    }
                                }
                            }
                        }
                    }
                }

                // Section 3: Preference Preferences & Themes
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(20.dp),
                        elevation = CardDefaults.cardElevation(2.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                text = "Preferensi & Tema",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            // Theme Mode setting row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("Mode Gelap (Dark Mode)", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                                    Text("Ganti visual layar ke slate-gelap", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                                }
                                Switch(
                                    checked = currentUser?.isDarkMode == true,
                                    onCheckedChange = { viewModel.toggleDarkMode(it) }
                                )
                            }

                            // Notification Reminder Mode row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("Notifikasi Pengingat Catat", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                                    Text("Kirim pengingat harian catat dana", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                                }
                                Switch(
                                    checked = currentUser?.isReminderActive == true,
                                    onCheckedChange = { viewModel.toggleReminder(it) }
                                )
                            }
                        }
                    }
                }

                // Section 4: Local Database Backup & Restore Sync Card
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(20.dp),
                        elevation = CardDefaults.cardElevation(2.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "Cadangkan & Pulihkan Database (Backup)",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Salin kode teks backup Anda untuk disimpan offline, atau tempelkan kode tersebut di ponsel lain untuk pemulihan instan.",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                            Spacer(modifier = Modifier.height(4.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Button(
                                    onClick = {
                                        viewModel.exportBackup { backupJson ->
                                            clipboardManager.setText(AnnotatedString(backupJson))
                                            Toast.makeText(context, "Kode Cadangan disalin ke Papan Klip!", Toast.LENGTH_LONG).show()
                                        }
                                    },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                                ) {
                                    Icon(Icons.Default.Backup, contentDescription = "Backup")
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Cadangkan", fontSize = 13.sp)
                                }

                                Button(
                                    onClick = { showImportDialog = true },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                                ) {
                                    Icon(Icons.Default.Restore, contentDescription = "Restore")
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Puluhkan", fontSize = 13.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Emoji Picker Window
    if (showEmojiPicker) {
        val emojiPool = listOf("💵", "📈", "🎁", "🤝", "🪙", "🍟", "🍔", "🛍️", "🚗", "🏠", "💊", "🎬", "⚡", "📚", "🍽️", "✈️", "🏋️", "💈", "🎈")
        Dialog(onDismissRequest = { showEmojiPicker = false }) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Pilih Icon Emoji", fontSize = 16.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 12.dp))
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(5),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.height(180.dp)
                    ) {
                        items(emojiPool) { item ->
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f))
                                    .clickable {
                                        catIcon = item
                                        showEmojiPicker = false
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = item, fontSize = 24.sp)
                            }
                        }
                    }
                }
            }
        }
    }

    // Paste Backup JSON Restore Window
    if (showImportDialog) {
        Dialog(onDismissRequest = { showImportDialog = false }) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "Pulihkan Data",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Tempelkan kode teks backup JSON yang sebelumnya disalin pada kotak di bawah:",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )

                    OutlinedTextField(
                        value = importJsonStr,
                        onValueChange = { importJsonStr = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp),
                        placeholder = { Text("Kode JSON ada di sini...") },
                        shape = RoundedCornerShape(12.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = { showImportDialog = false },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Batal")
                        }
                        Button(
                            onClick = {
                                if (importJsonStr.isBlank()) {
                                    Toast.makeText(context, "Tempelkan kode Cadangan terlebih dahulu!", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }
                                viewModel.importBackup(importJsonStr) { success ->
                                    if (success) {
                                        Toast.makeText(context, "Data berhasil dipulihkan secara instan!", Toast.LENGTH_LONG).show()
                                        showImportDialog = false
                                        importJsonStr = ""
                                    } else {
                                        Toast.makeText(context, "Format kode salah atau terjadi modifikasi data. Gagal memulihkan!", Toast.LENGTH_LONG).show()
                                    }
                                }
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Puluhkan")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CategoryCardRow(cat: CategoryEntity, onDelete: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = cat.icon, fontSize = 20.sp)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = cat.name,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            // Simple trash icon to delete custom categories
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Hapus",
                    tint = RedExpense.copy(alpha = 0.8f)
                )
            }
        }
    }
}
