# 2단계 첫 PR — `core:database`

## Context

1단계 Foundation 은 완료(✅) — `core:model`, `core:common`, `core:designsystem`, `app` 스켈레톤이 동작 가능한 상태이며 Hilt 그래프, ktlint/detekt, BoM 정책까지 정착되었다. 2단계 Core Engine 의 6 개 모듈(`database` / `network` / `datastore` / `data` / `domain` / `ui`) 중 **1~3 번은 상호 의존이 없어 병렬 구현 가능**하다.

본 plan 은 그 중 **첫 PR = `core:database` 도입**만 다룬다. Offline-first 원칙상 Room 이 SSOT 가 되므로, 후속 `core:data` Repository / `feature:*` UI 가 모두 이 모듈의 Entity·DAO 스키마를 전제로 작성된다. 따라서 이번 PR 의 목표는:

1. `core:database` 모듈 자체를 settings 에 등록하고 빌드 통과시키기.
2. **4 개 Entity / 4 개 DAO / Converters / Database / 빈 Migration 골격** 을 모두 채워, 2 단계 잔여 모듈이 의존을 추가하는 즉시 사용할 수 있게 한다.
3. Room in-memory 기반 DAO 테스트로 스키마·쿼리 회귀를 막는다.

UseCase·Repository 는 본 PR 범위 밖이다.

---

## 사전 확인된 인프라 (재사용)

- **컨벤션 플러그인 — 신규 작성 불필요**
  - `manicule.android.library` — `build-logic/.../AndroidLibraryConventionPlugin.kt`
  - `manicule.android.hilt` — `AndroidHiltConventionPlugin.kt` (KSP + hilt-android + hilt-compiler 자동 적용)
  - `manicule.android.room` — `AndroidRoomConventionPlugin.kt` (KSP, room-runtime/ktx/paging, schemas 디렉터리, androidTest room-testing 자동 적용)
- **`libs.versions.toml`** — Room 2.8.4, KSP 2.3.6, Hilt 2.59.2, room-paging, room-testing 모두 등록 완료. 추가 alias 없음.
- **`core:model`** — `Book`, `BookEntry`, `ReadingRecord`, `ReadingStatus`, `SearchQuery` 모두 `kotlinx.datetime.LocalDate` / `Instant` 기반. Entity ↔ Model 매핑은 `core:data` PR 에서 다룬다.
- **`core:common`** — `time/DateExt.kt`, `Clock`, Hilt `DispatchersModule` 존재. Room 의 Converters 는 `core:common` 의존 없이 `kotlinx-datetime` 만 사용한다(불필요한 결합 회피).

---

## 산출물

### 1. 모듈 등록

- `settings.gradle.kts` — `//include(":core:database")` 주석 해제 ([CLAUDE.md](../CLAUDE.md) 의 "모듈 추가 절차" 1.)
- `core/database/build.gradle.kts` 신규 작성

  ```kotlin
  plugins {
      alias(libs.plugins.manicule.android.library)
      alias(libs.plugins.manicule.android.hilt)
      alias(libs.plugins.manicule.android.room)
  }

  android {
      namespace = "com.leeseungyun1020.manicule.core.database"
  }

  dependencies {
      implementation(projects.core.model)
      implementation(libs.kotlinx.datetime)

      androidTestImplementation(libs.androidx.test.ext.junit)
      androidTestImplementation(libs.kotlinx.coroutines.test)
      androidTestImplementation(libs.turbine)
      androidTestImplementation(libs.truth)
  }
  ```

- `core/database/src/main/AndroidManifest.xml` — 빈 manifest (Android library 규약)
- `app/build.gradle.kts` — `implementation(projects.core.database)` 는 본 PR 에서 **추가하지 않는다**. Hilt 그래프 누락 검증은 `core:data` PR 이후 한꺼번에 진행 ([current.md](current.md) 의 "출시 전 점검" 그대로).

### 2. Entity (`core/database/.../entity/`)

