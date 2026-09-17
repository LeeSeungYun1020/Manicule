# Kotlin 정적 검사

## 도구와 역할

- ktlint Gradle 플러그인과 엔진 버전은 서로 다르다. `gradle/libs.versions.toml`의 `ktlint`는 플러그인 14.2.0, `ktlint-engine`은 엔진 1.5.0이다. 엔진을 명시해 플러그인 갱신만으로 서식이 바뀌지 않게 한다.
- 코드 스타일은 `.editorconfig`의 `ktlint_official`로 결정한다. Gradle의 별도 `android` 스타일 옵션은 사용하지 않는다. EditorConfig의 값 뒤에는 인라인 주석을 붙이지 않는다.
- ktlint는 서식·이름·import를 검사한다. detekt는 기본 설정을 상속하고 복잡도·잠재 오류·미사용 코드 등을 검사한다. `detekt-formatting`은 추가하지 않는다.
- detekt `style`과 `naming` 그룹은 활성화한다. 줄 길이·modifier 순서·파일 끝 개행·wildcard import 및 ktlint와 겹치는 이름/파일명 규칙만 개별 제외한다. 매개변수 이름·enum 이름·패키지 경로 일치·이름 가림과 미사용 코드 검사는 유지한다.

## Compose 예외

- `Unit` 반환 Composable은 PascalCase, 값을 반환하는 Composable은 일반 함수 명명 규칙을 따른다. ktlint의 `Composable` 애너테이션 예외는 이름 검사를 건너뛰므로 이 구분 자체를 강제하지 않는다.
- `LongParameterList`·`LongMethod`의 `ignoreAnnotated: ['Composable']`은 UI 선언을 위해서만 적용한다. 일반 함수의 기본 임계값(인자 6개, 길이 60줄)과 기존 기본값 인자 제외 정책은 유지한다. `ignoreAnnotatedParameter`는 함수 예외가 아니다.
- 타입 해석 없이 실행하는 `detekt`에서도 작동하도록 애너테이션의 짧은 이름을 사용한다.
- `UnusedPrivateMember`, `TooManyFunctions`, `MagicNumber`에는 `Preview`와 프로젝트의 `ManiculePreview`를 명시한다. 모든 Composable의 미사용 코드 검사를 끄지는 않는다.
- 숫자 리터럴은 테마 프로퍼티·애너테이션·Preview 샘플에서 허용한다. 일반 로직의 검사는 유지하고, 고정 날짜 형식/단계 매핑과 Preview provider 등은 해당 선언에만 이유와 함께 예외를 둔다.
- `ReturnCount`는 함수 시작의 guard clause를 제외한다. 트랜잭션 상태 분기와 리마인더의 실패·취소 전파는 해당 함수에만 `ReturnCount`/`ThrowsCount` 예외를 둔다.

## 줄바꿈 설정 검토 (#58)

과거 `59ef423` 기록에는 `multiline-expression-wrapping`과 `chain-method-continuation`의 포맷 충돌이 기재되어 있다. 이후 `2259850`에서 체인 규칙도 비활성화했다. 과거 환경의 문제와 현재 동작을 구분한다.

ktlint 1.5.0으로 전체 추적 Kotlin/Gradle 스크립트의 임시 복사본을 만들고, 두 규칙의 활성/비활성 네 조합에서 포맷을 두 번 실행했다. 네 조합 모두 성공했고 두 번째 실행에 추가 변경이 없었다. 두 규칙을 활성화한 결과는 비활성 상태의 포맷 결과와 42개 파일에서 달랐지만, 현재 코드에서 자동 수정 충돌은 재현되지 않았다.

두 규칙과 `no-empty-first-line-in-class-body` 비활성화는 기존 서식을 유지하는 정책이다. 규칙을 다시 켜거나 엔진을 갱신할 때는 별도 서식 변경으로 검토하고, 대표 Modifier 체인뿐 아니라 전체 소스에 대한 포맷 반복 실행으로 안정성을 확인한다.

## 검증

```sh
./gradlew ktlintFormat
./gradlew ktlintCheck detekt check :app:assembleDebug
```

`local.properties`의 `NLK_AUTH_KEY`와 Android SDK 설정이 필요하다. 포맷을 재실행한 뒤 소스 diff가 늘어나지 않는지도 확인한다.

#58에서는 별도 임시 Kotlin 샘플로 일반 함수의 잘못된 이름·긴 인자 목록·긴 본문, 미사용 매개변수·private 함수/프로퍼티/클래스가 검출되는지 확인했다. 같은 길이/인자 수의 Composable과 두 Preview 애너테이션은 지정된 규칙에서만 제외되는 것도 확인했다.

## 근거

- [Compose API 가이드](https://android.googlesource.com/platform/frameworks/support/+/androidx-main/compose/docs/compose-api-guidelines.md)
- [detekt 1.23.8 Compose 설정](https://detekt.dev/docs/1.23.8/introduction/compose/)
- [detekt 애너테이션 기반 예외](https://detekt.dev/docs/1.23.8/introduction/suppressors/)
- [EditorConfig 인라인 주석 명세](https://spec.editorconfig.org/#no-inline-comments)
