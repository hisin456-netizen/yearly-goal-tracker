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

const WEEKDAY_COUNT = 7;

let currentUser = null;
let viewYear;
let viewMonth; // 1-12
let schedules = [];
let selectedDate; // 'YYYY-MM-DD'
let editingId = null; // schedule id currently being edited, or null when adding new

function toDateStr(date) {
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`;
}

async function init() {
  if (!tokenStorage.getToken()) { window.location.replace('/login.html'); return; }
  try { currentUser = await api.auth.getMe(); } catch { window.location.replace('/login.html'); return; }

  const today = new Date();
  viewYear = today.getFullYear();
  viewMonth = today.getMonth() + 1;
  selectedDate = toDateStr(today);

  bindEvents();
  await loadMonth();
}

function bindEvents() {
  document.getElementById('back-btn').addEventListener('click', () => {
    window.history.length > 1 ? window.history.back() : window.location.replace('/');
  });

  document.getElementById('prev-month-btn').addEventListener('click', () => shiftMonth(-1));
  document.getElementById('next-month-btn').addEventListener('click', () => shiftMonth(1));

  document.getElementById('sch-submit-btn').addEventListener('click', submitSchedule);

  document.getElementById('open-settings-btn').addEventListener('click', openSettingsModal);
  document.getElementById('settings-cancel-btn').addEventListener('click', closeSettingsModal);
  document.getElementById('settings-save-btn').addEventListener('click', saveSettings);
  document.getElementById('settings-modal-overlay').addEventListener('click', (e) => {
    if (e.target.id === 'settings-modal-overlay') closeSettingsModal();
  });
}

async function shiftMonth(delta) {
  viewMonth += delta;
  if (viewMonth > 12) { viewMonth = 1; viewYear += 1; }
  if (viewMonth < 1) { viewMonth = 12; viewYear -= 1; }
  await loadMonth();
}

async function loadMonth() {
  document.getElementById('month-label').textContent = `${viewYear}년 ${viewMonth}월`;
  try {
    schedules = await api.schedules.getByMonth(viewYear, viewMonth);
  } catch (err) {
    showToast('일정을 불러오지 못했습니다.', 'error');
    schedules = [];
  }
  renderGrid();
  renderDayPanel();
}

function renderGrid() {
  const gridEl = document.getElementById('cal-grid');
  gridEl.innerHTML = '';

  const firstOfMonth = new Date(viewYear, viewMonth - 1, 1);
  const startOffset = firstOfMonth.getDay(); // 0=Sun
  const gridStart = new Date(viewYear, viewMonth - 1, 1 - startOffset);

  const todayStr = toDateStr(new Date());
  const schedulesByDate = {};
  schedules.forEach(s => {
    (schedulesByDate[s.scheduleDate] ||= []).push(s);
  });

  for (let i = 0; i < 42; i++) {
    const cellDate = new Date(gridStart);
    cellDate.setDate(gridStart.getDate() + i);
    const dateStr = toDateStr(cellDate);
    const isOtherMonth = cellDate.getMonth() + 1 !== viewMonth;
    const dayEvents = schedulesByDate[dateStr] || [];

    const cell = document.createElement('div');
    cell.className = 'cal-day-cell';
    if (isOtherMonth) cell.classList.add('other-month');
    if (dateStr === todayStr) cell.classList.add('today');
    if (dateStr === selectedDate) cell.classList.add('selected');
    cell.dataset.date = dateStr;

    cell.innerHTML = `
      <span class="cal-day-num">${cellDate.getDate()}</span>
      <div class="cal-day-events">
        ${dayEvents.slice(0, 3).map(ev => `
          <span class="cal-event-chip${ev.notified ? ' notified' : ''}">${escapeHtml(ev.title)}</span>
        `).join('')}
        ${dayEvents.length > 3 ? `<span class="cal-event-chip">+${dayEvents.length - 3}개</span>` : ''}
      </div>
    `;

    cell.addEventListener('click', () => {
      selectedDate = dateStr;
      if (isOtherMonth) {
        viewYear = cellDate.getFullYear();
        viewMonth = cellDate.getMonth() + 1;
        loadMonth();
        return;
      }
      renderGrid();
      renderDayPanel();
    });

    gridEl.appendChild(cell);
  }
}

function renderDayPanel() {
  const label = document.getElementById('selected-date-label');
  const [y, m, d] = selectedDate.split('-');
  label.textContent = `${y}년 ${Number(m)}월 ${Number(d)}일`;

  const listEl = document.getElementById('day-schedule-list');
  const dayEvents = schedules
    .filter(s => s.scheduleDate === selectedDate)
    .sort((a, b) => a.startTime.localeCompare(b.startTime));

  if (dayEvents.length === 0) {
    listEl.innerHTML = `<div class="cal-day-list-empty">등록된 일정이 없습니다.</div>`;
  } else {
    listEl.innerHTML = dayEvents.map(ev => `
      <div class="cal-schedule-item${ev.id === editingId ? ' editing' : ''}" data-id="${ev.id}">
        <div class="cal-schedule-item-main" data-id="${ev.id}">
          <div class="cal-schedule-time">${ev.startTime}</div>
          <div class="cal-schedule-title">${escapeHtml(ev.title)}</div>
        </div>
        <button class="cal-schedule-delete-btn" data-id="${ev.id}" title="삭제">🗑️</button>
      </div>
    `).join('');

    listEl.querySelectorAll('.cal-schedule-item-main').forEach(el => {
      el.addEventListener('click', () => startEditing(Number(el.dataset.id)));
    });

    listEl.querySelectorAll('.cal-schedule-delete-btn').forEach(btn => {
      btn.addEventListener('click', async (e) => {
        e.stopPropagation();
        if (!confirm('이 일정을 삭제하시겠어요?')) return;
        try {
          await api.schedules.delete(Number(btn.dataset.id));
          showToast('삭제되었습니다.');
          if (editingId === Number(btn.dataset.id)) resetForm();
          await loadMonth();
        } catch { showToast('삭제에 실패했습니다.', 'error'); }
      });
    });
  }

  resetForm();
}

function startEditing(id) {
  if (id === editingId) { resetForm(); renderDayList(); return; }
  const ev = schedules.find(s => s.id === id);
  if (!ev) return;
  editingId = id;
  document.getElementById('sch-title').value = ev.title;
  document.getElementById('sch-time').value = ev.startTime.slice(0, 5);
  document.getElementById('sch-reminder').value = ev.reminderMinutesBefore ?? '';
  document.getElementById('sch-memo').value = ev.memo || '';
  document.getElementById('sch-submit-btn').textContent = '일정 수정';
  renderDayList();
}

function resetForm() {
  editingId = null;
  document.getElementById('sch-title').value = '';
  document.getElementById('sch-time').value = '';
  document.getElementById('sch-reminder').value = '';
  document.getElementById('sch-memo').value = '';
  document.getElementById('sch-submit-btn').textContent = '일정 추가';
}

function renderDayList() {
  document.querySelectorAll('.cal-schedule-item').forEach(item => {
    item.classList.toggle('editing', Number(item.dataset.id) === editingId);
  });
}

async function submitSchedule() {
  const title = document.getElementById('sch-title').value.trim();
  const startTime = document.getElementById('sch-time').value;
  const reminderRaw = document.getElementById('sch-reminder').value;
  const memo = document.getElementById('sch-memo').value.trim();

  if (!title || !startTime) {
    showToast('제목과 시간을 입력해주세요.', 'error');
    return;
  }

  const btn = document.getElementById('sch-submit-btn');
  btn.disabled = true;
  btn.textContent = '저장 중...';

  const payload = {
    title,
    memo: memo || null,
    scheduleDate: selectedDate,
    startTime,
    reminderMinutesBefore: reminderRaw !== '' ? Number(reminderRaw) : null,
  };

  try {
    if (editingId) {
      await api.schedules.update(editingId, payload);
      showToast('일정이 수정되었습니다!');
    } else {
      await api.schedules.create(payload);
      showToast('일정이 등록되었습니다!');
    }
    editingId = null;
    await loadMonth();
  } catch (err) {
    showToast(err.message || '저장에 실패했습니다.', 'error');
  } finally {
    btn.disabled = false;
    btn.textContent = '일정 추가';
  }
}

function openSettingsModal() {
  document.getElementById('settings-webhook-url').value = currentUser?.discordWebhookUrl || '';
  document.getElementById('settings-default-reminder').value = currentUser?.defaultReminderMinutes ?? 30;
  document.getElementById('settings-modal-overlay').classList.add('open');
  loadLoginHistory();
}

async function loadLoginHistory() {
  const listEl = document.getElementById('login-history-list');
  listEl.innerHTML = `<div class="cal-day-list-empty">불러오는 중...</div>`;
  try {
    const history = await api.getLoginHistory();
    if (history.length === 0) {
      listEl.innerHTML = `<div class="cal-day-list-empty">로그인 기록이 없습니다.</div>`;
      return;
    }
    listEl.innerHTML = history.map(h => `
      <div class="cal-login-item">
        <span class="cal-login-badge ${h.success ? 'ok' : 'fail'}">${h.success ? '성공' : '실패'}</span>
        <div class="cal-login-item-main">
          <div class="cal-login-time">${formatDateTime(h.loginAt)}</div>
          <div class="cal-login-meta">${h.ipAddress || '-'} · ${escapeHtml(truncate(h.userAgent, 40))}</div>
        </div>
      </div>
    `).join('');
  } catch {
    listEl.innerHTML = `<div class="cal-day-list-empty">기록을 불러오지 못했습니다.</div>`;
  }
}

function formatDateTime(dateTimeStr) {
  if (!dateTimeStr) return '';
  const d = new Date(dateTimeStr);
  return `${d.getFullYear()}.${String(d.getMonth()+1).padStart(2,'0')}.${String(d.getDate()).padStart(2,'0')} ${String(d.getHours()).padStart(2,'0')}:${String(d.getMinutes()).padStart(2,'0')}`;
}

function truncate(str, len) {
  if (!str) return '-';
  return str.length > len ? str.slice(0, len) + '…' : str;
}

function closeSettingsModal() {
  document.getElementById('settings-modal-overlay').classList.remove('open');
}

async function saveSettings() {
  const webhookUrl = document.getElementById('settings-webhook-url').value.trim();
  const defaultReminder = document.getElementById('settings-default-reminder').value;

  const btn = document.getElementById('settings-save-btn');
  btn.disabled = true;

  try {
    currentUser = await api.updateNotificationSettings({
      discordWebhookUrl: webhookUrl || null,
      defaultReminderMinutes: defaultReminder !== '' ? Number(defaultReminder) : null,
    });
    showToast('알림 설정이 저장되었습니다!');
    closeSettingsModal();
  } catch (err) {
    showToast(err.message || '저장에 실패했습니다.', 'error');
  } finally {
    btn.disabled = false;
  }
}

function escapeHtml(str) {
  if (!str) return '';
  return str.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;');
}

init();