| 파일                       | PK / Index                                              | 비고                                                                                                                                                                              |
|--------------------------|---------------------------------------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `BookEntity.kt`          | `@PrimaryKey isbn: String`                              | `Book` 의 모든 필드를 그대로 가진다(`publishedDate: LocalDate?`, nullable 필드 그대로). FK 없음 — 검색 결과 미등록 책도 캐싱.                                                                                |
| `BookEntryEntity.kt`     | `@PrimaryKey isbn: String` (= Book 1:1)                 | `status`, `rating: Int?`, `memo: String?`, `addedAt: Instant`, `updatedAt: Instant`, `finishedAt: LocalDate?`. FK → `BookEntity.isbn`, `onDelete = CASCADE`. `currentPage` 는 저장하지 않고 DAO 조인 쿼리로 파생(중복 캐시 회피). |
| `ReadingRecordEntity.kt` | `@PrimaryKey(autoGenerate = true) id: Long`             | `isbn`, `date: LocalDate`, `cumulativePage: Int`. **유니크 인덱스 `(isbn, date)`** — 같은 날 같은 책에 두 기록은 의미가 없으므로 DB 레벨에서 강제. FK → `BookEntity.isbn`, `onDelete = CASCADE`. 추가 인덱스 `(date)` — 잔디 집계 범위 쿼리용. |
| `RecentQueryEntity.kt`   | `@PrimaryKey query: String`                             | `lastUsedAt: Instant`. 동일 검색어 재입력 시 timestamp 갱신(upsert).                                                                                                                       |

### 3. DAO (`core/database/.../dao/`)

| DAO                   | 메서드                                                                                                                                                                  | 반환                            |
|-----------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------|-------------------------------|
| `BookDao.kt`          | `upsert(book)`, `getByIsbn(isbn): BookEntity?`, `observeByIsbn(isbn): Flow<BookEntity?>`                                                                              | suspend / Flow                |
| `BookEntryDao.kt`     | `upsert(entry)`, `delete(isbn)`, `observeByIsbn(isbn): Flow<BookEntryWithCurrentPage?>`, `observeByStatus(status): Flow<List<BookEntryWithCurrentPage>>`, `observeAll(): Flow<List<BookEntryWithCurrentPage>>` | Flow                          |
| `ReadingRecordDao.kt` | `upsert(record): Long`, `delete(id)`, `observeByIsbn(isbn): Flow<List<ReadingRecordEntity>>`, `observeBetween(start, end): Flow<List<ReadingRecordEntity>>`, `latestCumulativeFor(isbn): Int?` | Flow / suspend                |
| `RecentQueryDao.kt`   | `upsert(query)`, `observeRecent(limit: Int = 10): Flow<List<RecentQueryEntity>>`, `clear()`                                                                            | Flow / suspend                |

- **정책**: 관찰형은 `Flow`, 단발성 mutation 은 `suspend`. 정렬·필터·페이징 정책 결정(서재 정렬 3 종 등)은 `core:data` PR 에서 보강 — 본 PR 은 *최소 쿼리*만.
- `BookEntryWithCurrentPage` 는 `@Embedded entry` + `@ColumnInfo currentPage: Int?` 형태의 결과 클래스(`dao/projection/` 에 배치). `currentPage` 는 **가장 최근 날짜(`date` 기준)** 의 `ReadingRecordEntity.cumulativePage` — `ReadingRecordEntity` 의 `(isbn) 별 ORDER BY date DESC LIMIT 1` 상관 서브쿼리로 도출(LEFT JOIN 이므로 기록 없으면 `null`). 단조 증가가 깨진 경우(기록 수정·재독)에도 `core:model` 의 정의("가장 최근 `ReadingRecord.cumulativePage`")와 일치하며 `latestCumulativeFor` 와 같은 의미.

### 4. Database / Converters / Migration

- `ManiculeDatabase.kt` — `@Database(entities = [...4..], version = 1, exportSchema = true)`, `@TypeConverters(Converters::class)`, `abstract` DAO accessor 4 개. `DatabaseModule` 에서 Hilt `@Provides @Singleton` 으로 `Room.databaseBuilder` 노출. DB 이름: `"manicule.db"`.
- `converter/Converters.kt` — `LocalDate ↔ String` (ISO-8601), `Instant ↔ Long` (epoch ms), `ReadingStatus ↔ String` (`enum.name` — `ReadingStatus.fromOrNull` 재사용 가능). `kotlinx-datetime` 직접 사용.
- `migration/Migrations.kt` — 빈 `Migrations` object + 빈 배열 상수. v1 시작이라 실제 마이그레이션은 없지만 후속 PR 이 즉시 추가할 수 있는 자리만 만들어둠.
- `schemas/` — Room 컨벤션 플러그인이 `$projectDir/schemas` 로 출력. **`com.leeseungyun1020.manicule.core.database.ManiculeDatabase/1.json` 을 VCS 에 커밋한다** (마이그레이션 회귀 검증의 근거).

### 5. DI (`core/database/.../di/`)

- `DatabaseModule.kt` — `@InstallIn(SingletonComponent::class)` Hilt 모듈. `provideDatabase(@ApplicationContext)`, `provideBookDao(db)`, … DAO 4 개 모두 `@Provides`.

