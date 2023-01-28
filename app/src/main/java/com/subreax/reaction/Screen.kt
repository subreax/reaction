package com.subreax.reaction

import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink

sealed class Screen(val route: String) {
    object Welcome : Screen("welcome_screen")
    object SignIn : Screen("sign_in_screen")
    object SignUp : Screen("sign_up_screen")
    object Home : Screen("home_screen")
    object Chat : Screen("chat_screen") {
        val chatIdArg = "chatId"
        val routeWithArgs = "$route/{$chatIdArg}"
        val args = listOf(
            navArgument(chatIdArg) { type = NavType.StringType }
        )
    }
    object JoinChat : Screen("join_chat") {
        val chatIdArg = "chatId"
        val routeWithArgs = "$route/{$chatIdArg}"
        val deepLinks = listOf(
            navDeepLink { uriPattern = "reaction://join/{$chatIdArg}" }
        )
    }
}
