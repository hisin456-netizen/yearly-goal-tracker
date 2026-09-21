import { api } from '../api.js';

let currentUser = null;
let viewYear;
let viewMonth; // 1-12
let schedules = [];
let selectedDate; // 'YYYY-MM-DD'
let editingId = null; // schedule id currently being edited, or null when adding new

function toDateStr(date) {
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`;
}

export async function renderCalendarSection(container, user, showToast) {
  currentUser = user;

  const today = new Date();
  viewYear = today.getFullYear();
  viewMonth = today.getMonth() + 1;
  selectedDate = toDateStr(today);

  container.innerHTML = `
    <div class="section-header">
      <div class="section-title-group"><h2><span>📆</span> 캘린더 · 일정</h2></div>
      <div style="display:flex; align-items:center; gap:14px;">
        <div style="display:flex; align-items:center; gap:8px;">
          <button class="btn-icon btn-secondary" id="cal-prev-month-btn" aria-label="이전 달">‹</button>
          <span id="cal-month-label" style="font-size:0.9rem; font-weight:700; min-width:96px; text-align:center;"></span>
          <button class="btn-icon btn-secondary" id="cal-next-month-btn" aria-label="다음 달">›</button>
        </div>
        <button class="btn btn-secondary btn-sm" id="cal-open-settings-btn">🔔 알림 설정</button>
      </div>
    </div>

    <div class="cal-layout">
      <section class="cal-grid-panel glass-card">
        <div class="cal-weekdays">
          <span>일</span><span>월</span><span>화</span><span>수</span><span>목</span><span>금</span><span>토</span>
        </div>
        <div class="cal-grid" id="cal-grid"></div>
      </section>

      <aside class="cal-day-panel glass-card">
        <h3 id="cal-selected-date-label">오늘</h3>
        <div id="cal-day-schedule-list" class="cal-day-list"></div>
        <div class="cal-add-form">
          <div class="form-group">
            <label>제목</label>
            <input type="text" id="cal-sch-title" class="cal-input" placeholder="예: 헬스장, 스터디 모임">
          </div>
          <div class="cal-form-row">
            <div class="form-group">
              <label>시간</label>
              <input type="time" id="cal-sch-time" class="cal-input">
            </div>
            <div class="form-group">
              <label>리마인드(분 전)</label>
              <input type="number" id="cal-sch-reminder" class="cal-input" min="0" placeholder="기본값">
            </div>
          </div>
          <div class="form-group">
            <label>메모</label>
            <textarea id="cal-sch-memo" class="cal-input cal-textarea" placeholder="선택 입력"></textarea>
          </div>
          <button type="button" class="btn btn-primary" id="cal-sch-submit-btn" style="width:100%;">일정 추가</button>
        </div>
      </aside>
    </div>

    <div class="cal-modal-overlay" id="cal-settings-modal-overlay">
      <div class="cal-modal glass-card">
        <h3>🔔 Discord 알림 설정</h3>
        <p class="cal-modal-hint">디스코드 채널의 Incoming Webhook URL을 등록하면, 일정 시작 전에 알림을 받을 수 있어요.</p>
        <div class="form-group">
          <label>Discord Webhook URL</label>
          <input type="text" id="cal-settings-webhook-url" class="cal-input" placeholder="https://discord.com/api/webhooks/...">
        </div>
        <div class="form-group">
          <label>기본 리마인드 시간 (분 전)</label>
          <input type="number" id="cal-settings-default-reminder" class="cal-input" min="0">
        </div>
        <div class="cal-modal-actions">
          <button type="button" class="btn btn-secondary" id="cal-settings-cancel-btn">취소</button>
          <button type="button" class="btn btn-primary" id="cal-settings-save-btn">저장</button>
        </div>

        <div class="cal-login-history-section">
          <h4>🔐 최근 로그인 기록</h4>
          <div id="cal-login-history-list" class="cal-login-history-list"></div>
        </div>
      </div>
    </div>
  `;

  bindEvents(container, showToast);
  await loadMonth(container, showToast);
}

function bindEvents(container, showToast) {
  container.querySelector('#cal-prev-month-btn').addEventListener('click', () => shiftMonth(container, -1, showToast));
  container.querySelector('#cal-next-month-btn').addEventListener('click', () => shiftMonth(container, 1, showToast));

  container.querySelector('#cal-sch-submit-btn').addEventListener('click', () => submitSchedule(container, showToast));

  container.querySelector('#cal-open-settings-btn').addEventListener('click', () => openSettingsModal(container));
  container.querySelector('#cal-settings-cancel-btn').addEventListener('click', () => closeSettingsModal(container));
  container.querySelector('#cal-settings-save-btn').addEventListener('click', () => saveSettings(container, showToast));
  container.querySelector('#cal-settings-modal-overlay').addEventListener('click', (e) => {
    if (e.target.id === 'cal-settings-modal-overlay') closeSettingsModal(container);
  });
}

async function shiftMonth(container, delta, showToast) {
  viewMonth += delta;
  if (viewMonth > 12) { viewMonth = 1; viewYear += 1; }
  if (viewMonth < 1) { viewMonth = 12; viewYear -= 1; }
  await loadMonth(container, showToast);
}

async function loadMonth(container, showToast) {
  container.querySelector('#cal-month-label').textContent = `${viewYear}년 ${viewMonth}월`;
  try {
    schedules = await api.schedules.getByMonth(viewYear, viewMonth);
  } catch (err) {
    showToast('일정을 불러오지 못했습니다.', 'error');
    schedules = [];
  }
  renderGrid(container, showToast);
  renderDayPanel(container, showToast);
}

function renderGrid(container, showToast) {
  const gridEl = container.querySelector('#cal-grid');
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
        loadMonth(container, showToast);
        return;
      }
      renderGrid(container, showToast);
      renderDayPanel(container, showToast);
    });

    gridEl.appendChild(cell);
  }
}

function renderDayPanel(container, showToast) {
  const label = container.querySelector('#cal-selected-date-label');
  const [y, m, d] = selectedDate.split('-');
  label.textContent = `${y}년 ${Number(m)}월 ${Number(d)}일`;

  const listEl = container.querySelector('#cal-day-schedule-list');
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
      el.addEventListener('click', () => startEditing(container, Number(el.dataset.id)));
    });

    listEl.querySelectorAll('.cal-schedule-delete-btn').forEach(btn => {
      btn.addEventListener('click', async (e) => {
        e.stopPropagation();
        if (!confirm('이 일정을 삭제하시겠어요?')) return;
        try {
          await api.schedules.delete(Number(btn.dataset.id));
          showToast('삭제되었습니다.');
          if (editingId === Number(btn.dataset.id)) resetForm(container);
          await loadMonth(container, showToast);
        } catch { showToast('삭제에 실패했습니다.', 'error'); }
      });
    });
  }

  resetForm(container);
}

function startEditing(container, id) {
  if (id === editingId) { resetForm(container); renderDayList(container); return; }
  const ev = schedules.find(s => s.id === id);
  if (!ev) return;
  editingId = id;
  container.querySelector('#cal-sch-title').value = ev.title;
  container.querySelector('#cal-sch-time').value = ev.startTime.slice(0, 5);
  container.querySelector('#cal-sch-reminder').value = ev.reminderMinutesBefore ?? '';
  container.querySelector('#cal-sch-memo').value = ev.memo || '';
  container.querySelector('#cal-sch-submit-btn').textContent = '일정 수정';
  renderDayList(container);
}

function resetForm(container) {
  editingId = null;
  container.querySelector('#cal-sch-title').value = '';
  container.querySelector('#cal-sch-time').value = '';
  container.querySelector('#cal-sch-reminder').value = '';
  container.querySelector('#cal-sch-memo').value = '';
  container.querySelector('#cal-sch-submit-btn').textContent = '일정 추가';
}

function renderDayList(container) {
  container.querySelectorAll('.cal-schedule-item').forEach(item => {
    item.classList.toggle('editing', Number(item.dataset.id) === editingId);
  });
}

async function submitSchedule(container, showToast) {
  const title = container.querySelector('#cal-sch-title').value.trim();
  const startTime = container.querySelector('#cal-sch-time').value;
  const reminderRaw = container.querySelector('#cal-sch-reminder').value;
  const memo = container.querySelector('#cal-sch-memo').value.trim();

  if (!title || !startTime) {
    showToast('제목과 시간을 입력해주세요.', 'error');
    return;
  }

  const btn = container.querySelector('#cal-sch-submit-btn');
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
    await loadMonth(container, showToast);
  } catch (err) {
    showToast(err.message || '저장에 실패했습니다.', 'error');
  } finally {
    btn.disabled = false;
    btn.textContent = '일정 추가';
  }
}

function openSettingsModal(container) {
  container.querySelector('#cal-settings-webhook-url').value = currentUser?.discordWebhookUrl || '';
  container.querySelector('#cal-settings-default-reminder').value = currentUser?.defaultReminderMinutes ?? 30;
  container.querySelector('#cal-settings-modal-overlay').classList.add('open');
  loadLoginHistory(container);
}

async function loadLoginHistory(container) {
  const listEl = container.querySelector('#cal-login-history-list');
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

function closeSettingsModal(container) {
  container.querySelector('#cal-settings-modal-overlay').classList.remove('open');
}

async function saveSettings(container, showToast) {
  const webhookUrl = container.querySelector('#cal-settings-webhook-url').value.trim();
  const defaultReminder = container.querySelector('#cal-settings-default-reminder').value;

  const btn = container.querySelector('#cal-settings-save-btn');
  btn.disabled = true;

  try {
    currentUser = await api.updateNotificationSettings({
      discordWebhookUrl: webhookUrl || null,
      defaultReminderMinutes: defaultReminder !== '' ? Number(defaultReminder) : null,
    });
    showToast('알림 설정이 저장되었습니다!');
    closeSettingsModal(container);
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
