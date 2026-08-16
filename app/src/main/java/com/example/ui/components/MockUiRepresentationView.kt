package com.example.ui.components

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Preview
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.MockElementType
import com.example.data.model.MockSectionType
import com.example.data.model.MockUiElement
import com.example.data.model.MockUiLayout
import com.example.data.model.MockUiSection
import com.example.ui.theme.AppBackground
import com.example.ui.theme.AppCardBg
import com.example.ui.theme.AppCardBorder
import com.example.ui.theme.AppSurface
import com.example.ui.theme.BrandAccent
import com.example.ui.theme.TechCyan
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary

/**
 * Renders a full mock representation of the UI elements generated from
 * the parsed Gemini layout description.
 */
@Composable
fun MockUiRepresentationView(
    mockLayout: MockUiLayout,
    modifier: Modifier = Modifier,
    onBackClick: (() -> Unit)? = null
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val context = LocalContext.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AppBackground)
            .testTag("mock_ui_representation_view")
    ) {
        // Top Header Info Bar
        MockViewHeader(
            mockLayout = mockLayout,
            selectedTab = selectedTab,
            onTabSelected = { selectedTab = it }
        )

        AnimatedVisibility(
            visible = selectedTab == 0,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            // Interactive Mock UI Preview
            MockScreenPreviewContent(
                mockLayout = mockLayout,
                onBackClick = onBackClick,
                modifier = Modifier.fillMaxSize()
            )
        }

        AnimatedVisibility(
            visible = selectedTab == 1,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            // Structural Hierarchy & Blueprint
            MockStructureBlueprintView(
                mockLayout = mockLayout,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Composable
private fun MockViewHeader(
    mockLayout: MockUiLayout,
    selectedTab: Int,
    onTabSelected: (Int) -> Unit
) {
    Surface(
        color = AppSurface,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(top = 8.dp, start = 14.dp, end = 14.dp, bottom = 0.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Brush.linearGradient(listOf(TechCyan, BrandAccent))),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = Color(0xFF071926),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "AI Mock Representation",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary,
                                fontSize = 14.sp
                            )
                        )
                        Text(
                            text = "Parsed from Gemini • ${mockLayout.screenType}",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = TextSecondary,
                                fontSize = 10.sp
                            )
                        )
                    }
                }

                // Screen Type Tag
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0x3300E5FF))
                        .border(1.dp, TechCyan.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = mockLayout.screenType.uppercase(),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = TechCyan,
                            fontSize = 9.sp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Tab bar switcher
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.Transparent,
                contentColor = TechCyan,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = TechCyan,
                        height = 2.5.dp
                    )
                },
                divider = { HorizontalDivider(color = Color(0xFF1E2D4A)) }
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { onTabSelected(0) },
                    text = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(Icons.Default.Preview, contentDescription = null, modifier = Modifier.size(15.dp))
                            Text("Interactive Mock", fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { onTabSelected(1) },
                    text = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(Icons.Default.Layers, contentDescription = null, modifier = Modifier.size(15.dp))
                            Text("Structure & Nodes", fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                )
            }
        }
    }
}

/**
 * Renders the simulated mobile device mock screen.
 */
