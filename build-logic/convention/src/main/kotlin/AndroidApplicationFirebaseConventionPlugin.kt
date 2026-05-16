import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByType

/**
 * Application 모듈에 Firebase 관련 Gradle 플러그인과 BoM 기반 의존성을 적용한다.
 *
 * - `com.google.gms.google-services` — google-services.json 처리
 * - `com.google.firebase.crashlytics` — Crashlytics 매핑 파일 업로드
 * - Firebase BoM(`libs.firebase.bom`) 으로 버전을 통일 관리하고 Crashlytics / Analytics 의존성 추가
 *
 * ## 사용 방법
 *
 * `app/build.gradle.kts` 의 plugins 블록에서 명시적으로 적용한다 — 본 플러그인은
 * 다른 컨벤션 플러그인이 자동으로 끌어가지 않는다 (Firebase 프로젝트가 연결되지 않은
 * 상태에서 적용하면 google-services 플러그인이 빌드를 실패시키기 때문).
 *
 * ```kotlin
 * plugins {
 *     alias(libs.plugins.manicule.android.application)
 *     alias(libs.plugins.manicule.android.application.firebase)
 * }
 * ```
 *
 * 적용 전 체크리스트:
 * 1. Firebase 콘솔에서 Android 앱을 등록하고 `google-services.json` 을 `app/` 에 추가
 * 2. 본 컨벤션 플러그인을 `app/build.gradle.kts` 에 적용 — BoM·Crashlytics·Analytics
 *    의존성이 자동으로 추가된다 (개별 라이브러리 버전은 BoM 이 결정)
 */
class AndroidApplicationFirebaseConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("com.google.gms.google-services")
                apply("com.google.firebase.crashlytics")
            }

            val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")
            dependencies {
                val bom = libs.findLibrary("firebase-bom").get()
                add("implementation", platform(bom))
                add("implementation", libs.findLibrary("firebase-analytics").get())
                add("implementation", libs.findLibrary("firebase-crashlytics").get())
            }
        }
    }
}
