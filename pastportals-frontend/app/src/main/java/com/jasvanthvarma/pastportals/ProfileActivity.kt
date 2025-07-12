package com.jasvanthvarma.pastportals

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class RecentVideo(
    val title: String,
    val era: String,
    val date: String,
    val duration: String,
    val thumbnailRes: Int = R.drawable.prof
)

data class PreferredEra(
    val name: String,
    val icon: ImageVector,
    val isSelected: Boolean = false
)

class ProfileActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PastPortalsTheme {
                ProfileScreen()
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen() {
    val context = LocalContext.current
    var userName by remember { mutableStateOf("Jasvanth Varma") }
    var userEmail by remember { mutableStateOf("jasvanth@pastportals.com") }
    var joinDate by remember { mutableStateOf("Member since March 2024") }
    var totalVideos by remember { mutableStateOf(47) }
    var watchTime by remember { mutableStateOf("8.5 hours") }

    // Sample recent videos
    val recentVideos = remember {
        listOf(
            RecentVideo("The Fall of Constantinople", "Medieval Era", "2 days ago", "4:32"),
            RecentVideo("Leonardo da Vinci's Workshop", "Renaissance", "5 days ago", "6:18"),
            RecentVideo("Ancient Roman Gladiators", "Ancient Rome", "1 week ago", "5:45"),
            RecentVideo("Egyptian Pyramid Construction", "Ancient Egypt", "2 weeks ago", "7:22"),
            RecentVideo("Viking Expeditions", "Medieval Era", "3 weeks ago", "5:12")
        )
    }

    // Sample preferred eras
    var preferredEras by remember {
        mutableStateOf(listOf(
            PreferredEra("Ancient Egypt", Icons.Default.AccountBalance, true),
            PreferredEra("Medieval", Icons.Default.Castle, true),
            PreferredEra("Renaissance", Icons.Default.Palette, false),
            PreferredEra("Modern", Icons.Default.Public, false)
        ))
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0D1117), // Deep space dark
                        Color(0xFF1C2128), // Slightly lighter
                        Color(0xFF0D1117)  // Back to deep
                    )
                )
            )
    ) {
        item {
            // Top Bar with Back Button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { context.startActivity(Intent(context, DashboardActivity::class.java)) },
                    modifier = Modifier
                        .background(
                            Color(0xFF21262D),
                            CircleShape
                        )
                        .size(44.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color(0xFF8B7355)
                    )
                }

                Text(
                    text = "Profile",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFE6EDF3)
                )

                IconButton(
                    onClick = { /* Settings */ },
                    modifier = Modifier
                        .background(
                            Color(0xFF21262D),
                            CircleShape
                        )
                        .size(44.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = Color(0xFF8B7355)
                    )
                }
            }
        }

        item {
            // Profile Header Section
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .shadow(
                        elevation = 8.dp,
                        shape = RoundedCornerShape(20.dp)
                    ),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF1C2128)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Profile Picture with Vintage Border
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .background(
                                brush = Brush.radialGradient(
                                    colors = listOf(Color(0xFF8B7355), Color(0xFF5D4E37))
                                ),
                                shape = CircleShape
                            )
                            .padding(4.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.prof),
                            contentDescription = "Profile Picture",
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                                .clickable { /* Handle Profile Picture Change */ },
                            contentScale = ContentScale.Crop
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // User Info
                    Text(
                        text = userName,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFE6EDF3)
                    )

                    Text(
                        text = userEmail,
                        fontSize = 16.sp,
                        color = Color(0xFF8B949E)
                    )

                    Text(
                        text = joinDate,
                        fontSize = 14.sp,
                        color = Color(0xFF656D76)
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Stats Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        StatItem("Videos Created", totalVideos.toString())
                        StatItem("Watch Time", watchTime)
                        StatItem("Favorite Era", "Medieval")
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))

            // Action Buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ActionButton(
                    text = "Edit Profile",
                    icon = Icons.Default.Edit,
                    color = Color(0xFF8B7355),
                    modifier = Modifier.weight(1f)
                ) {
                    // Handle edit profile
                }

                ActionButton(
                    text = "Preferences",
                    icon = Icons.Default.Tune,
                    color = Color(0xFF4A90E2),
                    modifier = Modifier.weight(1f)
                ) {
                    // Handle preferences
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))

            // Preferred Eras Section
            Text(
                text = "Preferred Historical Eras",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFFE6EDF3),
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(preferredEras) { era ->
                    EraChip(
                        era = era,
                        onClick = {
                            preferredEras = preferredEras.map {
                                if (it.name == era.name) it.copy(isSelected = !it.isSelected)
                                else it
                            }
                        }
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))

            // Recent Videos Section
            Text(
                text = "Recently Generated Videos",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFFE6EDF3),
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))
        }

        items(recentVideos) { video ->
            VideoHistoryCard(
                video = video,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
            )
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))

            // Logout Button
            Button(
                onClick = {
                    context.startActivity(Intent(context, LoginActivity::class.java))
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFDC3545)
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Logout,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Logout",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun StatItem(label: String, value: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF8B7355)
        )
        Text(
            text = label,
            fontSize = 12.sp,
            color = Color(0xFF8B949E),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun ActionButton(
    text: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(48.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = color
        )
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun EraChip(
    era: PreferredEra,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .clickable { onClick() }
            .shadow(
                elevation = if (era.isSelected) 6.dp else 2.dp,
                shape = RoundedCornerShape(20.dp)
            ),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (era.isSelected) Color(0xFF8B7355) else Color(0xFF21262D)
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = era.icon,
                contentDescription = null,
                tint = if (era.isSelected) Color.White else Color(0xFF8B949E),
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = era.name,
                color = if (era.isSelected) Color.White else Color(0xFF8B949E),
                fontSize = 14.sp,
                fontWeight = if (era.isSelected) FontWeight.Medium else FontWeight.Normal
            )
        }
    }
}

@Composable
fun VideoHistoryCard(
    video: RecentVideo,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { /* Handle video click */ }
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(16.dp)
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1C2128)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Video Thumbnail
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF8B7355))
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Video Info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = video.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFFE6EDF3),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = video.era,
                        fontSize = 14.sp,
                        color = Color(0xFF8B7355)
                    )
                    Text(
                        text = " • ",
                        fontSize = 14.sp,
                        color = Color(0xFF656D76)
                    )
                    Text(
                        text = video.duration,
                        fontSize = 14.sp,
                        color = Color(0xFF8B949E)
                    )
                }

                Text(
                    text = video.date,
                    fontSize = 12.sp,
                    color = Color(0xFF656D76)
                )
            }

            // More Options
            IconButton(
                onClick = { /* Handle more options */ }
            ) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "More options",
                    tint = Color(0xFF8B949E)
                )
            }
        }
    }
}