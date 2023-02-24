package com.subreax.reaction

import android.os.Build
import android.os.Bundle
import android.view.WindowInsets
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.subreax.reaction.ui.LocalStatusBarPadding
import com.subreax.reaction.ui.theme.ReactionTheme
import com.subreax.reaction.utils.toDp


class MainActivity : ComponentActivity() {
    private lateinit var navController: NavHostController
    private var statusBarPadding by mutableStateOf(PaddingValues(0.dp))

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        listenForStatusBarHeight { height ->
            statusBarPadding = PaddingValues(top = height)
        }

        val appContainer = (application as ReactionApplication).appContainer

        setContent {
            configureStatusBar()
            navController = rememberNavController()

            ReactionTheme {
                CompositionLocalProvider(LocalStatusBarPadding provides statusBarPadding) {
                    ReactionNavHost(
                        navController = navController,
                        appContainer = appContainer,
                        startDestination = if (appContainer.authRepository.isSignedIn()) Screen.Home else Screen.Welcome
                    )
                }
            }
        }
    }

    private fun listenForStatusBarHeight(onValue: (Dp) -> Unit) {
        window.decorView.setOnApplyWindowInsetsListener { view, insets ->
            val h = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                insets.getInsets(WindowInsets.Type.statusBars()).top.toDp()
            } else {
                insets.systemWindowInsetTop.toDp()
            }
            onValue(h)
            insets
        }
    }

    @Composable
    private fun configureStatusBar() {
        val systemUiController = rememberSystemUiController()
        val useDarkIcons = !isSystemInDarkTheme()
        DisposableEffect(systemUiController, useDarkIcons) {
            systemUiController.setSystemBarsColor(
                color = Color.Transparent,
                darkIcons = useDarkIcons
            )
            onDispose { }
        }
    }
}
