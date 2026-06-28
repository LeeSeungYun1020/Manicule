# dc1d9df — refactor: BookRepository Offline-First 패턴 적용 및 조회/동기화 로직 분리

**Files**: `BookRepository.kt`, `BookRepositoryImpl.kt`

## 결정
- 로컬 DB 상태만 관찰하는 `observeBook(Flow)`과 원격 데이터를 동기화하는 `syncBook(Result)`으로 로직 분리.

## 이유
- 단일 객체(`Book`)라도 `Result`만 반환하면 백그라운드 동기화 후 화면 업데이트(반응형 UI) 처리가 불가능함.
- `Flow`로 로컬 DB를 계속 관찰하여 오프라인 상태에서도 즉시 캐시된 데이터를 표시하고, `syncBook`을 통해 네트워크 에러 여부를 UI 계층(ViewModel)에서 직접 처리하여 4가지 시나리오(로컬/원격 성공 및 실패 조합)를 완벽하게 대응할 수 있도록 함.
