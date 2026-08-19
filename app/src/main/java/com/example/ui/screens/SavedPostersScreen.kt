package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.data.model.SavedPoster
import com.example.data.model.TemplateAspectRatio
import com.example.ui.theme.CraftoPrimary
import com.example.ui.theme.CraftoRose
import com.example.ui.viewmodel.PosterViewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavedPostersScreen(
    viewModel: PosterViewModel,
    onNavigateToHome: () -> Unit,
    modifier: Modifier = Modifier
) {
    val savedPosters by viewModel.savedPosters.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var posterToDelete by remember { mutableStateOf<SavedPoster?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "My Saved Creations",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${savedPosters.size} Posters saved on device",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 11.sp
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        if (savedPosters.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(24.dp)
                ) {
                    Text(text = "🖼️", fontSize = 52.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "No saved posters yet",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Pick any festival or morning greeting from templates and save or share your creation!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(18.dp))
                    Button(
                        onClick = onNavigateToHome,
                        colors = ButtonDefaults.buttonColors(containerColor = CraftoPrimary),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.testTag("empty_explore_templates_btn")
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Explore Templates", fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    top = innerPadding.calculateTopPadding() + 8.dp,
                    bottom = innerPadding.calculateBottomPadding() + 88.dp
                ),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("saved_posters_grid")
            ) {
                items(savedPosters, key = { it.id }) { poster ->
                    SavedPosterGridItem(
                        poster = poster,
                        onShare = {
                            try {
                                val file = File(poster.filePath)
                                if (file.exists()) {
                                    val uri = FileProvider.getUriForFile(
                                        context,
                                        "${context.packageName}.fileprovider",
                                        file
                                    )
                                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                        type = "image/jpeg"
                                        putExtra(Intent.EXTRA_STREAM, uri)
                                        putExtra(Intent.EXTRA_TEXT, "${poster.quoteSnippet}\n\n— Created with Jaadu App ✨")
                                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    }
                                    val chooser = Intent.createChooser(shareIntent, "Share Poster")
                                    chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    context.startActivity(chooser)
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        },
                        onDelete = {
                            posterToDelete = poster
                        }
                    )
                }
            }
        }

        // Delete Confirmation Dialog
        posterToDelete?.let { target ->
            AlertDialog(
                onDismissRequest = { posterToDelete = null },
                title = { Text("Delete Poster?") },
                text = { Text("Are you sure you want to remove \"${target.title}\" from your creations?") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            viewModel.deleteSavedPoster(target.id)
                            posterToDelete = null
                        }
                    ) {
                        Text("Delete", color = CraftoRose, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { posterToDelete = null }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Composable
private fun SavedPosterGridItem(
    poster: SavedPoster,
    onShare: () -> Unit,
    onDelete: () -> Unit
) {
    val isStory = poster.aspectRatio == TemplateAspectRatio.STORY_9_16
    val ratio = if (isStory) 9f / 16f else 1f
    val file = File(poster.filePath)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("saved_poster_card_${poster.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(ratio)
                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                    .background(MaterialTheme.colorScheme.surface)
            ) {
                if (file.exists()) {
                    AsyncImage(
                        model = file,
                        contentDescription = poster.title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(36.dp))
                    }
                }
            }

            Column(modifier = Modifier.padding(10.dp)) {
                Text(
                    text = poster.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                val dateFormatted = remember(poster.timestamp) {
                    val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                    sdf.format(Date(poster.timestamp))
                }

                Text(
                    text = "$dateFormatted • ${poster.category}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(30.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = CraftoRose,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    IconButton(
                        onClick = onShare,
                        modifier = Modifier.size(30.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share",
                            tint = CraftoPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}
