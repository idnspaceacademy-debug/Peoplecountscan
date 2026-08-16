package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ControlPointDuplicate
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.ViewAgenda
import androidx.compose.material.icons.filled.ViewModule
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.MockUiLayout
import com.example.data.model.ProcessedSketchEntity
import com.example.ui.components.MockUiRepresentationView
import com.example.ui.theme.AppBackground
import com.example.ui.theme.AppCardBg
import com.example.ui.theme.AppCardBorder
import com.example.ui.theme.AppSurface
import com.example.ui.theme.BrandAccent
import com.example.ui.theme.TechCyan
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary
import com.example.ui.viewmodel.PeopleCountViewModel
import com.example.utils.SketchLayoutParser

enum class GallerySortOption(val label: String) {
    NEWEST("Terbaru"),
    OLDEST("Terlama"),
    TITLE_AZ("Nama (A-Z)"),
    MOST_ELEMENTS("Elemen Terbanyak")
}

@Composable
fun SketchGalleryScreen(
    viewModel: PeopleCountViewModel,
    onBack: () -> Unit,
    onOpenCaptureSketch: () -> Unit,
    modifier: Modifier = Modifier
) {
    val sketches by viewModel.processedSketches.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var searchQuery by remember { mutableStateOf("") }
    var selectedFilterCategory by remember { mutableStateOf("Semua") }
    var sortOption by remember { mutableStateOf(GallerySortOption.NEWEST) }
    var isGridView by remember { mutableStateOf(false) }
    var showSortMenu by remember { mutableStateOf(false) }

    // Dialog states
    var sketchToPreview by remember { mutableStateOf<ProcessedSketchEntity?>(null) }
    var sketchToEdit by remember { mutableStateOf<ProcessedSketchEntity?>(null) }
    var sketchToDelete by remember { mutableStateOf<ProcessedSketchEntity?>(null) }

    // Filter and Sort logic
    val filteredSketches by remember(sketches, searchQuery, selectedFilterCategory, sortOption) {
        derivedStateOf {
            var list = sketches.filter { item ->
                val matchesQuery = if (searchQuery.isBlank()) true else {
                    item.title.contains(searchQuery, ignoreCase = true) ||
                            item.tags.contains(searchQuery, ignoreCase = true) ||
                            item.screenType.contains(searchQuery, ignoreCase = true) ||
                            item.summary.contains(searchQuery, ignoreCase = true)
                }

                val matchesCategory = when (selectedFilterCategory) {
                    "Semua" -> true
                    "Favorit ⭐" -> item.isFavorite
                    "Dashboard" -> item.screenType.contains("Dashboard", ignoreCase = true)
                    "Form & Registrasi" -> item.screenType.contains("Form", ignoreCase = true) || item.screenType.contains("Registrasi", ignoreCase = true)
                    "Monitoring Keamanan" -> item.screenType.contains("Monitoring", ignoreCase = true) || item.screenType.contains("Keamanan", ignoreCase = true)
                    "IoT & Sensor" -> item.tags.contains("Sensor", ignoreCase = true) || item.tags.contains("IoT", ignoreCase = true)
                    else -> true
                }

                matchesQuery && matchesCategory
            }

            when (sortOption) {
                GallerySortOption.NEWEST -> list.sortedByDescending { it.createdAt }
                GallerySortOption.OLDEST -> list.sortedBy { it.createdAt }
                GallerySortOption.TITLE_AZ -> list.sortedBy { it.title.lowercase() }
                GallerySortOption.MOST_ELEMENTS -> list.sortedByDescending { it.elementCount }
            }
        }
    }

    val totalCount = sketches.size
    val favoriteCount = sketches.count { it.isFavorite }
    val totalElements = sketches.sumOf { it.elementCount }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(AppBackground)
            .testTag("sketch_gallery_screen")
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // --- TOP APP BAR ---
            GalleryTopAppBar(
                totalCount = totalCount,
                favoriteCount = favoriteCount,
                onBack = onBack,
                onNewSketch = onOpenCaptureSketch
            )

            // --- SEARCH & STATS BAR ---
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(AppSurface)
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                // Search Input Field
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = {
                        Text(
                            "Cari desain, tag, atau jenis layout...",
                            style = MaterialTheme.typography.bodySmall.copy(color = TextTertiary, fontSize = 12.sp)
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = TechCyan,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }, modifier = Modifier.size(24.dp)) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Clear",
                                    tint = TextSecondary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFF09101D),
                        unfocusedContainerColor = Color(0xFF09101D),
                        focusedBorderColor = TechCyan,
                        unfocusedBorderColor = Color(0xFF1E2D4A),
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                        .testTag("gallery_search_input")
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Filter Categories Chips & View Mode Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Filter Chips (Scrollable)
                    val categories = listOf("Semua", "Favorit ⭐", "Dashboard", "Form & Registrasi", "Monitoring Keamanan", "IoT & Sensor")
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        categories.forEach { category ->
                            val isSelected = selectedFilterCategory == category
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedFilterCategory = category },
                                label = {
                                    Text(
                                        text = category,
                                        fontSize = 11.5.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = TechCyan.copy(alpha = 0.2f),
                                    selectedLabelColor = TechCyan,
                                    containerColor = Color(0xFF0D1829),
                                    labelColor = TextSecondary
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = isSelected,
                                    borderColor = Color(0xFF1E2D4A),
                                    selectedBorderColor = TechCyan.copy(alpha = 0.6f)
                                ),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.height(30.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Sort & View Layout Toggles
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        // Sort Button with dropdown
                        Box {
                            IconButton(
                                onClick = { showSortMenu = true },
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFF09101D))
                                    .border(1.dp, Color(0xFF1E2D4A), RoundedCornerShape(8.dp))
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Sort,
                                    contentDescription = "Sort",
                                    tint = TechCyan,
                                    modifier = Modifier.size(16.dp)
                                )
                            }

                            DropdownMenu(
                                expanded = showSortMenu,
                                onDismissRequest = { showSortMenu = false },
                                modifier = Modifier.background(AppSurface)
                            ) {
                                GallerySortOption.values().forEach { option ->
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                text = option.label,
                                                style = MaterialTheme.typography.bodySmall.copy(
                                                    color = if (sortOption == option) TechCyan else TextPrimary,
                                                    fontWeight = if (sortOption == option) FontWeight.Bold else FontWeight.Normal
                                                )
                                            )
                                        },
                                        onClick = {
                                            sortOption = option
                                            showSortMenu = false
                                        }
                                    )
                                }
                            }
                        }

                        // Grid / List View Toggle Button
                        IconButton(
                            onClick = { isGridView = !isGridView },
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF09101D))
                                .border(1.dp, Color(0xFF1E2D4A), RoundedCornerShape(8.dp))
                        ) {
                            Icon(
                                imageVector = if (isGridView) Icons.Default.ViewAgenda else Icons.Default.GridView,
                                contentDescription = "Toggle Grid/List",
                                tint = BrandAccent,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            // --- SUMMARY STATS TICKER ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF080F1D))
                    .padding(horizontal = 14.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Menampilkan ${filteredSketches.size} dari $totalCount desain (${favoriteCount} favorit)",
                    style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary, fontSize = 11.sp)
                )

                Text(
                    text = "Urutan: ${sortOption.label}",
                    style = MaterialTheme.typography.labelSmall.copy(color = TechCyan, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                )
            }

            HorizontalDivider(color = Color(0xFF19253C), thickness = 1.dp)

            // --- GALLERY CONTENT (GRID OR LIST) ---
            if (filteredSketches.isEmpty()) {
                EmptyGalleryView(
                    isFiltering = searchQuery.isNotEmpty() || selectedFilterCategory != "Semua",
                    onResetFilter = {
                        searchQuery = ""
                        selectedFilterCategory = "Semua"
                    },
                    onNewSketch = onOpenCaptureSketch
                )
            } else {
                if (isGridView) {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        contentPadding = PaddingValues(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("gallery_grid_list")
                    ) {
                        items(
                            items = filteredSketches,
                            key = { item -> if (item.id != 0L) item.id else "${item.createdAt}_${item.title.hashCode()}" }
                        ) { item ->
                            SketchGridCard(
                                item = item,
                                onCardClick = { sketchToPreview = item },
                                onToggleFavorite = { viewModel.toggleSketchFavorite(item.id, !item.isFavorite) },
                                onEdit = { sketchToEdit = item },
                                onDuplicate = {
                                    viewModel.duplicateSketch(item.id)
                                    Toast.makeText(context, "Desain berhasil diduplikasi", Toast.LENGTH_SHORT).show()
                                },
                                onDelete = { sketchToDelete = item }
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("gallery_vertical_list")
                    ) {
                        items(
                            items = filteredSketches,
                            key = { item -> if (item.id != 0L) item.id else "${item.createdAt}_${item.title.hashCode()}" }
                        ) { item ->
                            SketchListCard(
                                item = item,
                                onPreviewMockup = { sketchToPreview = item },
                                onToggleFavorite = { viewModel.toggleSketchFavorite(item.id, !item.isFavorite) },
                                onEdit = { sketchToEdit = item },
                                onDuplicate = {
                                    viewModel.duplicateSketch(item.id)
                                    Toast.makeText(context, "Desain berhasil diduplikasi", Toast.LENGTH_SHORT).show()
                                },
                                onDelete = { sketchToDelete = item },
                                onCopySummary = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    val clip = ClipData.newPlainText("Sketch Blueprint", "${item.title}\n\n${item.summary}\n\n${item.rawDescription}")
                                    clipboard.setPrimaryClip(clip)
                                    Toast.makeText(context, "Ringkasan disalin ke clipboard", Toast.LENGTH_SHORT).show()
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    // --- DETAIL / MOCKUP PREVIEW DIALOG ---
    sketchToPreview?.let { sketch ->
        val layout = remember(sketch) {
            SketchLayoutParser.jsonToLayout(sketch.layoutJson)
        }

        Dialog(
            onDismissRequest = { sketchToPreview = null },
            properties = DialogProperties(
                usePlatformDefaultWidth = false,
                dismissOnBackPress = true,
                dismissOnClickOutside = false
            )
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.96f)
                    .fillMaxHeight(0.92f)
                    .clip(RoundedCornerShape(16.dp))
                    .border(1.dp, Color(0xFF1E2D4A), RoundedCornerShape(16.dp)),
                color = AppBackground
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Dialog Header with Quick Actions
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(AppSurface)
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(TechCyan.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Widgets,
                                    contentDescription = null,
                                    tint = TechCyan,
                                    modifier = Modifier.size(16.dp)
                                )
                            }

                            Column {
                                Text(
                                    text = sketch.title,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        fontSize = 14.sp
                                    ),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "${sketch.screenType} • ${sketch.displayDate}",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = TextSecondary,
                                        fontSize = 10.5.sp
                                    )
                                )
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Favorite toggle inside modal
                            IconButton(
                                onClick = {
                                    viewModel.toggleSketchFavorite(sketch.id, !sketch.isFavorite)
                                    sketchToPreview = sketch.copy(isFavorite = !sketch.isFavorite)
                                },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = if (sketch.isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                                    contentDescription = "Favorite",
                                    tint = if (sketch.isFavorite) Color(0xFFFBBF24) else Color(0xFF64748B),
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            IconButton(
                                onClick = { sketchToPreview = null },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Close",
                                    tint = Color(0xFF94A3B8),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }

                    // Content: Interactive Mock Representation View
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f)
                    ) {
                        MockUiRepresentationView(
                            mockLayout = layout,
                            onBackClick = { sketchToPreview = null },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }
    }

    // --- EDIT METADATA DIALOG ---
    sketchToEdit?.let { sketch ->
        var editTitle by remember { mutableStateOf(sketch.title) }
        var editTags by remember { mutableStateOf(sketch.tags) }
        var editNotes by remember { mutableStateOf(sketch.notes) }

        AlertDialog(
            onDismissRequest = { sketchToEdit = null },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, tint = TechCyan, modifier = Modifier.size(20.dp))
                    Text("Edit Detail Desain", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Sesuaikan nama judul, tag kategori, atau catatan desain sketsa Anda.", color = TextSecondary, fontSize = 12.sp)

                    OutlinedTextField(
                        value = editTitle,
                        onValueChange = { editTitle = it },
                        label = { Text("Nama Desain", fontSize = 12.sp) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = TechCyan,
                            unfocusedBorderColor = Color(0xFF1E2D4A)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = editTags,
                        onValueChange = { editTags = it },
                        label = { Text("Tag Kategori (pisahkan koma)", fontSize = 12.sp) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = TechCyan,
                            unfocusedBorderColor = Color(0xFF1E2D4A)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = editNotes,
                        onValueChange = { editNotes = it },
                        label = { Text("Catatan Tambahan (Opsional)", fontSize = 12.sp) },
                        minLines = 2,
                        maxLines = 4,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = TechCyan,
                            unfocusedBorderColor = Color(0xFF1E2D4A)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (editTitle.isNotBlank()) {
                            viewModel.updateSketchMetadata(sketch.id, editTitle.trim(), editTags.trim(), editNotes.trim())
                            Toast.makeText(context, "Perubahan berhasil disimpan", Toast.LENGTH_SHORT).show()
                            sketchToEdit = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = TechCyan, contentColor = Color(0xFF071926)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Simpan", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { sketchToEdit = null }) {
                    Text("Batal", color = TextSecondary)
                }
            },
            containerColor = AppSurface,
            shape = RoundedCornerShape(14.dp)
        )
    }

    // --- DELETE CONFIRMATION DIALOG ---
    sketchToDelete?.let { sketch ->
        AlertDialog(
            onDismissRequest = { sketchToDelete = null },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(20.dp))
                    Text("Hapus Desain Sketsa?", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Text(
                    "Apakah Anda yakin ingin menghapus \"${sketch.title}\"? Tindakan ini tidak dapat dibatalkan.",
                    color = TextSecondary,
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteSketch(sketch.id)
                        Toast.makeText(context, "Desain berhasil dihapus", Toast.LENGTH_SHORT).show()
                        sketchToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444), contentColor = Color.White),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Hapus", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { sketchToDelete = null }) {
                    Text("Batal", color = TextSecondary)
                }
            },
            containerColor = AppSurface,
            shape = RoundedCornerShape(14.dp)
        )
    }
}

@Composable
private fun GalleryTopAppBar(
    totalCount: Int,
    favoriteCount: Int,
    onBack: () -> Unit,
    onNewSketch: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF0C1322))
            .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(36.dp)
                    .testTag("gallery_back_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }

            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = TechCyan,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "Galeri Desain Sketsa AI",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 16.sp
                        )
                    )
                }

                Text(
                    text = "Kelola riwayat sketsa & layout yang telah diproses",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                )
            }
        }

        // New Sketch Action Button
        Button(
            onClick = onNewSketch,
            colors = ButtonDefaults.buttonColors(
                containerColor = TechCyan,
                contentColor = Color(0xFF061826)
            ),
            shape = RoundedCornerShape(10.dp),
            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
            modifier = Modifier
                .height(34.dp)
                .testTag("gallery_new_sketch_btn")
        ) {
            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "Sketsa Baru",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 11.sp
                )
            )
        }
    }
}