@Composable
private fun MockScreenPreviewContent(
    mockLayout: MockUiLayout,
    onBackClick: (() -> Unit)?,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val context = LocalContext.current

    // State holders for interactive elements inside the mock UI
    val inputValues = remember { mutableStateMapOf<String, String>() }
    val toggleStates = remember { mutableStateMapOf<String, Boolean>() }
    val chipSelections = remember { mutableStateMapOf<String, Boolean>() }

    Column(
        modifier = modifier
            .verticalScroll(scrollState)
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Simulated App Bar
        MockAppBarComponent(
            appBar = mockLayout.appBar,
            onBackClick = onBackClick
        )

        // Render each section
        mockLayout.sections.forEachIndexed { index, section ->
            MockSectionCard(
                section = section,
                inputValues = inputValues,
                toggleStates = toggleStates,
                chipSelections = chipSelections,
                onButtonClick = { label ->
                    Toast.makeText(context, "Action: $label clicked!", Toast.LENGTH_SHORT).show()
                }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun MockAppBarComponent(
    appBar: com.example.data.model.MockAppBar?,
    onBackClick: (() -> Unit)?
) {
    val barTitle = appBar?.title ?: "App Screen"
    val actions = appBar?.actions ?: listOf("Search", "More")

    Surface(
        color = Color(0xFF101B2E),
        shape = RoundedCornerShape(10.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E304E)),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("mock_app_bar")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                if (appBar?.hasBackAction == true || onBackClick != null) {
                    IconButton(
                        onClick = { onBackClick?.invoke() },
                        modifier = Modifier.size(34.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF1E2D4A)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Dashboard,
                            contentDescription = null,
                            tint = TechCyan,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                }

                Text(
                    text = barTitle,
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                actions.forEach { action ->
                    IconButton(
                        onClick = { /* Simulated action */ },
                        modifier = Modifier.size(32.dp)
                    ) {
                        val icon = when (action.lowercase()) {
                            "search" -> Icons.Default.Search
                            "settings" -> Icons.Default.Settings
                            "notifications" -> Icons.Default.Notifications
                            else -> Icons.Default.MoreVert
                        }
                        Icon(
                            imageVector = icon,
                            contentDescription = action,
                            tint = Color(0xFF94A3B8),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MockSectionCard(
    section: MockUiSection,
    inputValues: MutableMap<String, String>,
    toggleStates: MutableMap<String, Boolean>,
    chipSelections: MutableMap<String, Boolean>,
    onButtonClick: (String) -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = AppCardBg),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, AppCardBorder),
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
            .testTag("mock_section_${section.title ?: "card"}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Section Title
            if (!section.title.isNullOrBlank()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = section.title,
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 13.sp
                        )
                    )
                    Text(
                        text = section.sectionType.name,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = TextTertiary,
                            fontSize = 9.sp
                        )
                    )
                }
                HorizontalDivider(color = Color(0xFF1E2D4A), thickness = 0.8.dp)
            }

            // Section Elements layout
            when (section.sectionType) {
                MockSectionType.HERO_METRIC -> {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        section.elements.forEach { elem ->
                            MockHeroMetricCard(elem)
                        }
                    }
                }
                MockSectionType.FORM -> {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        section.elements.forEach { elem ->
                            MockElementRenderer(
                                element = elem,
                                inputValues = inputValues,
                                toggleStates = toggleStates,
                                chipSelections = chipSelections,
                                onButtonClick = onButtonClick
                            )
                        }
                    }
                }
                MockSectionType.ACTION_BUTTONS -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        section.elements.forEach { elem ->
                            Box(modifier = Modifier.weight(1f)) {
                                MockElementRenderer(
                                    element = elem,
                                    inputValues = inputValues,
                                    toggleStates = toggleStates,
                                    chipSelections = chipSelections,
                                    onButtonClick = onButtonClick
                                )
                            }
                        }
                    }
                }
                else -> {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        section.elements.forEach { elem ->
                            MockElementRenderer(
                                element = elem,
                                inputValues = inputValues,
                                toggleStates = toggleStates,
                                chipSelections = chipSelections,
                                onButtonClick = onButtonClick
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MockHeroMetricCard(element: MockUiElement) {
    Surface(
        color = Color(0xFF0F1A2C),
        shape = RoundedCornerShape(10.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (element.isPrimary) TechCyan.copy(alpha = 0.6f) else Color(0xFF1E304E)
        ),
        modifier = Modifier
            .width(160.dp)
            .padding(vertical = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = element.label,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = TextSecondary,
                    fontSize = 10.5.sp
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = element.value ?: "100%",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = if (element.isPrimary) TechCyan else Color.White,
                    fontSize = 18.sp
                )
            )
            if (!element.subText.isNullOrBlank()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.TrendingUp,
                        contentDescription = null,
                        tint = BrandAccent,
                        modifier = Modifier.size(11.dp)
                    )
                    Text(
                        text = element.subText,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = BrandAccent,
                            fontSize = 9.5.sp
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun MockElementRenderer(
    element: MockUiElement,
    inputValues: MutableMap<String, String>,
    toggleStates: MutableMap<String, Boolean>,
    chipSelections: MutableMap<String, Boolean>,
    onButtonClick: (String) -> Unit
) {
    when (element.elementType) {
        MockElementType.BUTTON -> {
            if (element.isPrimary) {
                Button(
                    onClick = { onButtonClick(element.label) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = TechCyan,
                        contentColor = Color(0xFF071926)
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(42.dp)
                        .testTag("mock_button_${element.label}")
                ) {
                    Text(text = element.label, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            } else {
                OutlinedButton(
                    onClick = { onButtonClick(element.label) },
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color.White
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2B3E60)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(42.dp)
                ) {
                    Text(text = element.label, fontWeight = FontWeight.Medium, fontSize = 12.sp)
                }
            }
        }

        MockElementType.TEXT_FIELD -> {
            val textVal = inputValues[element.label] ?: ""
            OutlinedTextField(
                value = textVal,
                onValueChange = { inputValues[element.label] = it },
                label = { Text(element.label, fontSize = 11.5.sp) },
                placeholder = { Text(element.placeholder ?: "Enter value...", fontSize = 11.5.sp) },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = TechCyan,
                    unfocusedBorderColor = Color(0xFF1E2D4A),
                    focusedLabelColor = TechCyan,
                    unfocusedLabelColor = TextSecondary,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedContainerColor = Color(0xFF0B1424),
                    unfocusedContainerColor = Color(0xFF0B1424)
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("mock_input_${element.label}")
            )
        }

        MockElementType.TOGGLE -> {
            val isChecked = toggleStates[element.label] ?: (element.value == "true")
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF0B1424))
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = element.label,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                )
                Switch(
                    checked = isChecked,
                    onCheckedChange = { toggleStates[element.label] = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color(0xFF08261A),
                        checkedTrackColor = BrandAccent,
                        uncheckedThumbColor = Color.Gray,
                        uncheckedTrackColor = Color(0xFF1E2D4A)
                    )
                )
            }
        }

        MockElementType.CHIP -> {
            val isSelected = chipSelections[element.label] ?: false
            FilterChip(
                selected = isSelected,
                onClick = { chipSelections[element.label] = !isSelected },
                label = { Text(element.label, fontSize = 11.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Color(0x3300E5FF),
                    selectedLabelColor = TechCyan,
                    containerColor = Color(0xFF0B1424),
                    labelColor = Color(0xFFCBD5E1)
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = isSelected,
                    selectedBorderColor = TechCyan,
                    borderColor = Color(0xFF1E2D4A)
                )
            )
        }

        MockElementType.LIST_ITEM -> {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF0B1424))
                    .padding(10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = element.label,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp
                        )
                    )
                    if (!element.subText.isNullOrBlank()) {
                        Text(
                            text = element.subText,
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = TextSecondary,
                                fontSize = 10.sp
                            )
                        )
                    }
                }
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                    contentDescription = null,
                    tint = Color(0xFF475569),
                    modifier = Modifier.size(12.dp)
                )
            }
        }

        MockElementType.PROGRESS_INDICATOR -> {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF0B1424))
                    .padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(element.label, style = MaterialTheme.typography.bodySmall.copy(color = Color.White, fontSize = 11.5.sp))
                    Text(element.value ?: "75%", style = MaterialTheme.typography.bodySmall.copy(color = TechCyan, fontWeight = FontWeight.Bold, fontSize = 11.5.sp))
                }
                LinearProgressIndicator(
                    progress = { 0.75f },
                    color = TechCyan,
                    trackColor = Color(0xFF1E2D4A),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                )
            }
        }

        MockElementType.HEADING -> {
            Text(
                text = element.label,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = TechCyan,
                    fontSize = 13.5.sp
                ),
                modifier = Modifier.padding(vertical = 2.dp)
            )
        }

        else -> {
            Text(
                text = element.label,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = Color(0xFFCBD5E1),
                    fontSize = 11.5.sp
                ),
                modifier = Modifier.padding(vertical = 2.dp)
            )
        }
    }
}

