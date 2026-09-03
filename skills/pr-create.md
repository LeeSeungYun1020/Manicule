# PR 생성

## 전제 조건
- 작업 계획에서 확정한 PR 범위 완료 및 현재 브랜치로 PR 생성되지 않음
- `LeeSeungYun1020` 계정으로 전환 가능(`gh auth status`)

## 실행 절차

1. `plan/order.md`의 PR 분할 기준과 작업 계획의 리뷰 초점, 구현 범위, 제외 범위, 의존 작업과 완료 기준 확인.
2. 대상 base branch를 식별하고 git fetch 진행.
3. `git diff --name-only origin/<base>...HEAD`와 `git diff --stat origin/<base>...HEAD`로 변경 파일과 diff 통계를 확인해 현재 PR의 구현 범위에 속하는지 검사.
4. 다른 레인이나 선행 PR 변경이 포함된 base 오염을 발견하면 push 전에 중단하고 base를 정리.
5. `plan/order.md`의 규모 가드레일을 검사. 예외 기준을 넘으면 PR을 분할하거나 예외 사유를 준비.
6. 최근 머지 PR 조회(`gh pr list --state merged --limit 3`)와 본문 확인(`gh pr view <PR번호>`)으로 저장소의 정해진 PR 본문 형식이 유지되는지 확인.
7. `LeeSeungYun1020` 계정으로 전환(`gh auth switch --user LeeSeungYun1020`).
8. git push. push 불가능하면 중단하고 사용자에게 알림.
9. PR 본문을 `## 🛠 작업 내역`, `## 📝 특이 사항` 문단으로 작성. 작업 내역에는 리뷰 초점과 변경 범위를 번호 목록으로 정리. 특이 사항에는 검증 결과를 반드시 기록하고, 제외 범위, 의존 PR과 규모 예외 사유는 해당하는 경우에만 포함.
10. PR 생성. Assignees는 `LeeSeungYun1020`. Reviewers는 `lsy-auto`로 설정.


## 제약 사항

- PR 요약 시 필요하지 않은 부차적인 내용과 미사여구 삭제.
- 코드 변경에 부수적으로 따라온 `current`·`history` 갱신은 요약에서 제외하되, 문서 자체가 PR의 핵심 변경이면 요약에 포함.

## 예외(에러) 대응

- 계정 변경, 권한 문제, PR 생성에 실패하면 중단하고 사용자에 표시.
- 각 진행 중인 단계 표시(예: `<PR-1>`, `<PR-3>`)

## 최종 완료 및 검증

- github PR 상태 조회(`gh pr status`)하여 PR 등록 여부 확인.
