# adeafd2 — refactor: 바코드 분석 suspend API 적용

**Files**: `MlKitBarcodeAnalyzerFactory.kt`, `BarcodeAnalyzerSession.kt`, `BarcodeAnalysisException.kt`

## 결정
- `getBarcodes()` 호출 중 최초 일치 프레임의 모든 원문 값을 rendezvous Channel로 전달하고 detector 오류는 예외로 전파한다.

## 이유
- 단일 응답을 기다리는 suspend API에는 replay와 공유 구독보다 호출 중인 수신자에게만 결과를 전달하는 rendezvous Channel이 적합하다.
- CameraX analyzer와 ML Kit detector 수명은 session이 소유하며, `close()`에서 대기 호출 취소와 detector 해제를 한 번만 수행한다.
