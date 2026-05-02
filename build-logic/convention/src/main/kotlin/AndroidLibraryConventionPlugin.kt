import com.android.build.api.dsl.LibraryExtension
import com.leeseungyun1020.manicule.buildlogic.configureKotlinAndroid
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

class AndroidLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("com.android.library")
                apply("manicule.android.lint")
            }

            extensions.configure<LibraryExtension> {
                configureKotlinAndroid(this)
                defaultConfig.testInstrumentationRunner =
                    "androidx.test.runner.AndroidJUnitRunner"
                testOptions.animationsDisabled = true
            }
        }
    }
}
