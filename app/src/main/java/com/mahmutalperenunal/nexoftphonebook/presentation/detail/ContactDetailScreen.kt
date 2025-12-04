package com.mahmutalperenunal.nexoftphonebook.presentation.detail

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.DpOffset
import androidx.core.content.FileProvider
import androidx.core.content.ContextCompat
import com.mahmutalperenunal.nexoftphonebook.presentation.common.readBytesFromUri
import kotlinx.coroutines.delay
import java.io.File
import java.util.UUID

import com.mahmutalperenunal.nexoftphonebook.R

// Screen for viewing, creating, and editing a single contact
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactDetailScreen(
    state: ContactDetailState,
    onEvent: (ContactDetailEvent) -> Unit,
    onBack: () -> Unit
) {
    // Show completion screen after successfully creating a new contact
    if (state.isNewContact && state.createCompleted) {
        NewContactDoneScreen(
            onFinished = { onBack() }
        )
        return
    }

    // Handle success toast lifecycle and navigate back for existing contacts
    state.showSuccessToast?.let { message ->
        LaunchedEffect(message) {
            delay(2000)
            onEvent(ContactDetailEvent.OnToastShown)
            // After showing success toast for existing contacts, navigate back
            if (!state.isNewContact) {
                onBack()
            }
        }
    }

    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    var showPhotoSourceSheet by remember { mutableStateOf(false) }

    var cameraImageUri by remember { mutableStateOf<Uri?>(null) }

    // Pick a profile photo from the gallery and forward it to the ViewModel
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        uri ?: return@rememberLauncherForActivityResult
        val bytes = readBytesFromUri(context, uri) ?: return@rememberLauncherForActivityResult
        val fileName = "gallery_${UUID.randomUUID()}.jpg"

        onEvent(
            ContactDetailEvent.OnImageUploadRequested(
                imageBytes = bytes,
                fileName = fileName
            )
        )
    }

    // Capture a profile photo with the camera and forward it to the ViewModel
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success: Boolean ->
        if (!success) return@rememberLauncherForActivityResult
        val uri = cameraImageUri ?: return@rememberLauncherForActivityResult
        val bytes = readBytesFromUri(context, uri) ?: return@rememberLauncherForActivityResult
        val fileName = "camera_${UUID.randomUUID()}.jpg"

        onEvent(
            ContactDetailEvent.OnImageUploadRequested(
                imageBytes = bytes,
                fileName = fileName
            )
        )
    }

    // Launch the camera using a FileProvider-backed Uri
    fun launchCamera() {
        val file = File(
            context.cacheDir,
            "camera_${UUID.randomUUID()}.jpg"
        )
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        cameraImageUri = uri
        cameraLauncher.launch(uri)
    }

    fun launchGallery() {
        galleryLauncher.launch(
            PickVisualMediaRequest(
                ActivityResultContracts.PickVisualMedia.ImageOnly
            )
        )
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) launchCamera()
    }


    // Request READ/WRITE_CONTACTS and save to phone if both are granted
    val contactsPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val writeGranted = permissions[Manifest.permission.WRITE_CONTACTS] == true
        val readGranted = permissions[Manifest.permission.READ_CONTACTS] == true

        if (writeGranted && readGranted) {
            onEvent(ContactDetailEvent.OnSaveToPhoneClick)
        }
    }

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
                containerColor = MaterialTheme.colorScheme.surface
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
                    state.isNewContact && state.photoUrl == null -> stringResource(id = R.string.detail_photo_add)
                    state.isNewContact && state.photoUrl != null -> stringResource(id = R.string.detail_photo_change)
                    else -> stringResource(id = R.string.detail_photo_change)
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
                            text = stringResource(id = R.string.detail_first_name),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    singleLine = true,
                    enabled = state.isEditMode,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                        disabledTextColor = MaterialTheme.colorScheme.onSurface,
                        cursorColor = MaterialTheme.colorScheme.primary,
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        disabledBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                    ),
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Words,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = {
                            focusManager.moveFocus(FocusDirection.Down)
                        }
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = state.lastName,
                    onValueChange = { onEvent(ContactDetailEvent.OnLastNameChange(it)) },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = {
                        Text(
                            text = stringResource(id = R.string.detail_last_name),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    singleLine = true,
                    enabled = state.isEditMode,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                        disabledTextColor = MaterialTheme.colorScheme.onSurface,
                        cursorColor = MaterialTheme.colorScheme.primary,
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        disabledBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                    ),
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Words,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = {
                            focusManager.moveFocus(FocusDirection.Down)
                        }
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = state.phoneNumber,
                    onValueChange = { newValue ->
                        val filtered = buildString {
                            newValue.forEachIndexed { index, c ->
                                if (c.isDigit() || (c == '+' && index == 0)) {
                                    append(c)
                                }
                            }
                        }
                        onEvent(ContactDetailEvent.OnPhoneNumberChange(filtered))
                    },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = {
                        Text(
                            text = stringResource(id = R.string.detail_phone_number),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    singleLine = true,
                    enabled = state.isEditMode,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                        disabledTextColor = MaterialTheme.colorScheme.onSurface,
                        cursorColor = MaterialTheme.colorScheme.primary,
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        disabledBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                    ),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Phone,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            focusManager.clearFocus()
                        }
                    )
                )

                Spacer(modifier = Modifier.height(48.dp))

                // Save existing contacts into the device's native contacts app
                if (!state.isNewContact && !state.isEditMode) {
                    SaveToPhoneButton(
                        enabled = !state.isSavedInDevice,
                        onClick = {
                            val hasWritePermission = ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.WRITE_CONTACTS
                            ) == PackageManager.PERMISSION_GRANTED

                            val hasReadPermission = ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.READ_CONTACTS
                            ) == PackageManager.PERMISSION_GRANTED

                            if (hasWritePermission && hasReadPermission) {
                                onEvent(ContactDetailEvent.OnSaveToPhoneClick)
                            } else {
                                contactsPermissionLauncher.launch(
                                    arrayOf(
                                        Manifest.permission.READ_CONTACTS,
                                        Manifest.permission.WRITE_CONTACTS
                                    )
                                )
                            }
                        }
                    )

                    if (state.showAlreadySavedInfo) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = stringResource(id = R.string.detail_already_saved_info),
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
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

                    val hasCameraPermission = ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.CAMERA
                    ) == PackageManager.PERMISSION_GRANTED

                    if (hasCameraPermission) {
                        launchCamera()
                    } else {
                        cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                    }
                },
                onGalleryClick = {
                    showPhotoSourceSheet = false
                    launchGallery()
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
                text = stringResource(id = R.string.common_cancel),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.clickable {
                    onEvent(ContactDetailEvent.OnCancelEdit)
                    if (state.isNewContact) onBack()
                }
            )

            Text(
                text = if (state.isNewContact) {
                    stringResource(id = R.string.detail_new_contact_title)
                } else {
                    stringResource(id = R.string.detail_edit_contact_title)
                },
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold
                )
            )

            val doneColor =
                if (state.canSave) MaterialTheme.colorScheme.primary else Color.LightGray

            Text(
                text = stringResource(id = R.string.detail_done),
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
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = stringResource(id = R.string.detail_menu)
                    )
                }

                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false },
                    offset = DpOffset(x = 0.dp, y = 8.dp),
                    modifier = Modifier
                        .width(180.dp)
                        .background(MaterialTheme.colorScheme.surface)
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
                            text = stringResource(id = R.string.detail_menu_edit),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
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
                            text = stringResource(id = R.string.detail_menu_delete),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}