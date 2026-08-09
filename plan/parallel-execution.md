# Manicule — 6개 Agent 병렬 실행 계획

> 이 문서는 [order.md](order.md)의 버티컬 레인 중 현재 독립 실행 가능한 첫 PR 6개를 AI Agent에 배정하고, 구현부터 Ready PR 생성까지 자동 완료하기 위한 실행 기준이다.

## 1. 실행 목표

- V1, V2, V3, V4, V5, V7의 첫 리뷰 단위를 각각 독립 Agent에 배정한다.
- 모든 Agent는 동일한 최신 `origin/main`을 기준으로 격리된 Git worktree와 `ai/` 브랜치를 사용한다.
- Agent는 계획 확인, 공식 문서 조사, 구현, 테스트, 커밋, 최신 `main` 반영, push, Ready PR 생성과 상태 확인까지 담당한다.
- V6와 I1은 이번 배치에서 구현하지 않는다. V5와 V7의 공유 계약이 머지된 뒤 별도 배치로 시작한다.
- 6개 작업은 논리적으로 동시에 시작한다. 실행 환경의 동시 Agent 슬롯이 6개보다 적으면 가용 슬롯만큼 즉시 실행하고, 완료된 슬롯에 대기 작업을 자동 투입한다. Agent 간 기능 완료 대기는 두지 않는다.

## 2. 자동 진행 규칙

### 2.1 Agent 시작

