-- 1. 기존 데이터 정리 및 시퀀스 초기화
TRUNCATE TABLE check_ins, sub_tasks, goals, notification_logs, notification_rules, users RESTART IDENTITY CASCADE;

-- 2. 사용자 (User) - 기본 비밀번호: password123!
INSERT INTO users (username, email, password, role, created_at, updated_at) VALUES 
('정회석', 'huesuk4720@naver.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'ROLE_USER', NOW(), NOW());

-- 3. 연간 목표 (Goals)
INSERT INTO goals (user_id, title, description, category, status, target_progress_rate, start_date, end_date, created_at, updated_at) VALUES 
(1, 'AWS Solutions Architect Associate (SAA) 취득', '2026년 상반기 AWS SAA-C03 자격증 취득 및 클라우드 아키텍처 실무 역량 강화', 'STUDY', 'IN_PROGRESS', 100, '2026-01-01', '2026-08-31', NOW(), NOW()),
(1, '2026년 독서 24권 완독하기', '개발, 비즈니스, 인문학 분야 양서 24권 독서 및 서평 노션 정리', 'READING', 'IN_PROGRESS', 100, '2026-01-01', '2026-12-31', NOW(), NOW()),
(1, '주 3회 헬스장 루틴 유지', '주 3회 근력 운동 및 유산소 30분 루틴으로 건강 체력 증진', 'HEALTH', 'IN_PROGRESS', 100, '2026-01-01', '2026-12-31', NOW(), NOW()),
(1, '스프링부트 사이드 프로젝트 (Yearly) 완성', '개인 연간 목표 관리 서비스 풀스택 개발 및 실사용 배포', 'CAREER', 'IN_PROGRESS', 100, '2026-05-01', '2026-11-30', NOW(), NOW());

-- 4. 세부 태스크 (SubTasks)
INSERT INTO sub_tasks (goal_id, title, period_type, status, target_count, created_at, updated_at) VALUES 
(1, '온라인 강의 수강 및 핵심 아키텍처 정리', 'WEEKLY', 'IN_PROGRESS', 3, NOW(), NOW()),
(1, '기출 모의고사 풀이 및 오답 노트 작성', 'WEEKLY', 'IN_PROGRESS', 2, NOW(), NOW()),
(2, '매주 1챕터 집중 독서 및 핵심 인사이트 기록', 'WEEKLY', 'IN_PROGRESS', 1, NOW(), NOW()),
(3, '하체 및 코어 운동 & 천국의 계단 20분', 'WEEKLY', 'IN_PROGRESS', 3, NOW(), NOW()),
(4, '대시보드 UI 연동 및 프론트엔드 작업', 'WEEKLY', 'IN_PROGRESS', 2, NOW(), NOW());

-- 5. 일일 체크인 (CheckIns)
INSERT INTO check_ins (sub_task_id, check_in_date, progress_rate, status, memo, created_at, updated_at) VALUES 
(1, CURRENT_DATE - INTERVAL '1 day', 100, 'SUCCESS', 'Udemy 강의 섹션 5(VPC 심화) 완강', NOW(), NOW()),
(2, CURRENT_DATE, 85, 'SUCCESS', '1회차 모의고사 85점 달성! 오답 12문제 복습 완료', NOW(), NOW());
