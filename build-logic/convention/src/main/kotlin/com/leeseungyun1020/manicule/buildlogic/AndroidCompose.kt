package com.leeseungyun1020.manicule.buildlogic

import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.CommonExtension
import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

/**
 * Configure Compose-specific options for the given Android extension.
 * Caller must already have applied the Compose Compiler Gradle plugin.
 */
internal fun Project.configureAndroidCompose(
    commonExtension: CommonExtension,
) {
    addComposeOptIns()

    commonExtension.apply {
        when (this) {
            is ApplicationExtension -> {
                buildFeatures {
                    compose = true
                }
            }
            is LibraryExtension -> {
                buildFeatures {
                    compose = true
                }
            }
        }

        dependencies {
            val bom = libs.findLibrary("androidx.compose.bom").get()
            add("implementation", platform(bom))
            add("androidTestImplementation", platform(bom))

            add("implementation", libs.findLibrary("androidx.compose.ui").get())
            add("implementation", libs.findLibrary("androidx.compose.ui.tooling.preview").get())
            add("implementation", libs.findLibrary("androidx.compose.foundation").get())
            add("implementation", libs.findLibrary("androidx.compose.foundation.layout").get())
            add("implementation", libs.findLibrary("androidx.compose.material3").get())
            add("implementation", libs.findLibrary("androidx.compose.runtime").get())

            add("debugImplementation", libs.findLibrary("androidx.compose.ui.tooling").get())
            add("debugImplementation", libs.findLibrary("androidx.compose.ui.test.manifest").get())
        }
    }
}
