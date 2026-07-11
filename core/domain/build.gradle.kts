plugins {
    alias(libs.plugins.manicule.android.library)
    alias(libs.plugins.manicule.android.hilt)
}

android {
    namespace = "com.leeseungyun1020.manicule.core.domain"
}

dependencies {
    implementation(projects.core.model)
    implementation(projects.core.data)
    implementation(projects.core.common)

    implementation(libs.kotlinx.datetime)
    implementation(libs.androidx.paging.runtime)

    testImplementation(libs.junit4)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.truth)
    testImplementation(libs.turbine)
}
