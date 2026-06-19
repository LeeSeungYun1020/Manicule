import java.util.Properties

plugins {
    alias(libs.plugins.manicule.android.library)
    alias(libs.plugins.manicule.android.hilt)
    alias(libs.plugins.kotlin.serialization)
}

val localProps =
    Properties().apply {
        val localFile = rootProject.file("local.properties")
        if (localFile.exists()) {
            localFile.reader().use { reader -> load(reader) }
        }
    }

android {
    namespace = "com.leeseungyun1020.manicule.core.network"

    buildFeatures {
        buildConfig = true
    }

    defaultConfig {
        val nlkKey = localProps.getProperty("NLK_AUTH_KEY", "")
        buildConfigField("String", "NLK_AUTH_KEY", "\"$nlkKey\"")
    }
}

dependencies {
    implementation(projects.core.model)

    implementation(platform(libs.okhttp.bom))
    implementation(libs.okhttp.logging)

    implementation(platform(libs.retrofit.bom))
    implementation(libs.retrofit.core)
    implementation(libs.retrofit.kotlinx.serialization)

    implementation(libs.kotlinx.serialization.json)

    testImplementation(libs.junit4)
    testImplementation(libs.truth)
    testImplementation(libs.okhttp.mockwebserver)
    testImplementation(libs.kotlinx.coroutines.test)
}
