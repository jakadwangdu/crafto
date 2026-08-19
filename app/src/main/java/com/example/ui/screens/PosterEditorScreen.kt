package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FormatColorText
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Style
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.CutoutMaskShape
import com.example.data.model.FrameStyle
import com.example.data.model.PosterTemplate
import com.example.data.model.ProfileType
import com.example.data.sample.SampleData
import com.example.ui.components.InteractivePosterCanvas
import com.example.ui.theme.CraftoGold
import com.example.ui.theme.CraftoPrimary
import com.example.ui.theme.CraftoPrimaryContainer
import com.example.ui.viewmodel.PosterViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PosterEditorScreen(
    viewModel: PosterViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToProfile: () -> Unit,
    modifier: Modifier = Modifier
) {
    val editorState by viewModel.editorState.collectAsStateWithLifecycle()
    val activeProfile by viewModel.activeProfile.collectAsStateWithLifecycle()
    val allTemplates by viewModel.allTemplates.collectAsStateWithLifecycle()
    val isProcessingCutout by viewModel.isProcessingCutout.collectAsStateWithLifecycle()

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var showQuotesSheet by remember { mutableStateOf(false) }

    val template = editorState.template ?: allTemplates.firstOrNull() ?: SampleData.defaultTemplates.first()
    val profile = activeProfile ?: SampleData.defaultPersonalProfile

    val activeFrameStyle = editorState.customFrameStyle ?: profile.frameStyle
    val activeMaskShape = editorState.customMaskShape ?: profile.cutoutMaskShape

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = template.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                        Text(
                            text = "${template.category} • Tap & Drag Cutout to adjust",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 11.sp
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("editor_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    // Save to Gallery button
                    IconButton(
                        onClick = { viewModel.savePosterToGallery() },
                        enabled = !editorState.isSaving,
                        modifier = Modifier.testTag("editor_save_btn")
                    ) {
                        if (editorState.isSaving) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        } else {
                            Icon(
                                imageVector = Icons.Default.Download,
                                contentDescription = "Save to Gallery",
                                tint = CraftoPrimary
                            )
                        }
                    }

                    // Share button
                    Button(
                        onClick = { viewModel.sharePoster() },
                        enabled = !editorState.isSharing,
                        colors = ButtonDefaults.buttonColors(containerColor = CraftoPrimary),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .testTag("editor_share_btn")
                    ) {
                        if (editorState.isSharing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = Color.White
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Share", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            // Live Interactive Poster Canvas
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                InteractivePosterCanvas(
                    template = template,
                    userProfile = profile.copy(
                        frameStyle = activeFrameStyle,
                        cutoutMaskShape = activeMaskShape
                    ),
                    cutoutOffsetX = editorState.cutoutOffsetX,
                    cutoutOffsetY = editorState.cutoutOffsetY,
                    cutoutScale = editorState.cutoutScaleFactor,
                    customQuote = editorState.customQuoteText,
                    customQuoteColor = editorState.customQuoteColorHex,
                    onDragCutout = { dx, dy -> viewModel.updateCutoutOffset(dx, dy) },
                    onResetPosition = { viewModel.resetCutoutTransform() }
                )

                if (isProcessingCutout) {
                    Surface(
                        color = Color(0xAA000000),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Processing AI Cutout...",
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            // Profile Quick Toggle Banner
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = CraftoPrimaryContainer
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (profile.profileType == ProfileType.BUSINESS) Icons.Default.Business else Icons.Default.Person,
                            contentDescription = null,
                            tint = CraftoPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = if (profile.profileType == ProfileType.BUSINESS) profile.businessName else profile.name,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = CraftoPrimary
                            )
                            Text(
                                text = "Active Branding Overlay",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Row {
                        FilledTonalButton(
                            onClick = {
                                val nextType = if (profile.profileType == ProfileType.PERSONAL) ProfileType.BUSINESS else ProfileType.PERSONAL
                                viewModel.switchProfileType(nextType)
                            },
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.testTag("editor_switch_profile_btn")
                        ) {
                            Text(
                                text = if (profile.profileType == ProfileType.PERSONAL) "Use Business" else "Use Personal",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        FilledTonalButton(
                            onClick = onNavigateToProfile,
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text("Edit", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Editor Controls Tab Row
            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = MaterialTheme.colorScheme.surface,
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                Tab(
                    selected = selectedTabIndex == 0,
                    onClick = { selectedTabIndex = 0 },
                    text = { Text("Frames & Shape", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.Style, contentDescription = null, modifier = Modifier.size(18.dp)) }
                )
                Tab(
                    selected = selectedTabIndex == 1,
                    onClick = { selectedTabIndex = 1 },
                    text = { Text("Quote & Text", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.FormatQuote, contentDescription = null, modifier = Modifier.size(18.dp)) }
                )
                Tab(
                    selected = selectedTabIndex == 2,
                    onClick = { selectedTabIndex = 2 },
                    text = { Text("Cutout Size", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.Tune, contentDescription = null, modifier = Modifier.size(18.dp)) }
                )
                Tab(
                    selected = selectedTabIndex == 3,
                    onClick = { selectedTabIndex = 3 },
                    text = { Text("Templates", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(18.dp)) }
                )
            }

            // Tab Panels
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                when (selectedTabIndex) {
                    0 -> FramesAndShapesTab(
                        activeFrameStyle = activeFrameStyle,
                        activeMaskShape = activeMaskShape,
                        onFrameStyleChange = { viewModel.setEditorFrameStyle(it) },
                        onMaskShapeChange = { viewModel.setEditorMaskShape(it) }
                    )
                    1 -> QuoteAndTextTab(
                        quoteText = editorState.customQuoteText ?: template.quoteText,
                        onQuoteChange = { viewModel.setCustomQuote(it) },
                        onColorChange = { viewModel.setQuoteColor(it) },
                        onOpenQuotesLibrary = { showQuotesSheet = true }
                    )
                    2 -> CutoutControlsTab(
                        scale = editorState.cutoutScaleFactor,
                        offsetX = editorState.cutoutOffsetX,
                        offsetY = editorState.cutoutOffsetY,
                        onScaleChange = { viewModel.updateCutoutScale(it) },
                        onReset = { viewModel.resetCutoutTransform() }
                    )
                    3 -> TemplateSwitcherTab(
                        templates = allTemplates,
                        currentTemplate = template,
                        onSelectTemplate = { viewModel.swapEditorTemplate(it) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }

    // Quotes Library Modal Sheet
    if (showQuotesSheet) {
        QuotesLibraryBottomSheet(
            onQuoteSelected = { selectedQuote ->
                viewModel.setCustomQuote(selectedQuote)
            },
            onDismiss = { showQuotesSheet = false }
        )
    }
}

@Composable
private fun FramesAndShapesTab(
    activeFrameStyle: FrameStyle,
    activeMaskShape: CutoutMaskShape,
    onFrameStyleChange: (FrameStyle) -> Unit,
    onMaskShapeChange: (CutoutMaskShape) -> Unit
) {
    Column {
        Text(
            text = "Footer Card Frame Style",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(FrameStyle.values()) { style ->
                val label = when (style) {
                    FrameStyle.MODERN_PILL -> "Modern Slate Pill"
                    FrameStyle.GOLDEN_ROYAL -> "Golden Royal 👑"
                    FrameStyle.DARK_GLASS -> "Dark Frosted Glass"
                    FrameStyle.VIBRANT_BANNER -> "Vibrant Gradient"
                    FrameStyle.SLEEK_MINIMAL -> "Sleek Light"
                    FrameStyle.CORNER_STAMP -> "Corner Badge"
                }
                FilterChip(
                    selected = activeFrameStyle == style,
                    onClick = { onFrameStyleChange(style) },
                    label = { Text(label, fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = CraftoPrimary,
                        selectedLabelColor = Color.White
                    ),
                    modifier = Modifier.testTag("frame_chip_${style.name}")
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Cutout Mask Shape",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(CutoutMaskShape.values()) { shape ->
                val label = when (shape) {
                    CutoutMaskShape.TRANSPARENT_CUTOUT -> "AI Cutout (No BG)"
                    CutoutMaskShape.CIRCLE_RING -> "Indigo Circle Ring"
                    CutoutMaskShape.GOLDEN_FRAME -> "Golden Royal Ring"
                    CutoutMaskShape.ROUNDED_SQUARE -> "Rounded Square"
                    CutoutMaskShape.SHIELD -> "Shield Badge"
                }
                FilterChip(
                    selected = activeMaskShape == shape,
                    onClick = { onMaskShapeChange(shape) },
                    label = { Text(label, fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = CraftoPrimary,
                        selectedLabelColor = Color.White
                    ),
                    modifier = Modifier.testTag("mask_chip_${shape.name}")
                )
            }
        }
    }
}

@Composable
private fun QuoteAndTextTab(
    quoteText: String,
    onQuoteChange: (String) -> Unit,
    onColorChange: (String) -> Unit,
    onOpenQuotesLibrary: () -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Edit Poster Quote / Status",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )

            Button(
                onClick = onOpenQuotesLibrary,
                colors = ButtonDefaults.buttonColors(containerColor = CraftoPrimaryContainer, contentColor = CraftoPrimary),
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                modifier = Modifier.testTag("open_quotes_lib_btn")
            ) {
                Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Quotes Library", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = quoteText,
            onValueChange = onQuoteChange,
            modifier = Modifier
                .fillMaxWidth()
                .height(110.dp)
                .testTag("editor_quote_input"),
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = CraftoPrimary
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Quote Text Color",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(6.dp))

        val colorPresets = listOf(
            Pair("#FFFFFF", "White"),
            Pair("#FDE047", "Golden"),
            Pair("#38BDF8", "Cyan"),
            Pair("#4ADE80", "Lime"),
            Pair("#FB7185", "Rose"),
            Pair("#FB923C", "Orange"),
            Pair("#C084FC", "Violet")
        )

        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            items(colorPresets) { (hex, name) ->
                val color = Color(android.graphics.Color.parseColor(hex))
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(color)
                        .border(2.dp, Color.Gray, CircleShape)
                        .clickable { onColorChange(hex) }
                        .testTag("color_preset_$hex"),
                    contentAlignment = Alignment.Center
                ) {
                    // Dot inside
                }
            }
        }
    }
}

@Composable
private fun CutoutControlsTab(
    scale: Float,
    offsetX: Float,
    offsetY: Float,
    onScaleChange: (Float) -> Unit,
    onReset: () -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Cutout Scale & Size: ${(scale * 100).toInt()}%",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )

            IconButton(onClick = onReset) {
                Icon(Icons.Default.RestartAlt, contentDescription = "Reset Position")
            }
        }

        Slider(
            value = scale,
            onValueChange = onScaleChange,
            valueRange = 0.5f..2.0f,
            colors = SliderDefaults.colors(
                thumbColor = CraftoPrimary,
                activeTrackColor = CraftoPrimary
            ),
            modifier = Modifier.testTag("cutout_scale_slider")
        )

        Text(
            text = "💡 Tip: You can touch and drag your photo/logo directly on the poster preview canvas above to reposition it anywhere!",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun TemplateSwitcherTab(
    templates: List<PosterTemplate>,
    currentTemplate: PosterTemplate,
    onSelectTemplate: (PosterTemplate) -> Unit
) {
    Column {
        Text(
            text = "Swap Background Template",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(templates) { item ->
                val startColor = try { Color(android.graphics.Color.parseColor(item.bgGradientStart)) } catch (e: Exception) { CraftoPrimary }
                val isSelected = item.id == currentTemplate.id

                Box(
                    modifier = Modifier
                        .size(width = 90.dp, height = 90.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(startColor)
                        .border(
                            width = if (isSelected) 3.dp else 1.dp,
                            color = if (isSelected) CraftoGold else Color(0x44FFFFFF),
                            shape = RoundedCornerShape(14.dp)
                        )
                        .clickable { onSelectTemplate(item) }
                        .padding(6.dp)
                        .testTag("swap_template_${item.id}"),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(item.festiveEmoji ?: "✨", fontSize = 20.sp)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = item.title,
                            color = Color.White,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 2,
                            lineHeight = 11.sp
                        )
                    }
                }
            }
        }
    }
}