1. Coordinator는 `git fetch origin main` 후 기준 SHA를 고정한다.
2. 기준 SHA에서 Agent별 격리 worktree와 아래 지정 브랜치를 만든다. Agent끼리 worktree를 공유하거나 같은 worktree에서 브랜치를 전환하지 않는다.
3. 각 Agent는 [skills/work.md](../skills/work.md), [skills/code-change.md](../skills/code-change.md), [skills/pr-create.md](../skills/pr-create.md)의 구현·검증·PR 절차를 사용한다.
4. 구현 전 `android-cli`로 작업에 필요한 Android와 Compose 공식 문서를 검색하고, [Kotlin coding conventions](https://kotlinlang.org/docs/coding-conventions.html)을 확인한다. UI Agent는 [UI/UX 가이드](prototype/ui-ux-guidelines.md)도 반드시 확인한다.
5. 조사 결과는 구현 판단에 바로 반영한다. 라이브러리 버전은 기존 version catalog와 공식 안정 버전을 우선하며, 계획 범위를 넓히는 업그레이드는 하지 않는다.

### 2.2 사용자 개입 없이 진행하는 범위

- 준비 단계의 코드 전문, 파일별 diff, 수정 방향 질문과 파일별 승인은 출력하지 않고 계획에 따라 자동 진행한다.
- 구현 중의 일반적인 세부 판단은 기존 코드, 이 문서, `plan/` 문서, 공식 Android·Compose·Kotlin 문서를 우선순위로 삼아 Agent가 결정한다.
- 테스트 실패는 원인을 진단하고 계획 범위 안에서 수정한 뒤 재실행한다.
- 한 Agent가 막혀도 다른 Agent는 중단하지 않는다.
- 다음 경우에만 해당 Agent를 중단하고 Coordinator에 blocker를 보고한다.
  - GitHub 인증, push 권한 또는 PR 생성 권한 실패
  - 최신 `main`과 공개 API의 의미가 충돌해 양쪽을 보존할 수 없음
  - 계획에 없는 DB migration이나 파괴적 데이터 변경이 필요함
  - 외부 비밀값이나 사용자만 제공할 수 있는 정보가 필요함
  - 범위 내 자동 수정 후에도 동일한 빌드·테스트 실패가 재현됨

### 2.3 커밋과 Ready PR

- 각 커밋은 빌드 가능한 한 가지 구현 단위를 담는다. Agent별 권장 커밋은 아래 레인 계획을 따른다.
- 의사결정이 있는 커밋은 [history 규칙](../history/README.md)에 따라 기록하고, 완료 시 `plan/current.md`에서 자기 레인 행만 갱신한다.
- 기능별 검증 후 최신 `origin/main`을 반영하고 충돌을 해결한 뒤 전체 `check`를 다시 실행한다.
- 최근 머지 PR 3개의 형식을 확인하고 base `main`, assignee `LeeSeungYun1020`, reviewer `lsy-auto`로 Draft가 아닌 Ready PR을 만든다.
- `gh pr status`와 PR 상세 조회로 Ready 상태, base, assignee, reviewer, checks 등록을 확인해야 Agent 작업이 완료된다.
- 이번 배치의 완료 범위는 Ready PR 생성까지다. merge, 리뷰, 리뷰 반영과 리뷰 검수는 후속 배치다.

## 3. Agent 배정 요약

| Agent | 레인 | 브랜치 | 첫 Ready PR | 다른 Agent 의존성 |
|---|---|---|---|---|
| A1 | V1 검색 | `ai/v1-search-entry-history` | `feat: 검색 진입과 최근 검색어 화면 구현` | 없음 |
| A2 | V2 스캔 | `ai/v2-scanner-core` | `feat: CameraX 바코드 분석 기반 구현` | 없음 |
| A3 | V3 설정 | `ai/v3-reminder-platform` | `feat: 독서 리마인더 플랫폼 구현` | 없음 |
| A4 | V4 책 상세 | `ai/v4-book-info` | `feat: 책 정보 조회와 상세 기본 화면 구현` | 없음 |
| A5 | V5 서재 | `ai/v5-library-status-list` | `feat: 서재 상태별 목록 구현` | 없음 |
| A6 | V7 통계 | `ai/v7-shared-stats-aggregation` | `feat: 공유 독서 통계 집계 구현` | 없음 |

## 4. A1 — V1 검색 진입과 최근 검색어

### 범위와 소유권

- `feature:search`만 소유하며 공용 디자인시스템, `app`, 다른 feature를 수정하지 않는다.
- `SearchNavigation` stub을 실제 destination으로 교체하고 `SearchViewModel`, immutable `SearchUiState`, Route/Screen, 최근 검색어 목록과 빈 상태를 구현한다.
- 기존 `GetRecentQueriesUseCase(limit = 10)`와 Repository 계약을 변경 없이 사용한다.
- 기존 app 호출이 계속 컴파일되도록 navigation callback은 기본 no-op을 제공한다.

```kotlin
fun NavGraphBuilder.searchScreen(
    onNavigateBack: () -> Unit = {},
    onBookSelected: (String) -> Unit = {},
    onNavigateToScanner: () -> Unit = {},
)
```

### 완료 조건

- 최근 검색어가 없으면 `ManiculeSearchBar`와 검색 유도 `ManiculeEmptyState`를 표시한다.
- 최근 검색어가 있으면 최신순 목록, `ManiculeSectionHeader`, M3 `ListItem`, divider를 표시한다.
- Loading, 조회 Error, retry를 제공하고 앱바 대신 검색바 leading icon으로 뒤로간다.
- lifecycle-aware 수집, UDF, 문자열 리소스, 48dp 터치 타겟, semantics, IME/inset을 적용한다.
- Light/Dark, 긴 검색어, 큰 글꼴, Phone/Foldable/Tablet Preview와 UI 테스트를 작성한다.

### 제외 범위

- 350ms 디바운스, 입력 중 필터, 검색 제출과 Paging 결과
- 최근 검색어 개별/전체 삭제와 Undo
- 책 선택·스캔 callback 실제 연결과 `app` 조립

### 검증과 커밋

- ViewModel의 Loading→Content, 빈 목록, 재방출, 오류와 retry를 검증한다.
- Compose에서 빈 상태/목록/로딩/오류/뒤로가기/초기 포커스/다중 폭을 검증한다.
- `:feature:search:testDebugUnitTest`, `:feature:search:connectedDebugAndroidTest`, `ktlintCheck`, 전체 `check`를 통과한다.
- Android CLI로 AVD와 Preview 렌더링을 확인한다. app journey는 I1 이후로 미룬다.

## 5. A2 — V2 CameraX–ML Kit 스캔 기반

### 범위와 소유권

- `core:scanner`, 해당 테스트, `camera-mlkit-vision` version catalog 항목만 소유한다.
- CameraX `ImageAnalysis`와 ML Kit Barcode Scanner를 `MlKitAnalyzer`로 연결한다.
- ML Kit의 첫 non-null `Barcode.rawValue`를 변경 없이 전달하고, 후속 프레임의 중복 결과를 세션 단위로 억제한다.
- 특정 바코드 포맷을 제한하지 않고 `BarcodeScanning.getClient()` 기본 설정을 사용한다.

```kotlin
sealed interface ScanResult {
    data class Success(val rawValue: String) : ScanResult
    data class Failure(val cause: Throwable) : ScanResult
}

interface BarcodeAnalyzerSession : AutoCloseable {
    val analyzer: ImageAnalysis.Analyzer
}
```

- factory는 결과 callback과 executor를 받아 `BarcodeAnalyzerSession`을 생성한다.
- `MlKitAnalyzer.COORDINATE_SYSTEM_ORIGINAL`을 사용하고 session `close()`에서 ML Kit detector를 해제한다.
- `rawValue == null`은 무시하고 분석을 계속한다. 성공 후 재시도는 기존 session을 닫고 새 session을 생성한다.

### 명시적 제외

- ISBN 정규화, ISBN-10/13 체크섬, `978`/`979` 접두사 확인
- EAN-13 등 바코드 포맷 제한, ISBN 변환과 유효성 판정
- Repository·UseCase·네트워크 도서 조회와 별도 ISBN 없음 상태
- `feature:scanner` UI, 권한, Preview, 회전과 Navigation

도서 조회 PR은 `Success.rawValue`를 그대로 기존 조회 경로에 전달한다. 조회 결과가 없으면 기존 scanner 실패 화면과 검색 이동 흐름을 사용한다.

### 검증과 커밋

- 원문 보존, null 무시, 첫 성공 뒤 중복 억제, 새 session 재방출, detector 오류, close 자원 해제와 여러 결과 중 첫 non-null 선택을 fake로 검증한다.
- Truth와 명시적 JUnit runner를 사용하고 `ImageProxy`를 Mockito로 모킹하지 않는다.
- `:core:scanner:testDebugUnitTest`, `:core:scanner:lintDebug`, `ktlintCheck`, 전체 `check`를 통과한다.
- 이 PR은 카메라 bind와 기기 회전을 포함하지 않으므로 emulator 검증을 Ready 조건으로 두지 않는다.

## 6. A3 — V3 리마인더 플랫폼

### 범위와 소유권

- `ReminderContent`, 설정 Domain UseCase, `core:notifications`, WorkManager/Hilt 연결에 필요한 app manifest와 application만 소유한다.
- `core:data`, DataStore key, DAO와 `LibraryRepository` 공개 계약은 변경하지 않는다.
- 설정 UI 없이도 fake UseCase와 WorkManager 테스트로 실행 가능한 기반을 완결한다.

```kotlin
interface ReminderScheduler {
    suspend fun schedule(time: LocalTime)
    suspend fun cancel()
}

sealed interface ReminderContent {
    data class Book(val title: String) : ReminderContent
    data object Generic : ReminderContent
}
```

### 동작 계약

- enable 시 설정을 저장하고 현재 시간으로 예약하며, disable 시 설정을 저장하고 unique work를 취소한다.
- 시간 변경은 설정을 저장하고 enabled일 때만 예약을 갱신한다.
- 읽는 중 책 중 `updatedAt`이 가장 최신인 책 제목을 사용하고 없으면 현지화된 기본 메시지를 사용한다.
- 고정 unique work name, 24시간 `PeriodicWorkRequest`, `ExistingPeriodicWorkPolicy.UPDATE`를 사용한다.
- 주입된 Clock과 현재 timezone으로 다음 선택 시각까지 initial delay를 계산한다. 정확 알람이 아닌 best-effort 리마인더임을 KDoc/history에 기록한다.
- Hilt `CoroutineWorker`는 `GetReminderContentUseCase`만 호출한다.
- API 26+ 채널을 멱등 생성한다. API 33+ 권한 없음이나 시스템 알림 차단은 게시하지 않고 success, 일시적 콘텐츠 오류는 retry한다.
- `ManiculeApplication`에 `HiltWorkerFactory`를 연결하고 manifest에서는 `WorkManagerInitializer`만 제거한다.

### 제외 범위

- 설정 Compose UI, ViewModel, 권한 요청, 시간 picker
- 테마 UI·루트 적용, 라이선스와 버전 정보
- exact alarm과 `AlarmManager`

### 검증과 커밋

- initial delay의 시각 전/동일/후, 자정과 timezone, unique work 등록/UPDATE/취소를 검증한다.
- enable/disable, 시간 변경, 최신 읽는 중 책/fallback, 알림 권한·차단, Worker retry와 채널 멱등성을 검증한다.
- `:core:domain:test`, `:core:notifications:testDebugUnitTest`, `:core:notifications:connectedDebugAndroidTest`, `:app:assembleDebug`, `ktlintCheck`, 전체 `check`를 통과한다.
- Android CLI로 API 36 AVD를 확인하고 필요한 경우 전용 AVD에서 계측 테스트한다.

## 7. A4 — V4 책 정보 조회와 기본 탭

### 범위와 소유권

- `feature:bookdetail`과 상세 조회에 필요한 Book model, network, data, database, domain 경로를 소유한다.
- `LibraryRepository`, 독서 상태·리뷰·ReadingRecord API와 `app`은 수정하지 않는다.

```kotlin
@Serializable
data class BookDetailRoute(
    val isbn: String,
    val openMyRecords: Boolean = false,
)

fun NavGraphBuilder.bookDetailScreen(
    onNavigateBack: () -> Unit = {},
)

class GetBookDetailUseCase {
    operator fun invoke(isbn: String): Flow<Book?>
    suspend fun refresh(isbn: String): Result<Unit>
}
```

- `Book`과 `BookEntity` 끝에 기본값이 있는 nullable `introduction`, `tableOfContents`를 추가한다.
- Room version은 1을 유지하고 변경된 schema JSON만 재생성한다.

### 데이터와 UI 계약

- Room Flow를 즉시 구독하면서 refresh를 병렬 실행한다.
- 캐시가 있으면 refresh 실패에도 캐시를 유지하고 retry를 제공한다. 캐시 없이 refresh가 실패하거나 ISBN 검색 결과가 없으면 fatal Error다.
- DTO inline 소개·목차를 우선하고 비어 있을 때만 NLK의 HTTPS `nl.go.kr` 또는 하위 domain URL을 병렬 조회한다. 한쪽 실패는 기본 sync를 실패시키지 않는다.
- `openMyRecords=true` 또는 서재 등록 상태면 내 기록, 미등록이면 책 정보를 최초 기본 탭으로 정한다. 이후 entry 변경은 사용자 선택 탭을 바꾸지 않으며 선택은 `SavedStateHandle`에 보존한다.
- 책 정보 탭에 앱바, 탭, 표지와 서지정보, 소개·목차, 더보기/접기를 구현한다. 내 기록 탭은 명시적 내부 stub이다.

### 제외 범위

- 독서 상태 생성·변경, 별점·메모 저장
- 기록 목록·추가·수정·삭제·Undo
- 첫 기록 자동 전환, 완독 확인과 `app` 조립

### 검증과 커밋

- inline/URL 콘텐츠 우선순위, 허용 host, 부분 실패, ISBN 없음, mapper와 schema를 검증한다.
- 캐시·refresh 조합, 기본 탭, 사용자 선택 유지, SavedState 복구와 retry를 검증한다.
- Compose에서 서지 필드, 펼침/접힘, 로딩/오류, back/tab, 큰 글꼴과 다중 폼팩터를 검증한다.
- 관련 model/network/data/domain/database/bookdetail target test, `ktlintCheck`, 전체 `check`를 통과한다.

## 8. A5 — V5 서재 상태별 목록

### 범위와 소유권

- `feature:library`, `BookEntryDao`의 상태별 조회, `GetLibraryBooksUseCase`와 관련 테스트를 소유한다.
- Entity, Converter, DB version, schema와 `LibraryRepository` 저장·삭제 API는 변경하지 않는다.
- 기본 조회에 결정적 순서 `updatedAt DESC, isbn ASC`를 적용한다.

```kotlin
operator fun GetLibraryBooksUseCase.invoke(
    status: ReadingStatus? = null,
): Flow<List<BookEntry>>

fun NavGraphBuilder.libraryScreen(
    onNavigateToBookDetail: (isbn: String) -> Unit = {},
    onNavigateToSearch: () -> Unit = {},
    onNavigateToScanner: () -> Unit = {},
)
```

### 완료 조건

- Loading/Content/Error 상태와 기본 `READING` 탭을 구현하고 탭 변경은 `flatMapLatest`로 조회에 연결한다.
- 앱바, 세 상태 탭, 표지·제목 책 카드, 빈 상태 검색·스캔 버튼을 구현한다.
- `LazyVerticalGrid`의 adaptive column, ISBN key/content type, lifecycle-aware 수집과 stateless Screen을 사용한다.
- 이 PR 완료로 V6의 서재 상태별 조회 계약 의존성을 해소한다.

### 제외 범위

- `LibrarySort`, 정렬 방향·아이콘·캡션·bottom sheet
- 진행률·완독일 overlay
- long press, 삭제·Undo와 상태 변경
- `app` callback 조립

후속 PR에서만 기존 호출과 source-compatible한 `sort: LibrarySort = LibrarySort.Default`를 추가한다.

### 검증과 커밋

- DAO 전체/상태 조회, 기본 순서, ISBN tie-break와 Flow 재방출을 검증한다.
- UseCase, 초기 탭·탭 전환·구독 취소·오류 ViewModel, 세 탭·책 선택·빈 상태 callback Compose 동작을 검증한다.
- database/domain/library target test, `:app:assembleDebug`, `ktlintCheck`, 전체 `check`를 통과한다.
- Android CLI의 run/layout/screen으로 탭, 빈 상태와 접근성 node를 확인한다.

## 9. A6 — V7 공유 통계 집계

### 범위와 소유권

- 통계 model, `StatsRepository`, 구현체, `core:domain/stats`, ReadingRecord 집계 DAO·projection·local data source와 테스트를 소유한다.
- 기존 ReadingRecord CRUD·책별 조회 시그니처를 유지하고 Entity, DB version과 schema는 변경하지 않는다.

```kotlin
data class DailyReading(val date: LocalDate, val pagesRead: Int, val bookCount: Int)
data class ReadingTotals(val pagesRead: Int, val bookCount: Int)
data class TodaySummary(val date: LocalDate, val pagesRead: Int, val bookCount: Int)
data class PeriodSummary(
    val rangeStart: LocalDate,
    val rangeEnd: LocalDate,
    val longestStreak: Int,
    val pagesRead: Int,
    val bookCount: Int,
)
```

- Repository는 기간 기록, 일별 집계, 기간 합계와 기준일까지의 독서 날짜 Flow를 제공한다.
- Domain은 오늘 요약, streak, 달력, 기간 요약 UseCase를 제공한다. 기간 요약의 기존 `LibraryRepository` 의존성은 제거한다.

### 집계 규칙

- 모든 범위는 양끝 포함이고 역방향 범위는 `IllegalArgumentException`이다.
- 페이지는 각 세션의 `pagesRead` 합이며 재독·겹침도 각각 포함한다. 책 수는 범위 내 기록이 있는 고유 ISBN 수다.
- 일별 결과는 날짜 오름차순이고 빈 합계는 0페이지·0권이다.
- 달력은 누락 날짜를 0으로 채우고 기존 5단계 강도를 사용한다.
- 현재 streak는 마지막 독서일이 오늘 또는 어제일 때만 유효하며 미래 기록은 제외한다. 기간 `longestStreak`는 요청 범위 안에서 계산한다.
- 이 PR 완료로 V6의 공유 통계 계약 의존성을 해소한다.

### 제외 범위

- `feature:stats` UI·ViewModel·Navigation과 기간 selector
- 날짜별 책 sheet, 오늘 목록, 복합 차트와 app 조립

### 검증과 커밋

- DAO 범위 경계·정렬·여러 세션·재독·고유 ISBN·빈 결과·Flow 재방출을 검증한다.
- 오늘/timezone, 달력 누락 날짜·강도, streak, 윤년, 역방향과 미래 날짜를 검증한다.
- model/data/domain/database target test, `:app:assembleDebug`, `ktlintCheck`, 전체 `check`를 통과한다.

## 10. 충돌 관리와 Coordinator 완료 조건

### 10.1 소유권과 충돌

- A1, A2는 각각 자기 feature/core 모듈만 수정한다. A3만 이번 배치에서 app manifest/application을 수정한다.
- A4는 Book 상세 경로, A5는 Library 조회 계약, A6는 ReadingRecord 집계 조회를 소유한다.
- A4와 A5가 같은 Repository 영역에 접근해야 하면 A4는 저장·상태 변경, A5는 조회만 소유한다.
- A4와 A6가 같은 ReadingRecord 영역에 접근해야 하면 A4는 CRUD, A6는 집계 조회만 소유한다.
- 공통 충돌 예상 파일은 `plan/current.md`와 `history/README.md`다. 각 Agent는 자기 행과 append-only index만 수정한다.
- target unit test는 병렬 실행한다. emulator 계측 테스트와 전체 `check`는 장치와 Gradle 자원 경합을 막기 위해 Coordinator가 queue로 직렬화한다.

### 10.2 PR 전 동기화

1. Agent는 자기 target test를 완료한다.
2. Coordinator가 emulator/전체 검증 사용 순서를 부여한다.
3. Agent는 최신 `origin/main`을 반영하며 다른 레인의 신규 메서드와 index 항목을 모두 보존한다.
4. 충돌 해결 뒤 target test와 전체 `check`를 재실행한다.
5. Agent는 branch를 push하고 Ready PR을 만든 뒤 URL과 검증 결과를 Coordinator에 반환한다.

### 10.3 배치 완료 판정

- 6개 브랜치가 모두 원격에 push되어 있다.
- 6개 PR이 `main` 대상 Ready 상태이며 assignee와 reviewer가 지정되어 있다.
- 각 PR 본문에 사용자 행동, 제외 범위와 실행한 검증이 간결하게 기록되어 있다.
- 실패한 check가 없고, 실행 대기 중인 remote check는 PR 상태에 명시되어 있다.
- V5와 V7 PR은 각각 V6가 소비할 공개 계약을 PR 본문에 표시한다.
- V2 PR은 CameraX 유지, ML Kit `rawValue` 원문 전달, ISBN 검증과 포맷 제한 제외를 명시한다.

