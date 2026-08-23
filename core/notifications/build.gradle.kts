plugins {
    alias(libs.plugins.manicule.android.library)
    alias(libs.plugins.manicule.android.hilt)
}

android {
    namespace = "com.leeseungyun1020.manicule.core.notifications"

    testOptions {
        unitTests.isIncludeAndroidResources = true
    }
}

dependencies {
    implementation(projects.core.common)
    implementation(projects.core.domain)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.work.ktx)
    implementation(libs.androidx.hilt.work)
    implementation(libs.kotlinx.datetime)
    ksp(libs.androidx.hilt.compiler)

    testImplementation(libs.junit4)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.truth)
    testImplementation(libs.androidx.work.testing)
    testImplementation(libs.robolectric)

    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.truth)
}
