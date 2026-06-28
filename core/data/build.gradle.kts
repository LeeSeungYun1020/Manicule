plugins {
    alias(libs.plugins.manicule.android.library)
    alias(libs.plugins.manicule.android.hilt)
}

android {
    namespace = "com.leeseungyun1020.manicule.core.data"
    testOptions {
        unitTests.isReturnDefaultValues = true
    }
}

dependencies {
    implementation(projects.core.model)
    implementation(projects.core.database)
    implementation(projects.core.network)
    implementation(projects.core.datastore)
    implementation(projects.core.common)
    implementation(libs.kotlinx.datetime)

    testImplementation(libs.junit4)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.truth)
}
