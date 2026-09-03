# 스킬

## 개요

- 일반적으로 work(`code-change` 세부 절차 포함) → pr-create → code-review → apply-review → review-check 순으로 별도 세션에서 사용
- PR 분할·의존성·변경 소유권 기준은 `plan/order.md`, 레인별 현재 상태와 다음 범위는 `plan/current.md`를 단일 기준으로 사용. PR 작업 계획은 `plan/current.md`에 기록하지 않음.
- android-cli 스킬 사용 불가능하면 스킬 설치(`android skills add --all`) 시도. 설치 불가능 시 직접 설치 요청.

## 상세

- work.md: 코드 구현. 계획에 따라 코드 구현.
- code-change.md: work.md 실행 단계에서 파일 수정·commit·검증을 수행하는 세부 절차. 독립 작업 절차로 사용하지 않음.
- pr-create.md: PR 생성. 작업 완료 후 변경 사항 분석하여 PR 생성.
- code-review.md: 코드 리뷰. 해당 PR 코드 변경사항과 영향 코드를 점검하여 코드 리뷰 진행.
- apply-review.md: 리뷰 반영. 해당 PR comment를 조회하여 수정 필요성을 검토하고 개선 진행.
- review-check.md: 리뷰 검수. 해당 PR comment를 조회하여 해결 여부를 확인.
