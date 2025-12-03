package com.mahmutalperenunal.nexoftphonebook.presentation.detail

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material3.*
import androidx.compose.material3.Divider
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.DpOffset
import coil.compose.AsyncImage
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactDetailScreen(
    state: ContactDetailState,
    onEvent: (ContactDetailEvent) -> Unit,
    onBack: () -> Unit
) {
    if (state.isNewContact && state.createCompleted) {
        NewContactDoneScreen(
            onFinished = { onBack() }
        )
        return
    }

    state.showSuccessToast?.let { message ->
        LaunchedEffect(message) {
            delay(2000)
            onEvent(ContactDetailEvent.OnToastShown)
        }
    }

    var showPhotoSourceSheet by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.25f))
    ) {

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .padding(top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding())
                .align(Alignment.BottomCenter),
            shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp, vertical = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                TopBar(state = state, onEvent = onEvent, onBack = onBack)

                Spacer(modifier = Modifier.height(24.dp))

                // Avatar + glow
                AvatarWithGlow(
                    photoUrl = state.photoUrl,
                    showGlow = state.photoUrl != null
                )

                Spacer(modifier = Modifier.height(4.dp))

                val photoLabel = when {
                    state.isNewContact && state.photoUrl == null -> "Add Photo"
                    state.isNewContact && state.photoUrl != null -> "Change Photo"
                    else -> "Change Photo"
                }

                Text(
                    text = photoLabel,
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    modifier = Modifier.clickable {
                        // Camera / Gallery sheet
                        showPhotoSourceSheet = true
                    }
                )

                Spacer(modifier = Modifier.height(32.dp))

                // TextFields
                OutlinedTextField(
                    value = state.firstName,
                    onValueChange = { onEvent(ContactDetailEvent.OnFirstNameChange(it)) },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = {
                        Text(
                            text = "First Name",
                            color = Color.Gray
                        )
                    },
                    singleLine = true,
                    enabled = state.isEditMode,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black,
                        disabledTextColor = Color.Black,
                        cursorColor = Color.Black
                    ),
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Words
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = state.lastName,
                    onValueChange = { onEvent(ContactDetailEvent.OnLastNameChange(it)) },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = {
                        Text(
                            text = "Last Name",
                            color = Color.Gray
                        )
                    },
                    singleLine = true,
                    enabled = state.isEditMode,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black,
                        disabledTextColor = Color.Black,
                        cursorColor = Color.Black
                    ),
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Words
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = state.phoneNumber,
                    onValueChange = { onEvent(ContactDetailEvent.OnPhoneNumberChange(it)) },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = {
                        Text(
                            text = "Phone Number",
                            color = Color.Gray
                        )
                    },
                    singleLine = true,
                    enabled = state.isEditMode,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black,
                        disabledTextColor = Color.Black,
                        cursorColor = Color.Black
                    ),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Phone
                    )
                )

                Spacer(modifier = Modifier.height(48.dp))

                if (!state.isNewContact && !state.isEditMode) {
                    SaveToPhoneButton(
                        enabled = !state.isSavedInDevice,
                        onClick = { onEvent(ContactDetailEvent.OnSaveToPhoneClick) }
                    )

                    if (state.showAlreadySavedInfo) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = Color.Gray
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "This contact is already saved on your phone.",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = Color.Gray
                                )
                            )
                        }
                    }
                }
            }
        }

        // Loading overlay
        if (state.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        // Success toast (update / delete / save-to-phone)
        AnimatedVisibility(
            visible = state.showSuccessToast != null,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 24.dp, start = 20.dp, end = 20.dp)
        ) {
            state.showSuccessToast?.let { message ->
                SuccessToast(message = message)
            }
        }

        // Delete bottom sheet
        if (state.showDeleteBottomSheet) {
            DeleteConfirmationBottomSheet(
                onDismiss = { onEvent(ContactDetailEvent.OnDeleteCancel) },
                onConfirm = {
                    onEvent(ContactDetailEvent.OnDeleteConfirm)
                }
            )
        }

        // 📸 Add Photo sheet (Camera / Gallery)
        if (showPhotoSourceSheet) {
            PhotoSourceBottomSheet(
                onDismiss = { showPhotoSourceSheet = false },
                onCameraClick = {
                    showPhotoSourceSheet = false
                },
                onGalleryClick = {
                    showPhotoSourceSheet = false
                }
            )
        }
    }
}

