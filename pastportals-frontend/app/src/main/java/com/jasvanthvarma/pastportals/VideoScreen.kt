package com.jasvanthvarma.pastportals

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.seconds

data class GeneratedVideo(
    val id: String,
    val title: String,
    val description: String,
    val videoUrl: String,
    val thumbnailUrl: String? = null,
    val duration: Long = 0L,
    val createdAt: Long = System.currentTimeMillis()
)

data class VideoApiProvider(
    val name: String,
    val description: String,
    val features: List<String>,
    val pricing: String,
    val freeCredits: String?,
    val websiteUrl: String,
    val pricingUrl: String,
    val icon: ImageVector,
    val color: Color,
    val isRecommended: Boolean = false
)

// This is the Activity class
class VideoActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Get data from intent if needed
        val searchQuery = intent.getStringExtra("searchQuery") ?: ""

        // Sample video data - replace with your actual data
        val sampleVideo = GeneratedVideo(
            id = "sample_001",
            title = "Historical Video for $searchQuery",
            description = "Generated video content based on the year $searchQuery with historical events and narratives.",
            videoUrl = "android.resource://com.jasvanthvarma.pastportals/${R.raw.ai_1965}",
            duration = 60000L // 1 minute
        )

        setContent {
            PastPortalsTheme {
                VideoScreen(
                    generatedVideo = sampleVideo,
                    onBackClick = { finish() },
                    onShareClick = { /* Handle share */ },
                    onDownloadClick = { /* Handle download */ }
                )
            }
        }
    }
}

