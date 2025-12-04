########################################
# Genel ayarlar
########################################
-keepattributes Signature, InnerClasses, EnclosingMethod, *Annotation*

-dontnote org.jetbrains.annotations.**
-dontnote kotlin.**
-dontwarn kotlin.**
-dontwarn kotlinx.coroutines.**
-dontwarn com.airbnb.lottie.**
-dontwarn coil.**
-dontwarn androidx.palette.graphics.**

########################################
# Uygulama giriş noktaları
########################################

# Application, Activity, Service, BroadcastReceiver, ContentProvider
-keep class com.mahmutalperenunal.nexoftphonebook.** extends android.app.Application { *; }
-keep class com.mahmutalperenunal.nexoftphonebook.** extends android.app.Activity { *; }
-keep class com.mahmutalperenunal.nexoftphonebook.** extends android.app.Service { *; }
-keep class com.mahmutalperenunal.nexoftphonebook.** extends android.content.BroadcastReceiver { *; }
-keep class com.mahmutalperenunal.nexoftphonebook.** extends android.content.ContentProvider { *; }

########################################
# Jetpack Compose
########################################

-keepclassmembers class * {
    @androidx.compose.runtime.Composable <methods>;
}

########################################
# Room (DB, Dao, Entity)
########################################

-keep @androidx.room.Dao class * { *; }
-keep @androidx.room.Database class * { *; }
-keep @androidx.room.Entity class * { *; }

-keepclassmembers class * {
    @androidx.room.* <methods>;
    @androidx.room.* <fields>;
}

-dontwarn androidx.room.**

########################################
# Retrofit + HTTP
########################################

-keep class com.mahmutalperenunal.nexoftphonebook.data.remote.service.** { *; }
-keepclassmembers interface * {
    @retrofit2.http.* <methods>;
}

-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn retrofit2.**

########################################
# JSON DTO
########################################

-keep class com.mahmutalperenunal.nexoftphonebook.data.remote.dto.** { *; }

########################################
# Palette
########################################

-keep class androidx.palette.graphics.** { *; }

########################################
# Domain layer ve use case'ler
########################################

-keep class com.mahmutalperenunal.nexoftphonebook.domain.entity.** { *; }
-keep class com.mahmutalperenunal.nexoftphonebook.domain.usecase.** { *; }