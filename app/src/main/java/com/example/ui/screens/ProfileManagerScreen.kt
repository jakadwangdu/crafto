package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Save
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.data.model.CutoutMaskShape
import com.example.data.model.FrameStyle
import com.example.data.model.ProfileType
import com.example.data.model.UserProfile
import com.example.data.sample.SampleData
import com.example.ui.components.ProfileBadgeView
import com.example.ui.components.UserCutoutPreview
import com.example.ui.theme.CraftoGold
import com.example.ui.theme.CraftoPrimary
import com.example.ui.theme.CraftoPrimaryContainer
import com.example.ui.viewmodel.PosterViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileManagerScreen(
    viewModel: PosterViewModel,
    modifier: Modifier = Modifier
) {
    val activeProfile by viewModel.activeProfile.collectAsStateWithLifecycle()
    val allProfiles by viewModel.allProfiles.collectAsStateWithLifecycle()
    val isProcessingCutout by viewModel.isProcessingCutout.collectAsStateWithLifecycle()

    val currentProfile = activeProfile ?: SampleData.defaultPersonalProfile

    var profileTypeTab by remember {
        mutableIntStateOf(if (currentProfile.profileType == ProfileType.BUSINESS) 1 else 0)
    }

    // Form inputs state
    var name by remember { mutableStateOf(currentProfile.name) }
    var designation by remember { mutableStateOf(currentProfile.designation) }
    var businessName by remember { mutableStateOf(currentProfile.businessName) }
    var tagline by remember { mutableStateOf(currentProfile.tagline) }
    var phoneNumber by remember { mutableStateOf(currentProfile.phoneNumber) }
    var email by remember { mutableStateOf(currentProfile.email) }
    var website by remember { mutableStateOf(currentProfile.website) }
    var socialHandle by remember { mutableStateOf(currentProfile.socialHandle) }
    var address by remember { mutableStateOf(currentProfile.address) }

    var showPhone by remember { mutableStateOf(currentProfile.showPhone) }
    var showDesignation by remember { mutableStateOf(currentProfile.showDesignation) }
    var showSocial by remember { mutableStateOf(currentProfile.showSocial) }
    var showWatermark by remember { mutableStateOf(currentProfile.showWatermark) }

    var selectedMaskShape by remember { mutableStateOf(currentProfile.cutoutMaskShape) }
    var selectedFrameStyle by remember { mutableStateOf(currentProfile.frameStyle) }

    // Sync form inputs when active profile changes
    LaunchedEffect(currentProfile.id, currentProfile.profileType) {
        name = currentProfile.name
        designation = currentProfile.designation
        businessName = currentProfile.businessName
        tagline = currentProfile.tagline
        phoneNumber = currentProfile.phoneNumber
        email = currentProfile.email
        website = currentProfile.website
        socialHandle = currentProfile.socialHandle
        address = currentProfile.address
        showPhone = currentProfile.showPhone
        showDesignation = currentProfile.showDesignation
        showSocial = currentProfile.showSocial
        showWatermark = currentProfile.showWatermark
        selectedMaskShape = currentProfile.cutoutMaskShape
        selectedFrameStyle = currentProfile.frameStyle
        profileTypeTab = if (currentProfile.profileType == ProfileType.BUSINESS) 1 else 0
    }

    // Photo picker launcher
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            val updated = currentProfile.copy(
                imageUri = uri.toString(),
                cutoutMaskShape = selectedMaskShape
            )
            viewModel.saveProfile(updated, newImageSource = uri.toString(), runBgRemoval = true)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Brand Profile Setup",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Auto-inserts on every poster with AI Cutout",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 11.sp
                        )
                    }
                },
                actions = {
                    Button(
                        onClick = {
                            val isBusiness = profileTypeTab == 1
                            val updated = currentProfile.copy(
                                name = name,
                                designation = designation,
                                businessName = businessName,
                                tagline = tagline,
                                phoneNumber = phoneNumber,
                                email = email,
                                website = website,
                                socialHandle = socialHandle,
                                address = address,
                                showPhone = showPhone,
                                showDesignation = showDesignation,
                                showSocial = showSocial,
                                showWatermark = showWatermark,
                                cutoutMaskShape = selectedMaskShape,
                                frameStyle = selectedFrameStyle,
                                profileType = if (isBusiness) ProfileType.BUSINESS else ProfileType.PERSONAL
                            )
                            viewModel.saveProfile(updated)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CraftoPrimary),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .testTag("save_profile_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Save,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Save", fontWeight = FontWeight.Bold, fontSize = 13.sp)
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
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // Profile Type Tabs (Personal vs Business)
            TabRow(
                selectedTabIndex = profileTypeTab,
                containerColor = MaterialTheme.colorScheme.surface,
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                Tab(
                    selected = profileTypeTab == 0,
                    onClick = {
                        profileTypeTab = 0
                        viewModel.switchProfileType(ProfileType.PERSONAL)
                    },
                    text = { Text("Personal Profile", fontWeight = FontWeight.Bold, fontSize = 13.sp) },
                    icon = { Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(18.dp)) },
                    modifier = Modifier.testTag("tab_personal_profile")
                )
                Tab(
                    selected = profileTypeTab == 1,
                    onClick = {
                        profileTypeTab = 1
                        viewModel.switchProfileType(ProfileType.BUSINESS)
                    },
                    text = { Text("Business Profile", fontWeight = FontWeight.Bold, fontSize = 13.sp) },
                    icon = { Icon(Icons.Default.Business, contentDescription = null, modifier = Modifier.size(18.dp)) },
                    modifier = Modifier.testTag("tab_business_profile")
                )
            }

            // Photo / Logo & AI Background Removal Section
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (profileTypeTab == 1) "Business Logo / Brand Mark" else "Your Portrait Photo",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Source Photo
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier
                                    .size(80.dp)
                                    .clip(CircleShape)
                                    .border(2.dp, CraftoPrimary, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                if (currentProfile.imageUri == "sample_business") {
                                    androidx.compose.foundation.Image(
                                        painter = painterResource(id = R.drawable.img_sample_business),
                                        contentDescription = null,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                } else {
                                    androidx.compose.foundation.Image(
                                        painter = painterResource(id = R.drawable.img_sample_portrait),
                                        contentDescription = null,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Original", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }

                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "AI Processing",
                            tint = CraftoPrimary,
                            modifier = Modifier.size(24.dp)
                        )

                        // Processed Cutout
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier
                                    .size(80.dp)
                                    .testTag("profile_cutout_preview")
                            ) {
                                UserCutoutPreview(
                                    userProfile = currentProfile.copy(cutoutMaskShape = selectedMaskShape),
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("AI Cutout", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = CraftoGold)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Button(
                            onClick = { photoPickerLauncher.launch("image/*") },
                            colors = ButtonDefaults.buttonColors(containerColor = CraftoPrimary),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                            modifier = Modifier.testTag("upload_photo_btn")
                        ) {
                            Icon(Icons.Default.PhotoCamera, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Upload Photo", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        FilledTonalButton(
                            onClick = {
                                val imageSource = if (profileTypeTab == 1) "sample_business" else "sample_portrait"
                                viewModel.saveProfile(
                                    currentProfile.copy(cutoutMaskShape = selectedMaskShape),
                                    newImageSource = imageSource,
                                    runBgRemoval = true
                                )
                            },
                            enabled = !isProcessingCutout,
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                            modifier = Modifier.testTag("run_ai_cutout_btn")
                        ) {
                            if (isProcessingCutout) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                            } else {
                                Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Auto Remove BG", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Mask Shape Selector
            Text(
                text = "Cutout Framing Shape",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(CutoutMaskShape.values()) { shape ->
                    val label = when (shape) {
                        CutoutMaskShape.TRANSPARENT_CUTOUT -> "Full Cutout (No BG)"
                        CutoutMaskShape.CIRCLE_RING -> "Indigo Circle"
                        CutoutMaskShape.GOLDEN_FRAME -> "Golden Royal 👑"
                        CutoutMaskShape.ROUNDED_SQUARE -> "Rounded Square"
                        CutoutMaskShape.SHIELD -> "Shield Mark"
                    }
                    FilterChip(
                        selected = selectedMaskShape == shape,
                        onClick = {
                            selectedMaskShape = shape
                            viewModel.setEditorMaskShape(shape)
                        },
                        label = { Text(label, fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = CraftoPrimary,
                            selectedLabelColor = Color.White
                        ),
                        modifier = Modifier.testTag("shape_chip_${shape.name}")
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Profile Information Form
            Text(
                text = if (profileTypeTab == 1) "Business Details" else "Personal Details",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            if (profileTypeTab == 1) {
                // Business Fields
                OutlinedTextField(
                    value = businessName,
                    onValueChange = { businessName = it },
                    label = { Text("Business / Company Name") },
                    leadingIcon = { Icon(Icons.Default.Business, contentDescription = null) },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_business_name")
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = tagline,
                    onValueChange = { tagline = it },
                    label = { Text("Tagline / Slogan (e.g. Building Dreams)") },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_business_tagline")
                )
            } else {
                // Personal Fields
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Your Full Name") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_personal_name")
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = designation,
                    onValueChange = { designation = it },
                    label = { Text("Designation / Profession (e.g. Financial Consultant)") },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_personal_designation")
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Contact Info
            OutlinedTextField(
                value = phoneNumber,
                onValueChange = { phoneNumber = it },
                label = { Text("Phone / WhatsApp Number") },
                leadingIcon = { Icon(Icons.Default.Call, contentDescription = null) },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_phone_number")
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = socialHandle,
                onValueChange = { socialHandle = it },
                label = { Text("Social Handle or Website (e.g. @rahul.consults)") },
                leadingIcon = { Icon(Icons.Default.Language, contentDescription = null) },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_social_handle")
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Card Frame Style Selector
            Text(
                text = "Footer Card Frame Style",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
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
                        selected = selectedFrameStyle == style,
                        onClick = { selectedFrameStyle = style },
                        label = { Text(label, fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = CraftoPrimary,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Live Footer Badge Preview
            Text(
                text = "Live Poster Footer Preview",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))
            ProfileBadgeView(
                profile = currentProfile.copy(
                    name = name,
                    designation = designation,
                    businessName = businessName,
                    tagline = tagline,
                    phoneNumber = phoneNumber,
                    socialHandle = socialHandle,
                    showPhone = showPhone,
                    showDesignation = showDesignation,
                    showSocial = showSocial,
                    frameStyle = selectedFrameStyle,
                    profileType = if (profileTypeTab == 1) ProfileType.BUSINESS else ProfileType.PERSONAL
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Poster Display Preferences
            Text(
                text = "Poster Branding Display Options",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Show Phone / WhatsApp on Posters", fontSize = 13.sp)
                        Switch(
                            checked = showPhone,
                            onCheckedChange = { showPhone = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = CraftoPrimary)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Show Designation / Business Tagline", fontSize = 13.sp)
                        Switch(
                            checked = showDesignation,
                            onCheckedChange = { showDesignation = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = CraftoPrimary)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Show Social Media Handle", fontSize = 13.sp)
                        Switch(
                            checked = showSocial,
                            onCheckedChange = { showSocial = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = CraftoPrimary)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Show Crafto App Badge", fontSize = 13.sp)
                        Switch(
                            checked = showWatermark,
                            onCheckedChange = { showWatermark = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = CraftoPrimary)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Final Save Button
            Button(
                onClick = {
                    val isBusiness = profileTypeTab == 1
                    val updated = currentProfile.copy(
                        name = name,
                        designation = designation,
                        businessName = businessName,
                        tagline = tagline,
                        phoneNumber = phoneNumber,
                        email = email,
                        website = website,
                        socialHandle = socialHandle,
                        address = address,
                        showPhone = showPhone,
                        showDesignation = showDesignation,
                        showSocial = showSocial,
                        showWatermark = showWatermark,
                        cutoutMaskShape = selectedMaskShape,
                        frameStyle = selectedFrameStyle,
                        profileType = if (isBusiness) ProfileType.BUSINESS else ProfileType.PERSONAL
                    )
                    viewModel.saveProfile(updated)
                },
                colors = ButtonDefaults.buttonColors(containerColor = CraftoPrimary),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("save_profile_bottom_btn")
            ) {
                Icon(Icons.Default.CheckCircle, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Save Brand & Apply to All Posters", fontSize = 15.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}
