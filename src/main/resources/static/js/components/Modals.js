/**
 * Modals Component (Goal Create, Goal Edit, Goal Detail & SubTasks)
 */
import { state } from '../state.js';
import { api } from '../api.js';

const CATEGORY_NAMES = {
  STUDY: '공부/자격증',
  READING: '독서',
  HEALTH: '건강/운동',
  EXERCISE: '운동',
  CAREER: '커리어',
  FINANCE: '재테크',
  HOBBY: '취미',
  OTHER: '기타',
};

const STATUS_NAMES = {
  NOT_STARTED: '시작 전',
  IN_PROGRESS: '진행 중',
  COMPLETED: '완료됨',
  ARCHIVED: '보관됨',
};

export function setupModals(modalContainer, onDataChanged, showToast) {
  modalContainer.innerHTML = `
    <!-- 1. Create Goal Modal -->
    <div id="create-goal-modal" class="modal-overlay">
      <div class="modal-content">
        <div class="modal-header">
          <h3>🎯 새 연간 목표 등록</h3>
          <button type="button" class="close-btn" data-close="create-goal-modal">&times;</button>
        </div>
        <form id="create-goal-form">
          <div class="form-group">
            <label for="goal-title">목표 제목 *</label>
            <input type="text" id="goal-title" class="form-control" placeholder="예: AWS SAA 자격증 취득" required>
          </div>

          <div class="form-row-2">
            <div class="form-group">
              <label for="goal-category">카테고리 *</label>
              <select id="goal-category" class="form-control" required>
                <option value="STUDY">공부 / 자격증</option>
                <option value="READING">독서</option>
                <option value="HEALTH">건강 / 운동</option>
                <option value="CAREER">커리어</option>
                <option value="FINANCE">재테크</option>
                <option value="HOBBY">취미</option>
                <option value="OTHER">기타</option>
              </select>
            </div>
            <div class="form-group">
              <label for="goal-target-rate">목표 진척률 (%)</label>
              <input type="number" id="goal-target-rate" class="form-control" value="100" min="0" max="100">
            </div>
          </div>

          <div class="form-row-2">
            <div class="form-group">
              <label for="goal-start-date">시작일 *</label>
              <input type="date" id="goal-start-date" class="form-control" value="2026-01-01" required>
            </div>
            <div class="form-group">
              <label for="goal-end-date">종료일 *</label>
              <input type="date" id="goal-end-date" class="form-control" value="2026-12-31" required>
            </div>
          </div>

          <div class="form-group">
            <label for="goal-description">상세 설명</label>
            <textarea id="goal-description" class="form-control" rows="3" placeholder="목표 달성을 위한 동기나 핵심 계획을 적어주세요."></textarea>
          </div>

          <div class="modal-footer">
            <button type="button" class="btn btn-secondary" data-close="create-goal-modal">취소</button>
            <button type="submit" class="btn btn-primary">목표 만들기</button>
          </div>
        </form>
      </div>
    </div>

    <!-- 2. Edit Goal Modal -->
    <div id="edit-goal-modal" class="modal-overlay">
      <div class="modal-content">
        <div class="modal-header">
          <h3>✏️ 목표 정보 수정</h3>
          <button type="button" class="close-btn" data-close="edit-goal-modal">&times;</button>
        </div>
        <form id="edit-goal-form">
          <input type="hidden" id="edit-goal-id">

          <div class="form-group">
            <label for="edit-goal-title">목표 제목 *</label>
            <input type="text" id="edit-goal-title" class="form-control" required>
          </div>

          <div class="form-row-2">
            <div class="form-group">
              <label for="edit-goal-category">카테고리 *</label>
              <select id="edit-goal-category" class="form-control" required>
                <option value="STUDY">공부 / 자격증</option>
                <option value="READING">독서</option>
                <option value="HEALTH">건강 / 운동</option>
                <option value="CAREER">커리어</option>
                <option value="FINANCE">재테크</option>
                <option value="HOBBY">취미</option>
                <option value="OTHER">기타</option>
              </select>
            </div>
            <div class="form-group">
              <label for="edit-goal-status">진행 상태 *</label>
              <select id="edit-goal-status" class="form-control" required>
                <option value="IN_PROGRESS">진행 중 (IN_PROGRESS)</option>
                <option value="COMPLETED">완료됨 (COMPLETED)</option>
                <option value="NOT_STARTED">시작 전 (NOT_STARTED)</option>
                <option value="ARCHIVED">보관됨 (ARCHIVED)</option>
              </select>
            </div>
          </div>

          <div class="form-row-2">
            <div class="form-group">
              <label for="edit-goal-start-date">시작일 *</label>
              <input type="date" id="edit-goal-start-date" class="form-control" required>
            </div>
            <div class="form-group">
              <label for="edit-goal-end-date">종료일 *</label>
              <input type="date" id="edit-goal-end-date" class="form-control" required>
            </div>
          </div>

          <div class="form-group">
            <label for="edit-goal-target-rate">목표 진척률 (%)</label>
            <input type="number" id="edit-goal-target-rate" class="form-control" min="0" max="100">
          </div>

          <div class="form-group">
            <label for="edit-goal-description">상세 설명</label>
            <textarea id="edit-goal-description" class="form-control" rows="3" placeholder="목표 상세 내용"></textarea>
          </div>

          <div class="modal-footer">
            <button type="button" class="btn btn-secondary" data-close="edit-goal-modal">취소</button>
            <button type="submit" class="btn btn-primary">변경사항 저장</button>
          </div>
        </form>
      </div>
    </div>

    <!-- 3. Goal Detail & SubTasks Modal -->
    <div id="goal-detail-modal" class="modal-overlay">
      <div class="modal-content" style="max-width: 680px;">
        <div class="modal-header">
          <h3 id="detail-goal-title">목표 상세</h3>
          <button type="button" class="close-btn" data-close="goal-detail-modal">&times;</button>
        </div>

        <div id="detail-goal-body">
          <!-- Populated dynamically -->
        </div>
      </div>
    </div>
  `;

  // Close modal button listeners
  modalContainer.querySelectorAll('[data-close]').forEach((btn) => {
    btn.addEventListener('click', (e) => {
      const targetId = e.currentTarget.dataset.close;
      document.getElementById(targetId)?.classList.remove('open');
    });
  });

  // Handle outside click to close
  modalContainer.querySelectorAll('.modal-overlay').forEach((overlay) => {
    overlay.addEventListener('click', (e) => {
      if (e.target === overlay) {
        overlay.classList.remove('open');
      }
    });
  });

  // Handle Create Goal Form
  const createForm = document.getElementById('create-goal-form');
  createForm.addEventListener('submit', async (e) => {
    e.preventDefault();
    if (!state.currentUser) {
      showToast('사용자가 선택되지 않았습니다.', 'error');
      return;
    }

    const payload = {
      userId: state.currentUser.id,
      title: document.getElementById('goal-title').value.trim(),
      category: document.getElementById('goal-category').value,
      startDate: document.getElementById('goal-start-date').value,
      endDate: document.getElementById('goal-end-date').value,
      targetProgressRate: Number(document.getElementById('goal-target-rate').value) || 100,
      description: document.getElementById('goal-description').value.trim(),
    };

    try {
      await api.createGoal(payload);
      showToast('새 연간 목표가 성공적으로 등록되었습니다! 🚀');
      document.getElementById('create-goal-modal').classList.remove('open');
      createForm.reset();
      if (onDataChanged) await onDataChanged();
    } catch (err) {
      showToast(err.message || '목표 생성 실패', 'error');
    }
  });

  // Handle Edit Goal Form
  const editForm = document.getElementById('edit-goal-form');
  editForm.addEventListener('submit', async (e) => {
    e.preventDefault();
    const goalId = Number(document.getElementById('edit-goal-id').value);
    if (!goalId) return;

    const payload = {
      title: document.getElementById('edit-goal-title').value.trim(),
      category: document.getElementById('edit-goal-category').value,
      status: document.getElementById('edit-goal-status').value,
      startDate: document.getElementById('edit-goal-start-date').value,
      endDate: document.getElementById('edit-goal-end-date').value,
      targetProgressRate: Number(document.getElementById('edit-goal-target-rate').value) || 100,
      description: document.getElementById('edit-goal-description').value.trim(),
    };

    try {
      await api.updateGoal(goalId, payload);
      showToast('목표 정보가 성공적으로 수정되었습니다! ✏️');
      document.getElementById('edit-goal-modal').classList.remove('open');
      if (onDataChanged) await onDataChanged();
    } catch (err) {
      showToast(err.message || '목표 수정 실패', 'error');
    }
  });

  const openEditGoalModal = (goal) => {
    document.getElementById('edit-goal-id').value = goal.id;
    document.getElementById('edit-goal-title').value = goal.title || '';
    document.getElementById('edit-goal-category').value = goal.category || 'STUDY';
    document.getElementById('edit-goal-status').value = goal.status || 'IN_PROGRESS';
    document.getElementById('edit-goal-start-date').value = goal.startDate || '';
    document.getElementById('edit-goal-end-date').value = goal.endDate || '';
    document.getElementById('edit-goal-target-rate').value = goal.targetProgressRate || 100;
    document.getElementById('edit-goal-description').value = goal.description || '';

    // Close detail modal if open, open edit modal
    document.getElementById('goal-detail-modal').classList.remove('open');
    document.getElementById('edit-goal-modal').classList.add('open');
  };

  return {
    openCreateGoalModal: () => {
      document.getElementById('create-goal-modal').classList.add('open');
    },

    openEditGoalModal,

    openGoalDetailModal: async (goal, options = {}) => {
      const modal = document.getElementById('goal-detail-modal');
      const titleEl = document.getElementById('detail-goal-title');
      const bodyEl = document.getElementById('detail-goal-body');

      titleEl.textContent = goal.title;
      bodyEl.innerHTML = '<div style="text-align: center; padding: 24px;">상세 정보를 불러오는 중...</div>';
      modal.classList.add('open');

      try {
        // Fetch fresh detail with subtasks
        const detail = await api.getGoalDetail(goal.id);

        const isCompleted = detail.status === 'COMPLETED';

        const subTasksHtml = (detail.subTasks || [])
          .map(
            (st) => `
          <div style="display: flex; align-items: center; justify-content: space-between; padding: 10px 14px; background: rgba(255, 255, 255, 0.04); border-radius: 8px; margin-bottom: 8px;">
            <div>
              <div style="font-weight: 700; font-size: 0.9rem;">${st.title}</div>
              <div style="font-size: 0.76rem; color: var(--text-muted);">주기: ${st.periodType === 'WEEKLY' ? '주간' : '월간'} | 목표 횟수: ${st.targetCount}회</div>
            </div>
            <button type="button" class="btn btn-secondary btn-sm delete-subtask-btn" data-st-id="${st.id}" style="color: var(--color-coral); border-color: rgba(244, 63, 94, 0.3);">삭제</button>
          </div>
        `
          )
          .join('');

        bodyEl.innerHTML = `
          <!-- Header Status Bar with Quick Toggle -->
          <div style="display: flex; align-items: center; justify-content: space-between; background: rgba(255, 255, 255, 0.03); padding: 12px 16px; border-radius: 10px; margin-bottom: 16px; border: 1px solid var(--border-subtle);">
            <div style="display: flex; align-items: center; gap: 8px;">
              <span class="badge badge-${(detail.category || 'etc').toLowerCase()}">${CATEGORY_NAMES[detail.category] || detail.category}</span>
              <span class="goal-status-tag ${detail.status ? detail.status.toLowerCase() : ''}">${STATUS_NAMES[detail.status] || detail.status}</span>
              <span style="font-size: 0.8rem; color: var(--text-secondary);">목표치: <strong>${detail.targetProgressRate}%</strong></span>
            </div>
            <div>
              <button type="button" id="toggle-status-btn" class="btn btn-sm ${isCompleted ? 'btn-secondary' : 'btn-primary'}" style="font-size: 0.8rem; padding: 5px 12px;">
                ${isCompleted ? '🔄 진행 중으로 변경' : '✅ 완료 처리'}
              </button>
            </div>
          </div>

          <div style="margin-bottom: 20px;">
            <p style="color: var(--text-secondary); font-size: 0.9rem; margin-bottom: 12px; line-height: 1.5;">${detail.description || '상세 설명 없음'}</p>
            <div style="display: flex; gap: 16px; font-size: 0.82rem; color: var(--text-muted);">
              <span>📅 기간: <strong>${detail.startDate} ~ ${detail.endDate}</strong></span>
            </div>
          </div>

          <hr style="border: none; border-top: 1px solid var(--border-subtle); margin: 20px 0;">

          <div>
            <div style="display: flex; align-items: center; justify-content: space-between; margin-bottom: 12px;">
              <h4 style="font-size: 0.95rem; font-weight: 700;">📌 하위 태스크 (SubTasks)</h4>
              <button type="button" id="ai-suggest-btn" class="btn btn-sm" style="background: linear-gradient(135deg,#7c6ffa,#a78bfa); color:#fff; font-size:0.78rem; gap:5px;">
                ✨ AI 추천
              </button>
            </div>

            <!-- AI 추천 결과 영역 -->
            <div id="ai-suggestions-area" style="display:none; margin-bottom:14px;"></div>

            <div id="subtasks-container">
              ${subTasksHtml || '<p style="font-size: 0.82rem; color: var(--text-muted); margin-bottom: 12px;">등록된 하위 태스크가 없습니다.</p>'}
            </div>

            <!-- Add SubTask Form -->
            <form id="add-subtask-form" style="margin-top: 16px; padding: 14px; background: rgba(0, 0, 0, 0.25); border-radius: 12px; border: 1px solid var(--border-subtle);">
              <h5 style="font-size: 0.85rem; margin-bottom: 10px; color: var(--color-cyan);">+ 하위 태스크 추가하기</h5>
              <div class="form-group">
                <input type="text" id="new-st-title" class="form-control" placeholder="태스크 이름 (예: 매주 주말 모의고사 1회 풀이)" required>
              </div>
              <div class="form-row-2">
                <div class="form-group">
                  <label style="font-size: 0.78rem;">주기</label>
                  <select id="new-st-period" class="form-control">
                    <option value="WEEKLY">주간 (Weekly)</option>
                    <option value="MONTHLY">월간 (Monthly)</option>
                  </select>
                </div>
                <div class="form-group">
                  <label style="font-size: 0.78rem;">주기당 목표 횟수</label>
                  <input type="number" id="new-st-count" class="form-control" value="1" min="1" required>
                </div>
              </div>
              <button type="submit" class="btn btn-primary btn-sm" style="width: 100%;">태스크 추가</button>
            </form>
          </div>

          <div class="modal-footer" style="margin-top: 24px; display: flex; justify-content: space-between; align-items: center;">
            <div>
              <button type="button" id="delete-goal-btn" class="btn btn-secondary" style="color: var(--color-coral); border-color: rgba(244, 63, 94, 0.3);">🗑️ 목표 삭제</button>
            </div>
            <div style="display: flex; gap: 8px;">
              <button type="button" id="edit-goal-btn" class="btn btn-secondary">✏️ 목표 수정</button>
              <button type="button" class="btn btn-secondary" data-close="goal-detail-modal">닫기</button>
            </div>
          </div>
        `;

        // Toggle Status listener
        document.getElementById('toggle-status-btn')?.addEventListener('click', async () => {
          const newStatus = isCompleted ? 'IN_PROGRESS' : 'COMPLETED';
          try {
            await api.updateGoalStatus(detail.id, newStatus);
            showToast(`목표 상태가 '${STATUS_NAMES[newStatus]}'으로 변경되었습니다! ✨`);
            modal.classList.remove('open');
            if (onDataChanged) await onDataChanged();
          } catch (err) {
            showToast(err.message || '상태 변경 실패', 'error');
          }
        });

        // Edit Goal button listener
        document.getElementById('edit-goal-btn')?.addEventListener('click', () => {
          openEditGoalModal(detail);
        });

        // AI 추천 버튼
        document.getElementById('ai-suggest-btn')?.addEventListener('click', async () => {
          const btn = document.getElementById('ai-suggest-btn');
          const area = document.getElementById('ai-suggestions-area');
          btn.disabled = true;
          btn.textContent = '✨ AI 분석 중...';
          area.style.display = 'block';
          area.innerHTML = `<div style="padding:12px; background:rgba(124,111,250,0.08); border-radius:10px; border:1px solid rgba(124,111,250,0.2); font-size:0.82rem; color:var(--text-muted);">AI가 추천 서브태스크를 생성하고 있습니다...</div>`;

          try {
            const suggestions = await api.ai.suggestSubTasks({
              goalTitle: detail.title,
              category: detail.category,
              description: detail.description,
            });

            area.innerHTML = `
              <div style="padding:14px; background:rgba(124,111,250,0.08); border-radius:10px; border:1px solid rgba(124,111,250,0.2);">
                <div style="font-size:0.82rem; font-weight:700; color:#a5b4fc; margin-bottom:10px;">✨ AI 추천 서브태스크</div>
                ${suggestions.map((s, i) => `
                  <div class="ai-suggestion-item" style="display:flex; align-items:flex-start; gap:10px; padding:10px; background:rgba(255,255,255,0.03); border-radius:8px; margin-bottom:6px; border:1px solid rgba(255,255,255,0.06);">
                    <div style="flex:1;">
                      <div style="font-size:0.88rem; font-weight:700;">${s.title}</div>
                      <div style="font-size:0.74rem; color:var(--text-muted); margin-top:2px;">${s.periodType === 'DAILY' ? '매일' : s.periodType === 'WEEKLY' ? '주간' : '월간'} ${s.targetCount}회 · ${s.reason}</div>
                    </div>
                    <button type="button" class="btn btn-primary btn-sm add-suggestion-btn"
                      data-title="${encodeURIComponent(s.title)}"
                      data-period="${s.periodType}"
                      data-count="${s.targetCount}"
                      style="font-size:0.74rem; padding:4px 10px; white-space:nowrap;">
                      + 추가
                    </button>
                  </div>
                `).join('')}
              </div>
            `;

            area.querySelectorAll('.add-suggestion-btn').forEach(addBtn => {
              addBtn.addEventListener('click', async () => {
                const title = decodeURIComponent(addBtn.dataset.title);
                const periodType = addBtn.dataset.period;
                const targetCount = Number(addBtn.dataset.count);
                try {
                  await api.createSubTask(goal.id, { title, periodType, targetCount });
                  showToast(`"${title}" 추가되었습니다! 🎯`);
                  addBtn.textContent = '✓ 추가됨';
                  addBtn.disabled = true;
                  addBtn.style.background = 'rgba(32,201,151,0.3)';
                  if (onDataChanged) await onDataChanged();
                } catch (err) {
                  showToast(err.message || '추가 실패', 'error');
                }
              });
            });

          } catch (err) {
            area.innerHTML = `<div style="padding:12px; background:rgba(242,92,120,0.08); border-radius:10px; border:1px solid rgba(242,92,120,0.2); font-size:0.82rem; color:#fca5a5;">AI 추천 실패: ${err.message}</div>`;
          } finally {
            btn.disabled = false;
            btn.textContent = '✨ AI 추천';
          }
        });

        // Subtask form listener
        const subTaskForm = document.getElementById('add-subtask-form');
        subTaskForm.addEventListener('submit', async (e) => {
          e.preventDefault();
          const title = document.getElementById('new-st-title').value.trim();
          const periodType = document.getElementById('new-st-period').value;
          const targetCount = Number(document.getElementById('new-st-count').value) || 1;

          try {
            await api.createSubTask(goal.id, { title, periodType, targetCount });
            showToast('하위 태스크가 추가되었습니다! 🎯');
            if (onDataChanged) await onDataChanged();
            // Refresh modal
            const updatedGoal = state.goals.find((g) => g.id === goal.id) || goal;
            setupModals(modalContainer, onDataChanged, showToast).openGoalDetailModal(updatedGoal);
          } catch (err) {
            showToast(err.message || '하위 태스크 추가 실패', 'error');
          }
        });

        // Subtask delete listeners
        bodyEl.querySelectorAll('.delete-subtask-btn').forEach((btn) => {
          btn.addEventListener('click', async (e) => {
            const stId = Number(e.currentTarget.dataset.stId);
            if (confirm('이 하위 태스크를 삭제하시겠습니까?')) {
              try {
                await api.deleteSubTask(stId);
                showToast('하위 태스크가 삭제되었습니다.');
                if (onDataChanged) await onDataChanged();
                const updatedGoal = state.goals.find((g) => g.id === goal.id) || goal;
                setupModals(modalContainer, onDataChanged, showToast).openGoalDetailModal(updatedGoal);
              } catch (err) {
                showToast(err.message || '삭제 실패', 'error');
              }
            }
          });
        });

        // Goal delete listener
        document.getElementById('delete-goal-btn')?.addEventListener('click', async () => {
          if (confirm(`'${detail.title}' 목표를 정말 삭제하시겠습니까? 모든 하위 태스크와 체크인 기록도 함께 삭제됩니다.`)) {
            try {
              await api.deleteGoal(goal.id);
              showToast('목표가 삭제되었습니다.');
              modal.classList.remove('open');
              if (onDataChanged) onDataChanged();
            } catch (err) {
              showToast(err.message || '목표 삭제 실패', 'error');
            }
          }
        });

        // Close btn inside newly created markup
        bodyEl.querySelectorAll('[data-close]').forEach((btn) => {
          btn.addEventListener('click', () => modal.classList.remove('open'));
        });

        // Auto-trigger AI suggest if requested
        if (options.autoAi) {
          setTimeout(() => document.getElementById('ai-suggest-btn')?.click(), 400);
        }
      } catch (err) {
        bodyEl.innerHTML = `<div style="color: var(--color-coral); text-align: center; padding: 24px;">상세 정보를 불러오지 못했습니다: ${err.message}</div>`;
      }
    },
  };
}
