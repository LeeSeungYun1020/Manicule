plugins {
    alias(libs.plugins.manicule.android.feature)
    alias(libs.plugins.manicule.android.library.compose)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.leeseungyun1020.manicule.feature.home"
}

dependencies {
    implementation(libs.androidx.compose.material.iconsExtended)
    implementation(libs.kotlinx.datetime)

    androidTestImplementation(libs.androidx.compose.ui.test)
    androidTestImplementation(libs.truth)
}