// This is the Composable function (renamed from VideoActivity to VideoScreen)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoScreen(
    generatedVideo: GeneratedVideo,
    onBackClick: () -> Unit,
    onShareClick: () -> Unit = {},
    onDownloadClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var isFullscreen by remember { mutableStateOf(false) }
    var isPlaying by remember { mutableStateOf(false) }
    var currentPosition by remember { mutableLongStateOf(0L) }
    var duration by remember { mutableLongStateOf(0L) }
    var showControls by remember { mutableStateOf(true) }
    var isLoading by remember { mutableStateOf(true) }
    var hasError by remember { mutableStateOf(false) }
    var showApiOptions by remember { mutableStateOf(false) }

    // API Providers Data
    val apiProviders = remember {
        listOf(
            VideoApiProvider(
                name = "Runway ML",
                description = "Professional-grade text-to-video generation with high-quality outputs",
                features = listOf("4K video generation", "30fps output", "Advanced AI models", "Fast processing"),
                pricing = "Starting at $12/month",
                freeCredits = "Free credits included",
                websiteUrl = "https://runwayml.com",
                pricingUrl = "https://runwayml.com/pricing",
                icon = Icons.Default.VideoLibrary,
                color = Color(0xFF6C5CE7),
                isRecommended = true
            ),
            VideoApiProvider(
                name = "Hugging Face",
                description = "Open-source models with flexible pricing and custom deployment options",
                features = listOf("Multiple models", "Custom endpoints", "Open source", "Community support"),
                pricing = "Pay-per-use from $0.0001/request",
                freeCredits = "Free inference API available",
                websiteUrl = "https://huggingface.co",
                pricingUrl = "https://huggingface.co/pricing",
                icon = Icons.Default.Psychology,
                color = Color(0xFFFF6B6B)
            ),
            VideoApiProvider(
                name = "Replicate",
                description = "Cloud-based AI model hosting with simple API integration",
                features = listOf("One-click deployment", "Automatic scaling", "Multiple models", "Simple API"),
                pricing = "From $0.0001/prediction",
                freeCredits = "Free tier available",
                websiteUrl = "https://replicate.com",
                pricingUrl = "https://replicate.com/pricing",
                icon = Icons.Default.Cloud,
                color = Color(0xFF4ECDC4)
            ),
            VideoApiProvider(
                name = "Luma Dream Machine",
                description = "High-quality video generation with advanced AI technology",
                features = listOf("720p+ resolution", "Realistic motion", "Fast generation", "High quality"),
                pricing = "$0.35 per 5-second video",
                freeCredits = "30 free generations/month",
                websiteUrl = "https://lumalabs.ai",
                pricingUrl = "https://lumalabs.ai/dream-machine",
                icon = Icons.Default.Movie,
                color = Color(0xFFFF9FF3)
            ),
            VideoApiProvider(
                name = "Stability AI",
                description = "Stable Video Diffusion with open-source models",
                features = listOf("Open source", "Self-hosting option", "Multiple resolutions", "Community driven"),
                pricing = "API: $0.004/image",
                freeCredits = "Free with self-hosting",
                websiteUrl = "https://stability.ai",
                pricingUrl = "https://platform.stability.ai/pricing",
                icon = Icons.Default.AutoAwesome,
                color = Color(0xFF74B9FF)
            )
        )
    }

    // ExoPlayer instance
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            val mediaItem = MediaItem.fromUri(generatedVideo.videoUrl)
            setMediaItem(mediaItem)
            prepare()

            addListener(object : Player.Listener {
                override fun onPlaybackStateChanged(playbackState: Int) {
                    isLoading = playbackState == Player.STATE_BUFFERING
                    hasError = playbackState == Player.STATE_IDLE
                }

                override fun onIsPlayingChanged(playing: Boolean) {
                    isPlaying = playing
                }
            })
        }
    }

    // Update progress
    LaunchedEffect(isPlaying) {
        while (isPlaying) {
            currentPosition = exoPlayer.currentPosition
            duration = exoPlayer.duration.takeIf { it > 0 } ?: 0L
            delay(1.seconds)
        }
    }

    // Hide controls after delay
    LaunchedEffect(showControls) {
        if (showControls && isPlaying) {
            delay(3.seconds)
            showControls = false
        }
    }

    // Lifecycle handling
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> exoPlayer.pause()
                Lifecycle.Event.ON_RESUME -> if (isPlaying) exoPlayer.play()
                Lifecycle.Event.ON_DESTROY -> exoPlayer.release()
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            exoPlayer.release()
        }
    }

    // Handle back button for fullscreen
    BackHandler(enabled = isFullscreen) {
        isFullscreen = false
    }

    if (isFullscreen) {
        FullscreenVideoPlayer(
            exoPlayer = exoPlayer,
            isPlaying = isPlaying,
            currentPosition = currentPosition,
            duration = duration,
            showControls = showControls,
            isLoading = isLoading,
            hasError = hasError,
            onPlayPause = {
                if (isPlaying) exoPlayer.pause() else exoPlayer.play()
            },
            onSeek = { position -> exoPlayer.seekTo(position) },
            onShowControls = { showControls = true },
            onExitFullscreen = { isFullscreen = false }
        )
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            item {
                // Top App Bar
                TopAppBar(
                    title = { Text("Generated Video") },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                    },
                    actions = {
                        IconButton(onClick = onShareClick) {
                            Icon(Icons.Default.Share, contentDescription = "Share")
                        }
                        IconButton(onClick = onDownloadClick) {
                            Icon(Icons.Default.Download, contentDescription = "Download")
                        }
                    }
                )
            }

            item {
                // Video Player Section
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(16f / 9f)
                        .background(Color.Black)
                ) {
                    AndroidView(
                        factory = { ctx ->
                            PlayerView(ctx).apply {
                                player = exoPlayer
                                useController = false
                            }
                        },
                        modifier = Modifier
                            .fillMaxSize()
                            .clickable { showControls = true }
                    )

                    // Custom Controls Overlay
                    VideoControlsOverlay(
                        isPlaying = isPlaying,
                        currentPosition = currentPosition,
                        duration = duration,
                        showControls = showControls,
                        isLoading = isLoading,
                        hasError = hasError,
                        onPlayPause = {
                            if (isPlaying) exoPlayer.pause() else exoPlayer.play()
                        },
                        onSeek = { position -> exoPlayer.seekTo(position) },
                        onFullscreen = { isFullscreen = true },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            item {
                // Video Information Section
                VideoInfoSection(
                    video = generatedVideo,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                )
            }

            item {
                // Generate New Video Section
                GenerateVideoSection(
                    onGenerateClick = { showApiOptions = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            // API Options Section
            if (showApiOptions) {
                item {
                    Text(
                        "Choose Text-to-Video API Provider",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(16.dp)
                    )
                }

                items(apiProviders) { provider ->
                    ApiProviderCard(
                        provider = provider,
                        onProviderClick = { url ->
                            try {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                // Handle error
                            }
                        },
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                    )
                }

                item {
                    // Hide API Options Button
                    OutlinedButton(
                        onClick = { showApiOptions = false },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text("Hide API Options")
                    }
                }
            }
        }
    }
}

@Composable
private fun GenerateVideoSection(
    onGenerateClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Default.VideoCall,
                contentDescription = "Generate Video",
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                "Want to generate a new video?",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                "Choose from premium text-to-video APIs",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onGenerateClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Generate New Video")
            }
        }
    }
}

