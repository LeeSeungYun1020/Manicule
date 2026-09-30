# 읽음표 - 나의 독서 기록

**Manicule — A Reader's Mark**

읽고 싶은 책부터 다 읽은 책까지, 독서 상태와 감상을 한곳에 모으는 개인 서재 앱.<br>
바코드 스캔이나 검색으로 책을 찾고, 읽은 페이지를 기록하며 독서 습관을 만듭니다.

| 대상                   | 활용                        |
|----------------------|---------------------------|
| 읽은 책을 기록하고 싶은 사람     | 서재·별점·메모로 독서 이력 정리        |
| 꾸준히 읽는 습관을 만들고 싶은 사람 | 독서 달력·통계·리마인더 활용          |
| 나중에 읽을 책을 모아두는 사람    | 검색·스캔으로 찾은 책을 ‘읽고 싶음’에 보관 |

## 주요 기능

### 홈

- 독서 달력, 연속 기록, 오늘 읽은 페이지를 한 눈에 확인
- 첫 사용자와 다시 돌아온 복귀자를 위한 시작 안내
- 빠른 검색, 스캔

<img src="screenshot/home.png" width="240" alt="홈: 독서 요약과 읽는 중인 책">

<details>
<summary>첫 사용·데이터 상태별 홈 화면</summary>

|                                 첫 사용 안내                                 |                                      오늘 기록 없음                                       |
|:-----------------------------------------------------------------------:|:-----------------------------------------------------------------------------------:|
| <img src="screenshot/home-onboarding.png" width="240" alt="홈: 첫 사용 안내"> | <img src="screenshot/home-no-today.png" width="240" alt="어제까지 기록은 있지만 오늘 기록이 없는 홈"> |

|                                     기록 없음 · 대기 도서 있음                                     |                                      기록 없음 · 읽는 중 도서 있음                                       |
|:----------------------------------------------------------------------------------------:|:---------------------------------------------------------------------------------------------:|
| <img src="screenshot/home-want-no-records.png" width="240" alt="독서 기록 없이 읽고 싶은 책만 있는 홈"> | <img src="screenshot/home-reading-no-records.png" width="240" alt="읽는 중인 책은 있지만 독서 기록이 없는 홈"> |

|                               기록 있음 · 대기 도서 있음 · 읽는 중 없음                                |                                  기록 있음 · 대기 도서 없음 · 읽는 중 없음                                  |
|:---------------------------------------------------------------------------------------:|:--------------------------------------------------------------------------------------------:|
| <img src="screenshot/home-no-reading-want.png" width="240" alt="독서 요약과 고르기 버튼이 표시되는 홈"> | <img src="screenshot/home-no-reading-no-want.png" width="240" alt="독서 요약과 검색·스캔 버튼이 표시되는 홈"> |

</details>

### 책 검색·바코드 스캔

- 제목·저자·ISBN으로 국내 도서 검색, 검색 결과 페이지 단위 로딩
- 최근 검색어 저장·입력 중 필터링·삭제 실행취소
- 카메라로 바코드를 인식해 도서 조회, 권한 거부·인식 실패 시 텍스트 검색 안내

|                               최근 검색어                               |                                검색 결과                                 |                           바코드 스캔                            |
|:------------------------------------------------------------------:|:--------------------------------------------------------------------:|:-----------------------------------------------------------:|
| <img src="screenshot/search-history.png" width="220" alt="최근 검색어"> | <img src="screenshot/search-results.png" width="220" alt="도서 검색 결과"> | <img src="screenshot/scanner.png" width="220" alt="바코드 스캔"> |

<details>
<summary>검색 기록 없음</summary>

<img src="screenshot/search-empty.png" width="240" alt="최근 검색어가 없는 검색 화면">

</details>

### 책 정보·내 기록

- 도서 정보·소개·목차 조회, 독서 상태(읽고 싶음, 읽는 중, 다 읽음) 관리
- 별점·메모를 화면에서 바로 편집하고 저장
- 독서 후 날짜·시각·페이지 기록하여 진도 확인, 기록 수정·삭제·실행취소

|                                책 정보                                |                                    내 기록                                     |
|:------------------------------------------------------------------:|:---------------------------------------------------------------------------:|
| <img src="screenshot/book-info.png" width="240" alt="책 정보와 소개·목차"> | <img src="screenshot/book-records.png" width="240" alt="독서 상태·별점·메모·독서 기록"> |

<details>
<summary>기록 추가·빈 상태</summary>

|                                   기록 추가                                   |                                       기록 없음                                       |
|:-------------------------------------------------------------------------:|:---------------------------------------------------------------------------------:|
| <img src="screenshot/record-editor.png" width="240" alt="날짜·시각·쪽수 기록 시트"> | <img src="screenshot/book-records-empty.png" width="240" alt="별점·메모·독서 기록이 없는 책"> |

</details>

### 서재

