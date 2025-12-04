package com.mahmutalperenunal.nexoftphonebook.presentation.detail

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.net.Uri
import android.graphics.drawable.BitmapDrawable
import androidx.activity.compose.rememberLauncherForActivityResult
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.DpOffset
import androidx.palette.graphics.Palette
import androidx.core.content.FileProvider
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import coil.ImageLoader
import coil.request.ImageRequest
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.mahmutalperenunal.nexoftphonebook.R
import com.mahmutalperenunal.nexoftphonebook.presentation.ui.LocalAppImageLoader
import kotlinx.coroutines.delay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.UUID

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

    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    var showPhotoSourceSheet by remember { mutableStateOf(false) }

    var cameraImageUri by remember { mutableStateOf<Uri?>(null) }

    fun readBytesFromUri(
        context: Context,
        uri: Uri,
        maxDimension: Int = 720,
        quality: Int = 75
    ): ByteArray? {
        return try {
            val boundsOptions = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            context.contentResolver.openInputStream(uri)?.use { stream ->
                BitmapFactory.decodeStream(stream, null, boundsOptions)
            }

            val srcWidth = boundsOptions.outWidth
            val srcHeight = boundsOptions.outHeight
            if (srcWidth <= 0 || srcHeight <= 0) return null

            val larger = maxOf(srcWidth, srcHeight)
            var inSampleSize = 1
            if (larger > maxDimension) {
                inSampleSize = larger / maxDimension
                if (inSampleSize < 1) inSampleSize = 1
            }

            val decodeOptions = BitmapFactory.Options().apply {
                this.inSampleSize = inSampleSize
            }

            val scaledBitmap = context.contentResolver.openInputStream(uri)?.use { stream ->
                BitmapFactory.decodeStream(stream, null, decodeOptions)
            } ?: return null

            val output = ByteArrayOutputStream()
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, quality, output)
            scaledBitmap.recycle()

            output.toByteArray()
        } catch (_: Exception) {
            null
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
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
        galleryLauncher.launch("image/*")
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) launchCamera()
    }

    val galleryPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) launchGallery()
    }

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
                                tint = Color.Gray
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "This Contact Is Already Saved On Your Phone.",
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

                    val storagePermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        Manifest.permission.READ_MEDIA_IMAGES
                    } else {
                        Manifest.permission.READ_EXTERNAL_STORAGE
                    }

                    val hasStoragePermission = ContextCompat.checkSelfPermission(
                        context,
                        storagePermission
                    ) == PackageManager.PERMISSION_GRANTED

                    if (hasStoragePermission) {
                        launchGallery()
                    } else {
                        galleryPermissionLauncher.launch(storagePermission)
                    }
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

                    HorizontalDivider(
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
    val context = LocalContext.current

    var dominantColor by remember(photoUrl) {
        mutableStateOf(Color(0xFF000000).copy(alpha = 0.5f))
    }

    LaunchedEffect(photoUrl, showGlow) {
        if (showGlow && photoUrl != null) {
            val resultColor = withContext(Dispatchers.IO) {
                try {
                    val loader = ImageLoader(context)
                    val request = ImageRequest.Builder(context)
                        .data(photoUrl)
                        .allowHardware(false)
                        .build()
                    val result = loader.execute(request)
                    val bitmap = (result.drawable as? BitmapDrawable)?.bitmap ?: return@withContext null

                    val palette = Palette.from(bitmap).generate()
                    val swatch = palette.vibrantSwatch
                        ?: palette.lightVibrantSwatch
                        ?: palette.dominantSwatch

                    swatch?.rgb?.let { rgb -> Color(rgb) }
                } catch (_: Exception) {
                    null
                }
            }

            resultColor?.let { color ->
                dominantColor = color.copy(alpha = 0.5f)
            }
        } else {
            dominantColor = Color(0xFF000000).copy(alpha = 0.5f)
        }
    }

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
                                dominantColor,
                                Color.Transparent
                            )
                        )
                    )
            )
        }

        if (photoUrl != null) {
            val imageLoader = LocalAppImageLoader.current

            AsyncImage(
                model = photoUrl,
                imageLoader = imageLoader,
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
        border = ButtonDefaults.outlinedButtonBorder(enabled = true).copy(
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
                text = "Are You Sure You Want To Delete This Contact?",
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
                border = ButtonDefaults.outlinedButtonBorder(enabled = true).copy(
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
                border = ButtonDefaults.outlinedButtonBorder(enabled = true).copy(
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
    val composition by rememberLottieComposition(
        LottieCompositionSpec.RawRes(R.raw.done)
    )

    val progress by animateLottieCompositionAsState(
        composition = composition,
        iterations = 1
    )

    LaunchedEffect(progress) {
        if (progress >= 1f) {
            delay(200)
            onFinished()
        }
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
                modifier = Modifier.size(120.dp),
                contentAlignment = Alignment.Center
            ) {
                if (composition != null) {
                    LottieAnimation(
                        composition = composition,
                        progress = { progress }
                    )
                }
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
                text = "New Contact Saved 🎉",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = Color.Gray
                )
            )
        }
    }
}