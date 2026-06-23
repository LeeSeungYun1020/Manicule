# f5baa0f — UserPreferencesDataStore 불필요한 catch 블록 삭제

**Files**: `core/datastore/src/main/kotlin/com/leeseungyun1020/manicule/core/datastore/UserPreferencesDataStore.kt`

## 결정
- DataStore Flow 읽기 시 사용되던 `catch` (IOException 방어) 블록 삭제.

## 이유
- 최신 공식 문서 가이드에서 코드 간결성을 위해 해당 보일러플레이트를 생략하는 추세.
- 내부 파싱 로직(`runCatching`)으로 예외 방어막을 충분히 구축하였기에, 중복 코드를 제거하여 가독성 향상.
