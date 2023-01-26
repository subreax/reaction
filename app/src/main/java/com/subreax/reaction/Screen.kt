package com.subreax.reaction

import androidx.navigation.NavType
import androidx.navigation.navArgument

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
}
