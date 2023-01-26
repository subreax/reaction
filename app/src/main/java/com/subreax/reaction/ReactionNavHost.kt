package com.subreax.reaction

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.subreax.reaction.data.AppContainer
import com.subreax.reaction.ui.*
import com.subreax.reaction.ui.chat.ChatScreen
import com.subreax.reaction.ui.chat.ChatViewModel
import com.subreax.reaction.ui.home.HomeScreen
import com.subreax.reaction.ui.home.HomeViewModel
import com.subreax.reaction.ui.signin.SignInScreen
import com.subreax.reaction.ui.signin.SignInViewModel
import com.subreax.reaction.ui.signup.SignUpScreen
import com.subreax.reaction.ui.signup.SignUpViewModel

@Composable
fun ReactionNavHost(
    //statusBarHeight: Dp,
    navController: NavHostController,
    appContainer: AppContainer,
    startDestination: Screen
) {
    NavHost(
        navController = navController,
        startDestination = startDestination.route
    ) {
        composable(Screen.Welcome.route) {
            WelcomeScreen(
                onClickSignIn = {
                    navController.navigate(Screen.SignIn.route)
                },
                onClickSignUp = {
                    navController.navigate(Screen.SignUp.route)
                }
            )
        }

        composable(Screen.SignIn.route) {
            SignInScreen(
                signInViewModel = viewModel(
                    factory = SignInViewModel.Factory(appContainer.authRepository)
                ),
                onBackPressed = {
                    navController.popBackStack()
                },
                onSignInDone = {
                    navController.navigateToHomeScreen()
                }
            )
        }

        composable(Screen.SignUp.route) {
            SignUpScreen(
                signUpViewModel = viewModel(
                    factory = SignUpViewModel.Factory(appContainer.authRepository)
                ),
                onBackPressed = {
                    navController.popBackStack()
                },
                onSignUpDone = {
                    navController.navigateToHomeScreen()
                }
            )
        }

        composable(Screen.Home.route) {
            HomeScreen(
                //statusBarHeight = statusBarHeight,
                viewModel = viewModel(
                    factory = HomeViewModel.Factory(appContainer.chatRepository)
                ),
                onChatClicked = { chat ->
                    navController.navigate(
                        "${Screen.Chat.route}/${chat.id}"
                    )
                }
            )
        }

        composable(
            route = Screen.Chat.routeWithArgs,
            arguments = Screen.Chat.args
        ) { navBackStackEntry ->
            val userId = appContainer.authRepository.getUserId()
            //Log.d("ReactionNavHost", "userId: $userId")
            val chatId = navBackStackEntry.arguments?.getString(Screen.Chat.chatIdArg) ?: ""

            ChatScreen(
                viewModel = viewModel(
                    factory = ChatViewModel.Factory(userId, chatId, appContainer.chatRepository)
                ),
                onBackPressed = {
                    navController.popBackStack()
                }
            )
        }
    }
}


private fun NavController.navigateToHomeScreen() {
    navigate(Screen.Home.route) {
        popUpTo(Screen.Welcome.route) {
            inclusive = true
        }
    }
}