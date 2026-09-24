# 수정·검증·commit

[공통 규칙](skills.md)을 적용한다. `work.md`에서 정한 PR 범위 또는 `apply-review.md`에서 정한 리뷰 그룹의 수정 절차다.

## 실행

1. 확정한 범위 안에서 수정하고 변경 유형과 영향 모듈을 확인한다. 공용 API 변경은 사용하는 모듈도 영향 범위에 포함한다.
2. 아래 기준으로 검증하고 범위 밖 변경이 생기면 원인을 확인한다. 다른 작업의 변경을 함께 stage하지 않는다.
3. `git diff --check` 후 현재 작업 파일만 stage한다. commit 메시지는 `<type>: <변경 요약>`으로 작성한다. 타입은 `feat`, `fix`, `docs`, `style`, `refactor`, `test`, `chore`를 사용하고 타입 뒤 괄호(scope·작업 번호)를 붙이지 않는다.
4. commit한다. 커밋 시 오류가 발생하면 우회하지 않고 원인을 확인·해결한다.
5. 새 의사결정 기록이 필요한지는 공통 규칙에 따른다.

## 검증 선택

| 변경 | 검증 |
|---|---|
| Markdown·plan·history·skills | `git diff --check`, 문서 링크·표·지침 일관성 확인. 수동 Gradle 실행은 생략 |
| CI·스크립트 | 구문과 변경한 실행 경로 확인. Gradle 작업을 바꾸면 작업 그래프도 확인 |
| Kotlin·Gradle | 변경한 동작의 테스트와 영향 모듈 빌드 |
| 리소스·Manifest·UI | 영향 모듈 빌드·Lint, 동작에 맞는 UI·기기 검증 |
| 공용 계약·빌드 설정 | 의존 모듈까지 검증 범위 확대 |

- 단위 테스트는 Android 모듈의 `:<모듈>:testDebugUnitTest`, JVM 모듈의 `:<모듈>:test`를 사용한다. 테스트 대상이 없거나 skip된 작업을 테스트 통과로 보고하지 않는다.
- 전체 검증은 [GitHub Actions](../.github/workflows/check.yml)의 `./gradlew check --console=plain --max-workers=2`로 수행한다. `gh pr checks <PR> --json name,state,link`로 최종 PR의 `Gradle Check` 성공을 확인한다. CI 대기·실패를 통과로 보고하지 않고 로컬에서 전체 검증을 중복 실행하지 않는다. CI 오류 재현 등 필요한 경우에만 같은 명령을 로컬에서 실행한다.
- `check`는 기기 테스트와 APK 조립을 대체하지 않는다. 변경에 필요한 `assembleDebug`, instrumented/UI 테스트는 별도로 수행한다.
- 검증 후 관련 코드·설정·의존성·실행 환경이 바뀌면 영향을 받는 검증을 다시 수행한다. 결과 재사용은 동일 입력·명령의 성공이 확인되는 경우에 한하며, 문서만 추가한 경우 같은 코드 검증을 반복하지 않는다.
- 로컬 영향 모듈 검증은 `./gradlew :<모듈>:testDebugUnitTest :<모듈>:assembleDebug -q --console=plain > <로그파일> 2>&1`처럼 실행하고 종료 코드를 확인한다. JVM 모듈은 `test`·`assemble`을 사용한다. 실패하면 `tail -n 80 <로그파일>`부터 확인하고 필요한 문맥을 더 읽는다.
