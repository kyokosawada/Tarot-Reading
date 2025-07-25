package com.example.tarot

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.tarot.navigation.TarotNavigation
import com.example.tarot.ui.theme.TarotTheme
import com.example.tarot.viewmodel.AuthViewModel
import org.koin.androidx.compose.koinViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TarotTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    TarotApp()
                }
            }
        }
    }
}

@Composable
fun TarotApp() {
    val authViewModel: AuthViewModel = koinViewModel()

    TarotNavigation(
        authViewModel = authViewModel
    )
}
