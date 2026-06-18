# 3014974e30ff5e5d63222a99e78b86b3aeb6ef73

## 의사결정 기록

### core:network 모듈 구현
- **API 클라이언트**: 국립중앙도서관 ISBN 서지정보 API(JSON 형식) 스펙에 맞추어 `NlkApi` Retrofit 인터페이스를 정의했습니다. `kotlinx.serialization`을 사용하여 JSON 응답을 DTO(`NlkSearchResponseDto`, `NlkBookDto`)로 매핑하도록 구현했습니다.
- **인증 방식**: 매 API 요청 시 URL 쿼리로 `cert_key`가 포함되어야 하므로, OkHttp의 Interceptor(`NlkAuthInterceptor`)를 구현하여 중앙에서 자동 주입되도록 설계했습니다.
- **API 키 은닉화**: `NLK_AUTH_KEY`는 민감 정보이므로 `.gitignore` 처리된 `local.properties`에서 읽어오도록 처리했습니다. `core/network/build.gradle.kts`에서 `java.util.Properties`를 통해 이를 파싱하고, `BuildConfigField`로 앱에 노출하여 코드 레벨에서 안전하게 참조할 수 있게 만들었습니다.
- **의존성 주입**: Hilt를 통해 `NetworkModule`을 구성하여 OkHttp, Retrofit, NlkApi 인스턴스를 제공하도록 구성했습니다.
- **단위 테스트**: `MockWebServer`를 도입하여 네트워크 요청/응답 파싱을 검증하고, Interceptor의 키 주입 로직을 분리하여 검증하는 테스트를 추가했습니다.

### 이슈 해결
- `build.gradle.kts` 내 `localProps` 변수 선언 시 ktlint의 `standard:multiline-expression-wrapping` 오류가 발생하여, `java.util.Properties().apply { ... }` 블록의 줄바꿈과 들여쓰기를 공식 ktlint Kotlin Script 포맷에 맞추어 수정했습니다.
