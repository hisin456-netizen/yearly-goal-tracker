# Oracle Cloud Always Free 배포 가이드

앱 + PostgreSQL + Caddy(HTTPS)를 Oracle Cloud 무료 VM 한 대에 `docker compose`로 올린다.

> 무료 한도는 Oracle이 예고 없이 바꾼 이력이 있다(2026-06 Ampere A1: 4코어/24GB → 2코어/12GB).
> 이 앱은 1GB 안팎이면 돌기 때문에 줄어든 한도로도 충분하다. 다만 계정/정책은 주기적으로 확인한다.

## 0. 사전 준비 (내 PC)

배포는 GitHub 저장소에서 코드를 받아오는 방식이다. **먼저 현재 변경 사항을 커밋하고 push** 해야 한다.

```bash
git add -A
git commit -m "배포 설정 추가 및 권한 검사 보안 수정"
git push
```

## 1. Oracle Cloud 가입 & VM 만들기 (직접 진행)

1. https://www.oracle.com/cloud/free/ 에서 가입한다. 카드 인증이 필요하다(인증용 소액 승인 후 취소, 무료 리소스만 쓰면 과금 없음).
2. **홈 리전은 가입 후 못 바꾼다.** 서울(Seoul) 또는 춘천(Chuncheon)을 고른다.
3. 컴퓨트 → 인스턴스 → 인스턴스 생성
   - 이미지: **Ubuntu 22.04 또는 24.04**
   - 셰이프: **VM.Standard.A1.Flex** (Ampere ARM), OCPU 2 / 메모리 12GB 이하로 지정 (무료 한도)
     - "Out of capacity" 오류가 나면 시간대를 바꿔 재시도하거나, AMD `VM.Standard.E2.1.Micro`(1GB, 무료)는 메모리가 부족해 권장하지 않는다.
   - SSH 키 쌍을 새로 만들고 **개인 키(.key)를 저장**해 둔다.
   - 공용 IP 할당: 켬
4. 인스턴스가 만들어지면 **공용 IP**를 메모한다.

## 2. 방화벽 열기 (80, 443)

**(a) Oracle 콘솔(VCN 보안 목록)**
네트워킹 → 가상 클라우드 네트워크 → (내 VCN) → 서브넷 → 기본 보안 목록 → 수신 규칙 추가
- 소스 CIDR `0.0.0.0/0`, TCP, 대상 포트 `80`
- 소스 CIDR `0.0.0.0/0`, TCP, 대상 포트 `443`

**(b) VM 내부(iptables)** — Oracle의 Ubuntu 이미지는 기본적으로 80/443을 막아 둔다.

```bash
ssh -i <개인키.key> ubuntu@<공용IP>

sudo iptables -I INPUT 6 -m state --state NEW -p tcp --dport 80 -j ACCEPT
sudo iptables -I INPUT 6 -m state --state NEW -p tcp --dport 443 -j ACCEPT
sudo apt-get update && sudo apt-get install -y iptables-persistent   # 설치 중 저장 여부 질문에 Yes
sudo netfilter-persistent save
```

## 3. 도메인 준비 (무료: DuckDNS)

HTTPS 인증서를 받으려면 도메인이 필요하다.

1. https://www.duckdns.org 에 로그인해서 서브도메인을 만든다. 예: `mygoals` → `mygoals.duckdns.org`
2. current ip에 VM의 **공용 IP**를 입력하고 update 한다.

> 다른 도메인이 있으면 A 레코드를 VM 공용 IP로 지정해도 된다.

## 4. Docker 설치 & 배포 (VM에서)

```bash
curl -fsSL https://get.docker.com | sudo sh
sudo usermod -aG docker $USER && newgrp docker

git clone https://github.com/hisin456-netizen/yearly-goal-tracker.git
cd yearly-goal-tracker

cp .env.example .env
nano .env     # 아래 값 채우기
```

`.env` 에서 채울 값:

| 키 | 값 |
|---|---|
| `DB_PASSWORD` | 임의의 긴 문자열 (`openssl rand -hex 24`) |
| `JWT_SECRET` | `openssl rand -base64 48` 결과 |
| `ALLOWED_EMAILS` | 두 사람 이메일을 쉼표로 (예: `me@gmail.com,partner@gmail.com`). **반드시 채운다.** |
| `DOMAIN` | `mygoals.duckdns.org` |
| `APP_BASE_URL` | `https://mygoals.duckdns.org` |
| `GOOGLE_CLIENT_ID` / `GOOGLE_CLIENT_SECRET` | Google 로그인을 쓰면 실제 값, **안 쓰면 아무 문자열이나 `dummy` 로** 채운다 (앱 기동에 필요) |
| `GOOGLE_AI_API_KEY` | AI 추천 기능을 쓸 때만 |

실행:

```bash
docker compose --profile https up -d --build
docker compose ps
docker compose logs -f app      # "Started YearlyGoalTrackerApplication" 확인
```

브라우저에서 `https://mygoals.duckdns.org` 로 접속한다. 첫 접속 때 Caddy가 인증서를 발급하느라 몇 초 걸릴 수 있다.

> ARM(Ampere) VM에서는 첫 빌드에 몇 분 걸린다.

## 5. Google 로그인 (선택)

이메일/비밀번호 로그인은 별도 설정 없이 동작한다. Google 로그인을 쓰려면:

1. Google Cloud Console → API 및 서비스 → 사용자 인증 정보 → OAuth 클라이언트
2. **승인된 리디렉션 URI** 에 `https://<DOMAIN>/login/oauth2/code/google` 추가
3. `.env` 에 실제 Client ID/Secret 입력 후 `docker compose --profile https up -d`

> `duckdns.org` 같은 공용 서브도메인을 Google이 리디렉션 URI로 허용하는지는 확인하지 못했다.
> 안 되면 이메일 로그인을 쓰거나, 본인 소유 도메인을 연결한다.

## 6. 백업 (꼭 설정한다)

> **왜 중요한가:** Oracle은 7일 동안 CPU/네트워크(A1은 메모리도)가 20% 미만인 "유휴" 무료 인스턴스를 회수할 수 있다고 안내한다.
> 둘이서 쓰는 이 앱은 거의 항상 이 조건에 해당한다. 서버가 회수돼도 **서버 밖에 백업이 있으면** 새 서버에서 그대로 복원할 수 있다.

### 6-1. 서버에서 매일 자동 백업 (cron)

```bash
cd ~/yearly-goal-tracker
./scripts/backup.sh          # 먼저 손으로 한 번 실행해서 "backup OK" 가 나오는지 확인
ls -lh backups/              # db_*.sql.gz, uploads_*.tar.gz, *_latest.* 파일이 생긴다

crontab -e                   # 아래 한 줄 추가 (매일 새벽 4시 30분)
30 4 * * * cd $HOME/yearly-goal-tracker && ./scripts/backup.sh >> $HOME/ygt-backup.log 2>&1
```

- 서버 안에는 최근 14일치를 보관한다 (`KEEP_DAYS` 로 변경). 백업 중 오류가 나면 깨진 파일을 남기지 않고 오류를 로그에 남긴다.
- 서버 안의 백업만으로는 서버가 사라질 때 같이 사라진다. **다음 6-2를 꼭 한다.**

### 6-2. 내 PC로 가져오기 (서버 밖 보관)

내 PC의 PowerShell에서 (Windows 10/11 은 `scp` 가 기본 설치돼 있다):

```powershell
mkdir C:\ygt-backups -Force
scp -i C:\경로\ssh-key.key "ubuntu@<공용IP>:~/yearly-goal-tracker/backups/*_latest.*" C:\ygt-backups\
```

일주일에 한 번쯤, 또는 데이터를 많이 넣은 날 실행한다. 같은 이름으로 덮어쓰이므로, 날짜별로 남기려면 받은 폴더를 복사해 둔다.
(Windows 작업 스케줄러에 등록하면 자동화할 수 있다.)

### 6-3. 복원하기

같은 서버에서 되돌리거나, **새 서버로 옮길 때** 사용한다. 새 서버에서는 4단계(배포)까지 마친 뒤 실행한다.

```bash
# 내 PC → 새 서버로 백업 올리기 (PowerShell)
scp -i C:\경로\ssh-key.key C:\ygt-backups\*_latest.* ubuntu@<새 공용IP>:~/yearly-goal-tracker/backups/

# 서버에서 복원 (현재 데이터가 백업으로 덮어써지므로 yes 확인을 묻는다)
cd ~/yearly-goal-tracker
./scripts/restore.sh backups/db_latest.sql.gz backups/uploads_latest.tar.gz
```

> **복원이 실제로 되는지 미리 한 번 연습해 두는 것을 권한다.** 백업은 복원해 봐야 믿을 수 있다.

## 7. 운영

```bash
# 업데이트
git pull && docker compose --profile https up -d --build

# 로그
docker compose logs -f app

# 상태
docker compose ps
```

## 보안 설정 요약

- **로그인 시도 제한:** 같은 이메일로 5회(같은 IP는 20회) 실패하면 15분 동안 로그인이 차단된다(HTTP 429). 시간이 지나면 자동으로 풀린다.
  차단 중에는 비밀번호가 맞아도 거부된다. 그래도 **강한 비밀번호는 필수**다.
- **Swagger(API 문서)는 운영에서 비공개**다(404).
- **SSH 는 키 로그인만** 쓴다. 비밀번호 로그인은 켜지 않는다. 키 파일(`.key`)은 잃어버리지 않게 여러 곳에 백업한다.
- 서버의 `.env` 는 `chmod 600 .env` 로 다른 사용자가 못 읽게 한다.

## 주의

- **Oracle은 유휴 Always Free 인스턴스를 회수할 수 있다**(위 6번 참고). 서버 밖 백업(6-2)을 꼭 한다.
- **요금 실수 방지:** 인스턴스는 "Always Free 사용 가능" 표시가 있는 셰이프만 고르고, **"Pay As You Go 업그레이드"는 누르지 않는다.**
  가입 직후 청구 → 예산에서 **$1 예산 알림**을 만들어 둔다.
- **가입은 `ALLOWED_EMAILS` 에 적은 이메일만 가능하다.** 필수값이라 비우면 `docker compose` 가 실행되지 않는다.
  이메일 가입과 Google 로그인(새 계정 생성) 모두에 적용되고, 이미 가입된 계정의 로그인에는 영향이 없다.
  이메일을 바꾸려면 `.env` 수정 후 `docker compose --profile https up -d`.
- `.env`(비밀값)는 절대 git에 올리지 않는다(`.gitignore`에 포함됨).