/**
 * Structural hierarchy tree view representing the parsed Jetpack Compose hierarchy.
 */
@Composable
private fun MockStructureBlueprintView(
    mockLayout: MockUiLayout,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .verticalScroll(scrollState)
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = AppCardBg),
            shape = RoundedCornerShape(10.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, AppCardBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Parsed Compose Hierarchy",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = TechCyan,
                        fontSize = 13.sp
                    )
                )
                Text(
                    text = "Scaffold -> TopAppBar + Column (Scrollable)",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    color = BrandAccent
                )

                HorizontalDivider(color = Color(0xFF1E2D4A), thickness = 0.8.dp)

                mockLayout.sections.forEachIndexed { sIdx, section ->
                    Text(
                        text = "├─ Section[${sIdx + 1}]: ${section.title ?: "Container"} (${section.sectionType})",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    section.elements.forEachIndexed { eIdx, elem ->
                        val isLast = eIdx == section.elements.size - 1
                        val prefix = if (isLast) "│   └──" else "│   ├──"
                        Text(
                            text = "$prefix ${elem.elementType.name}: \"${elem.label}\"",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.5.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }
                }
            }
        }

        // Raw Analysis Output
        if (mockLayout.rawDescription.isNotBlank()) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF09111E)),
                shape = RoundedCornerShape(10.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E2D4A)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Raw Gemini Analysis Output",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF94A3B8),
                            fontSize = 12.sp
                        )
                    )
                    Text(
                        text = mockLayout.rawDescription,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.5.sp,
                        color = Color(0xFFCBD5E1),
                        lineHeight = 15.sp
                    )
                }
            }
        }
    }
}
