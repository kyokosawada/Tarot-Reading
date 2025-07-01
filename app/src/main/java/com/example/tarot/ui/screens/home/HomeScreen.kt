package com.example.tarot.ui.screens.home

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tarot.ui.theme.MysticCosmic
import com.example.tarot.ui.theme.MysticDarkBlue
import com.example.tarot.ui.theme.MysticGold
import com.example.tarot.ui.theme.MysticNavy
import com.example.tarot.ui.theme.MysticPurple
import com.example.tarot.ui.theme.MysticSilver
import com.example.tarot.ui.theme.MysticaGradientEnd
import com.example.tarot.ui.theme.MysticaGradientMid
import com.example.tarot.ui.theme.MysticaGradientStart
import com.example.tarot.ui.theme.TarotTheme
import com.example.tarot.ui.theme.TextAccent
import com.example.tarot.ui.theme.TextPrimary
import com.example.tarot.ui.theme.TextSecondary
import com.example.tarot.util.ImageResourceMapper
import com.example.tarot.viewmodel.AuthViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    authViewModel: AuthViewModel? = null,
    onNavigateToReading: (String) -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    // Simple auth data refresh when HomeScreen loads
    LaunchedEffect(Unit) {
        try {
            authViewModel?.refreshUserData()
        } catch (e: Exception) {
            // Log error but don't crash the app
            android.util.Log.e("HomeScreen", "Error refreshing user data", e)
        }
    }

    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            MysticNavigationDrawer(
                onNavigateToProfile = {
                    scope.launch { drawerState.close() }
                    onNavigateToProfile()
                }
            )
        }
    ) {
        androidx.compose.material3.Scaffold(
            modifier = modifier.fillMaxSize(),
            topBar = {
                androidx.compose.material3.TopAppBar(
                    title = {
                        Text(
                            text = "MYSTICA",
                            color = TextAccent,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 2.sp
                        )
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = {
                                scope.launch {
                                    if (drawerState.isClosed) {
                                        drawerState.open()
                                    } else {
                                        drawerState.close()
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "Open navigation menu",
                                tint = TextPrimary
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = onNavigateToProfile) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Go to profile",
                                tint = TextPrimary
                            )
                        }
                    },
                    colors = androidx.compose.material3.TopAppBarDefaults.topAppBarColors(
                        containerColor = MysticDarkBlue,
                        titleContentColor = TextAccent,
                        navigationIconContentColor = TextPrimary,
                        actionIconContentColor = TextPrimary
                    )
                )
            },
            containerColor = MysticDarkBlue
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                MysticaGradientStart,
                                MysticaGradientMid,
                                MysticaGradientEnd
                            )
                        )
                    )
            ) {
                // Main Content
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(32.dp))

                    // Subtitle
                    Text(
                        text = "Tarot & Divination",
                        fontSize = 14.sp,
                        color = TextSecondary,
                        letterSpacing = 2.sp,
                        modifier = Modifier.padding(bottom = 24.dp)
                    )

                    // Central Tarot Card
                    MysticTarotCard()

                    Spacer(modifier = Modifier.height(48.dp))

                    // Reading Options
                    ReadingOptions(onNavigateToReading = onNavigateToReading)

                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}

@Composable
fun MysticTarotCard() {
    val infiniteTransition = rememberInfiniteTransition()
    val floatAnim by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 10f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = androidx.compose.animation.core.FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )
    val rotateAnim by infiniteTransition.animateFloat(
        initialValue = -3f,
        targetValue = 3f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = androidx.compose.animation.core.LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )
    val scaleAnim by infiniteTransition.animateFloat(
        initialValue = 0.98f,
        targetValue = 1.02f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = androidx.compose.animation.core.FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Box(
        modifier = Modifier
            .size(width = 200.dp, height = 320.dp)
            .offset(y = floatAnim.dp)
            .rotate(rotateAnim)
            .scale(scaleAnim)
            .clip(RoundedCornerShape(16.dp)),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(ImageResourceMapper.getCardBackResource()),
            contentDescription = "Tarot Card Back",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
    }
}

@Composable
fun ReadingOptions(
    onNavigateToReading: (String) -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Daily Reading - Primary button
        Button(
            onClick = { onNavigateToReading("daily") },
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MysticGold,
                contentColor = MysticDarkBlue
            ),
            shape = RoundedCornerShape(28.dp)
        ) {
            Text(
                text = "Daily Reading",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        // Ask a Question
        OutlinedButton(
            onClick = { onNavigateToReading("question") },
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .height(56.dp),
            border = ButtonDefaults.outlinedButtonBorder.copy(
                brush = Brush.horizontalGradient(
                    colors = listOf(MysticGold, MysticSilver)
                )
            ),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = TextPrimary
            ),
            shape = RoundedCornerShape(28.dp)
        ) {
            Text(
                text = "Ask a Question",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        }

        // Palm Reading
        OutlinedButton(
            onClick = { onNavigateToReading("palm") },
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .height(56.dp),
            border = ButtonDefaults.outlinedButtonBorder.copy(
                brush = Brush.horizontalGradient(
                    colors = listOf(MysticPurple, MysticCosmic)
                )
            ),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = TextPrimary
            ),
            shape = RoundedCornerShape(28.dp)
        ) {
            Text(
                text = "Palm Reading",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun MysticNavigationDrawer(
    onNavigateToProfile: () -> Unit,
    modifier: Modifier = Modifier
) {
    ModalDrawerSheet(
        modifier = modifier.width(280.dp),
        drawerContainerColor = MysticNavy,
        drawerContentColor = TextPrimary
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                MysticPurple,
                                MysticCosmic
                            )
                        )
                    )
                    .padding(16.dp)
            ) {
                androidx.compose.foundation.layout.Row(
                    modifier = Modifier
                        .fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Logo on the left
                    Image(
                        painter = painterResource(id = com.example.tarot.R.drawable.mystica_logo),
                        contentDescription = "Mystica Logo",
                        modifier = Modifier.size(80.dp)
                    )

                    // Text on the right
                    Column(
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "MYSTICA",
                            fontWeight = FontWeight.Bold,
                            fontSize = 24.sp,
                            letterSpacing = 2.sp,
                            color = TextAccent,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )

                        Text(
                            text = "TAROT & DIVINATION",
                            fontWeight = FontWeight.Medium,
                            fontSize = 11.sp,
                            letterSpacing = 1.sp,
                            color = MysticGold.copy(alpha = 0.8f),
                            maxLines = 1
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Navigation Items
            MysticNavigationItem(
                text = "Profile",
                onClick = onNavigateToProfile
            )

            MysticNavigationItem(
                text = "Settings",
                onClick = { }
            )
        }
    }
}

@Composable
fun MysticNavigationItem(
    text: String,
    onClick: () -> Unit
) {
    TextButton(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = ButtonDefaults.textButtonColors(
            contentColor = TextPrimary
        )
    ) {
        Text(
            text = text,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Start,
            fontSize = 16.sp
        )
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    TarotTheme {
        HomeScreen()
    }
}

@Preview(showBackground = true, name = "Mystic Tarot Card")
@Composable
fun MysticTarotCardPreview() {
    TarotTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MysticDarkBlue),
            contentAlignment = Alignment.Center
        ) {
            MysticTarotCard()
        }
    }
}
