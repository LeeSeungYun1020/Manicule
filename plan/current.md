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

### 진행 참고 사항
- **core:domain**: UseCase는 3단계 슬라이스에서 구현 예정

## 운영

### 모듈 추가 절차
1. `settings.gradle.kts` 에서 `include(":...")` 주석 해제.
2. 모듈 폴더 + `build.gradle.kts` (convention plugin alias).
3. Android library 면 빈 `AndroidManifest.xml`.
4. 패키지 루트 `com.leeseungyun1020.manicule.<group>.<module>`.
5. `app/build.gradle.kts` 의 `implementation(projects.…)` 주석 해제.

### 출시 전 점검 (1단계에서 미뤄둔 항목)
- 2단계 완료 시 `app:run` 으로 Hilt 그래프 누락 검증.

### 커밋 / history
의사결정 있는 커밋은 [history/](../history/README.md) 에 `<short-hash>.md` 추가 (WHY 만). 순수 tooling/format 커밋은 만들지 않음.
