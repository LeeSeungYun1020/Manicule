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
- `ManiculeButton`, `ManiculeTextButton`, `ManiculeOutlinedButton`: Stadium 형태 및 크기/아이콘 위치 개선 ✅
- `ManiculeIconButton`: 공통 아이콘 버튼 추가 ✅
- `ManiculeCard`, `ManiculeDashedCard`: 외곽선 및 점선 카드 추가 ✅
- `ManiculeEmptyState`: DashedCard 적용 ✅
- `ManiculeDialog`: 커스텀 Dialog 형태로 개편 및 아이콘 추가 적용 ✅
- `ManiculeBottomSheet`: ModalBottomSheet 형태의 공통 바텀시트 추가 ✅
- `ManiculeSegmentedButton`: Segmented 버튼 컴포넌트 추가 ✅
- 디자인 시스템 토큰(색상/확장색상/치수) 도입 및 기반 정비 ✅
- `core:designsystem` 공통 컴포넌트(`ManiculeTopAppBar`, `ManiculeEmptyState`, `ManiculeDialog` 등) 토큰 적용 완료 ✅
- `ReadingCalendar` (기존 Contribution) 리네임 및 신규 디자인 토큰 적용 ✅

## ⏳ 대기 중인 작업 — 3단계 Vertical Slices

### 다음 세션에서 진행할 내용 (Slice 1 - feature:search)
- DAO/Repository 계층 보강: NLK API PagingSource 구현 및 검색 쿼리 추가 ✅
- Domain 계층 구현: SearchBooksUseCase Flow<PagingData> 로직 등 ✅
- Data/Domain 계층 보강: GetRecentQueriesUseCase, SaveRecentQueryUseCase, DeleteRecentQueryUseCase 구현 (최근 검색어 및 Undo 기능 용도) ⏳ 다음
- UI 계층 구현: SearchScreen, ViewModel, UiState 작성 (무한 스크롤, 디바운스, 입력 중 최근 검색어 로컬 필터 노출, 최근 검색어 없음 안내 카드, 최근 검색어 개별 삭제 스낵바 Undo, 총 검색 건수 표시, 검색 결과 없음 UI 및 스캔 화면 이동 연동 등) ⏳ 다음

## 운영

### 모듈 추가 절차
1. `settings.gradle.kts` 에서 `include(":...")` 주석 해제.
2. 모듈 폴더 + `build.gradle.kts` (convention plugin alias).
3. Android library 면 빈 `AndroidManifest.xml`.
4. 패키지 루트 `com.leeseungyun1020.manicule.<group>.<module>`.
5. `app/build.gradle.kts` 의 `implementation(projects.…)` 주석 해제.


### 커밋 / history
의사결정 있는 커밋은 [history/](../history/README.md) 에 `<short-hash>.md` 추가 (WHY 만). 순수 tooling/format 커밋은 만들지 않음.
