plugins {
    alias(libs.plugins.manicule.android.feature)
    alias(libs.plugins.manicule.android.library.compose)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.leeseungyun1020.manicule.feature.bookdetail"
}

dependencies {
    implementation(libs.kotlinx.datetime)
    testImplementation(projects.core.data)
    testImplementation(libs.androidx.paging.runtime)
    androidTestImplementation(libs.androidx.compose.ui.test)
    androidTestImplementation(libs.truth)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