- 독서 상태별 책 목록 관리(읽고 싶음, 읽는 중, 다 읽음)
- 읽는 중 탭에서는 책갈피 길이로 읽은 페이지를 알려 주고, 다 읽음 탭에서는 다 읽은 날짜 알려줌
- 추가한 날짜·수정한 날짜·별점과 정렬 방향 선택하여 정렬 가능
- 책을 길게 눌러 상태 변경·삭제, 실행취소 지원

|                                읽고 싶음                                 |                                   읽는 중                                    |                                   다 읽음                                   |
|:--------------------------------------------------------------------:|:-------------------------------------------------------------------------:|:------------------------------------------------------------------------:|
| <img src="screenshot/library-want.png" width="220" alt="읽고 싶은 책 목록"> | <img src="screenshot/library-reading.png" width="220" alt="읽는 중인 책과 진도율"> | <img src="screenshot/library-finished.png" width="220" alt="다 읽은 책과 날짜"> |

<details>
<summary>빈 서재·진도 0%·정렬</summary>

|                              빈 서재                               |                                            진도 0%                                             |                                    정렬                                     |
|:---------------------------------------------------------------:|:--------------------------------------------------------------------------------------------:|:-------------------------------------------------------------------------:|
| <img src="screenshot/library-empty.png" width="220" alt="빈 서재"> | <img src="screenshot/library-reading-zero.png" width="220" alt="독서 기록이 없어 책갈피가 없는 진도 0% 서재"> | <img src="screenshot/library-sort.png" width="220" alt="서재 정렬 기준과 방향 선택"> |

</details>

### 독서 통계

- 오늘·4주·1년·직접 선택 기간별 독서량 확인
- 독서량을 색으로 표시하는 독서 달력, 기간 내 최장 연속 기록·읽은 페이지·읽은 책 통계 제공
- 오늘 읽은 책 목록·날짜별 기록 조회
- 읽은 책(막대)·페이지(꺾은선) 복합 차트, 일·주·월 집계 단위 전환 가능

|                                오늘 읽은 책                                 |                                    4주 통계                                    |                                     일별 차트                                      |
|:----------------------------------------------------------------------:|:---------------------------------------------------------------------------:|:------------------------------------------------------------------------------:|
| <img src="screenshot/stats-today.png" width="220" alt="오늘의 독서량과 읽은 책"> | <img src="screenshot/stats-period.png" width="220" alt="4주 독서 달력·요약·복합 차트"> | <img src="screenshot/stats-chart-daily.png" width="220" alt="일별 읽은 책·페이지 그래프"> |

<details>
<summary>다른 기간·주/월 차트·날짜별 기록·빈 상태</summary>

|                                1년 통계                                 |                                직접 선택한 기간                                |
|:--------------------------------------------------------------------:|:-----------------------------------------------------------------------:|
| <img src="screenshot/stats-year.png" width="240" alt="1년 독서 달력과 요약"> | <img src="screenshot/stats-custom.png" width="240" alt="직접 선택한 기간의 통계"> |

|                                  주별 차트                                   |                                       월별 차트 · 1년                                        |
|:------------------------------------------------------------------------:|:---------------------------------------------------------------------------------------:|
| <img src="screenshot/stats-chart.png" width="240" alt="주별 읽은 책·페이지 그래프"> | <img src="screenshot/stats-chart-monthly.png" width="240" alt="1년 기간의 월별 읽은 책·페이지 그래프"> |

|                                 날짜별 기록                                  |                                 기간 설정                                  |
|:-----------------------------------------------------------------------:|:----------------------------------------------------------------------:|
| <img src="screenshot/stats-day.png" width="240" alt="선택한 날짜의 독서 기록 시트"> | <img src="screenshot/stats-range.png" width="240" alt="통계 시작일·종료일 설정"> |

</details>

### 설정

- 시스템·라이트·다크 테마 선택
- 매일 독서 리마인더 예약, 읽는 중인 책을 반영한 알림 메시지
- 앱 버전 확인, 오픈소스 목록 제공

<img src="screenshot/settings.png" width="240" alt="테마·독서 리마인더·지원 설정">

<details>
<summary>리마인더·테마·오픈소스 라이선스</summary>

|                                         리마인더 켜짐                                         |                                   리마인더 시간 설정                                   |
|:---------------------------------------------------------------------------------------:|:------------------------------------------------------------------------------:|
| <img src="screenshot/settings-reminder-on.png" width="240" alt="리마인더를 켜면 나타나는 시간 설정 행"> | <img src="screenshot/settings-reminder-time.png" width="240" alt="리마인더 시각 선택"> |

|                                 다크 테마                                  |                                오픈소스 라이선스                                 |
|:----------------------------------------------------------------------:|:------------------------------------------------------------------------:|
| <img src="screenshot/settings-dark.png" width="240" alt="다크 테마 설정 화면"> | <img src="screenshot/licenses.png" width="240" alt="실제 앱의 오픈소스 라이선스 목록"> |

</details>

## 기술 스택

