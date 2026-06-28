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

### 다음 세션에서 진행할 내용 (트랙 병행 가능)
- **[Track A] core:data** 나머지 Repository 및 DI (PR 2):
    - `LibraryRepository`, `StatsRepository`, `UserPreferencesRepository` 구현
    - Repository Hilt DI 모듈 바인딩
- **[Track B] core:ui** 모듈 생성 및 도서 UI 컴포넌트 구현 (PR 1): ✅
    - `core:ui` 모듈 셋업 (`build.gradle.kts` 등) ✅
    - BookCover, BookListItem 컴포넌트 작성 ✅

1. `core:database` — Room. ✅
2. `core:network` — Retrofit + NLK API. ✅
3. `core:datastore` — Preferences DataStore. ✅
- Track A (Data)
  - 4-1. `core:data` (Mapper) — 매퍼 구현. ✅
  - 4-2. `core:data` (Repo 1) — 일부 Repository 구현. ✅
  - 4-3. `core:data` (Repo 2) — 나머지 Repository 및 DI. ⏳ 다음
  - 5. `core:domain` — UseCase skeleton.
- Track B (UI)
  - 6-1. `core:ui` (도서) — Book Cover, Item UI 컴포넌트. ✅
  - 6-2. `core:ui` (기타) — ProgressBar, ContributionGrid. ⏳ 다음

## 운영

### 모듈 추가 절차
1. `settings.gradle.kts` 에서 `include(":...")` 주석 해제.
2. 모듈 폴더 + `build.gradle.kts` (convention plugin alias).
3. Android library 면 빈 `AndroidManifest.xml`.
4. 패키지 루트 `com.leeseungyun1020.manicule.<group>.<module>`.
5. `app/build.gradle.kts` 의 `implementation(projects.…)` 주석 해제.

### 출시 전 점검 (1단계에서 미뤄둔 항목)
- 2단계 완료 시 `app:run` 으로 Hilt 그래프 누락 검증.

### 커밋 / history
의사결정 있는 커밋은 [history/](../history/README.md) 에 `<short-hash>.md` 추가 (WHY 만). 순수 tooling/format 커밋은 만들지 않음.
