#!/usr/bin/env bash
# DB(+업로드 이미지) 백업 스크립트. 서버에서 cron 으로 매일 실행한다.
#
#   ./scripts/backup.sh
#
# 결과: backups/db_<시각>.sql.gz, backups/uploads_<시각>.tar.gz  (+ 항상 최신본을 가리키는 *_latest 복사본)
# 환경변수: KEEP_DAYS(보관 일수, 기본 14), BACKUP_DIR(기본 backups), COMPOSE_CMD(기본 "docker compose")
set -euo pipefail
export MSYS_NO_PATHCONV=1   # Windows Git Bash 에서 /app 같은 컨테이너 경로가 변형되지 않게 (리눅스에서는 영향 없음)

cd "$(dirname "$0")/.."
COMPOSE=${COMPOSE_CMD:-docker compose}
BACKUP_DIR=${BACKUP_DIR:-backups}
KEEP_DAYS=${KEEP_DAYS:-14}
STAMP=$(date +%F_%H%M%S)

mkdir -p "$BACKUP_DIR"
DB_FILE="$BACKUP_DIR/db_${STAMP}.sql.gz"
UP_FILE="$BACKUP_DIR/uploads_${STAMP}.tar.gz"

# 임시 파일에 먼저 쓰고 검증한 뒤 이름을 바꾼다. 중간에 실패해도 깨진 백업이 정상 백업처럼 남지 않는다.
trap 'rm -f "$DB_FILE.tmp" "$UP_FILE.tmp"' EXIT

# 1) DB: --clean --if-exists 로 떠 두면 기존 데이터가 있는 DB에도, 빈 새 DB에도 그대로 복원할 수 있다.
#    pipefail 이 켜져 있어서 pg_dump 가 실패하면 여기서 중단된다.
$COMPOSE exec -T db sh -c 'pg_dump -U "$POSTGRES_USER" -d "$POSTGRES_DB" --clean --if-exists' | gzip > "$DB_FILE.tmp"
gzip -t "$DB_FILE.tmp"
if ! gunzip -c "$DB_FILE.tmp" | grep -q "PostgreSQL database dump complete"; then
  echo "ERROR: DB 백업이 끝까지 완료되지 않았습니다 (덤프 끝 표시 없음)" >&2
  exit 1
fi
mv "$DB_FILE.tmp" "$DB_FILE"

# 2) 업로드된 이미지
$COMPOSE exec -T app tar czf - -C /app uploads > "$UP_FILE.tmp"
tar tzf "$UP_FILE.tmp" > /dev/null
mv "$UP_FILE.tmp" "$UP_FILE"

# 3) 내 PC로 가져가기 쉽도록 항상 최신본을 같은 이름으로도 복사해 둔다
cp -f "$DB_FILE" "$BACKUP_DIR/db_latest.sql.gz"
cp -f "$UP_FILE" "$BACKUP_DIR/uploads_latest.tar.gz"

# 4) 오래된 백업 삭제 (latest 복사본은 유지)
find "$BACKUP_DIR" -maxdepth 1 -type f \( -name 'db_2*.sql.gz' -o -name 'uploads_2*.tar.gz' \) -mtime +"$KEEP_DAYS" -delete

echo "[$(date '+%F %T')] backup OK: $(basename "$DB_FILE") ($(du -h "$DB_FILE" | cut -f1)), $(basename "$UP_FILE") ($(du -h "$UP_FILE" | cut -f1))"
