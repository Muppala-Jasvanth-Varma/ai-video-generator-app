package com.jasvanthvarma.pastportals

import android.util.Log
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import androidx.compose.ui.platform.LocalContext

data class SearchResult(
    val title: String,
    val imageUrl: String,
    val description: String
)

// Enhanced Retrofit API with all endpoints
interface ApiService {
    @GET("api/search")
    suspend fun search(@Query("query") query: String): List<SearchResult>

    // Add your actual endpoints here
    @GET("api/wikipedia/year/{year}")
    suspend fun getYearEvents(@retrofit2.http.Path("year") year: String): Any

    @GET("api/wikipedia/events/search")
    suspend fun searchEvents(
        @Query("query") query: String,
        @Query("year") year: String? = null
    ): Any

    @GET("api/wikipedia/people/search")
    suspend fun searchPeople(
        @Query("query") query: String,
        @Query("era") era: String? = null,
        @Query("occupation") occupation: String? = null
    ): Any

    @GET("api/wikipedia/day/{month}/{day}")
    suspend fun getDayEvents(
        @retrofit2.http.Path("month") month: String,
        @retrofit2.http.Path("day") day: String
    ): Any
}

// Enhanced Retrofit Client with correct base URL
object RetrofitClient {
    val api: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl("http://192.168.136.184:5000/") // Fixed base URL
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}

class DashboardActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DashboardScreen(onProfileClick = {
                val intent = Intent(this, ProfileActivity::class.java)
                startActivity(intent)
            })
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(onProfileClick: () -> Unit) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedSearchType by remember { mutableStateOf("events") } // Default to events
    val context = LocalContext.current
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val recommendedItems = listOf(
        "Shivaji Maharaj" to R.drawable.shivaji,
        "Quit India Movement" to R.drawable.quit_india,
        "Tirumala Temple History" to R.drawable.tirumala
    )

    val trendingItems = listOf(
        R.drawable.shivaji to "Shivaji Maharaj",
        R.drawable.quit_india to "Quit India Movement",
        R.drawable.tirumala to "Tirumala Temple History"
    )

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = { NavigationMenu(drawerState) }
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.TopStart
        ) {
            // Background Image with Dark Overlay
            Image(
                painter = painterResource(id = R.drawable.dash_bg),
                contentDescription = null,
                contentScale = ContentScale.FillBounds,
                modifier = Modifier.fillMaxSize()
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.Black.copy(alpha = 0.5f), Color.Transparent)
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                // Header Section with Menu Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { scope.launch { drawerState.open() } }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = "Menu",
                            tint = Color.White
                        )
                    }

                    Text(
                        text = "Dashboard",
                        fontSize = 32.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )

                    Image(
                        painter = painterResource(id = R.drawable.prof),
                        contentDescription = "Profile Picture",
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .clickable { onProfileClick() }
                            .border(2.dp, Color.White, CircleShape),
                        contentScale = ContentScale.Crop
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))
                Text(text = "Welcome back!", fontSize = 20.sp, color = Color.White, fontWeight = FontWeight.SemiBold)
                Text(text = "Explore historical events and experiences", fontSize = 16.sp, color = Color.LightGray)

                // Enhanced Search Section with Type Selection
                Spacer(modifier = Modifier.height(16.dp))

                // Search Type Chips
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 8.dp)
                ) {
                    SearchTypeChip(
                        label = "Events",
                        isSelected = selectedSearchType == "events",
                        onClick = { selectedSearchType = "events" }
                    )
                    SearchTypeChip(
                        label = "Years",
                        isSelected = selectedSearchType == "years",
                        onClick = { selectedSearchType = "years" }
                    )
                    SearchTypeChip(
                        label = "People",
                        isSelected = selectedSearchType == "people",
                        onClick = { selectedSearchType = "people" }
                    )
                    SearchTypeChip(
                        label = "Dates",
                        isSelected = selectedSearchType == "dates",
                        onClick = { selectedSearchType = "dates" }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Search Bar with dynamic placeholder
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = {
                        Text(
                            getSearchPlaceholder(selectedSearchType),
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    },
                    leadingIcon = {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_search),
                            contentDescription = "Search Icon",
                            tint = Color.White
                        )
                    },
                    trailingIcon = {
                        IconButton(onClick = {
                            if (searchQuery.isNotBlank()) {
                                val intent = Intent(context, ResultActivity::class.java).apply {
                                    putExtra("searchType", selectedSearchType)
                                    putExtra("searchQuery", searchQuery)
                                }
                                context.startActivity(intent)
                            }
                        }) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_search),
                                contentDescription = "Search",
                                tint = Color.White
                            )
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .background(Color.DarkGray.copy(alpha = 0.6f), shape = RoundedCornerShape(12.dp)),
                    shape = RoundedCornerShape(12.dp),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = Color.White,
                        unfocusedBorderColor = Color.Gray,
                        cursorColor = Color.White,
                    )
                )

                // Quick Search Suggestions based on selected type
                if (searchQuery.isNotBlank()) {
                    QuickSearchSuggestions(selectedSearchType, searchQuery, context)
                }

                val filteredRecommended = recommendedItems.filter {
                    it.first.contains(searchQuery, ignoreCase = true)
                }

                val filteredTrending = trendingItems.filter {
                    it.second.contains(searchQuery, ignoreCase = true)
                }

                // Trending Section
                Spacer(modifier = Modifier.height(20.dp))
                SectionTitle("🔥 Trending Now")
                HorizontalScrollableRow(filteredTrending)

                // Enhanced Explore Section
                Spacer(modifier = Modifier.height(20.dp))
                SectionTitle("📅 Quick Explore")
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(13.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        EraButton("Ancient", R.drawable.ancient_icon, "ancient")
                        EraButton("Medieval", R.drawable.medieval_icon, "medieval")
                    }
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(13.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        EraButton("Modern", R.drawable.ancient_icon, "modern") // Replace with modern icon
                        EraButton("Today in History", R.drawable.medieval_icon, "today") // Replace with calendar icon
                    }
                }

                // Recommended Section
                Spacer(modifier = Modifier.height(20.dp))
                SectionTitle("⭐ Recommended for You")
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    filteredRecommended.forEach { (title, imageRes) ->
                        HistoryItem(title, imageRes) {
                            // Handle click - search for this item
                            val intent = Intent(context, ResultActivity::class.java).apply {
                                putExtra("searchType", "events")
                                putExtra("searchQuery", title)
                            }
                            context.startActivity(intent)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(60.dp))
            }
        }
    }
}

// New Search Type Chip Component
@Composable
fun SearchTypeChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    FilterChip(
        onClick = onClick,
        label = { Text(label, fontSize = 14.sp) },
        selected = isSelected,
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = Color.White.copy(alpha = 0.9f),
            selectedLabelColor = Color.Black,
            containerColor = Color.Gray.copy(alpha = 0.6f),
            labelColor = Color.White
        )
    )
}

// Helper function for dynamic placeholders
fun getSearchPlaceholder(searchType: String): String {
    return when (searchType) {
        "events" -> "Search historical events..."
        "years" -> "Enter year (e.g., 1947)..."
        "people" -> "Search historical figures..."
        "dates" -> "Enter date (MM/DD)..."
        else -> "Search history..."
    }
}

