# ee4c7b1 — ai: context parameter 플래그 제거

**Files**: `AndroidCommon.kt`

## 결정
`-Xcontext-parameters` 컴파일러 플래그 모든 모듈에서 제거.

## 이유
- NIA 패턴 차용 중 의도 없이 포함된 Kotlin 2.0+ 실험 기능.
- 코드베이스 grep 결과 `context(...)` 사용처 0건 → 제거 안전.
- 실험 단계 기능을 무의식적으로 켜두지 않음. 필요할 때 의도적으로 활성.
