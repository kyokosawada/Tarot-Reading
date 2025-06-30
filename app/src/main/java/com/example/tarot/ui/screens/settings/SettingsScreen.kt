package com.example.tarot.ui.screens.settings

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.tarot.ui.theme.MysticGold
import com.example.tarot.ui.theme.MysticSilver
import com.example.tarot.ui.theme.TarotTheme
import com.example.tarot.viewmodel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBackClick: () -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("App Settings") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                TarotSettingsSection(
                    allowReversedCards = uiState.allowReversedCards,
                    isLoading = uiState.isLoading,
                    onToggleReversedCards = { viewModel.toggleReversedCards() }
                )
            }

            // Future settings sections can be added here
            // item { NotificationSettingsSection() }
            // item { ThemeSettingsSection() }
        }
    }
}

@Composable
fun TarotSettingsSection(
    allowReversedCards: Boolean,
    isLoading: Boolean,
    onToggleReversedCards: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(20.dp)
                .clip(RoundedCornerShape(8.dp))
        ) {
            Text(
                text = "Tarot Reading Preferences",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Reversed Cards Setting
            SettingItem(
                title = "Allow Reversed Cards",
                subtitle = "Include reversed card meanings in readings",
                isEnabled = allowReversedCards,
                isLoading = isLoading,
                onToggle = onToggleReversedCards
            )
        }
    }
}

@Composable
fun SettingItem(
    title: String,
    subtitle: String,
    isEnabled: Boolean,
    isLoading: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Animate colors based on the actual enabled state
    val animatedProgress by animateFloatAsState(
        targetValue = if (isEnabled) 1f else 0f,
        animationSpec = tween(
            durationMillis = 300,
            delayMillis = 0
        ),
        label = "switch_color_animation"
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp)),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 12.sp
            )
        }

        AnimatedContent(
            targetState = isLoading,
            transitionSpec = {
                fadeIn(animationSpec = tween(150, 150)) togetherWith
                        fadeOut(animationSpec = tween(150))
            },
            label = "loading_switch_transition"
        ) { loading ->
            if (loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.dp,
                    color = MysticGold
                )
            } else {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .padding(4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Switch(
                        checked = isEnabled,
                        onCheckedChange = { onToggle() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = androidx.compose.ui.graphics.lerp(
                                MysticSilver,
                                MysticGold,
                                animatedProgress
                            ),
                            checkedTrackColor = androidx.compose.ui.graphics.lerp(
                                MysticSilver.copy(alpha = 0.4f),
                                MysticGold.copy(alpha = 0.6f),
                                animatedProgress
                            ),
                            uncheckedThumbColor = androidx.compose.ui.graphics.lerp(
                                MysticSilver,
                                MysticGold,
                                animatedProgress
                            ),
                            uncheckedTrackColor = androidx.compose.ui.graphics.lerp(
                                MysticSilver.copy(alpha = 0.4f),
                                MysticGold.copy(alpha = 0.6f),
                                animatedProgress
                            )
                        )
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SettingsScreenPreview() {
    TarotTheme {
        TarotSettingsSection(
            allowReversedCards = true,
            isLoading = false,
            onToggleReversedCards = {}
        )
    }
}

@Preview(showBackground = true, name = "Settings Loading")
@Composable
fun SettingsScreenLoadingPreview() {
    TarotTheme {
        TarotSettingsSection(
            allowReversedCards = false,
            isLoading = true,
            onToggleReversedCards = {}
        )
    }
}