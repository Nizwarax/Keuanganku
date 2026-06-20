package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.data.KeuanganDatabase
import com.example.data.KeuanganRepository
import com.example.ui.KeuanganViewModel
import com.example.ui.KeuanganViewModelFactory
import com.example.ui.screens.LoginRegisterScreen
import com.example.ui.screens.MainContainer
import com.example.ui.screens.OnboardingScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Safely initialize the database, repository & viewmodel factory
        val database = KeuanganDatabase.getDatabase(applicationContext, lifecycleScope)
        val repository = KeuanganRepository(database.dao())
        val factory = KeuanganViewModelFactory(application, repository)
        val viewModel = ViewModelProvider(this, factory)[KeuanganViewModel::class.java]

        setContent {
            // Observe live user settings (including isDarkMode) inside the database
            val currentUserState by viewModel.currentUser.collectAsState(initial = null)
            val isDarkTheme = currentUserState?.isDarkMode ?: false

            MyApplicationTheme(darkTheme = isDarkTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize()
                ) {
                    var currentScreen by remember { mutableStateOf("splash") }

                    Crossfade(
                        targetState = currentScreen,
                        label = "AppScreenNavigation"
                    ) { screen ->
                        when (screen) {
                            "splash" -> SplashScreen(
                                onNavigateNext = {
                                    currentScreen = "onboarding"
                                }
                            )
                            "onboarding" -> OnboardingScreen(
                                onGetStarted = {
                                    currentScreen = "login"
                                }
                            )
                            "login" -> LoginRegisterScreen(
                                viewModel = viewModel,
                                onLoginSuccess = {
                                    currentScreen = "main"
                                }
                            )
                            "main" -> MainContainer(
                                viewModel = viewModel,
                                onLogoutRequested = {
                                    currentScreen = "login"
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