### 6. 테스트 (`core/database/src/androidTest/`)

- `BookDaoTest`, `BookEntryDaoTest`, `ReadingRecordDaoTest`, `RecentQueryDaoTest` — `Room.inMemoryDatabaseBuilder` + `runTest` + Turbine.
- 각 DAO 최소 2~3 케이스: upsert/observe, FK CASCADE (Book 삭제 시 Entry/Record 동반 삭제), `(isbn, date)` 유니크 충돌, `BookEntryWithCurrentPage` 가 **가장 최근 날짜** 의 누적 페이지를 반환(누적치가 하향된 케이스 포함하여 `MAX` 가 아님을 검증).
- `MigrationsSmokeTest` (선택, 가벼움) — v1 빌드만 검증.

---

## 결정 사항 (본 PR 내 확정)

- **`BookEntryEntity.PK = isbn`** — Book 과 1:1. 별도 surrogate id 불필요. 미등록 검색 결과는 `BookEntity` 만 캐싱되고 `BookEntry` 는 존재하지 않음으로 표현.
- **`currentPage` 는 컬럼이 아닌 파생값** — `ReadingRecord` 가 SSOT 이므로 **가장 최근 날짜(`date` 기준)** 의 `cumulativePage` 를 상관 서브쿼리로 도출. `MAX` 가 아닌 "최신 날짜" 기준이라 기록 수정·재독으로 누적치가 하향돼도 `core:model` 정의와 일치. 중복 캐시 컬럼을 두지 않아 정합성 버그 회피.
- **`ReadingStatus` 직렬화 = `String(enum.name)`** — 가독성·정렬·디버깅 이점이 Int 대비 압도적. 카디널리티 3.
- **`Instant ↔ Long` (epoch ms)** — `updatedAt` 등 정렬 키로 사용되므로 비교 비용이 낮은 정수 저장.
- **schemas/ 디렉터리 VCS 커밋** — Migration 회귀 방지의 근거. `$projectDir/schemas` 는 `build/` 밖이라 현재 `.gitignore`(`build/`, `**/build/`)로 이미 추적 대상 — `.gitignore` 수정 불필요.
- **`core:database` 는 `core:model` 에 의존하지만 Entity 가 도메인 모델을 *상속/포함하지 않는다*** — 매핑은 `core:data` 의 책임. 이번 PR 의 Entity 는 도메인과 독립적인 단순 평면 record.

---

## 핵심 파일 경로 (편집/생성 대상)

수정:
- `settings.gradle.kts`

신규:
- `core/database/build.gradle.kts`
- `core/database/src/main/AndroidManifest.xml`
- `core/database/src/main/kotlin/com/leeseungyun1020/manicule/core/database/`
  - `ManiculeDatabase.kt`
  - `di/DatabaseModule.kt`
  - `entity/{BookEntity,BookEntryEntity,ReadingRecordEntity,RecentQueryEntity}.kt`
  - `dao/{BookDao,BookEntryDao,ReadingRecordDao,RecentQueryDao}.kt`
  - `dao/projection/BookEntryWithCurrentPage.kt`
  - `converter/Converters.kt`
  - `migration/Migrations.kt`
- `core/database/src/androidTest/kotlin/.../{BookDaoTest,BookEntryDaoTest,ReadingRecordDaoTest,RecentQueryDaoTest,MigrationsSmokeTest}.kt`
- `core/database/schemas/com.leeseungyun1020.manicule.core.database.ManiculeDatabase/1.json` (Room KSP 가 생성, 커밋만)

---

## 검증 (End-to-End)

작업 후 순서대로:

1. `./gradlew :core:database:assembleDebug` — 컴파일·KSP 코드 생성 통과.
2. `./gradlew ktlintFormat` — CLAUDE.md 규칙.
3. `./gradlew :core:database:check` — lint + unit test.
4. `./gradlew :core:database:connectedDebugAndroidTest` — DAO 테스트 (에뮬레이터/실기기). CI 가 없는 환경에서는 로컬에서 1 회 확인.
5. `./gradlew check` — 전 모듈 회귀 확인.
6. `schemas/.../1.json` 이 생성되어 VCS 에 들어가는지 확인.

