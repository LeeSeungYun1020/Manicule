plugins {
    alias(libs.plugins.manicule.android.library)
    alias(libs.plugins.manicule.android.hilt)
    alias(libs.plugins.manicule.android.room)
}

android {
    namespace = "com.leeseungyun1020.manicule.core.database"
}

dependencies {
    implementation(projects.core.model)
    implementation(libs.kotlinx.datetime)

    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(libs.turbine)
    androidTestImplementation(libs.truth)
}
