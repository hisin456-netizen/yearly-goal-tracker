import { api, tokenStorage } from './api.js';

function showToast(message, type = 'success') {
  const container = document.getElementById('toast-container');
  if (!container) return;
  const toast = document.createElement('div');
  toast.className = `toast${type === 'error' ? ' error' : ''}`;
  toast.innerHTML = `<span>${type === 'error' ? '⚠️' : '✨'}</span><span>${message}</span>`;
  container.appendChild(toast);
  setTimeout(() => {
    toast.style.transition = 'opacity 0.3s ease';
    toast.style.opacity = '0';
    setTimeout(() => toast.remove(), 300);
  }, 3500);
}

const STATUSES = ['TODO', 'IN_PROGRESS', 'COMPLETED', 'DROPPED'];
const PERIOD_LABELS = { DAILY: '매일', WEEKLY: '매주', MONTHLY: '매월', QUARTERLY: '분기' };

const params = new URLSearchParams(location.search);
const goalId = Number(params.get('goalId'));
const goalTitle = params.get('goalTitle') || '목표';

let subTasks = [];
let draggedId = null;

async function init() {
  if (!tokenStorage.getToken()) { window.location.replace('/login.html'); return; }
  try { await api.auth.getMe(); } catch { window.location.replace('/login.html'); return; }
  if (!goalId) { window.location.replace('/'); return; }

  document.getElementById('goal-title-label').textContent = `${goalTitle} — 칸반보드`;
  document.title = `${goalTitle} — 칸반보드`;

  bindEvents();
  await loadSubTasks();
}

function bindEvents() {
  document.getElementById('back-btn').addEventListener('click', () => {
    window.history.length > 1 ? window.history.back() : window.location.replace('/');
  });

  document.getElementById('open-add-btn').addEventListener('click', openAddModal);
  document.getElementById('add-cancel-btn').addEventListener('click', closeAddModal);
  document.getElementById('add-submit-btn').addEventListener('click', submitNewSubTask);
  document.getElementById('add-modal-overlay').addEventListener('click', (e) => {
    if (e.target.id === 'add-modal-overlay') closeAddModal();
  });

  STATUSES.forEach(status => {
    const column = document.querySelector(`.kb-column[data-status="${status}"]`);
    column.addEventListener('dragover', (e) => {
      e.preventDefault();
      column.classList.add('drag-over');
    });
    column.addEventListener('dragleave', () => column.classList.remove('drag-over'));
    column.addEventListener('drop', (e) => {
      e.preventDefault();
      column.classList.remove('drag-over');
      if (draggedId != null) handleDrop(draggedId, status);
    });
  });
}

async function loadSubTasks() {
  try {
    subTasks = await api.getSubTasks(goalId);
  } catch {
    showToast('하위작업을 불러오지 못했습니다.', 'error');
    subTasks = [];
  }
  renderBoard();
}

function renderBoard() {
  STATUSES.forEach(status => {
    const listEl = document.getElementById(`list-${status}`);
    const cards = subTasks.filter(t => t.status === status);
    document.getElementById(`count-${status}`).textContent = cards.length;

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
        const url = `/log.html?stId=${task.id}&stTitle=${encodeURIComponent(task.title)}&goalTitle=${encodeURIComponent(goalTitle)}`;
        window.location.href = url;
      });
    });
  });
}

async function handleDrop(id, newStatus) {
  const task = subTasks.find(t => t.id === id);
  if (!task || task.status === newStatus) return;

  const prevStatus = task.status;
  task.status = newStatus; // optimistic update
  renderBoard();

  try {
    await api.updateSubTask(id, { status: newStatus });
  } catch (err) {
    task.status = prevStatus;
    renderBoard();
    showToast(err.message || '상태 변경에 실패했습니다.', 'error');
  }
}

function openAddModal() {
  document.getElementById('new-title').value = '';
  document.getElementById('new-period').value = 'WEEKLY';
  document.getElementById('new-target').value = '1';
  document.getElementById('add-modal-overlay').classList.add('open');
}

function closeAddModal() {
  document.getElementById('add-modal-overlay').classList.remove('open');
}

async function submitNewSubTask() {
  const title = document.getElementById('new-title').value.trim();
  const periodType = document.getElementById('new-period').value;
  const targetCount = Number(document.getElementById('new-target').value);

  if (!title) {
    showToast('제목을 입력해주세요.', 'error');
    return;
  }

  const btn = document.getElementById('add-submit-btn');
  btn.disabled = true;

  try {
    await api.createSubTask(goalId, { title, periodType, targetCount });
    showToast('하위작업이 추가되었습니다!');
    closeAddModal();
    await loadSubTasks();
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

init();
