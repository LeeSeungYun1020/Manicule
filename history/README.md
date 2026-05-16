# History

커밋별 의사결정 기록. 파일 이름 = `<short-hash>.md`.

## 원칙

- 이유만 간결하게 적는다 — 그 결정을 내린 이유, 검토했던 대안, 여러 대안 중 선택한 경우 트레이드 오프.
- 자신, 협업자, AI가 작업 결정을 이 파일로 확인할 수 있도록 키워드 중심으로 간단히 작성
- 의사결정이 없거나 순수 tooling/format 류 커밋은 history 만들지 않는다.

## 형식

```markdown
# <short-hash> — <commit subject>

**Files**: 핵심 파일 1–3 개

## 결정
- 한 줄 결론

## 이유
- 이 결정을 한 배경, 제약, 대안
```

## 인덱스

- [da35569](da35569.md) — 기반 시스템 구현 (Foundation 의 핵심 결정 모음)
- [0f0c1fa](0f0c1fa.md) — BoM 적용 (Compose / kotlinx / OkHttp / Retrofit / Coil)
- [3632472](3632472.md) — 타입 안전 네비게이션 적용
- [036d0f7](036d0f7.md) — ktlint + detekt 도입과 역할 분리
- [cf63cf8](cf63cf8.md) — Firebase 컨벤션 플러그인 + BoM
- [ee4c7b1](ee4c7b1.md) — `-Xcontext-parameters` 플래그 제거
- [5942347](5942347.md) — Network/Image BoM 전역 자동 적용 제거