@Composable
private fun ApiProviderCard(
    provider: VideoApiProvider,
    onProviderClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onProviderClick(provider.websiteUrl) },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(
                                provider.color.copy(alpha = 0.1f),
                                RoundedCornerShape(12.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            provider.icon,
                            contentDescription = provider.name,
                            tint = provider.color,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                provider.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )

                            if (provider.isRecommended) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Surface(
                                    color = Color(0xFF4CAF50),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        "RECOMMENDED",
                                        color = Color.White,
                                        fontSize = 10.sp,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }

                        Text(
                            provider.pricing,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                provider.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (provider.freeCredits != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "🎁 ${provider.freeCredits}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF4CAF50),
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Features
            provider.features.take(3).forEach { feature ->
                Row(
                    modifier = Modifier.padding(vertical = 1.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = Color(0xFF4CAF50)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        feature,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { onProviderClick(provider.websiteUrl) },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Visit Website")
                }

                Button(
                    onClick = { onProviderClick(provider.pricingUrl) },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("View Pricing")
                }
            }
        }
    }
}

@Composable
private fun VideoControlsOverlay(
    isPlaying: Boolean,
    currentPosition: Long,
    duration: Long,
    showControls: Boolean,
    isLoading: Boolean,
    hasError: Boolean,
    onPlayPause: () -> Unit,
    onSeek: (Long) -> Unit,
    onFullscreen: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        // Loading indicator
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = Color.White
            )
        }

        // Error state
        if (hasError) {
            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    Icons.Default.Error,
                    contentDescription = "Error",
                    tint = Color.White,
                    modifier = Modifier.size(48.dp)
                )
                Text(
                    "Failed to load video",
                    color = Color.White,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }

        // Controls
        if (showControls && !isLoading && !hasError) {
            // Play/Pause button in center
            IconButton(
                onClick = onPlayPause,
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(64.dp)
                    .background(
                        Color.Black.copy(alpha = 0.5f),
                        CircleShape
                    )
            ) {
                Icon(
                    if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (isPlaying) "Pause" else "Play",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }

            // Bottom controls
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(
                        Color.Black.copy(alpha = 0.7f)
                    )
                    .padding(16.dp)
            ) {
                // Progress bar
                if (duration > 0) {
                    val progress = (currentPosition.toFloat() / duration.toFloat()).coerceIn(0f, 1f)

                    Slider(
                        value = progress,
                        onValueChange = { newProgress ->
                            onSeek((newProgress * duration).toLong())
                        },
                        colors = SliderDefaults.colors(
                            thumbColor = Color.White,
                            activeTrackColor = Color.White,
                            inactiveTrackColor = Color.Gray
                        )
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            formatTime(currentPosition),
                            color = Color.White,
                            fontSize = 12.sp
                        )
                        Text(
                            formatTime(duration),
                            color = Color.White,
                            fontSize = 12.sp
                        )
                    }
                }

                // Control buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    IconButton(onClick = onFullscreen) {
                        Icon(
                            Icons.Default.Fullscreen,
                            contentDescription = "Fullscreen",
                            tint = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FullscreenVideoPlayer(
    exoPlayer: ExoPlayer,
    isPlaying: Boolean,
    currentPosition: Long,
    duration: Long,
    showControls: Boolean,
    isLoading: Boolean,
    hasError: Boolean,
    onPlayPause: () -> Unit,
    onSeek: (Long) -> Unit,
    onShowControls: () -> Unit,
    onExitFullscreen: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    player = exoPlayer
                    useController = false
                }
            },
            modifier = Modifier
                .fillMaxSize()
                .clickable { onShowControls() }
        )

        VideoControlsOverlay(
            isPlaying = isPlaying,
            currentPosition = currentPosition,
            duration = duration,
            showControls = showControls,
            isLoading = isLoading,
            hasError = hasError,
            onPlayPause = onPlayPause,
            onSeek = onSeek,
            onFullscreen = onExitFullscreen,
            modifier = Modifier.fillMaxSize()
        )

        // Exit fullscreen button
        if (showControls) {
            IconButton(
                onClick = onExitFullscreen,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
                    .background(
                        Color.Black.copy(alpha = 0.5f),
                        CircleShape
                    )
            ) {
                Icon(
                    Icons.Default.FullscreenExit,
                    contentDescription = "Exit Fullscreen",
                    tint = Color.White
                )
            }
        }
    }
}

@Composable
private fun VideoInfoSection(
    video: GeneratedVideo,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = video.title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = video.description,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    "Video Details",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(8.dp))

                VideoDetailRow("Video ID", video.id)
                VideoDetailRow("Created", formatDateTime(video.createdAt))
                if (video.duration > 0) {
                    VideoDetailRow("Duration", formatTime(video.duration))
                }
            }
        }
    }
}

@Composable
private fun VideoDetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

private fun formatTime(timeMs: Long): String {
    val totalSeconds = timeMs / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format("%02d:%02d", minutes, seconds)
}

private fun formatDateTime(timestamp: Long): String {
    val date = java.util.Date(timestamp)
    val format = java.text.SimpleDateFormat("MMM dd, yyyy HH:mm", java.util.Locale.getDefault())
    return format.format(date)
}