@Composable
private fun SketchListCard(
    item: ProcessedSketchEntity,
    onPreviewMockup: () -> Unit,
    onToggleFavorite: () -> Unit,
    onEdit: () -> Unit,
    onDuplicate: () -> Unit,
    onDelete: () -> Unit,
    onCopySummary: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, Color(0xFF1E2D4A), RoundedCornerShape(12.dp))
            .clickable { onPreviewMockup() }
            .testTag("sketch_card_${item.id}"),
        colors = CardDefaults.cardColors(containerColor = AppCardBg),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Header Row: Category Badge, Date, Favorite, Menu
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Category Badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(TechCyan.copy(alpha = 0.15f))
                            .border(1.dp, TechCyan.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = item.screenType,
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = TechCyan,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.5.sp
                            )
                        )
                    }

                    Text(
                        text = item.displayDate,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = TextTertiary,
                            fontSize = 11.sp
                        )
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Favorite star button
                    IconButton(
                        onClick = onToggleFavorite,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = if (item.isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                            contentDescription = "Toggle Favorite",
                            tint = if (item.isFavorite) Color(0xFFFBBF24) else Color(0xFF64748B),
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Card Options Overflow Menu
                    Box {
                        IconButton(
                            onClick = { showMenu = true },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "Menu",
                                tint = TextSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false },
                            modifier = Modifier.background(AppSurface)
                        ) {
                            DropdownMenuItem(
                                text = { Text("Lihat Mockup Lengkap", fontSize = 12.sp, color = TextPrimary) },
                                leadingIcon = { Icon(Icons.Default.Widgets, contentDescription = null, tint = TechCyan, modifier = Modifier.size(16.dp)) },
                                onClick = {
                                    showMenu = false
                                    onPreviewMockup()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Edit Nama & Catatan", fontSize = 12.sp, color = TextPrimary) },
                                leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null, tint = Color(0xFF60A5FA), modifier = Modifier.size(16.dp)) },
                                onClick = {
                                    showMenu = false
                                    onEdit()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Duplikat Desain", fontSize = 12.sp, color = TextPrimary) },
                                leadingIcon = { Icon(Icons.Default.ControlPointDuplicate, contentDescription = null, tint = BrandAccent, modifier = Modifier.size(16.dp)) },
                                onClick = {
                                    showMenu = false
                                    onDuplicate()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Salin Ringkasan", fontSize = 12.sp, color = TextPrimary) },
                                leadingIcon = { Icon(Icons.Default.ContentCopy, contentDescription = null, tint = Color(0xFFA78BFA), modifier = Modifier.size(16.dp)) },
                                onClick = {
                                    showMenu = false
                                    onCopySummary()
                                }
                            )
                            HorizontalDivider(color = Color(0xFF1E2D4A))
                            DropdownMenuItem(
                                text = { Text("Hapus Desain", fontSize = 12.sp, color = Color(0xFFEF4444)) },
                                leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(16.dp)) },
                                onClick = {
                                    showMenu = false
                                    onDelete()
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Title & Summary
            Text(
                text = item.title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 15.sp
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            if (item.summary.isNotBlank()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = item.summary,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = TextSecondary,
                        fontSize = 12.sp
                    ),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Metadata & Action Button Bottom Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Section & Elements Count Pill
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFF09101D))
                            .padding(horizontal = 6.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(Icons.Default.Layers, contentDescription = null, tint = BrandAccent, modifier = Modifier.size(12.dp))
                        Text(
                            text = "${item.sectionCount} Seksi",
                            style = MaterialTheme.typography.labelSmall.copy(color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                        )
                    }

                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFF09101D))
                            .padding(horizontal = 6.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(Icons.Default.Widgets, contentDescription = null, tint = TechCyan, modifier = Modifier.size(12.dp))
                        Text(
                            text = "${item.elementCount} Elemen",
                            style = MaterialTheme.typography.labelSmall.copy(color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                        )
                    }
                }

                // Quick View Mockup Action Button
                Button(
                    onClick = onPreviewMockup,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF1E2D4A),
                        contentColor = TechCyan
                    ),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.height(28.dp)
                ) {
                    Text(
                        text = "Lihat Mockup",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun SketchGridCard(
    item: ProcessedSketchEntity,
    onCardClick: () -> Unit,
    onToggleFavorite: () -> Unit,
    onEdit: () -> Unit,
    onDuplicate: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, Color(0xFF1E2D4A), RoundedCornerShape(12.dp))
            .clickable { onCardClick() }
            .testTag("sketch_grid_card_${item.id}"),
        colors = CardDefaults.cardColors(containerColor = AppCardBg),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            // Blueprint Preview Placeholder
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(76.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(Color(0xFF0D1C33), Color(0xFF091222))
                        )
                    )
                    .border(1.dp, Color(0xFF192843), RoundedCornerShape(8.dp))
                    .padding(6.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Dashboard,
                        contentDescription = null,
                        tint = TechCyan.copy(alpha = 0.8f),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${item.elementCount} UI Elements",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = TechCyan,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }

                // Favorite badge top right
                IconButton(
                    onClick = onToggleFavorite,
                    modifier = Modifier
                        .size(24.dp)
                        .align(Alignment.TopEnd)
                ) {
                    Icon(
                        imageVector = if (item.isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                        contentDescription = "Favorite",
                        tint = if (item.isFavorite) Color(0xFFFBBF24) else Color(0xFF64748B),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Title
            Text(
                text = item.title,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 13.sp
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(2.dp))

            // Screen Type & Date
            Text(
                text = item.screenType,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = TechCyan,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 10.5.sp
                ),
                maxLines = 1
            )

            Text(
                text = item.displayDate,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = TextTertiary,
                    fontSize = 9.5.sp
                ),
                maxLines = 1
            )
        }
    }
}

