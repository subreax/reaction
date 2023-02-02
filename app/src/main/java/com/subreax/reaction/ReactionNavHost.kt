package com.subreax.reaction

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.subreax.reaction.data.AppContainer
import com.subreax.reaction.ui.WelcomeScreen
import com.subreax.reaction.ui.chat.ChatScreen
import com.subreax.reaction.ui.chat.ChatViewModel
import com.subreax.reaction.ui.chatdetails.ChatDetailsScreen
import com.subreax.reaction.ui.chatdetails.ChatDetailsViewModel
import com.subreax.reaction.ui.chatshare.ChatShareScreen
import com.subreax.reaction.ui.chatshare.ChatShareViewModel
import com.subreax.reaction.ui.home.HomeScreen
import com.subreax.reaction.ui.home.HomeViewModel
import com.subreax.reaction.ui.joinchat.JoinChatScreen
import com.subreax.reaction.ui.joinchat.JoinChatViewModel
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
                    factory = ChatViewModel.Factory(
                        userId,
                        chatId,
                        navToDetailsScreen = {
                            navController.navigate(
                                "${Screen.ChatDetails.route}/${chatId}"
                            )
                        },
                        appContainer.chatRepository
                    )
                ),
                // todo: fix back nav button behaviour to this
                onBackPressed = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(navController.currentDestination?.route!!) {
                            inclusive = true
                        }
                    }
                }
            )
        }

        composable(
            route = Screen.JoinChat.routeWithArgs,
            deepLinks = Screen.JoinChat.deepLinks
        ) { navBackStackEntry ->
            val chatId = navBackStackEntry.arguments?.getString(Screen.JoinChat.chatIdArg) ?: "room_id_is_empty"
            JoinChatScreen(
                joinChatViewModel = viewModel(
                    factory = JoinChatViewModel.Factory(
                        chatId = chatId,
                        chatRepository = appContainer.chatRepository,
                        navHome = {
                            navController.navigateToHomeScreen()
                        },
                        navToChat = { _chatId ->
                            val currentRoute = navController.currentDestination?.route ?: Screen.Welcome.route
                            navController.navigate("${Screen.Chat.route}/$_chatId") {
                                popUpTo(currentRoute) { inclusive = true }
                            }
                        }
                    )
                )
            )
        }

        composable(
            route = Screen.ChatDetails.routeWithArgs,
            arguments = Screen.ChatDetails.args
        ) { navBackStackEntry ->
            val chatId = navBackStackEntry.arguments?.getString(Screen.ChatDetails.chatIdArg) ?: "chat_id_is_empty"

            ChatDetailsScreen(
                chatDetailsViewModel = viewModel(
                    factory = ChatDetailsViewModel.Factory(
                        chatId = chatId,
                        navBack = { navController.popBackStack() },
                        navToChatSharing = {
                            navController.navigate("${Screen.ChatShare.route}/$chatId")
                        },
                        navToChatEditor = { },
                        appContainer.chatRepository
                    )
                )
            )
        }

        composable(
            route = Screen.ChatShare.routeWithArgs,
            arguments = Screen.ChatShare.args
        ) { navBackStackEntry ->
            val args = navBackStackEntry.arguments!!
            val chatId = args.getString(Screen.ChatShare.chatIdArg) ?: ""

            ChatShareScreen(
                viewModel(
                    factory = ChatShareViewModel.Factory(
                        chatId,
                        appContainer.chatRepository
                    )
                ),
                //colors = listOf(Color(0xFF79EB71), Color(0xFF257AC2)),
                onBackPressed = { navController.popBackStack() }
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