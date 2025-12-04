package com.mahmutalperenunal.nexoftphonebook.presentation.contacts

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.compose.ui.res.stringResource
import com.mahmutalperenunal.nexoftphonebook.R
import kotlinx.coroutines.delay

// Main Contacts screen handling permissions, search, history, list, and empty states
@Composable
fun ContactsScreen(
    state: ContactsState,
    onEvent: (ContactsEvent) -> Unit
) {
    val hasContacts = state.sections.any { it.items.isNotEmpty() }

    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    var isSearchFocused by remember { mutableStateOf(false) }
    var isDebouncing by remember { mutableStateOf(false) }

    val hasRequestedContactsPermission = remember { mutableStateOf(false) }

    val readContactsPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            onEvent(ContactsEvent.OnRetry)
        }
    }

    // Request READ_CONTACTS permission once when the screen is first composed
    LaunchedEffect(Unit) {
        if (!hasRequestedContactsPermission.value) {
            val hasReadPermission = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_CONTACTS
            ) == PackageManager.PERMISSION_GRANTED

            if (!hasReadPermission) {
                readContactsPermissionLauncher.launch(Manifest.permission.READ_CONTACTS)
            }

            hasRequestedContactsPermission.value = true
        }
    }

    // Debounce search input and trigger search after a short delay
    LaunchedEffect(state.searchQuery) {
        // Start debounce loading
        isDebouncing = true

        delay(300L)

        // Execute search
        onEvent(ContactsEvent.OnSearchSubmit(state.searchQuery))

        // End debounce loading
        isDebouncing = false
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.surface
    ) {
        Box(modifier = Modifier
            .fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(horizontal = 20.dp, vertical = 24.dp)
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(id = R.string.contacts_title),
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        )
                    )

                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary)
                            .clickable { onEvent(ContactsEvent.OnAddContactClick) },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = stringResource(id = R.string.contacts_add_contact),
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Search bar
                OutlinedTextField(
                    value = state.searchQuery,
                    onValueChange = { onEvent(ContactsEvent.OnSearchQueryChange(it)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .onFocusChanged { focusState ->
                            isSearchFocused = focusState.isFocused
                        },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    placeholder = {
                        Text(
                            text = stringResource(id = R.string.contacts_search_by_name),
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = if (isSearchFocused) {
                                MaterialTheme.colorScheme.onSurface
                            } else {
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                            }
                        )
                    },
                    trailingIcon = {
                        if (state.searchQuery.isNotBlank()) {
                            IconButton(
                                onClick = {
                                    // Clear query, cancel search, and remove focus
                                    onEvent(ContactsEvent.OnSearchQueryChange(""))
                                    onEvent(ContactsEvent.OnSearchSubmit(""))
                                    focusManager.clearFocus(force = true)
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = stringResource(id = R.string.contacts_clear_search),
                                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                            }
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        disabledBorderColor = Color.Transparent,
                        errorBorderColor = Color.Transparent,
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        disabledContainerColor = MaterialTheme.colorScheme.surface,
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                        cursorColor = MaterialTheme.colorScheme.onSurface
                    ),
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Search
                    ),
                    keyboardActions = KeyboardActions(
                        onSearch = {
                            onEvent(ContactsEvent.OnSearchSubmit(state.searchQuery))
                        }
                    )
                )

                Spacer(modifier = Modifier.height(24.dp))

                val showSearchHistory =
                    isSearchFocused && state.searchQuery.isBlank() && state.searchHistory.isNotEmpty()

                // Show search history suggestions while the search field is focused and empty
                AnimatedVisibility(
                    visible = showSearchHistory,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Column {
                        SearchHistorySection(
                            history = state.searchHistory,
                            onItemClick = { query ->
                                onEvent(ContactsEvent.OnSearchHistoryItemClick(query))
                            },
                            onDeleteItem = { id ->
                                onEvent(ContactsEvent.OnDeleteHistoryItem(id))
                            },
                            onClearAll = {
                                onEvent(ContactsEvent.OnClearAllHistory)
                            }
                        )

                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }

                val isSearching = state.searchQuery.isNotBlank()

                when {
                    state.isLoading -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }

                    state.errorMessage != null -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = state.errorMessage,
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    // Show loading while debounce is active
                    isDebouncing && state.searchQuery.isNotBlank() -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }

                    isSearching && !hasContacts -> {
                        SearchNoResultsState()
                    }

                    isSearching -> {
                        SearchResultsCard(
                            sections = state.sections,
                            onContactClick = { id ->
                                onEvent(ContactsEvent.OnContactClick(id))
                            }
                        )
                    }

                    // Hide list/empty state while search bar is focused with empty query
                    isSearchFocused && state.searchQuery.isBlank() -> {
                        // Intentionally left empty so only the search history section is visible
                    }

                    !hasContacts -> {
                        EmptyContactsState(
                            onCreateNewContactClick = {
                                onEvent(ContactsEvent.OnAddContactClick)
                            }
                        )
                    }

                    else -> {
                        ContactsList(
                            sections = state.sections,
                            onContactClick = { id ->
                                onEvent(ContactsEvent.OnContactClick(id))
                            },
                            onEditClick = { id ->
                                onEvent(ContactsEvent.OnEditClick(id))
                            },
                            onDeleteClick = { id ->
                                onEvent(ContactsEvent.OnDeleteClick(id))
                            }
                        )
                    }
                }
            }

            if (state.isDeleteSheetVisible && state.deleteCandidate != null) {
                DeleteConfirmationBottomSheet(
                    onDismiss = { onEvent(ContactsEvent.OnDismissDelete) },
                    onConfirm = { onEvent(ContactsEvent.OnConfirmDelete) }
                )
            }

            // Temporary success toast shown at the bottom of the screen
            AnimatedVisibility(
                visible = state.successMessage != null,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 24.dp, start = 20.dp, end = 20.dp)
            ) {
                state.successMessage?.let { message ->
                    SuccessToast(
                        message = message,
                        onDismiss = { onEvent(ContactsEvent.OnListToastShown) }
                    )
                }
            }
        }
    }
}