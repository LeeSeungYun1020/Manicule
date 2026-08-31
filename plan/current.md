# Manicule — 진행 현황

> 참고: [plan.md](plan.md) 기획 · [structure.md](structure.md) 모듈/파일 구조 · [order.md](order.md) 선행 계약/버티컬 레인 · [history/](../history/README.md) 커밋별 의사결정
> 새 세션은 **"다음 실행 가능 작업"**부터 진행.

## 완료

- 최근 검색어 조회·저장·전체 삭제·개별 삭제 Repository와 UseCase
- C1 독서 기록·공유 쿼리 계약
- C2 feature 모듈·Navigation 경계 계약
- C3 상단·탐색, 피드백·상태, 통계·도서 표시 공용 UI 계약
- 독서 리마인더 예약·발행 플랫폼(개인화 알림, 예약 실패 복원, 시스템 시간 변경 시 재예약)

## 버티컬 레인

| 레인 | 기능 | `depends_on` | 상태 | 다음 범위 |
|---|---|---|---|---|
| V1 | 검색 | 없음 | 🚧 진입·최근 검색어·오류 자동 재시도·폴백 완료 | 입력 디바운스·로컬 필터·검색 실행 |
| V2 | 스캔 | 없음 | 🚧 수요 기반 Scanner core 완료 | BarcodeReader lifecycle bind → 조회 UseCase → 권한·인식 UI → 성공/실패/회전 테스트 |
| V3 | 설정 | 없음 | 🚧 리마인더 플랫폼 완료 | 설정 UI·ViewModel → 권한 요청·시간 picker → 테마 연동 테스트 |
| V4 | 책 상세 | 없음 | 🚧 BookDetail 도메인 분리·책 정보 조회·기본 탭 선택 유지·캐시 유지 새로고침 상태·Preview·상태 테스트·필수 서지정보 검증 완료 | 독서 상태 변경 API와 내 기록 상태 UI |
| V5 | 서재 | 상태 변경은 V4 상태 API | 🚧 상태별 목록·빈 상태·책 상세/검색/스캔 이동 완료 | `LibrarySort` 쿼리·UseCase·정렬 UI·테스트 → 오버레이·상태 변경·삭제·Undo |
| V6 | 홈 | V5 서재 조회 계약; V7 공유 집계 | ⏳ 의존성 충족·실행 가능 | 공유 집계·서재 조회 재사용 → 홈 UseCase/UI → 첫 사용자·읽는 중 없음·요약 이동 테스트 |
| V7 | 통계 | 없음 | 🚧 공유 집계 구현 완료 | 독서 달력·날짜별 기록 sheet → 오늘 목록 → 복합 차트·스크롤 테스트 |

## 앱 조립 레인

| 레인 | 범위 | `depends_on` | 상태 |
|---|---|---|---|
| I1 | `ManiculeNavHost`, 최상위 콜백, destination 점진 교체, 앱 루트 테마와 E2E 조립 | 각 destination의 V navigation PR; 루트 테마는 V3 테마 계약 | 🚧 검색·책 상세·서재 destination과 현재 콜백 조립 완료; 나머지는 각 V navigation PR과 V3 테마 계약 대기 |

## 다음 실행 가능 작업

1. V5 상태별 서재 조회와 V7 공유 집계가 모두 머지됐으므로 V6 홈을 시작한다. V5 정렬과 V7 UI 완료는 기다리지 않는다.
2. V4 독서 상태 변경 API를 진행해 V5의 롱프레스 상태 변경 범위를 해소한다.
3. V1 검색 실행, V2 스캔 UI, V3 설정 UI, V5 정렬, V7 통계 UI는 서로 독립적으로 진행할 수 있다.
4. V5 상태 변경은 V4의 상태 API가 머지된 뒤 연결한다.
5. I1은 각 V navigation PR이 머지되는 즉시 해당 destination을 조립하고, V3 테마 계약 뒤 앱 루트 테마를 연결한다.

세부 작업과 공용 API 소유 규칙은 [order.md](order.md)를 따른다. UI 착수 전에는 [ui-ux-guidelines.md](prototype/ui-ux-guidelines.md)의 확정 편차, 색상 기준과 컴포넌트 커버리지를 확인한다.

## 병렬 작업 운영

- 작업 레인은 PR과 1:1이 아니다. 한 레인에서 사용자 행동별로 여러 리뷰 가능한 PR을 순차 생성한다.
- 각 PR은 빌드 가능해야 하며, 변경한 행동과 실패 경로의 테스트를 포함한다. 독립 검증 가능한 공용 계약·플랫폼 기반만 별도 기반 PR로 허용한다.
- 문자열·아이콘·Gradle·이 진행 문서의 단순 추가가 겹치는 것은 허용한다.
- 동일 기능 중복 구현과 기존 공용 API 의미의 동시 변경은 금지한다.
- 여러 작업자가 같은 파일에 항목을 추가해 생긴 단순 병합 충돌은 최종 머지자가 최신 `main` 기준으로 정리한다.
- 공용 API 변경을 기다리는 레인은 해당 소유 PR 머지 후 rebase한다.
- 각 feature route와 navigation 확장 함수는 해당 feature가 소유한다. C2가 stub을 생성하고 V 레인이 같은 파일을 구현하며, `app`의 destination 조립은 I1만 변경한다.
- V3는 테마 설정 저장·조회 계약을 소유하고, I1은 기존 설정 흐름을 `MainActivity`의 루트 `ManiculeTheme`에 연결한다.

### 모듈 추가 절차

1. `settings.gradle.kts`에서 `include(":...")` 주석 해제.
2. 모듈 폴더 + `build.gradle.kts`(convention plugin alias).
3. Android library면 빈 `AndroidManifest.xml` 추가.
4. 패키지 루트 `com.leeseungyun1020.manicule.<group>.<module>` 사용.
5. `app/build.gradle.kts`의 `implementation(projects.…)` 주석 해제.

### 커밋 / history

의사결정 있는 커밋은 [history/](../history/README.md)에 `<short-hash>.md`를 추가한다. 순수 tooling/format 커밋에는 만들지 않는다.
