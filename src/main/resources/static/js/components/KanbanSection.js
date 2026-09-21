import { api } from '../api.js';

const STATUSES = ['TODO', 'IN_PROGRESS', 'COMPLETED', 'DROPPED'];
const STATUS_LABELS = { TODO: '할일', IN_PROGRESS: '진행중', COMPLETED: '완료', DROPPED: '중단' };
const STATUS_DOTS = { TODO: 'dot-todo', IN_PROGRESS: 'dot-in-progress', COMPLETED: 'dot-completed', DROPPED: 'dot-dropped' };
const PERIOD_LABELS = { DAILY: '매일', WEEKLY: '매주', MONTHLY: '매월', QUARTERLY: '분기' };

let subTasks = [];
let draggedId = null;
let currentGoalId = null;
let currentGoalTitle = '';

/**
 * goalId가 없으면 "목표를 선택해주세요" 빈 상태를 보여준다.
 * 목표 카드의 "칸반보드" 액션이 goalId/goalTitle을 넘겨 다시 호출한다.
 */
export async function renderKanbanSection(container, goalId, goalTitle, showToast) {
  currentGoalId = goalId || null;
  currentGoalTitle = goalTitle || '';

  if (!currentGoalId) {
    container.innerHTML = `
      <div class="section-header">
        <div class="section-title-group"><h2><span>🗂️</span> 세부 목표</h2></div>
      </div>
      <div class="empty-state" style="padding: 48px 20px;">
        <span class="empty-state-icon">🗂️</span>
        <p>위 <strong>목표</strong> 카드에서 "칸반보드"를 클릭하면<br>그 목표의 하위작업이 여기 보드로 펼쳐집니다.</p>
      </div>
    `;
    return;
  }

  container.innerHTML = `
    <div class="section-header">
      <div class="section-title-group">
        <h2><span>🗂️</span> 세부 목표 <span style="color:var(--accent-primary); font-weight:600; font-size:0.82rem; margin-left:4px;">— ${escapeHtml(goalTitle || '')}</span></h2>
      </div>
      <button type="button" class="btn btn-primary btn-sm" id="kb-open-add-btn">+ 새 하위작업</button>
    </div>

    <div class="kb-board">
      ${STATUSES.map(s => `
        <section class="kb-column" data-status="${s}">
          <div class="kb-column-header">
            <span class="kb-column-dot ${STATUS_DOTS[s]}"></span>
            <h3>${STATUS_LABELS[s]}</h3>
            <span class="kb-column-count" id="kb-count-${s}">0</span>
          </div>
          <div class="kb-card-list" id="kb-list-${s}"></div>
        </section>
      `).join('')}
    </div>

    <div class="kb-modal-overlay" id="kb-add-modal-overlay">
      <div class="kb-modal glass-card">
        <h3>새 하위작업</h3>
        <div class="form-group">
          <label>제목</label>
          <input type="text" id="kb-new-title" class="kb-input" placeholder="예: 매일 30분 문제풀이">
        </div>
        <div class="kb-form-row">
          <div class="form-group">
            <label>주기</label>
            <select id="kb-new-period" class="kb-input">
              <option value="DAILY">매일</option>
              <option value="WEEKLY" selected>매주</option>
              <option value="MONTHLY">매월</option>
              <option value="QUARTERLY">분기</option>
            </select>
          </div>
          <div class="form-group">
            <label>목표 횟수</label>
            <input type="number" id="kb-new-target" class="kb-input" min="1" value="1">
          </div>
        </div>
        <div class="kb-modal-actions">
          <button type="button" class="btn btn-secondary" id="kb-add-cancel-btn">취소</button>
          <button type="button" class="btn btn-primary" id="kb-add-submit-btn">추가</button>
        </div>
      </div>
    </div>
  `;

  bindEvents(container, goalTitle, showToast);
  await loadSubTasks(container, showToast);
}

