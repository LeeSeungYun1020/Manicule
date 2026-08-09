plugins {
    alias(libs.plugins.manicule.android.library)
    alias(libs.plugins.manicule.android.hilt)
}

android {
    namespace = "com.leeseungyun1020.manicule.core.notifications"
}

dependencies {
    implementation(projects.core.domain)

    implementation(libs.androidx.work.ktx)
    implementation(libs.androidx.hilt.work)
    ksp(libs.androidx.hilt.compiler)

    testImplementation(libs.junit4)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.truth)
    testImplementation(libs.androidx.work.testing)
}
