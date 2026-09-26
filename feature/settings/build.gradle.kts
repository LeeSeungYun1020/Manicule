plugins {
    alias(libs.plugins.manicule.android.feature)
    alias(libs.plugins.manicule.android.library.compose)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.leeseungyun1020.manicule.feature.settings"
}

dependencies {
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material.iconsExtended)
    implementation(libs.kotlinx.datetime)
    implementation(libs.kotlinx.serialization.json)

    testImplementation(projects.core.data)

    androidTestImplementation(projects.core.data)
    androidTestImplementation(libs.androidx.compose.ui.test)
    androidTestImplementation(libs.truth)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
