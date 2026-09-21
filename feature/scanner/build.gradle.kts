plugins {
    alias(libs.plugins.manicule.android.feature)
    alias(libs.plugins.manicule.android.library.compose)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.leeseungyun1020.manicule.feature.scanner"
}

dependencies {
    implementation(projects.core.domain)
    implementation(projects.core.scanner)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.camera.core)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.view)

    androidTestImplementation(libs.androidx.compose.ui.test)
    androidTestImplementation(libs.androidx.test.rules)
    androidTestImplementation(libs.truth)
    androidTestImplementation(projects.core.data)
    androidTestImplementation(libs.androidx.paging.runtime)
    androidTestImplementation(libs.kotlinx.datetime)

    testImplementation(projects.core.data)
    testImplementation(libs.androidx.paging.runtime)
    testImplementation(libs.kotlinx.datetime)
}
