# 코드 리뷰

## 전제 조건

- 사용자가 리뷰를 요청한 PR이 열린 상태.
- `lsy-auto` 계정으로 전환 가능(`gh auth status`)

## 실행 절차

1. PR 상세 내용 및 변경 사항 확인.
2. PR 브랜치를 로컬로 패치하여 체크아웃. 코드 직접 확인.
3. PR 변경사항 바탕으로 코드 사용부 확인하여 결함, 사이드이펙트 점검.
4. 계획(`plan`)과 Android/Kotlin 가이드 준수 여부 확인.
5. `lsy-auto` 계정으로 전환(`gh auth switch --user lsy-auto`)
6. 발견한 문제 중 계획 누락, 현재 PR에서 수정되지 않은 파일의 구조 개선 요구 같이 인라인 코멘트로 작성 불가능한 사항 comment로 등록. `gh pr comment <PR번호> --body "내용"`
7. 인라인 코멘트 작성 가능한 항목은 `review.json` 파일에 개별 하나의 리뷰 세션으로 정리
	- 전체 요약 코멘트를 비우고, 개별 인라인 코멘트 목록으로만 구성.
	- 각 인라인 코멘트는 path(파일 경로), side(이전 파일인 경우 "LEFT", 수정된 파일인 경우 "RIGHT"), line(파일에서 코드 라인 번호), body(리뷰 의견)를 명시.

```json
{
  "event": "COMMENT",
  "comments": [
    {
      "path": "src/main/kotlin/com/example/domain/BookMapper.kt",
      "line": 15,
      "side": "RIGHT",
      "body": "Kotlin에서는 명시적인 타입 변환 대신 'as?'나 스마트 캐스트를 활용하는 것이 좀 더 안전하고 코틀린답습니다."
    },
    {
      "path": "src/main/kotlin/com/example/domain/BookService.kt",
      "line": 42,
      "side": "RIGHT",
      "body": "이 메소드는 부모 클래스나 외부 데이터에 상태 변화(Side-effect)를 주기 때문에, 메소드명 끝에 'OrNull'을 붙이거나 예외 처리(try-catch)를 추가할 필요가 있습니다."
    }
  ]
}
```

8. `lsy-auto` 계정으로 전환(`gh auth switch --user lsy-auto`)
9. `review.json` 파일을 지정하여 리뷰 전송. `gh api repos/:owner/:repo/pulls/<PR번호>/reviews --method POST --input review.json`
10. 임시 리뷰 파일 삭제

## 제약 사항

- 작업 내용은 android 가이드와 kotlin 가이드를 준수해야 함.
	- android-cli 스킬 활용. android 가이드 확인 후 리뷰 진행.
	- kotlin Documentation(https://kotlinlang.org/docs/coding-conventions.html) 준수 여부 확인. Java 스타일로 잘못 작성된 코드 확인.
- 필요하지 않은 부차적인 내용(인사)과 미사여구 삭제.
- 불필요한 전체 요약 코멘트 작성 금지. 반드시 분리된 개별 코멘트만 작성.
- 칭찬 코멘트 작성 금지. 개선점과 오류에 집중.

## 예외(에러) 대응

- 계정 변경, 권한 문제, PR 생성에 실패하면 중단하고 사용자에 표시.
- 각 진행 중인 단계 표시(예: `<REVIEW-1>`, `<REVIEW-3>`)

## 최종 완료 및 검증

- 제출한 리뷰가 PR에 정상 등록되었는지 확인.
