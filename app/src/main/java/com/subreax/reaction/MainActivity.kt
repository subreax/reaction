package com.subreax.reaction

import android.content.res.Resources
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.graphics.Color
import androidx.core.view.WindowCompat
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.subreax.reaction.data.SocketService
import com.subreax.reaction.ui.theme.ReactionTheme
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking


class MainActivity : ComponentActivity() {
    private lateinit var navController: NavHostController

    //private var statusBarHeight by mutableStateOf(0)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        /*window.decorView.setOnApplyWindowInsetsListener { view, insets ->
            val h = toDp(insets.systemWindowInsetTop)
            statusBarHeight = h
            Log.d("MainActivity", "statusBar: $h")
            insets
        }*/

        val appContainer = (application as ReactionApplication).appContainer

        if (savedInstanceState == null) {
            runBlocking {
                appContainer.authRepository.init(applicationContext)
            }
        }

        setContent {
            val systemUiController = rememberSystemUiController()
            val useDarkIcons = !isSystemInDarkTheme()
            DisposableEffect(systemUiController, useDarkIcons) {
                systemUiController.setSystemBarsColor(
                    color = Color.Transparent,
                    darkIcons = useDarkIcons
                )
                onDispose { }
            }


            navController = rememberNavController()

            ReactionTheme {
                ReactionNavHost(
                    navController = navController,
                    appContainer = appContainer,
                    startDestination = if (appContainer.authRepository.isSignedIn()) Screen.Home else Screen.Welcome
                )
            }
        }
    }


    private fun toDp(px: Int): Int {
        return (px / Resources.getSystem().displayMetrics.density).toInt()
    }
}
