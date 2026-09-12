plugins {
    alias(libs.plugins.manicule.android.feature)
    alias(libs.plugins.manicule.android.library.compose)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.leeseungyun1020.manicule.feature.search"
    testOptions {
        unitTests.isReturnDefaultValues = true
    }
}

dependencies {
    implementation(libs.androidx.compose.material.iconsExtended)
    implementation(libs.androidx.paging.compose)
    implementation(libs.kotlinx.datetime)

    testImplementation(projects.core.data)
    testImplementation(libs.androidx.paging.testing)

    androidTestImplementation(libs.androidx.compose.ui.test)
    androidTestImplementation(libs.truth)
}
