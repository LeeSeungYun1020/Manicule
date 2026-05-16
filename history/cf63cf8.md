# cf63cf8 — ai: Firebase 컨벤션 플러그인 + BoM 적용

**Files**: `AndroidApplicationFirebaseConventionPlugin.kt`, `libs.versions.toml`

## 결정
Firebase BoM 34.13.0 도입 + 전용 convention plugin. 다른 convention plugin 이 자동 적용하지 않음. `app` 에도 아직 미적용.

## 이유
- Firebase Crashlytics 공식 가이드가 BoM 권장 → analytics ↔ crashlytics 호환성 보장.
- `google-services.json` 없으면 plugin 이 빌드 실패 → 콘솔 연결 전까지 alias 등록만, 적용은 보류.
- 활성화 절차: 콘솔 앱 등록 → `google-services.json` 을 `app/` 에 배치 → `app` plugins 에 `alias(libs.plugins.manicule.android.application.firebase)` 추가.
