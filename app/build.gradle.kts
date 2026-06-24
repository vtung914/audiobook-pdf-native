plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("kotlin-kapt")
}

android {
    namespace = "com.vtung.audiosach"
    compileSdk = 34
    defaultConfig {
        applicationId = "com.vtung.audiosach"
        minSdk = 26
        targetSdk = 34
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
}

dependencies {
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    
    // SỬ DỤNG BẢN NÀY, ĐÂY LÀ BẢN ĐƯỢC CỘNG ĐỒNG ANDROID FIX LỖI ĐÓNG GÓI
    implementation("com.github.krschult:epublib:3.1") 
    
    implementation("org.jsoup:jsoup:1.16.1")
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
}