@Composable
private fun EmptyGalleryView(
    isFiltering: Boolean,
    onResetFilter: () -> Unit,
    onNewSketch: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(Color(0xFF0F1E36))
                .border(1.dp, Color(0xFF1E2D4A), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Widgets,
                contentDescription = null,
                tint = TechCyan,
                modifier = Modifier.size(32.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = if (isFiltering) "Tidak ada desain yang cocok" else "Belum Ada Desain Sketsa Tersimpan",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = Color.White,
                fontSize = 15.sp
            )
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = if (isFiltering) {
                "Coba ubah kata kunci pencarian atau reset filter kategori."
            } else {
                "Gunakan kamera untuk mengambil foto sketsa UI Anda dan biarkan Gemini AI mengekstrak wireframe layout secara instan!"
            },
            style = MaterialTheme.typography.bodySmall.copy(
                color = TextSecondary,
                fontSize = 12.sp
            ),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )

        Spacer(modifier = Modifier.height(18.dp))

        if (isFiltering) {
            OutlinedButton(
                onClick = onResetFilter,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Reset Pencarian & Filter", fontSize = 12.sp)
            }
        } else {
            Button(
                onClick = onNewSketch,
                colors = ButtonDefaults.buttonColors(
                    containerColor = TechCyan,
                    contentColor = Color(0xFF071926)
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Ambil Foto Sketsa Baru", fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }
    }
}
