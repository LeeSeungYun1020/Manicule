# 리뷰 검수

[공통 규칙](skills.md)을 적용한다. 요청한 PR에 리뷰 답글이 있는지 확인한다. 게시·승인 계정은 `lsy-auto`다.

## 검수

1. [GitHub 조회 명령](github.md)으로 PR 정보를 확인하고 [작업 디렉터리 규칙](skills.md#작업-디렉터리)에 따라 준비한다. 같은 브랜치는 갱신 후 사용하고, 다른 브랜치이면 별도 워크트리에서 진행한다.
2. 공통 명령으로 미해결 인라인 스레드와 답글·리뷰 본문·일반 PR 댓글을 `--paginate`로 조회한다. 지적·질문과 후속 답변을 의견 유형·ID·원문 URL로 연결하며, 일반 댓글에 resolve 상태가 없다는 이유로 검수에서 제외하지 않는다.
3. 각 의견의 답변을 실제 코드·관련 테스트·PR 설명·전체 계획 문서의 관련 요구사항·경계와 대조한다. 로컬 작업 계획 파일은 요구하지 않으며 세부 구현 근거는 구현자의 답글에서 확인한다. 답변이나 근거가 부족하면 미해결로 남긴다. 같은 원인은 함께 분석하되 해결 판단은 의견별로 유지한다.
4. 계정과 PR head를 재확인한다. head가 바뀌면 영향 부분을 다시 검수한다. 해결된 인라인 스레드는 아래 명령으로 resolve하고 `isResolved: true`를 확인한다. 일반 댓글·리뷰 본문의 해결 판단은 의견 ID와 근거를 처리 결과에 남긴다.
5. 해결되지 않은 의견에는 구체적인 남은 문제·추가 설명 요청과 필요 시 관련 공식 문서 링크를 [답글 명령](apply-review.md#답글)으로 게시한다. 반려·새 문제가 있어도 나머지 검수를 계속하며 직접 수정하거나 반영 단계로 전환하지 않는다.
6. 인라인·일반 댓글·리뷰 본문의 모든 지적·질문이 해결되고 새 문제가 없으면 전체 계획과의 정합성 확인한다.
7. 최신 head를 다시 확인하고 `gh api repos/:owner/:repo/pulls/<PR>/reviews -f event=APPROVE -f commit_id=<검토SHA> --jq '{id,state,commit_id}'`로 메시지 없이 승인한다. 반환된 `APPROVED`와 검토 SHA의 일치를 확인한다. 남은 문제가 있으면 승인하지 않는다. 의견별 처리 결과·승인 여부·남은 제약을 반환하고 종료한다.

```sh
gh api graphql -f query='mutation($id: ID!) { resolveReviewThread(input: {threadId: $id}) { thread { id isResolved } } }' -F id=<thread_id> --jq '.data.resolveReviewThread.thread'
```
