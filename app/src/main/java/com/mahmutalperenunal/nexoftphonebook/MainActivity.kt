package com.mahmutalperenunal.nexoftphonebook

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mahmutalperenunal.nexoftphonebook.domain.model.ContactsEvent
import com.mahmutalperenunal.nexoftphonebook.domain.viewmodel.ContactsViewModel
import com.mahmutalperenunal.nexoftphonebook.domain.viewmodel.ViewModelFactory
import com.mahmutalperenunal.nexoftphonebook.presentation.contacts.ContactsScreen
import com.mahmutalperenunal.nexoftphonebook.ui.theme.NexoftPhoneBookTheme

class MainActivity : ComponentActivity() {

    private val appContainer: AppContainer by lazy {
        (application as NexoftPhoneBookApp).appContainer
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val contactsViewModel: ContactsViewModel = viewModel(
                factory = ViewModelFactory(
                    getContactsUseCase = appContainer.getContactsUseCase,
                    searchContactsUseCase = appContainer.searchContactsUseCase,
                    deleteContactUseCase = appContainer.deleteContactUseCase,
                    getSearchHistoryUseCase = appContainer.getSearchHistoryUseCase,
                    saveSearchQueryUseCase = appContainer.saveSearchQueryUseCase
                )
            )

            val state = contactsViewModel.state.collectAsStateWithLifecycle()

            NexoftPhoneBookTheme {
                ContactsScreen(
                    state = state.value,
                    onEvent = { event: ContactsEvent ->
                        contactsViewModel.onEvent(event)
                    }
                )
            }
        }
    }
}