# AGENTS.md

## 프로젝트 개요
연간 목표(1년 계획)를 등록하고 진행 상황을 체크하며, 계획을 지키지 않았을 때 알림을 받는 개인용 목표 관리 서비스. 스프링부트 학습을 위한 첫 사이드 프로젝트.

## 기술 스택
- **Language**: Java 21
- **Framework**: Spring Boot 3.x / 4.x
- **Build Tool**: Gradle
- **ORM**: Spring Data JPA + Hibernate
- **DB**:
  - 로컬 개발/테스트: H2 (in-memory)
  - 추후 운영 전환 예정: PostgreSQL
- **인증**: 미정 (추후 Spring Security + JWT 예정)
- **Lombok**: 사용 (getter/setter, builder 등 보일러플레이트 최소화)

## 패키지 구조 규칙
- **Base package**: `com.hoeseok.yearly_goal_tracker`
- **계층 구조**:
```
com.hoeseok.yearly_goal_tracker
├── controller  (REST 컨트롤러)
├── service     (비즈니스 로직)
├── repository  (JPA Repository 인터페이스)
├── domain      (Entity 클래스)
├── dto         (요청/응답 DTO)
└── config      (설정 클래스)
```

## 코딩 컨벤션
- Entity 클래스는 Lombok `@Getter`, `@Builder` 사용 (Setter는 필요한 경우만 최소화)
- REST API는 `/api/v1/` prefix 사용
- 응답은 가능하면 DTO로 변환해서 반환 (Entity 직접 노출 금지)
- 커밋 메시지는 한글 또는 영어 모두 허용하되, "기능 추가", "버그 수정" 등 명확한 동사로 시작

## 핵심 도메인 모델 (초안)
```text
User (사용자)
└─ Goal (연간 목표): title, category, startDate, endDate, status
   ├─ SubTask (하위 태스크): title, period(주간/월간), status
   │  └─ CheckIn (체크인 기록): date, status, progressRate
   └─ NotificationRule (알림 규칙): type, condition
      └─ NotificationLog (발송 이력): sentAt, channel, content
```

## 핵심 기능 우선순위 (MVP)
1. Goal, SubTask CRUD REST API
2. CheckIn 등록/조회 API
3. 스케줄러 기반 미이행 감지 배치 (`@Scheduled`)
4. 이메일 알림 발송 (Spring Mail)
5. (이후) 프론트엔드 대시보드 연동

## 작업 시 주의사항
- 새 기능 작업 시 반드시 테스트 코드도 같이 작성
- `application.properties`에 민감정보(DB 비밀번호, API 키) 하드코딩 금지 → 환경변수 또는 별도 profile 파일 사용
- 이 프로젝트는 Salesforce/KFMS 프로젝트와 무관한 완전 별도 사이드 프로젝트임. Salesforce 관련 설정(`.sf` 등)이 생성되지 않도록 주의

## 참고 문서
- 프로젝트 계획서: `docs/project-plan.md` 또는 `docs/프로젝트_계획서_연간목표트래커.md`
