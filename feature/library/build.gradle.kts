plugins {
    alias(libs.plugins.manicule.android.feature)
    alias(libs.plugins.manicule.android.library.compose)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.leeseungyun1020.manicule.feature.library"
}

dependencies {
    implementation(libs.kotlinx.datetime)
    testImplementation(projects.core.data)
    androidTestImplementation(libs.androidx.compose.ui.test)
    androidTestImplementation(libs.truth)
}
