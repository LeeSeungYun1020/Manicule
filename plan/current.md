# Manicule — 진행 현황

> 참고: [plan.md](plan.md) 기획 · [structure.md](structure.md) 모듈/파일 구조 · [order.md](order.md) 단계 · [history/](../history/README.md) 커밋별 의사결정
> 새 세션은 **"다음 작업"** 부터 진행.

## 단계

| 단계 | 이름 | 상태 |
|---|---|---|
| 1 | Foundation | ✅ |
| 2 | Core Engine | ✅ |
| 3 | Vertical Slices | ⏳ 진행중 |
| 4 | System Integration Slices | ⏳ |
| 5 | App Assembly | ⏳ |
| 6 | Verification | ⏳ |

## ⏳ 진행 중인 작업 — 공통 컴포넌트(디자인시스템) 개선

### 진행 현황
- 기반 정비 완료: 디자인 시스템 토큰(색상/치수) 도입 및 버튼, 카드, 바텀시트 등 공통 컴포넌트 일괄 개선 적용 ✅
- `ReadingCalendar` (기존 Contribution) 리네임 및 신규 디자인 토큰 적용 ✅
- `ReadingCalendarGrid`의 작은 셀 크기를 유지하면서 그리드 단위 터치 판정과 날짜별 독서량 툴팁 제공 ✅

## ⏳ 대기 중인 작업 — 3단계 Vertical Slices

### 다음 세션에서 진행할 내용 (Slice 1 - feature:search UI 및 연계 컴포넌트)
- Data/Domain 계층 보강: 최근 검색어 CRUD UseCase 및 Repository 구현 (Undo 기능 용도) ⏳ 다음
- 공통 컴포넌트 구현: `ManiculeSearchBar`, `ManiculeSnackbarHost`, `ManiculeChip` 등 `structure.md`에 명시된 검색 화면용 기반 컴포넌트 신규 작성 ⏳ 다음
- UI 계층 구현: SearchScreen, ViewModel 구현 (무한 스크롤, 디바운스, 입력 중 로컬 필터 노출, 빈 상태 안내 카드, 스낵바 Undo 연동 등) ⏳ 다음

## 운영

### 모듈 추가 절차
1. `settings.gradle.kts` 에서 `include(":...")` 주석 해제.
2. 모듈 폴더 + `build.gradle.kts` (convention plugin alias).
3. Android library 면 빈 `AndroidManifest.xml`.
4. 패키지 루트 `com.leeseungyun1020.manicule.<group>.<module>`.
5. `app/build.gradle.kts` 의 `implementation(projects.…)` 주석 해제.


### 커밋 / history
의사결정 있는 커밋은 [history/](../history/README.md) 에 `<short-hash>.md` 추가 (WHY 만). 순수 tooling/format 커밋은 만들지 않음.
