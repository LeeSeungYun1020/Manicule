# Manicule — 진행 현황

> 참고: [plan.md](plan.md) 기획 · [structure.md](structure.md) 모듈/파일 구조 · [order.md](order.md) 단계 · [history/](../history/README.md) 커밋별 의사결정
> 새 세션은 **"다음 작업"** 부터 진행.

## 단계

| 단계 | 이름 | 상태 |
|---|---|---|
| 1 | Foundation | ✅ |
| 2 | Core Engine | ⏳ 다음 |
| 3 | Vertical Slices | ⏳ |
| 4 | System Integration Slices | ⏳ |
| 5 | App Assembly | ⏳ |
| 6 | Verification | ⏳ |

## ⏳ 다음 작업 — 2단계 Core Engine

작업 순서 (1~3 은 병렬 가능). 각 모듈의 파일 구조 / 클래스 명세는 **structure.md 4장** 참고, 테스트 범위는 **structure.md 7장** 참고.

1. **`core:database`** — Room. `manicule.android.library` + `.hilt` + `.room` 적용.
2. **`core:network`** — Retrofit + NLK API. `build.gradle.kts` 에 `platform(libs.okhttp.bom)`, `platform(libs.retrofit.bom)` 직접 선언. `NLK_AUTH_KEY` 는 `local.properties` 로드 방식 결정 필요.
3. **`core:datastore`** — Preferences DataStore.
4. **`core:data`** — Repository 6종 + 매퍼.
5. **`core:domain`** — UseCase 는 슬라이스에서 채움. 골격만.
6. **`core:ui`** — `build.gradle.kts` 에 `platform(libs.coil.bom)` 직접 선언. 잔디 셀 디자인 토큰(크기/간격) 결정 필요.

2단계 완료 후: **3단계 Slice 1 = `feature:search`** (디바운스 350 ms + Paging 3).

## 운영

### 모듈 추가 절차
1. `settings.gradle.kts` 에서 `include(":...")` 주석 해제.
2. 모듈 폴더 + `build.gradle.kts` (convention plugin alias).
3. Android library 면 빈 `AndroidManifest.xml`.
4. 패키지 루트 `com.leeseungyun1020.manicule.<group>.<module>`.
5. `app/build.gradle.kts` 의 `implementation(projects.…)` 주석 해제.

### 출시 전 점검 (1단계에서 미뤄둔 항목)
- `allowBackup=true` 데이터 정밀화 (`dataExtractionRules`) — PR #1 리뷰 지적.
- 다크 모드 Grass 0단계 가시성 — surface 와 색 차이 부족.
- 2단계 완료 시 `app:run` 으로 Hilt 그래프 누락 검증.

### 커밋 / history
의사결정 있는 커밋은 [history/](../history/README.md) 에 `<short-hash>.md` 추가 (WHY 만). 순수 tooling/format 커밋은 만들지 않음.
