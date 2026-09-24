# GitHub 조회·게시

- GitHub는 `--json`·`--jq`로 필드를 제한하되 페이지네이션·리뷰 본문·답글·해결 상태를 보존한다.
- 게시 계정은 `gh api user --jq .login`으로 확인한다. 계정이 다르면 해당 절차의 계정으로 전환하되, 병렬 세션과 인증 설정이 공유된다는 점에 유의한다. PR head는 변경 직전 `gh pr view <PR> --json headRefOid --jq .headRefOid`로 확인한다.
- 게시 성공은 GitHub API의 반환 ID·URL·상태로 확인한다. 반환 결과만으로 성공 여부를 알 수 없으면 해당 항목을 조회한다. 전체 `gh pr status`로 답글·resolve 성공을 판단하지 않는다.
- CI 실패는 check 링크의 run·job을 식별해 `gh run view <run_id> --log-failed` 또는 `gh api`로 해당 로그·결과를 조회한다. 긴 로그는 파일로 보관하고 관련 오류만 읽는다. 로그로 원인을 확인할 수 없거나 재현이 필요한 경우에만 로컬 검증을 다시 실행한다.

리뷰·반영·검수에서 사용하는 조회 명령이다. `<PR>`과 `<thread_id>`는 실제 값으로 치환한다. 스레드와 답글을 각각 페이지네이션해 답글 누락을 방지한다. 필요한 조회만 실행한다.

```sh
# PR 정보: 원문은 한 번 읽고 이후에는 필요한 필드만 선택
gh pr view <PR> --json number,state,url,baseRefName,headRefName,headRefOid,headRepository,headRepositoryOwner,body
gh pr diff <PR> --name-only
# 미해결 인라인 스레드
gh api graphql --paginate -F owner='{owner}' -F name='{repo}' -F number=<PR> -F query=@skills/queries/review-threads.graphql --jq '.data.repository.pullRequest.reviewThreads.nodes[] | select(.isResolved == false)'
# 각 스레드의 원문과 모든 답글. databaseId는 REST 답글 게시에 사용할 comment_id
gh api graphql --paginate -F id=<thread_id> -F query=@skills/queries/thread-comments.graphql --jq '.data.node.comments.nodes[]'
# 인라인 밖 의견도 확인
gh api repos/:owner/:repo/pulls/<PR>/reviews --paginate --jq '.[] | select(.body != "") | {id,body,state,commit_id,html_url,user: .user.login}'
gh api repos/:owner/:repo/issues/<PR>/comments --paginate --jq '.[] | {id,body,html_url,user: .user.login}'
```
