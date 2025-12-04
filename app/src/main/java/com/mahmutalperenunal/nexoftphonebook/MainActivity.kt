package com.mahmutalperenunal.nexoftphonebook

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.mahmutalperenunal.nexoftphonebook.presentation.contacts.ContactsEvent
import com.mahmutalperenunal.nexoftphonebook.presentation.contacts.ContactsScreen
import com.mahmutalperenunal.nexoftphonebook.presentation.contacts.ContactsViewModel
import com.mahmutalperenunal.nexoftphonebook.presentation.detail.ContactDetailScreen
import com.mahmutalperenunal.nexoftphonebook.presentation.detail.ContactDetailViewModel
import com.mahmutalperenunal.nexoftphonebook.presentation.common.ProvideAppImageLoader
import com.mahmutalperenunal.nexoftphonebook.ui.theme.NexoftPhoneBookTheme


// Main activity hosting navigation and screens for the phone book app
class MainActivity : ComponentActivity() {

    private val appContainer: AppContainer by lazy {
        (application as NexoftPhoneBookApp).appContainer
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen()
        setContent {
            ProvideAppImageLoader {
                NexoftPhoneBookTheme {
                    SetupSystemBars()
                    val navController = rememberNavController()

                    NavHost(
                        navController = navController,
                        startDestination = "contacts"
                    ) {
                        composable("contacts") {
                            val contactsViewModel: ContactsViewModel = viewModel(
                                factory = ContactsViewModel.ContactsViewModelFactory(
                                    getContactsUseCase = appContainer.getContactsUseCase,
                                    searchContactsUseCase = appContainer.searchContactsUseCase,
                                    deleteContactUseCase = appContainer.deleteContactUseCase,
                                    getSearchHistoryUseCase = appContainer.getSearchHistoryUseCase,
                                    saveSearchQueryUseCase = appContainer.saveSearchQueryUseCase,
                                    deleteSearchHistoryItemUseCase = appContainer.deleteSearchHistoryItemUseCase,
                                    clearSearchHistoryUseCase = appContainer.clearSearchHistoryUseCase
                                )
                            )
                            val state by contactsViewModel.state.collectAsStateWithLifecycle()

                            ContactsScreen(
                                state = state,
                                onEvent = { event ->
                                    when (event) {
                                        is ContactsEvent.OnAddContactClick -> {
                                            navController.navigate("contact/new")
                                        }

                                        is ContactsEvent.OnContactClick -> {
                                            navController.navigate("contact/${event.contactId}?edit=false")
                                        }

                                        is ContactsEvent.OnEditClick -> {
                                            navController.navigate("contact/${event.contactId}?edit=true")
                                        }

                                        else -> contactsViewModel.onEvent(event)
                                    }
                                }
                            )
                        }

                        composable("contact/new") {
                            val vm: ContactDetailViewModel = viewModel(
                                factory = ContactDetailViewModel.ContactDetailViewModelFactory(
                                    isNewContact = true,
                                    contactId = null,
                                    startInEditMode = true,
                                    getContactDetailUseCase = appContainer.getContactDetailUseCase,
                                    upsertContactUseCase = appContainer.upsertContactUseCase,
                                    deleteContactUseCase = appContainer.deleteContactUseCase,
                                    saveContactToDeviceUseCase = appContainer.saveContactToDeviceUseCase,
                                    uploadProfileImageUseCase = appContainer.uploadProfileImageUseCase
                                )
                            )
                            val state by vm.state.collectAsStateWithLifecycle()

                            ContactDetailScreen(
                                state = state,
                                onEvent = vm::onEvent,
                                onBack = { navController.popBackStack() }
                            )
                        }

                        composable(
                            route = "contact/{id}?edit={edit}",
                            arguments = listOf(
                                navArgument("id") { type = NavType.StringType },
                                navArgument("edit") {
                                    type = NavType.BoolType
                                    defaultValue = false
                                }
                            )
                        ) { backStackEntry ->
                            val id = backStackEntry.arguments?.getString("id")
                            val startInEdit = backStackEntry.arguments?.getBoolean("edit") ?: false

                            val vm: ContactDetailViewModel = viewModel(
                                factory = ContactDetailViewModel.ContactDetailViewModelFactory(
                                    isNewContact = false,
                                    contactId = id,
                                    startInEditMode = startInEdit,
                                    getContactDetailUseCase = appContainer.getContactDetailUseCase,
                                    upsertContactUseCase = appContainer.upsertContactUseCase,
                                    deleteContactUseCase = appContainer.deleteContactUseCase,
                                    saveContactToDeviceUseCase = appContainer.saveContactToDeviceUseCase,
                                    uploadProfileImageUseCase = appContainer.uploadProfileImageUseCase
                                )
                            )
                            val state by vm.state.collectAsStateWithLifecycle()

                            ContactDetailScreen(
                                state = state,
                                onEvent = { event ->
                                    vm.onEvent(event)
                                },
                                onBack = { navController.popBackStack() }
                            )
                        }
                    }
                }
            }
        }
    }
}

// Configures transparent system bars and icon colors based on the current theme
@Composable
fun SetupSystemBars() {
    val systemUiController = rememberSystemUiController()
    val useDarkIcons = !isSystemInDarkTheme()

    SideEffect {
        systemUiController.setStatusBarColor(
            color = Color.Transparent,
            darkIcons = useDarkIcons
        )
        systemUiController.setNavigationBarColor(
            color = Color.Transparent,
            darkIcons = useDarkIcons
        )
    }
}