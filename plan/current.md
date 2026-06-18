# Manicule — 진행 현황

> 참고: [plan.md](plan.md) 기획 · [structure.md](structure.md) 모듈/파일 구조 · [order.md](order.md) 단계 · [history/](../history/README.md) 커밋별 의사결정
> 새 세션은 **"다음 작업"** 부터 진행.

## 단계

| 단계 | 이름 | 상태 |
|---|---|---|
| 1 | Foundation | ✅ |
| 2 | Core Engine | ⏳ 다음 |
| 3 | Vertical Slices | ⏳ |
| 4 | System Integration Slices | ⏳ |
| 5 | App Assembly | ⏳ |
| 6 | Verification | ⏳ |

## ⏳ 다음 작업 — 2단계 Core Engine

### 다음 세션에서 진행할 내용
- **core:datastore** 모듈 구현:
    - Preferences DataStore를 사용하여 사용자 설정(예: 최근 검색어 저장) 구현.
- **core:data** 모듈 구현:
    - Repository 인터페이스 및 구현체 작성.
    - 네트워크/데이터베이스 모델과 도메인 모델 간 매핑.
    - `core:network`에서 작성한 NLK API와 `core:database` 연동.

### 참고 사항
- 이번 세션에서 `core:network` 모듈 생성을 마치고, 국립중앙도서관 ISBN 서지정보 API(JSON) 클라이언트 연동을 완료했습니다.
- `local.properties`를 활용하여 API 키를 안전하게 주입하는 구조를 구축했습니다. 
- **네트워크 모듈 리뷰 완료**: `core:network` 모듈의 응답 DTO와 파라미터가 API 가이드에 맞게 잘 구현되어 정상 동작함을 검증. 단, `NlkAuthInterceptor` 기능 중복 문제를 방지하고자 `NlkApi.kt`에서 불필요한 `cert_key` 필수 파라미터를 제거함.
- **core:domain** — UseCase 는 슬라이스에서 채움. 골격만.
- **core:ui** — `build.gradle.kts` 에 `platform(libs.coil.bom)` 직접 선언. 잔디 셀 디자인 토큰(크기/간격) 결정 필요.

2단계 완료 후: **3단계 Slice 1 = `feature:search`** (디바운스 350 ms + Paging 3).

## 운영

### 모듈 추가 절차
1. `settings.gradle.kts` 에서 `include(":...")` 주석 해제.
2. 모듈 폴더 + `build.gradle.kts` (convention plugin alias).
3. Android library 면 빈 `AndroidManifest.xml`.
4. 패키지 루트 `com.leeseungyun1020.manicule.<group>.<module>`.
5. `app/build.gradle.kts` 의 `implementation(projects.…)` 주석 해제.

### 출시 전 점검 (1단계에서 미뤄둔 항목)
- `allowBackup=true` 데이터 정밀화 (`dataExtractionRules`) — PR #1 리뷰 지적.
- 다크 모드 Grass 0단계 가시성 — surface 와 색 차이 부족.
- 2단계 완료 시 `app:run` 으로 Hilt 그래프 누락 검증.

### 커밋 / history
의사결정 있는 커밋은 [history/](../history/README.md) 에 `<short-hash>.md` 추가 (WHY 만). 순수 tooling/format 커밋은 만들지 않음.
