# 리뷰 검수

## 전제 조건

- 사용자가 리뷰 검수 요청한 PR에 리뷰에 대한 답글이 작성되어 있음
- `lsy-auto` 계정으로 전환 가능(`gh auth status`)

## 실행 절차

### 답글 분석

1. PR 전체 comment 조회.
2. 각 리뷰를 번호로 정리하고 각 리뷰마다 다음 단계를 진행. 이미 해결된(resolve) 리뷰는 건너뜀.
3. 답글 확인하여 타당성 검토 진행.
4. 실제 답글 내용이 해결되었는지 또는 코드에 반영되었는지 확인.
5. 리뷰 해결이 가능하면 리뷰 해결 단계 진행, 불가하면 리뷰 반려 단계 진행.
6. 모든 리뷰가 해결되었다면 PR 승인 단계 진행, 반려된 리뷰가 있다면 사용자에게 처리 결과 요약 정리하여 출력 후 종료.

### 리뷰 해결

1. 스레드 ID 조회
2. `lsy-auto` 계정으로 전환(`gh auth status`, `gh auth switch`)
3. 해당 리뷰 해결(resolve) 처리

스레드 ID 조회 예시
```
gh api graphql -f query='
  {
    repository(owner: "$오너", name: "$저장소_이름") {
      pullRequest(number: $PR번호) {
        reviewThreads(first: 100) {
          nodes { id isResolved comments(first: 1) { nodes { body path } } }
        }
      }
    }
  }'
```

리뷰 해결 처리 예시
```
gh api graphql -f query='
  mutation {
    resolveReviewThread(input: {threadId: "$조회한_스레드_ID"}) {
      thread { isResolved }
    }
  }'
```

### 리뷰 반려

1. 해당 리뷰에서 해결되지 않은 부분 확인
2. 개선 사항 수집 및 수정 방향 정리. 관련있다면 android developers, kotlin docs 같은 공식 사이트 링크를 포함.
3. `lsy-auto` 계정으로 전환(`gh auth status`, `gh auth switch`)
4. 해당 리뷰에 정리한 내용을 답글로 게시(`gh api -X POST /repos/:owner/:repo/pulls/comments/<조회한_comment_id>/replies`). 별도 comment로 작성되지 않도록 유의.

### PR 승인

1. `plan` 디렉토리 내 계획 준수 여부 확인
2. `lsy-auto` 계정으로 전환(`gh auth status`, `gh auth switch`)
3. PR approve, 메시지 없이 approve 처리.

## 제약 사항

- PR 요약 시 필요하지 않은 부차적인 내용과 미사여구 삭제.
- 에러 방지를 위해 gh 명령어를 &&로 묶어 실행하지 말고, 단계별로 분리하여 안전하게 순차 실행.

## 예외(에러) 대응

- 계정 변경, 권한 문제, PR 생성에 실패하면 중단하고 사용자에 표시.
- 각 진행 중인 단계 표시(예: `<CHECK-답글 분석-6>`, `<CHECK-리뷰 반려-3>`)

## 최종 완료 및 검증

- github PR 상태 조회(`gh pr status`)하여 PR 등록 여부 확인.
