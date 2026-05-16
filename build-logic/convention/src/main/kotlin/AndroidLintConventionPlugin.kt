import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.LibraryExtension
import io.gitlab.arturbosch.detekt.extensions.DetektExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jlleitschuh.gradle.ktlint.KtlintExtension

/**
 * 모든 모듈에 ktlint + detekt + Android Lint를 적용.
 * `manicule.android.*` / `manicule.jvm.library` 컨벤션 플러그인에서 자동 적용된다.
 *
 * 설정 파일: `.editorconfig` (ktlint), `config/detekt/detekt.yml` (detekt)
 */
class AndroidLintConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("org.jlleitschuh.gradle.ktlint")
                apply("io.gitlab.arturbosch.detekt")
            }

            // ktlint
            extensions.configure<KtlintExtension> {
                android.set(true)
                ignoreFailures.set(false)
                verbose.set(true)
                outputToConsole.set(true)
                filter {
                    exclude { element -> element.file.path.contains("/build/") }
                    exclude { element -> element.file.path.contains("/generated/") }
                }
            }

            // detekt
            extensions.configure<DetektExtension> {
                config.setFrom(rootProject.files("config/detekt/detekt.yml"))
                buildUponDefaultConfig = true
                allRules = false
                ignoreFailures = false
                parallel = true
                basePath = rootProject.projectDir.absolutePath
            }

            // Android Lint
            pluginManager.withPlugin("com.android.application") {
                extensions.configure<ApplicationExtension> {
                    lint {
                        baseline = file("lint-baseline.xml").takeIf { it.exists() }
                        abortOnError = true
                        warningsAsErrors = false
                        checkDependencies = true
                        sarifReport = true
                    }
                }
            }
            pluginManager.withPlugin("com.android.library") {
                extensions.configure<LibraryExtension> {
                    lint {
                        baseline = file("lint-baseline.xml").takeIf { it.exists() }
                        abortOnError = true
                        warningsAsErrors = false
                        checkDependencies = true
                        sarifReport = true
                    }
                }
            }
        }
    }
}