// Quick Search Suggestions Component
@Composable
fun QuickSearchSuggestions(searchType: String, query: String, context: android.content.Context) {
    val suggestions = when (searchType) {
        "events" -> listOf("World War", "Independence", "Revolution", "Battle", "Discovery")
        "years" -> listOf("1947", "1857", "1526", "1192", "1206")
        "people" -> listOf("Gandhi", "Akbar", "Shivaji", "Nehru", "Tagore")
        "dates" -> listOf("08/15", "01/26", "10/02", "05/01", "11/14")
        else -> emptyList()
    }.filter { it.contains(query, ignoreCase = true) }

    if (suggestions.isNotEmpty()) {
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(suggestions.take(5)) { suggestion ->
                SuggestionChip(suggestion) {
                    val intent = Intent(context, ResultActivity::class.java).apply {
                        putExtra("searchType", searchType)
                        putExtra("searchQuery", suggestion)
                    }
                    context.startActivity(intent)
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
fun SuggestionChip(text: String, onClick: () -> Unit) {
    AssistChip(
        onClick = onClick,
        label = { Text(text, fontSize = 12.sp) },
        colors = AssistChipDefaults.assistChipColors(
            containerColor = Color.Blue.copy(alpha = 0.7f),
            labelColor = Color.White
        )
    )
}

// Reusable Section Title
@Composable
fun SectionTitle(title: String) {
    Text(
        text = title,
        fontSize = 22.sp,
        fontWeight = FontWeight.Bold,
        color = Color.White,
        modifier = Modifier.padding(vertical = 8.dp)
    )
}

// Trending Row
@Composable
fun HorizontalScrollableRow(filteredTrending: List<Pair<Int, String>>) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
    ) {
        ImageBox(R.drawable.shivaji)
        ImageBox(R.drawable.quit_india)
        ImageBox(R.drawable.tirumala)
    }
}

// Image Box Component
@Composable
fun ImageBox(imageRes: Int) {
    Box(
        modifier = Modifier
            .height(180.dp)
            .width(140.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color.DarkGray.copy(alpha = 0.8f))
    ) {
        Image(
            painter = painterResource(id = imageRes),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
    }
}

// Enhanced Era Selection Button
@Composable
fun EraButton(label: String, iconRes: Int, eraType: String) {
    val context = LocalContext.current

    Button(
        onClick = {
            val intent = Intent(context, ResultActivity::class.java).apply {
                putExtra("searchType", if (eraType == "today") "dates" else "people")
                putExtra("searchQuery", if (eraType == "today") getCurrentDate() else eraType)
                if (eraType != "today") putExtra("era", eraType)
            }
            context.startActivity(intent)
        },
        colors = ButtonDefaults.buttonColors(containerColor = Color.Gray.copy(alpha = 0.6f)),
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .height(50.dp)
            .padding(4.dp)
    ) {
        Icon(painter = painterResource(id = iconRes), contentDescription = null)
        Spacer(modifier = Modifier.width(8.dp))
        Text(label, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
    }
}

// Helper function to get current date
fun getCurrentDate(): String {
    val calendar = java.util.Calendar.getInstance()
    val month = String.format("%02d", calendar.get(java.util.Calendar.MONTH) + 1)
    val day = String.format("%02d", calendar.get(java.util.Calendar.DAY_OF_MONTH))
    return "$month/$day"
}

// Enhanced History Item List with click handler
@Composable
fun HistoryItem(title: String, imageRes: Int, onClick: () -> Unit = {}) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color.Black.copy(alpha = 0.8f))
            .clickable { onClick() }
            .padding(12.dp)
    ) {
        Image(
            painter = painterResource(id = imageRes),
            contentDescription = null,
            modifier = Modifier.size(40.dp),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(title, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun NavigationMenu(drawerState: DrawerState) {
    val scope = rememberCoroutineScope()

    ModalDrawerSheet {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Navigation", fontSize = 22.sp, fontWeight = FontWeight.Bold)

            NavigationItem("🏠 Home") { scope.launch { drawerState.close() } }
            NavigationItem("👤 Profile") { /* Navigate to Profile */ }
            NavigationItem("⚙️ Settings") { /* Navigate to Settings */ }
            NavigationItem("🚪 Logout") { /* Handle Logout */ }
        }
    }
}

@Composable
fun NavigationItem(label: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, fontSize = 18.sp, fontWeight = FontWeight.Medium)
    }
}