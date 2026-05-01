plugins {
    alias(libs.plugins.manicule.android.library)
    alias(libs.plugins.manicule.android.library.compose)
}

android {
    namespace = "com.leeseungyun1020.manicule.core.designsystem"
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.compose.material.iconsExtended)

    testImplementation(libs.junit4)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test)
}
