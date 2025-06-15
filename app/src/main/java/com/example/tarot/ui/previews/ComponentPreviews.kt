package com.example.tarot.ui.previews

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.tarot.ui.screens.home.DailyInsightCard
import com.example.tarot.ui.screens.home.ProfileHeader
import com.example.tarot.ui.screens.home.ReadingType
import com.example.tarot.ui.screens.home.ReadingTypeCard
import com.example.tarot.ui.screens.home.StatsSection
import com.example.tarot.ui.screens.home.WelcomeCard
import com.example.tarot.ui.theme.TarotTheme

@Preview(showBackground = true, name = "All Components - Light Theme")
@Composable
fun AllComponentsPreview() {
    TarotTheme {
        LazyColumn(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = "Tarot App Components",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            item {
                Column {
                    Text(
                        text = "Welcome Card",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    WelcomeCard()
                }
            }

            item {
                Column {
                    Text(
                        text = "Daily Insight Card",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    DailyInsightCard()
                }
            }

            item {
                Column {
                    Text(
                        text = "Profile Header",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    ProfileHeader()
                }
            }

            item {
                Column {
                    Text(
                        text = "Stats Section",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    StatsSection()
                }
            }

            items(sampleReadingTypes) { readingType ->
                Column {
                    Text(
                        text = "Reading Type: ${readingType.title}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    ReadingTypeCard(
                        readingType = readingType,
                        onClick = {}
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, name = "All Components - Dark Theme")
@Composable
fun AllComponentsDarkPreview() {
    TarotTheme(darkTheme = true) {
        LazyColumn(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = "Tarot App Components (Dark)",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            item {
                WelcomeCard()
            }

            item {
                DailyInsightCard()
            }

            item {
                ProfileHeader()
            }
        }
    }
}

private val sampleReadingTypes = listOf(
    ReadingType(
        type = "love",
        title = "Love & Relationships",
        description = "Explore matters of the heart",
        emoji = "💕"
    ),
    ReadingType(
        type = "career",
        title = "Career & Money",
        description = "Guidance for your professional path",
        emoji = "💼"
    ),
    ReadingType(
        type = "spiritual",
        title = "Spiritual Growth",
        description = "Connect with your inner self",
        emoji = "🧘"
    )
)