| 분류       | 기술                                                                                                |
|----------|---------------------------------------------------------------------------------------------------|
| 언어·비동기   | Kotlin, Coroutines, Flow·StateFlow, kotlinx-datetime                                              |
| UI       | Jetpack Compose, Material 3, WindowSizeClass, Compose Canvas(통계 차트)                               |
| 상태·화면 이동 | ViewModel, SavedStateHandle, Lifecycle, Navigation Compose(타입 안전한 Route)                          |
| 의존성 주입   | Hilt, KSP                                                                                         |
| 로컬 저장    | Room, Preferences DataStore                                                                       |
| 네트워크·직렬화 | Retrofit, OkHttp, kotlinx.serialization, 국립중앙도서관 ISBN 서지정보 API                                    |
| 목록·이미지   | Paging 3, Coil 3                                                                                  |
| 바코드      | CameraX, ML Kit Barcode Scanning                                                                  |
| 리마인더     | WorkManager, Android 알림 채널                                                                        |
| 빌드       | Gradle Kotlin DSL, Version Catalog, Convention Plugins, AGP 9.2, Java 17, core library desugaring |
| 테스트      | JUnit 4, Truth, Coroutines Test, Turbine, MockWebServer, Compose UI Test, AndroidX Test           |
| 정적 분석    | ktlint, detekt, Android Lint                                                                      |

- 지원 기준: **Android 7.0(API 24) 이상**, compile / target SDK **36**
- 도서 검색·정보 최초 조회에 네트워크 필요하며, 서재·독서 기록은 로컬에 저장하여 오프라인 우선 동작
- 라이브러리 버전: [Version Catalog](gradle/libs.versions.toml)에서 확인

## 아키텍처

### Android App Architecture

UI → Domain → Data의 3계층 구조와 단방향 데이터 흐름(UDF)을 사용합니다.

| 계층     | 구성                          | 책임                             |
|--------|-----------------------------|--------------------------------|
| UI     | Compose, ViewModel, UiState | 사용자 이벤트 처리·화면 상태 관찰            |
| Domain | UseCase                     | 독서 상태 전환·기록 저장·통계 집계 등 비즈니스 규칙 |
| Data   | Repository, DataSource      | 로컬·원격 데이터 접근과 DTO·Entity 변환    |

- **UDF**: UI 이벤트 → ViewModel → UseCase, `StateFlow<UiState>` → Compose 렌더링
- **로컬 데이터 중심**: Room에 도서·서재·기록을 저장하고 Flow로 관찰, 설정은 DataStore에 저장
- **데이터 경계**: Repository의 계약과 구현은 `core:data`에 두고 외부에는 도메인 모델을 노출
- **화면 경계**: feature끼리 직접 의존하지 않으며, `app`이 화면 이동 콜백·백스택·DI를 조립
- **플랫폼 경계**: `core:notifications`가 Domain의 리마인더 계약을 WorkManager로 구현

### 모듈 의존 방향

화살표는 의존하는 쪽 → 의존 대상입니다. 공통 `core:model`·`core:common` 의존은 생략했습니다.

```mermaid
flowchart TD
    app[app] --> features[feature:*]
    app --> domain[core:domain]
    app --> design[core:designsystem]
    app --> notifications[core:notifications]
    features --> domain
    features --> ui[core:ui]
    features --> design
    scannerFeature[feature:scanner] --> scanner[core:scanner]
    domain --> data[core:data]
    domain --> scanner
    data --> database[core:database]
    data --> network[core:network]
    data --> datastore[core:datastore]
    ui --> design
    notifications --> domain
```

`feature:scanner`는 공통 feature 의존성에 더해 카메라 Preview 연결을 위해 `core:scanner`를 직접 참조합니다.

### 멀티 모듈 구성

앱 1개 + feature 7개 + core 11개로 구성합니다. `build-logic`은 빌드 설정을 공유하는 별도 included build입니다.

```text
Manicule/
├── app/                    # Application·Activity·Navigation·DI 조립
├── feature/
│   ├── home/               # 홈·시작 안내·독서 요약
│   ├── search/             # 도서 검색·최근 검색어
│   ├── scanner/            # 스캔 화면·권한·카메라 Preview
│   ├── bookdetail/         # 책 정보·리뷰·독서 기록
│   ├── library/            # 상태별 서재·정렬
│   ├── stats/              # 독서 달력·기간별 통계·차트
│   └── settings/           # 테마·알림 설정·라이선스
├── core/
│   ├── designsystem/       # 테마·디자인 토큰·공통 컴포넌트
│   ├── ui/                 # 도서 카드·표지·독서 달력 등 공유 UI
│   ├── common/             # Dispatcher·시간·Result·공통 유틸리티
│   ├── model/              # 도서·서재·독서 기록 모델
│   ├── domain/             # UseCase·리마인더 계약
│   ├── data/               # Repository 계약·구현·매퍼
│   ├── database/           # Room Database·DAO·Entity
│   ├── datastore/          # 사용자 설정 저장
│   ├── network/            # 국립중앙도서관 API 클라이언트
│   ├── scanner/            # CameraX·ML Kit 바코드 분석
│   └── notifications/      # WorkManager 예약·알림 발송
├── build-logic/            # Gradle Convention Plugins
├── plan/                   # 기획·구조·진행 현황·UI 프로토타입
└── screenshot/             # README 화면 캡처
```