이후:
- **commit** ([CLAUDE.md](../CLAUDE.md) 규칙).
- 의사결정이 있었으므로 **[`history/<short-hash>.md`](../history/README.md)** 작성 — 주제: "Room 도입과 SSOT 스키마 결정" (BookEntry PK=isbn, currentPage 파생 컬럼화 거부, ReadingStatus String 직렬화, schemas 커밋 정책).
- **[current.md](current.md)** 의 "2 단계 다음 작업" 1 번 항목에 ✅ 표기, 다음 PR(`core:network` 또는 `core:datastore`) 을 강조하도록 갱신.

후속 PR (`core:data`) 에서 `app` 의 `implementation(projects.core.database)` 추가 + Hilt 그래프 빌드 검증을 함께 수행한다 ([current.md](current.md) 의 "출시 전 점검" 정책 그대로).

---

## 구현 분할 (PR · commit)

전체를 한 번에 구현·리뷰하면 부담이 크다. 다음 제약으로 쪼갠다 — commit 당 **수정 파일 < 10**, **파일당 변경 ≤ 100 줄**, **commit 리뷰 ≤ 1 시간**, 가능하면 각 commit 이 **빌드 통과**. 라인수는 `core:model` 필드 수 + 표준 Room 보일러플레이트 기반 **추정치**.

산출물을 **스키마 계층(PR1)** 과 **쿼리·테스트 계층(PR2)** 으로 가른다. DAO accessor 추가는 `1.json`(schema) 을 바꾸지 않으므로 PR 을 나눠도 스키마 churn 이 없다.

### PR 1 — 스키마 계층

모듈이 빌드되고 `schemas/.../1.json` 이 export·커밋된 상태까지.

| commit | 파일 | 추정 | 빌드/검증 | 리뷰 |
|--------|------|------|-----------|------|
| **1. 모듈 등록** | `settings.gradle.kts`(주석 해제), `core/database/build.gradle.kts`, `src/main/AndroidManifest.xml` | 3파일·~21줄 | `:core:database:assembleDebug` (빈 모듈) | ~10분 |
| **2. Entity 4개** | `entity/{Book,BookEntry,ReadingRecord,RecentQuery}Entity.kt` | 4파일·각 ≤35줄 | 컴파일(@Database 없이 @Entity 컴파일 가능) | ~30분 (스키마 결정 핵심) |
| **3. Converters+Database+Migration+DB DI** | `converter/Converters.kt`, `ManiculeDatabase.kt`(DAO accessor 없이), `migration/Migrations.kt`, `di/DatabaseModule.kt`(provideDatabase 만), `schemas/.../1.json`(생성·커밋) | 5파일·손수 ~75줄 | `assembleDebug` → `1.json` 생성 후 커밋 | ~25분 |

> 3 commit · 12 파일 · 각 commit 빌드 통과. DAO accessor 없는 `@Database` 는 1 commit 과도기(Room 허용).

### PR 2 — DAO & 테스트

| commit | 파일 | 추정 | 빌드/검증 | 리뷰 |
|--------|------|------|-----------|------|
| **4. DAO+projection+연결** | `dao/{Book,BookEntry,ReadingRecord,RecentQuery}Dao.kt`, `dao/projection/BookEntryWithCurrentPage.kt`, `ManiculeDatabase.kt`(DAO accessor +4), `di/DatabaseModule.kt`(DAO @Provides +4) | 7파일·각 변경 ≤28줄 | `:core:database:assembleDebug` (KSP DAO impl), schema 불변 | ~40분 (쿼리 정확성) |
| **5. instrumented 테스트** | `{Book,BookEntry,ReadingRecord,RecentQuery}DaoTest.kt`, `MigrationsSmokeTest.kt` | 5파일·각 ≤70줄 | `:core:database:connectedDebugAndroidTest` | ~35분 |

> 2 commit · 12 파일. `BookEntryDaoTest` 는 currentPage 가 **최신 날짜** 기준임을 누적치 하향 케이스로 검증.

### 공통 워크플로 (CLAUDE.md)
각 commit 직전: `ktlintFormat` → 범위 `check` 통과 → commit. PR2 머지 전 `./gradlew check` 전 모듈 회귀.

- **PR1 머지 후**: `history/` 에 "Room 도입+SSOT 스키마 결정", `current.md` 진행 갱신.
- **PR2 머지 후**: history 에 "currentPage 최신 날짜 파생(상관 서브쿼리)" 추가, `current.md` 2단계 1번 ✅.

### 단일 PR 대안
PR 분리가 과하면 commit 1~5 를 **한 PR 의 5 commit** 으로 그대로 적용 가능(제약은 commit 단위라 동일 충족). 본 계획은 스키마(계약) vs 쿼리(구현) 리뷰 관점 분리를 위해 2 PR 권장.
