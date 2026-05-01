plugins {
    alias(libs.plugins.manicule.android.application)
    alias(libs.plugins.manicule.android.application.compose)
    alias(libs.plugins.manicule.android.hilt)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.leeseungyun1020.manicule"

    defaultConfig {
        applicationId = "com.leeseungyun1020.manicule"
        versionCode = 1
        versionName = "0.1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
            isMinifyEnabled = false
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            // TODO: Play 스토어 배포 전 release 전용 keystore 로 교체
            signingConfig = signingConfigs.getByName("debug")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    // 1단계 — Foundation 모듈 의존
    implementation(projects.core.designsystem)
    implementation(projects.core.model)
    implementation(projects.core.common)

    // 후속 단계에서 활성화
    // implementation(projects.core.ui)
    // implementation(projects.core.domain)
    // implementation(projects.core.data)
    // implementation(projects.core.datastore)
    // implementation(projects.core.database)
    // implementation(projects.core.network)
    // implementation(projects.core.notifications)
    // implementation(projects.core.scanner)
    // implementation(projects.feature.home)
    // implementation(projects.feature.search)
    // implementation(projects.feature.scanner)
    // implementation(projects.feature.bookdetail)
    // implementation(projects.feature.library)
    // implementation(projects.feature.stats)
    // implementation(projects.feature.settings)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.compose.material3.windowSizeClass)
    implementation(libs.androidx.window)
    implementation(libs.material)

    testImplementation(libs.junit4)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.test.espresso.core)
}
