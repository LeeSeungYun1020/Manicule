# 0f0c1fa — fix: BoM 적용

**Files**: `gradle/libs.versions.toml`, `AndroidCommon.kt`, `JvmLibraryConventionPlugin.kt`

## 결정
Compose / kotlinx-coroutines / kotlinx-serialization / OkHttp / Retrofit / Coil 모두 BoM 좌표로 통일.

## 이유
- 모듈 늘면 같은 family 안 sub-artifact 버전이 drift.
- BoM = `platform(...)` 은 버전 제약만 추가, 클래스 안 끌어옴 → 안 쓰는 모듈에 적용해도 비용 0.
- BoM constraint 는 Gradle classpath 상속(`testCompileClasspath`/`androidTestCompileClasspath` ← `compileClasspath` ← `implementation`)으로 test / androidTest 까지 자동 전파 → `implementation` 한 곳에만 추가하면 충분. `testImplementation(platform(bom))` 등 명시 추가는 중복.
- 이 시점엔 OkHttp / Retrofit / Coil 도 모든 Android 모듈에 자동 적용 → 후에 [5942347](5942347.md) 에서 분리.
