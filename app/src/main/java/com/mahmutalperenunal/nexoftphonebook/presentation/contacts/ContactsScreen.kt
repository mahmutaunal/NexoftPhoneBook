package com.mahmutalperenunal.nexoftphonebook.presentation.contacts

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.mahmutalperenunal.nexoftphonebook.domain.model.ContactsEvent
import com.mahmutalperenunal.nexoftphonebook.domain.model.ContactsState

// Main Contacts screen showing loading, error, or contact count state
@Composable
fun ContactsScreen(
    state: ContactsState,
    onEvent: (ContactsEvent) -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        when {
            state.isLoading -> {
                CircularProgressIndicator()
            }

            state.errorMessage != null -> {
                Text(
                    text = state.errorMessage,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            else -> {
                Text(
                    text = "Kişi sayısı: ${state.sections.sumOf { it.items.size }}",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}