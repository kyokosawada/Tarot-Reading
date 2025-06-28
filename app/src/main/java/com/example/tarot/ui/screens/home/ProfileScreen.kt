package com.example.tarot.ui.screens.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tarot.R
import com.example.tarot.ui.theme.TarotTheme
import com.example.tarot.viewmodel.AuthViewModel
import com.example.tarot.viewmodel.User

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    authViewModel: AuthViewModel,
    onBackClick: () -> Unit = {},
    onEditProfileClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
    onLogoutClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val authUiState by authViewModel.uiState.collectAsState()
    val user = authUiState.user

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onEditProfileClick) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Profile"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        if (user != null) {
            LazyColumn(
                modifier = modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    ProfileHeader(user = user)
                }

                item {
                    StatsSection()
                }

                item {
                    PreferencesSection(
                        onSettingsClick = onSettingsClick
                    )
                }

                item {
                    AccountSection(
                        onLogoutClick = onLogoutClick,
                        isLoading = authUiState.isLoading
                    )
                }
            }
        } else {
            // Loading or error state
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
    }
}

@Composable
fun ProfileHeader(
    user: User,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Profile Avatar using the added avatar.png
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.avatar),
                    contentDescription = "Profile Avatar",
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))

            // Display name - prefer username over name if available
            Text(
                text = user.username?.takeIf { it.isNotBlank() }
                    ?: user.name.takeIf { it.isNotBlank() } ?: "Mystic Reader",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )

            // Display email
            Text(
                text = user.email,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
            )
            
            Spacer(modifier = Modifier.height(8.dp))

            // Display join date if available
            val joinDate = user.createdAt?.let { timestamp ->
                val date = java.util.Date(timestamp)
                val formatter =
                    java.text.SimpleDateFormat("MMMM yyyy", java.util.Locale.getDefault())
                "Joined ${formatter.format(date)}"
            } ?: "Mystic Reader Member"

            Text(
                text = joinDate,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
fun StatsSection(
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "Your Journey",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(
                    label = "Readings",
                    value = "47",
                    emoji = "📚"
                )
                
                StatItem(
                    label = "Streak",
                    value = "12 days",
                    emoji = "🔥"
                )
                
                StatItem(
                    label = "Level",
                    value = "Mystic",
                    emoji = "⭐"
                )
            }
        }
    }
}

@Composable
fun StatItem(
    label: String,
    value: String,
    emoji: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = emoji,
            fontSize = 24.sp,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun PreferencesSection(
    onSettingsClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "Preferences",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            PreferenceItem(
                icon = Icons.Default.Notifications,
                title = "Notifications",
                subtitle = "Daily readings and insights",
                onClick = { }
            )
            
            PreferenceItem(
                icon = Icons.AutoMirrored.Filled.List,
                title = "Reading History",
                subtitle = "View your past readings",
                onClick = { }
            )
            
            PreferenceItem(
                icon = Icons.Default.Settings,
                title = "App Settings",
                subtitle = "Customize your experience",
                onClick = onSettingsClick
            )
        }
    }
}

@Composable
fun AccountSection(
    onLogoutClick: () -> Unit = {},
    modifier: Modifier = Modifier,
    isLoading: Boolean = false
) {
    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "Account",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            PreferenceItem(
                icon = Icons.Default.Person,
                title = "Edit Profile",
                subtitle = "Update your information",
                onClick = { }
            )
            
            PreferenceItem(
                icon = Icons.Default.Email,
                title = "Contact Support",
                subtitle = "Get help with your account",
                onClick = { }
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Button(
                onClick = onLogoutClick,
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onError,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                        contentDescription = null,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text("Sign Out")
                }
            }
        }
    }
}

@Composable
fun PreferenceItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ProfileScreenPreview() {
    TarotTheme {
        // Mock user data for preview
        val mockUser = User(
            id = "preview_id",
            name = "Mystic Reader",
            email = "mystic@example.com",
            username = "mystic_seeker",
            birthMonth = 3,
            birthYear = 1995,
            isProfileComplete = true,
            createdAt = System.currentTimeMillis() - (30L * 24 * 60 * 60 * 1000) // 30 days ago
        )
        ProfileHeader(user = mockUser)
    }
}

@Preview(showBackground = true, name = "Profile Screen - Dark Theme")
@Composable
fun ProfileScreenDarkPreview() {
    TarotTheme(darkTheme = true) {
        // Mock user data for preview
        val mockUser = User(
            id = "preview_id",
            name = "Mystic Reader",
            email = "mystic@example.com",
            username = "mystic_seeker",
            birthMonth = 10,
            birthYear = 1990,
            isProfileComplete = true,
            createdAt = System.currentTimeMillis() - (90L * 24 * 60 * 60 * 1000) // 90 days ago
        )
        ProfileHeader(user = mockUser)
    }
}

@Preview(showBackground = true, name = "Profile Header")
@Composable
fun ProfileHeaderPreview() {
    TarotTheme {
        // Mock user data for preview
        val mockUser = User(
            id = "preview_id",
            name = "Luna Starlight",
            email = "luna.starlight@example.com",
            username = "luna_mystic",
            birthMonth = 7,
            birthYear = 1992,
            isProfileComplete = true,
            createdAt = 1640995200000L // January 1, 2022
        )
        ProfileHeader(user = mockUser)
    }
}

@Preview(showBackground = true, name = "Stats Section")
@Composable
fun StatsSectionPreview() {
    TarotTheme {
        StatsSection()
    }
}

@Preview(showBackground = true, name = "Preferences Section")
@Composable
fun PreferencesSectionPreview() {
    TarotTheme {
        PreferencesSection()
    }
}

@Preview(showBackground = true, name = "Account Section")
@Composable
fun AccountSectionPreview() {
    TarotTheme {
        AccountSection(isLoading = false)
    }
}