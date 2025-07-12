package com.jasvanthvarma.pastportals

import androidx.compose.material3.Button
import androidx.compose.material3.Text
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jasvanthvarma.pastportals.network.RetrofitClient
import com.jasvanthvarma.pastportals.viewmodel.ApiResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.jasvanthvarma.pastportals.VideoActivity
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import com.jasvanthvarma.pastportals.network.ApiService
import com.jasvanthvarma.pastportals.network.GeminiPromptRequest
import com.jasvanthvarma.pastportals.viewmodel.YearSummary
import kotlinx.coroutines.delay

class ResultActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val searchQuery = intent.getStringExtra("searchQuery") ?: ""

        setContent {
            PastPortalsTheme {
                ResultScreen(
                    query = searchQuery,
                    onBackPressed = { finish() },
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultScreen(
    query: String,
    onBackPressed: () -> Unit = {},
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val lazyListState = rememberLazyListState()

    // Existing state
    var result by remember { mutableStateOf<ApiResponse?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var userPrompt by remember { mutableStateOf("") }
    var isSubmittingPrompt by remember { mutableStateOf(false) }

    // Audio state
    var audioResponse by remember { mutableStateOf<AudioResponse?>(null) }
    var isGeneratingAudio by remember { mutableStateOf(false) }
    var audioError by remember { mutableStateOf<String?>(null) }

    // Audio player
    val audioPlayer = remember { AudioPlayerManager(context) }
    val isPlaying by audioPlayer.isPlaying.collectAsStateWithLifecycle()

    // Function to handle event click and scroll to prompt section
    val onTimelineEventClick = { eventTitle: String, eventDate: String ->
        val eventText = "Write a creative story about: $eventTitle (which occurred on $eventDate)"
        userPrompt = eventText

        // Scroll to the prompt section
        scope.launch {
            // Calculate the approximate item index for the prompt section
            // This assumes the prompt section is near the end of the list
            val totalItems = (result?.yearSummary?.timeline?.size ?: 0) +
                    (result?.yearSummary?.image_prompts?.size ?: 0) +
                    6 // Header, summary, timeline header, prompt section, etc.

            lazyListState.animateScrollToItem(
                index = maxOf(0, totalItems - 2), // Scroll to prompt section
                scrollOffset = 0
            )
        }
    }

    // Cleanup audio player on dispose
    DisposableEffect(Unit) {
        onDispose {
            audioPlayer.release()
        }
    }

    // Fetch Wikipedia data
    LaunchedEffect(query) {
        try {
            val response = RetrofitClient.apiService.getYearSummary(query)
            withContext(Dispatchers.Main) {
                result = response
                isLoading = false
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                errorMessage = "Failed to fetch data: ${e.message}"
                isLoading = false
            }
        }
    }

    // Function to generate audio
    fun generateAudio() {
        scope.launch {
            try {
                isGeneratingAudio = true
                audioError = null

                val audioRequest = AudioRequest(
                    year = query.toInt(),
                    audioType = "combined"
                )

                val response = RetrofitClient.apiService.generateWikipediaAudio(audioRequest)

                if (response.success) {
                    audioResponse = response
                } else {
                    audioError = response.error ?: "Failed to generate audio"
                }
            } catch (e: Exception) {
                audioError = "Audio generation failed: ${e.message}"
            } finally {
                isGeneratingAudio = false
            }
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Year $query",
                        style = MaterialTheme.typography.headlineSmall
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackPressed) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    // Audio generation button
                    IconButton(
                        onClick = { generateAudio() },
                        enabled = !isGeneratingAudio && result != null
                    ) {
                        if (isGeneratingAudio) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                Icons.Default.VolumeUp,
                                contentDescription = "Generate Audio",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        },
    ) { paddingValues ->
        when {
            isLoading -> {
                LoadingScreen(modifier = Modifier.padding(paddingValues))
            }

            errorMessage != null -> {
                ErrorScreen(
                    errorMessage = errorMessage ?: "Unknown error",
                    modifier = Modifier.padding(paddingValues)
                )
            }

            result != null && result!!.success -> {
                val yearSummary = result!!.yearSummary

                LazyColumn(
                    state = lazyListState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(vertical = 16.dp)
                ) {
                    // Year Header Card
                    item {
                        YearHeaderCard(year = yearSummary.year)
                    }

                    // Audio Player Section
                    if (audioResponse != null) {
                        item {
                            AudioPlayerCard(
                                audioResponse = audioResponse!!,
                                audioPlayer = audioPlayer,
                                isPlaying = isPlaying
                            )
                        }
                    }

                    // Audio Error
                    if (audioError != null) {
                        item {
                            ErrorCard(
                                message = audioError!!,
                                onDismiss = { audioError = null }
                            )
                        }
                    }

                    // Summary Section
                    item {
                        SectionCard(
                            title = "Historical Summary",
                            icon = Icons.Default.Info,
                            content = yearSummary.paragraph
                        )
                    }

                    // Timeline Section
                    item {
                        TimelineHeader()
                    }

                    // Add click instruction text
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Text(
                                text = "💡 Tip: Click on any event below to create a custom prompt based on that event",
                                modifier = Modifier.padding(12.dp),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    items(yearSummary.timeline) { event ->
                        TimelineEventCard(
                            title = event.title,
                            date = event.date,
                            onClick = { onTimelineEventClick(event.title, event.date) }
                        )
                    }

                    // Image Prompts Section
                    if (yearSummary.image_prompts.isNotEmpty()) {
                        item {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "Visual Inspirations",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        items(yearSummary.image_prompts) { prompt ->
                            ImagePromptCard(
                                prompt = prompt,
                                onClick = {
                                    userPrompt = "Create a visual representation of: $prompt"
                                    scope.launch {
                                        val totalItems = yearSummary.timeline.size +
                                                yearSummary.image_prompts.size + 6
                                        lazyListState.animateScrollToItem(
                                            index = maxOf(0, totalItems - 2),
                                            scrollOffset = 0
                                        )
                                    }
                                }
                            )
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                        CustomPromptCard(
                            prompt = userPrompt,
                            onPromptChange = { userPrompt = it },
                            onSubmit = {
                                // Handle prompt submission here
                                if (userPrompt.isNotBlank()) {
                                    isSubmittingPrompt = true
                                    // Add your logic to process the prompt
                                    // For example, you might want to generate an image or send it to an API

                                    // Simulate processing (replace with actual implementation)
                                    scope.launch {
                                        try {
                                            // Your prompt processing logic here
                                            // For now, just simulate a delay
                                            kotlinx.coroutines.delay(2000)

                                            // Reset the prompt after successful submission
                                            userPrompt = ""
                                        } catch (e: Exception) {
                                            // Handle error
                                        } finally {
                                            isSubmittingPrompt = false
                                        }
                                    }
                                }
                            },
                            isSubmitting = isSubmittingPrompt
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(24.dp))

                        Button(
                            onClick = {
                                val intent = Intent(context, VideoActivity::class.java)
                                // You can pass data to VideoActivity if needed
                                intent.putExtra("searchQuery", query)
                                context.startActivity(intent)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Text(
                                text = "Generate Video",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Spacer(modifier = Modifier.height(32.dp))
                    }
                }
            }

            else -> {
                NoDataScreen(
                    query = query,
                    modifier = Modifier.padding(paddingValues)
                )
            }
        }
    }
}

// Updated TimelineEventCard with click functionality
@Composable
fun TimelineEventCard(
    title: String,
    date: String,
    onClick: (() -> Unit)? = null
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (onClick != null) {
                    Modifier.clickable { onClick() }
                } else {
                    Modifier
                }
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (onClick != null) {
                MaterialTheme.colorScheme.surfaceVariant
            } else {
                MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp)
        ) {
            // Timeline indicator
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(MaterialTheme.colorScheme.primary)
                    .align(Alignment.Top)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = date,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            // Add click indicator if clickable
            if (onClick != null) {
                Icon(
                    Icons.Default.TouchApp,
                    contentDescription = "Click to use as prompt",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .size(20.dp)
                        .align(Alignment.CenterVertically)
                )
            }
        }
    }
}

// Updated ImagePromptCard with click functionality
@Composable
fun ImagePromptCard(
    prompt: String,
    onClick: (() -> Unit)? = null
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (onClick != null) {
                    Modifier.clickable { onClick() }
                } else {
                    Modifier
                }
            ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = prompt,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )

            if (onClick != null) {
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    Icons.Default.TouchApp,
                    contentDescription = "Click to use as prompt",
                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

// Keep all your existing Composable functions unchanged...
@Composable
fun ErrorCard(
    message: String,
    onDismiss: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Error,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = message,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            IconButton(onClick = onDismiss) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Dismiss",
                    tint = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }
    }
}

@Composable
fun AudioPlayerCard(
    audioResponse: AudioResponse,
    audioPlayer: AudioPlayerManager,
    isPlaying: Boolean
) {
    // Replace this with your actual server URL
    val baseUrl = "http://192.168.136.184:5000" // Update this!

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Default.GraphicEq,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Text(
                        text = "Audio Narration",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }

                Row {
                    // Play/Pause Button
                    IconButton(
                        onClick = {
                            if (isPlaying) {
                                audioPlayer.pauseAudio()
                            } else {
                                audioResponse.audioFiles?.combined?.let { audioFile ->
                                    val audioUrl = "$baseUrl${audioFile.url}"
                                    audioPlayer.playAudio(audioUrl)
                                }
                            }
                        }
                    ) {
                        Icon(
                            if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isPlaying) "Pause" else "Play",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    // Stop Button
                    IconButton(
                        onClick = { audioPlayer.stopAudio() }
                    ) {
                        Icon(
                            Icons.Default.Stop,
                            contentDescription = "Stop",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // Audio Options
            if (audioResponse.audioFiles?.timeline != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Individual Events:",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )

                Spacer(modifier = Modifier.height(8.dp))

                audioResponse.audioFiles!!.timeline!!.forEach { timelineAudio ->
                    OutlinedButton(
                        onClick = {
                            val audioUrl = "$baseUrl${timelineAudio.url}"
                            audioPlayer.playAudio(audioUrl)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp)
                    ) {
                        Icon(
                            Icons.Default.PlayArrow,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = timelineAudio.title,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun LoadingScreen(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.dash_bg),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            alpha = 0.3f
        )
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(48.dp),
                strokeWidth = 4.dp
            )
            Text(
                "Exploring the depths of history...",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun ErrorScreen(
    errorMessage: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.errorContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(48.dp)
                )
                Text(
                    text = "Oops! Something went wrong",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = errorMessage,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun NoDataScreen(
    query: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.DateRange,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.outline
            )
            Text(
                text = "No historical data found",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "We couldn't find any information for the year \"$query\".",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun YearHeaderCard(year: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f)
                        )
                    )
                )
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = year,
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun SectionCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    content: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Text(
                text = content,
                style = MaterialTheme.typography.bodyMedium,
                lineHeight = 20.sp
            )
        }
    }
}

@Composable
fun TimelineHeader() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Timeline,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary
        )
        Text(
            text = "Timeline of Events",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
fun PastPortalsTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        content = content
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomPromptCard(

    prompt: String,
    onPromptChange: (String) -> Unit,
    onSubmit: () -> Unit,
    isSubmitting: Boolean = false,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val keyboardController = LocalSoftwareKeyboardController.current

    // New state variables for Gemini integration
    var generatedPrompt by remember { mutableStateOf<String?>(null) }
    var isGeneratingPrompt by remember { mutableStateOf(false) }
    var promptError by remember { mutableStateOf<String?>(null) }
    // Add this state at the top inside CustomPromptCard
    var isLoadingToVideo by remember { mutableStateOf(false) }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Create Your Own Prompt",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            // Description
            Text(
                text = "Write your own creative prompt inspired by this historical period:",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onTertiaryContainer
            )

            // Text Input
            OutlinedTextField(
                value = prompt,
                onValueChange = onPromptChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = {
                    Text("Enter your creative prompt here...")
                },
                minLines = 3,
                maxLines = 6,
                enabled = !isSubmitting && !isGeneratingPrompt,
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        keyboardController?.hide()
                    }
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )

            // Error Display
            if (promptError != null) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Error,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = promptError!!,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }

            // Generated Prompt Display
            if (generatedPrompt != null) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "AI-Enhanced Prompt:",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Text(
                            text = generatedPrompt!!,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }

            // Buttons Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Generate Prompt Button
                Button(
                    onClick = {
                        if (prompt.isNotBlank()) {
                            scope.launch {
                                try {
                                    isGeneratingPrompt = true
                                    promptError = null

                                    val request = GeminiPromptRequest(wikipediaText = prompt)
                                    val response = RetrofitClient.apiService.generatePromptFromText(request)

                                    if (response.success && response.prompt != null) {
                                        generatedPrompt = response.prompt
                                    } else {
                                        promptError = response.error ?: "Failed to generate prompt"
                                    }
                                } catch (e: Exception) {
                                    promptError = "Network error: ${e.message}"
                                } finally {
                                    isGeneratingPrompt = false
                                }
                            }
                        }
                    },
                    enabled = prompt.isNotBlank() && !isGeneratingPrompt && !isSubmitting,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary
                    )
                ) {
                    if (isGeneratingPrompt) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onSecondary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Generating...")
                    } else {
                        Icon(
                            Icons.Default.AutoAwesome,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Enhance")
                    }
                }

                // Generate Video Button
                if (isLoadingToVideo) {
                    Button(
                        onClick = {},
                        enabled = false,
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Loading...")
                    }

                    // Trigger delayed navigation
                    LaunchedEffect(Unit) {
                        delay(20_000L) // 20 seconds delay
                        val intent = Intent(context, VideoActivity::class.java)
                        val finalPrompt = generatedPrompt ?: prompt
                        intent.putExtra("generatedPrompt", finalPrompt)
                        intent.putExtra("originalPrompt", prompt)
                        context.startActivity(intent)
                        isLoadingToVideo = false
                    }

                } else {
                    Button(
                        onClick = {
                            isLoadingToVideo = true
                        },
                        enabled = (generatedPrompt != null || prompt.isNotBlank()) && !isGeneratingPrompt,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            Icons.Default.VideoLibrary,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Generate Video")
                    }
                }
            }

            // Helper text
            Text(
                text = if (generatedPrompt != null) {
                    "✨ Your prompt has been enhanced! You can now generate a video with the improved prompt."
                } else {
                    "💡 Tip: Click 'Enhance' to improve your prompt with AI, then generate your video."
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
        }
    }
}

// In your ResultScreen, replace the video generation logic:
private fun generateHistoricalVideo(yearSummary: YearSummary, customPrompt: String? = null) {
    val scope = null
    scope?.launch {
        try {
            val request = ApiService.HistoricalVideoRequest(
                yearData = yearSummary,
                customPrompt = customPrompt.takeIf { !it.isNullOrBlank() }
            )

            val response = RetrofitClient.apiService.generateHistoricalVideo(request)

            if (response.isSuccessful && response.body()?.success == true) {
                val videoResponse = response.body()!!
                // Handle successful video generation
                videoResponse.jobId
                // Start polling for status if needed
            } else {
                response.body()?.error ?: "Failed to generate video"
            }
        } catch (e: Exception) {
            "Video generation failed: ${e.message}"
        }
    }
}