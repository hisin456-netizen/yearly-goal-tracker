# yearly-goal-tracker
1년 목표 추적 및 미이행 알림 서비스

## 배포 (Docker Compose)

앱(Spring Boot)과 PostgreSQL을 한 번에 띄운다. 운영 설정은 `application-prod.properties`이고, 비밀값은 전부 환경변수(`.env`)로 받는다.

```bash
cp .env.example .env     # 값 채우기 (DB_PASSWORD, JWT_SECRET, GOOGLE_*, ALLOWED_EMAILS, DOMAIN, APP_BASE_URL)
docker compose up -d --build                     # 로컬: http://localhost:8080 (127.0.0.1 에만 바인딩)
docker compose --profile https up -d --build     # 서버: Caddy가 80/443 + 자동 HTTPS
```

- Oracle Cloud 무료 VM 배포 절차는 [docs/deploy-oracle-cloud.md](docs/deploy-oracle-cloud.md) 참고.
- 데이터는 Docker 볼륨(`pgdata`, `uploads`, `caddy_data`)에 저장되므로 컨테이너를 다시 만들어도 유지된다.
- `APP_BASE_URL`은 실제 접속 주소(https)로 지정한다. Google 로그인 후 이 주소로 돌아온다.
- Google Cloud Console → 사용자 인증 정보 → OAuth 클라이언트의 **승인된 리디렉션 URI**에
  `${APP_BASE_URL}/login/oauth2/code/google` 를 추가해야 Google 로그인이 동작한다.
- 앱은 HTTPS를 직접 처리하지 않는다. `https` 프로필의 Caddy(또는 다른 리버스 프록시/PaaS)가 HTTPS를 종단한다.
- 운영 프로필(`prod`)에서는 `DemoDataMigrationRunner`(비밀번호를 `password123!`로 초기화하는 로컬 개발용 코드)가 비활성화된다.

업데이트 배포:

```bash
git pull && docker compose up -d --build
```

백업 / 복원 (DB + 업로드 이미지, 자세한 내용은 배포 가이드 6번):

```bash
./scripts/backup.sh                                                        # backups/ 에 저장 (cron 으로 매일 실행 권장)
./scripts/restore.sh backups/db_latest.sql.gz backups/uploads_latest.tar.gz   # 복원 (현재 데이터를 덮어씀)
```

보안 관련 동작:
- 로그인 5회 연속 실패(같은 IP는 20회) 시 15분간 차단(429), 운영 프로필에서는 Swagger 비공개, 가입은 `ALLOWED_EMAILS` 만 허용.
