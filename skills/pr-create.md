# PR 생성

[공통 규칙](skills.md)과 [GitHub 명령](github.md)을 적용한다. 확정한 PR 범위가 완료되었고 현재 브랜치에 PR이 없는지 확인한다. 게시 계정은 `LeeSeungYun1020`이다.

## 실행

1. 작업 계획과 `plan/order.md`의 PR 분할·규모 기준을 확인한다. 대상 base를 식별하고 fetch한다.
2. `git diff --numstat origin/<base>...HEAD`로 파일과 규모를 확인하고 필요한 diff를 읽는다. 다른 레인·선행 PR 변경이 섞인 base 오염은 push 전에 정리한다.
3. 규모 가드레일을 넘으면 분할하거나 예외 사유를 기록한다. 본문은 [PR 템플릿](../.github/PULL_REQUEST_TEMPLATE.md)에 따라 작성한다.
4. 작업 내역에 리뷰 초점과 변경 범위를 번호 목록으로 작성한다. 특이 사항에 검증 결과와 관련 전체 계획 문서의 파일·절(없으면 해당 없음)을 기록하고, 이슈 해결 건이면 `Close #번호`를 포함한다. 제외 범위·의존 PR·규모 예외는 해당할 때만 작성한다. 로컬 작업 계획 파일은 PR에 포함하지 않으며, 세부 구현 근거는 필요한 리뷰 답글에 남긴다. 본문에서 부수적인 current/history 갱신 설명은 생략한다.
5. 계정 확인 후 push한다. 성공하면 PR을 생성하고 Assignee `LeeSeungYun1020`, Reviewer `lsy-auto`를 지정한다. 여러 줄 본문은 파일로 작성해 `--body-file`로 전달한다.
6. 생성 응답의 URL과 필요 시 `gh pr view <PR번호> --json url,state,headRefOid,assignees,reviewRequests`로 등록 결과를 확인한다.
7. `gh pr checks <PR번호> --json name,state,link`로 `Gradle Check` 결과를 확인한다. 아직 없거나 대기 중이면 CI 대기로 보고하고, 실패하면 해당 run의 오류를 확인한다.
