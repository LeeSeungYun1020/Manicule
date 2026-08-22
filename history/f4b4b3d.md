# f4b4b3d — feat: CameraX 바코드 분석 기반 구현

**Files**: `MlKitBarcodeAnalyzerFactory.kt`, `BarcodeAnalyzerSession.kt`, `ScanResult.kt`

## 결정
- CameraX `MlKitAnalyzer`와 기본 ML Kit barcode scanner로 분석 세션을 구성하고 첫 non-null `rawValue`를 원문 그대로 전달한다.

## 이유
- core `ImageAnalysis`와 결합하므로 원본 좌표계를 사용하며, ISBN 여부와 조회 성공은 후속 Domain 흐름이 판단한다.
- 세션의 첫 성공만 전달해 연속 프레임 중복을 막고, detector는 세션 종료 시 멱등 해제한다.
- 포맷 제한은 성능상 유리하지만 원문 전달 계약과 모든 포맷 허용 요구를 위해 적용하지 않는다.