@Composable
private fun TopBar(
    state: ContactDetailState,
    onEvent: (ContactDetailEvent) -> Unit,
    onBack: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (state.isEditMode) {
            // New Contact & Edit Contact
            Text(
                text = "Cancel",
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.clickable {
                    onEvent(ContactDetailEvent.OnCancelEdit)
                    if (state.isNewContact) onBack()
                }
            )

            Text(
                text = if (state.isNewContact) "New Contact" else "Edit Contact",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold
                )
            )

            val doneColor =
                if (state.canSave) MaterialTheme.colorScheme.primary else Color.LightGray

            Text(
                text = "Done",
                color = doneColor,
                modifier = Modifier
                    .clickable(enabled = state.canSave) {
                        onEvent(ContactDetailEvent.OnSaveClick)
                    }
            )
        } else {
            // View mode (Edit/Delete)
            Spacer(modifier = Modifier.weight(1f))

            var menuExpanded by remember { mutableStateOf(false) }

            Box(
                modifier = Modifier.wrapContentSize(Alignment.TopEnd)
            ) {
                IconButton(onClick = { menuExpanded = true }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "Menu")
                }

                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false },
                    offset = DpOffset(x = 0.dp, y = 8.dp),
                    modifier = Modifier
                        .width(180.dp)
                        .background(Color.White)
                ) {
                    // Edit row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                menuExpanded = false
                                onEvent(ContactDetailEvent.OnToggleEdit)
                            }
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Edit",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Black
                        )
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = null,
                            tint = Color.Black
                        )
                    }

                    Divider(
                        color = Color.LightGray.copy(alpha = 0.4f)
                    )

                    // Delete row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                menuExpanded = false
                                onEvent(ContactDetailEvent.OnDeleteClick)
                            }
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Delete",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Red
                        )
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = null,
                            tint = Color.Red
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AvatarWithGlow(
    photoUrl: String?,
    showGlow: Boolean
) {
    Box(
        modifier = Modifier.size(120.dp),
        contentAlignment = Alignment.Center
    ) {
        if (showGlow) {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                Color(0xFF000000).copy(alpha = 0.6f),
                                Color.Transparent
                            )
                        )
                    )
            )
        }

        if (photoUrl != null) {
            AsyncImage(
                model = photoUrl,
                contentDescription = null,
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
            )
        } else {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(Color.LightGray.copy(alpha = 0.4f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = Color.Gray,
                    modifier = Modifier.size(40.dp)
                )
            }
        }
    }
}

@Composable
private fun SaveToPhoneButton(
    enabled: Boolean,
    onClick: () -> Unit
) {
    val borderColor = if (enabled) Color.Black else Color.LightGray
    val textColor = if (enabled) Color.Black else Color.LightGray

    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(28.dp),
        border = ButtonDefaults.outlinedButtonBorder.copy(
            width = 1.dp,
            brush = Brush.linearGradient(listOf(borderColor, borderColor))
        ),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = Color.White,
            contentColor = textColor
        )
    ) {
        Icon(imageVector = Icons.Default.BookmarkBorder, contentDescription = null, tint = textColor)
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "Save to My Phone Contact",
            color = textColor
        )
    }
}

@Composable
private fun SuccessToast(message: String) {
    val successColor = Color(0xFF4CAF50)

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color.White,
        tonalElevation = 8.dp,
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(successColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = message,
                color = successColor,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.SemiBold
                )
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DeleteConfirmationBottomSheet(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        scrimColor = Color.Black.copy(alpha = 0.6f),
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Delete Contact",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.SemiBold
                )
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Are you sure you want to delete this contact?",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(28.dp)
                ) {
                    Text("No")
                }
                Button(
                    onClick = onConfirm,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Black
                    )
                ) {
                    Text("Yes")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PhotoSourceBottomSheet(
    onDismiss: () -> Unit,
    onCameraClick: () -> Unit,
    onGalleryClick: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        scrimColor = Color.Black.copy(alpha = 0.6f),
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(
                onClick = onCameraClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(26.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = Color.White,
                    contentColor = Color.Black
                ),
                border = ButtonDefaults.outlinedButtonBorder.copy(
                    width = 1.dp,
                    brush = Brush.linearGradient(listOf(Color.Black, Color.Black))
                )
            ) {
                Icon(
                    imageVector = Icons.Outlined.CameraAlt,
                    contentDescription = null,
                    tint = Color.Black
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Camera")
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = onGalleryClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(26.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = Color.White,
                    contentColor = Color.Black
                ),
                border = ButtonDefaults.outlinedButtonBorder.copy(
                    width = 1.dp,
                    brush = Brush.linearGradient(listOf(Color.Black, Color.Black))
                )
            ) {
                Icon(
                    imageVector = Icons.Outlined.Image,
                    contentDescription = null,
                    tint = Color.Black
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Gallery")
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Cancel",
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.clickable { onDismiss() }
            )
        }
    }
}

@Composable
private fun NewContactDoneScreen(
    onFinished: () -> Unit
) {
    LaunchedEffect(Unit) {
        delay(1500)
        onFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF4CAF50)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(48.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "All Done!",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.SemiBold
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "New contact saved 🎉",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = Color.Gray
                )
            )
        }
    }
}