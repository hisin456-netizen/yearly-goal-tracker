#!/usr/bin/env bash
# 백업에서 복원한다. 새 서버로 옮기거나 데이터를 되돌릴 때 사용한다.
#
#   ./scripts/restore.sh backups/db_latest.sql.gz [backups/uploads_latest.tar.gz]
#
# !! 현재 DB 의 모든 데이터가 백업 내용으로 덮어써진다. 실행 전에 확인 질문(yes)을 한다.
# 환경변수: ASSUME_YES=1 (확인 질문 생략), COMPOSE_CMD(기본 "docker compose")
set -euo pipefail
export MSYS_NO_PATHCONV=1   # Windows Git Bash 에서 /app 같은 컨테이너 경로가 변형되지 않게 (리눅스에서는 영향 없음)

cd "$(dirname "$0")/.."
COMPOSE=${COMPOSE_CMD:-docker compose}
DB_FILE=${1:?"사용법: $0 <db_백업.sql.gz> [uploads_백업.tar.gz]"}
UP_FILE=${2:-}

[ -f "$DB_FILE" ] || { echo "ERROR: 파일이 없습니다: $DB_FILE" >&2; exit 1; }
gzip -t "$DB_FILE"
if [ -n "$UP_FILE" ]; then
  [ -f "$UP_FILE" ] || { echo "ERROR: 파일이 없습니다: $UP_FILE" >&2; exit 1; }
  tar tzf "$UP_FILE" > /dev/null
fi

if [ "${ASSUME_YES:-}" != "1" ]; then
  echo "!! 현재 DB 의 모든 데이터가 다음 백업으로 덮어써집니다: $DB_FILE"
  read -r -p "계속하려면 yes 를 입력하세요: " answer
  [ "$answer" = "yes" ] || { echo "취소했습니다."; exit 1; }
fi

echo "앱을 멈추고 DB 를 복원합니다..."
$COMPOSE stop app        # 복원하는 동안 앱이 DB 에 쓰지 않도록 멈춘다
$COMPOSE up -d db

# DB 가 연결을 받을 때까지 기다린다
for _ in $(seq 1 30); do
  $COMPOSE exec -T db sh -c 'pg_isready -U "$POSTGRES_USER" -d "$POSTGRES_DB"' > /dev/null 2>&1 && break
  sleep 2
done

gunzip -c "$DB_FILE" | $COMPOSE exec -T db sh -c 'psql -q -v ON_ERROR_STOP=1 -U "$POSTGRES_USER" -d "$POSTGRES_DB"' > /dev/null
echo "DB 복원 완료"

if [ -n "$UP_FILE" ]; then
  # 앱 이미지의 entrypoint(java)를 tar 로 바꿔서 uploads 볼륨에 풀어 넣는다
  $COMPOSE run --rm --no-deps -T --entrypoint tar app xzf - -C /app < "$UP_FILE"
  echo "업로드 이미지 복원 완료"
fi

$COMPOSE up -d app
echo "복원이 끝났습니다. 앱을 다시 시작했습니다."
