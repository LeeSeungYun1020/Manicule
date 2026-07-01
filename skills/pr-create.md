# PR 생성

## 전제 조건
- 작업 완료 및 현재 브랜치로 PR 생성되지 않음
- `LeeSeungYun1020` 계정으로 전환 가능(`gh auth status`)

## 실행 절차

1. git fetch 및 git push, push 불가능하면 정지 후 사용자에 알림.
2. 이전 PR 포맷 확인. 최근 머지 PR 조회(`gh pr list --state merged --limit 3`) 후 본문 포맷(`gh pr view <PR번호>`) 확인
3. 작업 내용 간결하게 요약 정리. 문서화 관련 내용은 요약에 포함하지 않음.
4. `LeeSeungYun1020` 계정으로 전환(`gh auth switch --user LeeSeungYun1020`)
5. PR 생성. Assignees는 `LeeSeungYun1020`. Reviewers는 `lsy-auto`로 설정.


## 제약 사항

- PR 요약 시 필요하지 않은 부차적인 내용과 미사여구 삭제.

## 예외(에러) 대응

- 계정 변경, 권한 문제, PR 생성에 실패하면 중단하고 사용자에 표시.
- 각 진행 중인 단계 표시(예: `<PR-1>`, `<PR-3>`)

## 최종 완료 및 검증

- github PR 상태 조회(`gh pr status`)하여 PR 등록 여부 확인.
