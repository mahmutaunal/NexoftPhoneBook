package com.mahmutalperenunal.nexoftphonebook.presentation.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Check
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.palette.graphics.Palette
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.mahmutalperenunal.nexoftphonebook.R
import com.mahmutalperenunal.nexoftphonebook.presentation.common.LocalAppImageLoader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import android.graphics.drawable.BitmapDrawable
import androidx.compose.foundation.isSystemInDarkTheme

@Composable
fun AvatarWithGlow(
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
                    val bitmap =
                        (result.drawable as? BitmapDrawable)?.bitmap ?: return@withContext null

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
fun SaveToPhoneButton(
    enabled: Boolean,
    onClick: () -> Unit
) {
    val borderColor = if (enabled) {
        MaterialTheme.colorScheme.onSurface
    } else {
        MaterialTheme.colorScheme.outline
    }
    val textColor = if (enabled) {
        MaterialTheme.colorScheme.onSurface
    } else {
        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
    }

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
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = textColor
        )
    ) {
        Icon(
            imageVector = Icons.Default.BookmarkBorder,
            contentDescription = null,
            tint = textColor
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = stringResource(id = R.string.detail_save_to_phone_contact),
            color = textColor
        )
    }
}

@Composable
fun SuccessToast(message: String) {
    val successColor = Color(0xFF4CAF50)

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
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
                    tint = MaterialTheme.colorScheme.surface,
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
fun DeleteConfirmationBottomSheet(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    val isDarkTheme = isSystemInDarkTheme()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        scrimColor = Color.Black.copy(alpha = 0.6f),
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(id = R.string.contacts_delete_title),
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.SemiBold
                )
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(id = R.string.contacts_delete_message),
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
                    Text(text = stringResource(id = R.string.common_no))
                }
                Button(
                    onClick = onConfirm,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isDarkTheme) Color.White else Color.Black,
                        contentColor = if (isDarkTheme) Color.Black else Color.White
                    )
                ) {
                    Text(text = stringResource(id = R.string.common_yes))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhotoSourceBottomSheet(
    onDismiss: () -> Unit,
    onCameraClick: () -> Unit,
    onGalleryClick: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        scrimColor = Color.Black.copy(alpha = 0.6f),
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val buttonContainerColor = MaterialTheme.colorScheme.surface
            val buttonContentColor = MaterialTheme.colorScheme.onSurface
            val buttonBorderColor = MaterialTheme.colorScheme.outline

            Button(
                onClick = onCameraClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(26.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = buttonContainerColor,
                    contentColor = buttonContentColor
                ),
                border = ButtonDefaults.outlinedButtonBorder(enabled = true).copy(
                    width = 1.dp,
                    brush = Brush.linearGradient(listOf(buttonBorderColor, buttonBorderColor))
                )
            ) {
                Icon(
                    imageVector = Icons.Outlined.CameraAlt,
                    contentDescription = null,
                    tint = buttonContentColor
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = stringResource(id = R.string.detail_photo_camera))
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = onGalleryClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(26.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = buttonContainerColor,
                    contentColor = buttonContentColor
                ),
                border = ButtonDefaults.outlinedButtonBorder(enabled = true).copy(
                    width = 1.dp,
                    brush = Brush.linearGradient(listOf(buttonBorderColor, buttonBorderColor))
                )
            ) {
                Icon(
                    imageVector = Icons.Outlined.Image,
                    contentDescription = null,
                    tint = buttonContentColor
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = stringResource(id = R.string.detail_photo_gallery))
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = stringResource(id = R.string.common_cancel),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.clickable { onDismiss() }
            )
        }
    }
}

// Full-screen confirmation shown after successfully creating a new contact
@Composable
fun NewContactDoneScreen(
    onFinished: () -> Unit
) {
    val composition by rememberLottieComposition(
        LottieCompositionSpec.RawRes(R.raw.done)
    )

    val progress by animateLottieCompositionAsState(
        composition = composition,
        iterations = 1
    )

    // Automatically call onFinished shortly after the animation reaches the end
    LaunchedEffect(progress) {
        if (progress >= 1f) {
            delay(200)
            onFinished()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface),
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
                text = stringResource(id = R.string.detail_new_contact_done_title),
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.SemiBold
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(id = R.string.detail_new_contact_done_subtitle),
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = Color.Gray
                )
            )
        }
    }
}