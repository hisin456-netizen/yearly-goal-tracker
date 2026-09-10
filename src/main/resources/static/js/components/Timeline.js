import { state } from '../state.js';

const MONTHS = ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'];
const CATEGORY_NAMES = {
  STUDY: '공부/자격증', READING: '독서', HEALTH: '건강/운동',
  EXERCISE: '운동', CAREER: '커리어', FINANCE: '재테크', HOBBY: '취미', OTHER: '기타',
};
const STATUS_NAMES = {
  ALL: '모든 상태', IN_PROGRESS: '진행 중', COMPLETED: '완료됨',
  NOT_STARTED: '시작 전', ARCHIVED: '보관됨',
};

// handlers: { onSelect, onEdit, onAiSuggest }
export function renderTimeline(container, handlers) {
  const onSelect     = typeof handlers === 'function' ? handlers : handlers.onSelect;
  const onEdit       = typeof handlers === 'function' ? null     : handlers.onEdit;
  const onAiSuggest  = typeof handlers === 'function' ? null     : handlers.onAiSuggest;

  const currentMonthIndex = new Date().getMonth();

  let filteredGoals = state.goals;
  if (state.categoryFilter !== 'ALL') {
    filteredGoals = filteredGoals.filter(g => g.category === state.categoryFilter);
  }
  if (state.statusFilter && state.statusFilter !== 'ALL') {
    filteredGoals = filteredGoals.filter(g => g.status === state.statusFilter);
  }
  if (state.searchKeyword && state.searchKeyword.trim() !== '') {
    const kw = state.searchKeyword.trim().toLowerCase();
    filteredGoals = filteredGoals.filter(
      g => (g.title && g.title.toLowerCase().includes(kw)) ||
           (g.description && g.description.toLowerCase().includes(kw))
    );
  }

  const categories = ['ALL', 'STUDY', 'READING', 'HEALTH', 'CAREER', 'FINANCE'];
  const chipsHtml = categories.map(cat => `
    <button class="chip-btn ${state.categoryFilter === cat ? 'active' : ''}" data-category="${cat}">
      ${cat === 'ALL' ? '전체 목표' : CATEGORY_NAMES[cat] || cat}
    </button>
  `).join('');

  const statusOptionsHtml = Object.entries(STATUS_NAMES)
    .map(([val, label]) => `<option value="${val}" ${state.statusFilter === val ? 'selected' : ''}>${label}</option>`)
    .join('');

  const monthsHtml = MONTHS.map((m, idx) => `
    <span class="${idx === currentMonthIndex ? 'timeline-current-month' : ''}">${m}</span>
  `).join('');

  let rowsHtml = '';
  if (filteredGoals.length === 0) {
    rowsHtml = `
      <div class="empty-state">
        <div class="empty-state-icon">🎯</div>
        <p>조건에 맞는 목표가 없습니다. 필터를 변경하거나 <strong>[+ 새 목표]</strong>를 등록해 보세요!</p>
      </div>
    `;
  } else {
    rowsHtml = filteredGoals.map(goal => {
      const start = new Date(goal.startDate);
      const end   = new Date(goal.endDate);
      const startMonth = Math.max(0, start.getMonth() + start.getDate() / 31);
      const endMonth   = Math.min(12, end.getMonth() + end.getDate() / 31);
      const leftPercent  = (startMonth / 12) * 100;
      const widthPercent = Math.max(4, ((endMonth - startMonth) / 12) * 100);

      const categoryClass = (goal.category || 'etc').toLowerCase();
      const progressRate  = goal.targetProgressRate || 100;
      const isCompleted   = goal.status === 'COMPLETED';

      return `
        <div class="goal-timeline-row ${isCompleted ? 'goal-completed' : ''}" data-goal-id="${goal.id}">
          <div class="goal-row-main">
            <div class="goal-row-info">
              <span class="goal-row-title" title="${goal.title}">
                ${isCompleted ? '✅ ' : ''}${goal.title}
              </span>
              <div class="goal-row-meta">
                <span class="badge badge-${categoryClass}">${CATEGORY_NAMES[goal.category] || goal.category}</span>
                <span class="goal-status-tag ${goal.status ? goal.status.toLowerCase() : ''}">${STATUS_NAMES[goal.status] || goal.status}</span>
                <span class="goal-rate-badge">목표 ${progressRate}%</span>
              </div>
            </div>

            <div class="gantt-track-area">
              <div class="gantt-grid-lines">
                ${Array(12).fill('<div class="gantt-grid-col"></div>').join('')}
              </div>
              <div class="gantt-bar ${categoryClass} ${isCompleted ? 'completed-bar' : ''}" style="left: ${leftPercent}%; width: ${widthPercent}%;">
                <div class="gantt-progress-glow" title="${goal.startDate} ~ ${goal.endDate}"></div>
              </div>
            </div>
          </div>

          <div class="goal-row-actions">
            <button class="goal-quick-btn btn-ai" data-action="ai" data-goal-id="${goal.id}">✨ AI 추천</button>
            <button class="goal-quick-btn btn-edit" data-action="edit" data-goal-id="${goal.id}">✏️ 수정</button>
            <button class="goal-quick-btn btn-kanban" data-action="kanban" data-goal-id="${goal.id}">🗂️ 칸반보드</button>
            <button class="goal-quick-btn btn-detail" data-action="detail" data-goal-id="${goal.id}">상세보기 →</button>
          </div>
        </div>
      `;
    }).join('');
  }

  container.innerHTML = `
    <div class="section-header" style="flex-wrap: wrap; gap: 14px;">
      <div class="section-title-group">
        <h2><span>📅</span> ${state.currentYear} 연간 목표 타임라인</h2>
        <span style="font-size: 0.8rem; color: var(--text-muted); margin-left: 8px;">(총 ${filteredGoals.length}개)</span>
      </div>

      <div style="display: flex; align-items: center; gap: 10px; margin-left: auto; flex-wrap: wrap;">
        <div class="timeline-search-box" style="position: relative;">
          <input
            type="text"
            id="timeline-search-input"
            class="form-control"
            placeholder="🔍 목표 검색..."
            value="${state.searchKeyword || ''}"
            style="padding: 6px 12px; font-size: 0.82rem; width: 160px; height: 34px; border-radius: 20px; background: rgba(255,255,255,0.05); border: 1px solid var(--border-subtle);"
          />
        </div>

        <div class="pill-selector" style="padding: 4px 12px; height: 34px;">
          <span>⚡</span>
          <select id="timeline-status-select" aria-label="목표 상태 필터" style="font-size: 0.82rem;">
            ${statusOptionsHtml}
          </select>
        </div>
      </div>

      <div class="category-filter-chips" style="width: 100%; margin-top: 4px;">
        ${chipsHtml}
      </div>
    </div>

    <div class="timeline-container">
      <div class="timeline-months-bar">
        <span>목표 항목</span>
        ${monthsHtml}
      </div>
      <div class="goal-rows-list">
        ${rowsHtml}
      </div>
    </div>
  `;

  // Filter chips
  container.querySelectorAll('.chip-btn').forEach(btn => {
    btn.addEventListener('click', e => state.setCategoryFilter(e.currentTarget.dataset.category));
  });

  // Status select
  container.querySelector('#timeline-status-select')?.addEventListener('change', e => {
    state.setStatusFilter(e.target.value);
  });

  // Search (debounced)
  const searchInput = container.querySelector('#timeline-search-input');
  if (searchInput) {
    let timeout = null;
    searchInput.addEventListener('input', e => {
      clearTimeout(timeout);
      timeout = setTimeout(() => state.setSearchKeyword(e.target.value), 250);
    });
  }

  // Row & button click delegation
  container.querySelector('.goal-rows-list')?.addEventListener('click', e => {
    const actionBtn = e.target.closest('[data-action]');
    if (actionBtn) {
      e.stopPropagation();
      const goalId = Number(actionBtn.dataset.goalId);
      const goal   = state.goals.find(g => g.id === goalId);
      if (!goal) return;

      const action = actionBtn.dataset.action;
      if (action === 'detail') onSelect?.(goal);
      if (action === 'edit')   onEdit?.(goal);
      if (action === 'ai')     onAiSuggest?.(goal);
      if (action === 'kanban') {
        window.location.href = `/kanban.html?goalId=${goal.id}&goalTitle=${encodeURIComponent(goal.title)}`;
      }
      return;
    }

    const row = e.target.closest('.goal-timeline-row');
    if (row) {
      const goalId = Number(row.dataset.goalId);
      const goal   = state.goals.find(g => g.id === goalId);
      if (goal) onSelect?.(goal);
    }
  });
}
