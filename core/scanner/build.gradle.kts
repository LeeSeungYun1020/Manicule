plugins {
    alias(libs.plugins.manicule.android.library)
    alias(libs.plugins.manicule.android.hilt)
}

android {
    namespace = "com.leeseungyun1020.manicule.core.scanner"
}

dependencies {
    implementation(libs.androidx.camera.core)
    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.mlkit.barcode.scanning)

    testImplementation(libs.junit4)
    testImplementation(libs.truth)
}