function bindEvents(container, goalTitle, showToast) {
  container.querySelector('#kb-open-add-btn').addEventListener('click', () => openAddModal(container));
  container.querySelector('#kb-add-cancel-btn').addEventListener('click', () => closeAddModal(container));
  container.querySelector('#kb-add-submit-btn').addEventListener('click', () => submitNewSubTask(container, showToast));
  container.querySelector('#kb-add-modal-overlay').addEventListener('click', (e) => {
    if (e.target.id === 'kb-add-modal-overlay') closeAddModal(container);
  });

  STATUSES.forEach(status => {
    const column = container.querySelector(`.kb-column[data-status="${status}"]`);
    column.addEventListener('dragover', (e) => {
      e.preventDefault();
      column.classList.add('drag-over');
    });
    column.addEventListener('dragleave', () => column.classList.remove('drag-over'));
    column.addEventListener('drop', (e) => {
      e.preventDefault();
      column.classList.remove('drag-over');
      if (draggedId != null) handleDrop(container, draggedId, status, showToast);
    });
  });
}

async function loadSubTasks(container, showToast) {
  try {
    subTasks = await api.getSubTasks(currentGoalId);
  } catch {
    showToast('하위작업을 불러오지 못했습니다.', 'error');
    subTasks = [];
  }
  renderBoard(container, showToast);
}

function renderBoard(container, showToast) {
  STATUSES.forEach(status => {
    const listEl = container.querySelector(`#kb-list-${status}`);
    const cards = subTasks.filter(t => t.status === status);
    container.querySelector(`#kb-count-${status}`).textContent = cards.length;

    if (cards.length === 0) {
      listEl.innerHTML = `<div class="kb-empty">카드가 없습니다</div>`;
      return;
    }

    listEl.innerHTML = cards.map(t => `
      <div class="kb-card" draggable="true" data-id="${t.id}">
        <div class="kb-card-title">${escapeHtml(t.title)}</div>
        <span class="kb-card-badge">${PERIOD_LABELS[t.periodType] || t.periodType} · ${t.targetCount}회</span>
      </div>
    `).join('');

    listEl.querySelectorAll('.kb-card').forEach(card => {
      card.addEventListener('dragstart', () => {
        draggedId = Number(card.dataset.id);
        card.classList.add('dragging');
      });
      card.addEventListener('dragend', () => {
        card.classList.remove('dragging');
        draggedId = null;
      });
      card.addEventListener('click', () => {
        const task = subTasks.find(t => t.id === Number(card.dataset.id));
        if (!task) return;
        const url = `/log.html?stId=${task.id}&stTitle=${encodeURIComponent(task.title)}&goalTitle=${encodeURIComponent(currentGoalTitle || '목표')}`;
        window.location.href = url;
      });
    });
  });
}

async function handleDrop(container, id, newStatus, showToast) {
  const task = subTasks.find(t => t.id === id);
  if (!task || task.status === newStatus) return;

  const prevStatus = task.status;
  task.status = newStatus; // optimistic update
  renderBoard(container, showToast);

  try {
    await api.updateSubTask(id, { status: newStatus });
  } catch (err) {
    task.status = prevStatus;
    renderBoard(container, showToast);
    showToast(err.message || '상태 변경에 실패했습니다.', 'error');
  }
}

function openAddModal(container) {
  container.querySelector('#kb-new-title').value = '';
  container.querySelector('#kb-new-period').value = 'WEEKLY';
  container.querySelector('#kb-new-target').value = '1';
  container.querySelector('#kb-add-modal-overlay').classList.add('open');
}

function closeAddModal(container) {
  container.querySelector('#kb-add-modal-overlay').classList.remove('open');
}

async function submitNewSubTask(container, showToast) {
  const title = container.querySelector('#kb-new-title').value.trim();
  const periodType = container.querySelector('#kb-new-period').value;
  const targetCount = Number(container.querySelector('#kb-new-target').value);

  if (!title) {
    showToast('제목을 입력해주세요.', 'error');
    return;
  }

  const btn = container.querySelector('#kb-add-submit-btn');
  btn.disabled = true;

  try {
    await api.createSubTask(currentGoalId, { title, periodType, targetCount });
    showToast('하위작업이 추가되었습니다!');
    closeAddModal(container);
    await loadSubTasks(container, showToast);
  } catch (err) {
    showToast(err.message || '추가에 실패했습니다.', 'error');
  } finally {
    btn.disabled = false;
  }
}

function escapeHtml(str) {
  if (!str) return '';
  return str.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;');
}
