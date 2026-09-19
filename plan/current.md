# Manicule — 진행 현황

> 참고: [plan.md](plan.md) 기획 · [structure.md](structure.md) 모듈/파일 구조 · [order.md](order.md) 선행 계약/버티컬 레인 · [history/](../history/README.md) 커밋별 의사결정
> 새 세션은 **"다음 실행 가능 작업"**부터 진행.

## 완료

- 최근 검색어 조회·저장·전체 삭제·개별 삭제 Repository와 UseCase
- C1 독서 기록·공유 쿼리 계약
- C2 feature 모듈·Navigation 경계 계약
- C3 상단·탐색, 피드백·상태, 통계·도서 표시 공용 UI 계약
- 독서 리마인더 예약·발행 플랫폼(개인화 알림, 예약 실패 복원, 시스템 시간 변경 시 재예약)
- 리마인더 설정 UI(조회·토글·시간 변경·알림 권한·시스템 설정 복구·영역별 로딩·오류 처리·실패 재시도)

## 버티컬 레인

| 레인 | 기능 | `depends_on` | 상태 | 다음 범위 |
|---|---|---|---|---|
| V1 | 검색 | 없음 | 🚧 진입·최근 검색어·로컬 필터·검색 실행·Paging 결과·상세 선택·복귀 상태·비활성 스캔 버튼·상태 테스트 완료 | 최근 검색어 개별·전체 삭제와 Undo; V2 준비 후 스캔 이동 조립 |
| V2 | 스캔 | 없음 | 🚧 권한 안내·CameraX Preview·lifecycle·표시 회전·4a/4c UI 완료 | 바코드 인식 → ISBN 조회 → 책 상세 이동·실기기 인식 검증 |
| V3 | 설정 | 없음 | 🚧 리마인더 설정 UI 완료 | 테마·라이선스·버전 |
| V4 | 책 상세 | 없음 | 🚧 책 정보 조회·탭 유지·캐시 새로고침, 독서 상태 변경 API·미등록/UNSET 미선택 UI·최초 서재 등록·완독일·저장 실패 재시도·테스트 완료; 독서 기록 추가 API·미등록/UNSET 및 첫 WANT 기록 자동 전환·원자적 저장·실패 복구 테스트 완료 | 독서 기록 목록·추가 시트와 저장 연결 → 완독 확인; 별점·메모 인라인 저장 |
| V5 | 서재 | 상태 변경은 V4 상태 API | 🚧 상태별 목록·빈 상태·책 상세/검색/스캔 이동·정렬 완료 | 진행률/완독일 오버레이 → 롱프레스 상태 변경·삭제·Undo |
| V6 | 홈 | V5 서재 조회 계약; V7 공유 집계 | 🚧 서재 초기 탭 진입 계약 완료 | 홈 상태 조합·첫 사용자 화면 |
| V7 | 통계 | 없음 | 🚧 공유 집계·월요일 기준 공용 달력 선택·밀집형 고정 셀·높이/패딩 제약 대응 완료 | 통계 달력 조립·날짜별 기록 sheet → 오늘 목록 → 복합 차트·스크롤 테스트 |

## 앱 조립 레인

| 레인 | 범위 | `depends_on` | 상태 |
|---|---|---|---|
| I1 | `ManiculeNavHost`, 점진적 cross-destination 연결, 전역 백스택 정책, 앱 루트 테마와 E2E 조립 | 각 destination의 V navigation PR; 루트 테마는 V3 테마 계약 | 🚧 검색·책 상세·서재 destination과 검색 결과 → 책 상세 콜백 조립 완료; 검색 → 스캔은 V2 준비 대기, 나머지는 각 V navigation PR과 V3 테마 계약 대기 |

## 다음 실행 가능 작업

1. V5 다음 PR로 읽는 중 진도율과 다 읽음 완독일 오버레이를 구현한다. V4 독서 상태 변경 API가 머지되었으므로 롱프레스 상태 변경 범위를 연결할 수 있다.
2. V6 선행 작업인 서재 초기 탭 진입 계약이 머지되었으므로 홈 상태 조합·첫 사용자 화면을 진행한다. V7 UI 완료는 기다리지 않는다.
3. V4는 독서 기록 추가 API를 기반으로 목록·추가 시트와 저장 연결을 진행하고, 완독 확인 판정 UseCase를 후속 구현한다. 별점·메모 인라인 저장과 기록 수정·삭제·Undo는 남은 범위다.
4. V1 최근 검색어 개별·전체 삭제와 Undo, V2 스캔 UI, V3 테마·라이선스·버전, V7 통계 UI는 서로 독립적으로 진행할 수 있다.
5. I1은 각 V navigation PR이 머지되는 즉시 cross-destination 이동을 점진 조립하고, V3 테마 계약 뒤 앱 루트 테마를 연결한다.

세부 작업과 공용 API 소유 규칙은 [order.md](order.md)를 따른다. UI 착수 전에는 [ui-ux-guidelines.md](prototype/ui-ux-guidelines.md)의 확정 편차, 색상 기준과 컴포넌트 커버리지를 확인한다.

## 병렬 작업 운영

- 작업 레인은 PR과 1:1이 아니다. 한 레인에서 사용자 행동별로 여러 리뷰 가능한 PR을 순차 생성한다.
- 각 PR은 빌드 가능해야 하며, 변경한 행동과 실패 경로의 테스트를 포함한다. 독립 검증 가능한 공용 계약·플랫폼 기반만 별도 기반 PR로 허용한다.
- 문자열·아이콘·Gradle·이 진행 문서의 단순 추가가 겹치는 것은 허용한다.
- 동일 기능 중복 구현과 기존 공용 API 의미의 동시 변경은 금지한다.
- 여러 작업자가 같은 파일에 항목을 추가해 생긴 단순 병합 충돌은 최종 머지자가 최신 `main` 기준으로 정리한다.
- 공용 API 변경을 기다리는 레인은 해당 소유 PR 머지 후 rebase한다.
- 각 feature route와 navigation 확장 함수는 해당 feature가 소유한다. destination-local 이동(뒤로가기 등)은 V 레인이 app 최소 연결까지 완료하며, cross-destination 이동 조립과 전역 백스택 정책은 I1이 소유한다.
- V3는 테마 설정 저장·조회 계약을 소유하고, I1은 기존 설정 흐름을 `MainActivity`의 루트 `ManiculeTheme`에 연결한다.

### 모듈 추가 절차

1. `settings.gradle.kts`에서 `include(":...")` 주석 해제.
2. 모듈 폴더 + `build.gradle.kts`(convention plugin alias).
3. Android library면 빈 `AndroidManifest.xml` 추가.
4. 패키지 루트 `com.leeseungyun1020.manicule.<group>.<module>` 사용.
5. `app/build.gradle.kts`의 `implementation(projects.…)` 주석 해제.

### 커밋 / history

의사결정 있는 커밋은 [history/](../history/README.md)에 `<short-hash>.md`를 추가한다. 순수 tooling/format 커밋에는 만들지 않는다.
