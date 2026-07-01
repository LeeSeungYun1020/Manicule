plugins {
    alias(libs.plugins.manicule.android.library)
    alias(libs.plugins.manicule.android.library.compose)
}

android {
    namespace = "com.leeseungyun1020.manicule.core.ui"
}

dependencies {
    implementation(projects.core.designsystem)
    implementation(projects.core.model)

    implementation(libs.androidx.core.ktx)

    // Coil for Image Loading
    implementation(platform(libs.coil.bom))
    implementation(libs.coil.compose)
    implementation(libs.coil.network.okhttp)

    testImplementation(libs.junit4)
    androidTestImplementation(libs.androidx.test.ext.junit)
}
