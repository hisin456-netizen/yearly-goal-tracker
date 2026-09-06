import { api, tokenStorage } from './api.js';
import { state } from './state.js';
import { renderTimeline } from './components/Timeline.js';
import { renderTodayCheckIn } from './components/TodayCheckIn.js';
import { setupModals } from './components/Modals.js';
import { renderHeatmap } from './components/Heatmap.js';
import { renderLineChart } from './components/LineChart.js';
import { renderNotes } from './components/Notes.js';

// ── Toast ─────────────────────────────────────────────
export function showToast(message, type = 'success') {
  const container = document.getElementById('toast-container');
  if (!container) return;

  const toast = document.createElement('div');
  toast.className = `toast${type === 'error' ? ' error' : ''}`;
  toast.innerHTML = `<span>${type === 'error' ? '⚠️' : '✨'}</span><span>${message}</span>`;
  container.appendChild(toast);

  setTimeout(() => {
    toast.style.transition = 'opacity 0.3s ease, transform 0.3s ease';
    toast.style.opacity = '0';
    toast.style.transform = 'translateX(20px)';
    setTimeout(() => toast.remove(), 300);
  }, 3500);
}

// ── KPI Cards ─────────────────────────────────────────
async function updateKpiCards() {
  let summary = null;
  try {
    summary = await api.dashboard.getSummary();
  } catch {
    // fallback to local state
  }

  const goals = state.goals;
  const avgProgress  = summary ? summary.annualProgressRate : (goals.length > 0 ? Math.round(goals.reduce((s, g) => s + (g.targetProgressRate || 100), 0) / goals.length) : 0);
  const activeCount  = summary ? summary.activeGoals         : goals.filter(g => g.status === 'IN_PROGRESS').length;
  const weeklyRate   = summary ? summary.weeklyCheckInRate    : 0;
  const streakDays   = summary ? summary.currentStreakDays    : 0;

  const set = (id, val) => { const el = document.getElementById(id); if (el) el.textContent = val; };

  set('kpi-progress-val',   `${avgProgress}%`);
  set('kpi-progress-text',  `${avgProgress}%`);
  set('kpi-active-goals-val', activeCount);
  set('kpi-weekly-rate-val',  `${weeklyRate}%`);
  set('kpi-streak-val',       streakDays);

  // Weekly rate label
  const weeklyLabel = document.getElementById('kpi-weekly-label');
  if (weeklyLabel) weeklyLabel.textContent = weeklyRate >= 80 ? '우수 실천율' : weeklyRate >= 50 ? '양호 실천율' : '실천율';

  // Streak label
  const streakLabel = document.getElementById('kpi-streak-label');
  if (streakLabel) streakLabel.textContent = streakDays >= 7 ? '🔥 일 연속' : '일 연속';

  // SVG progress ring
  const circle = document.getElementById('kpi-progress-circle');
  if (circle) {
    const r = 24;
    const circ = 2 * Math.PI * r;
    circle.style.strokeDasharray = `${circ} ${circ}`;
    circle.style.strokeDashoffset = circ - (avgProgress / 100) * circ;
  }
}

// ── Data Reload ───────────────────────────────────────
async function reloadData() {
  if (!state.currentUser) return;

  try {
    const basicGoals = await api.getGoals();
    const detailed   = await Promise.all(basicGoals.map(g => api.getGoalDetail(g.id).catch(() => g)));
    state.setGoals(detailed);
    await updateKpiCards();

    const heatmapEl = document.getElementById('heatmap-container');
    if (heatmapEl) await renderHeatmap(heatmapEl);

    const linechartEl = document.getElementById('linechart-container');
    if (linechartEl) await renderLineChart(linechartEl);

    const notesEl = document.getElementById('notes-container');
    if (notesEl) await renderNotes(notesEl, state.goals, showToast);
  } catch (err) {
    showToast(`데이터 로드 실패: ${err.message}`, 'error');
  }
}

// ── Section Nav ───────────────────────────────────────
function initSectionNav() {
  const tabs = document.querySelectorAll('.section-nav-tab');
  if (!tabs.length) return;

  tabs.forEach(tab => {
    tab.addEventListener('click', () => {
      tabs.forEach(t => t.classList.remove('active'));
      tab.classList.add('active');
      const target = document.getElementById(tab.dataset.target);
      if (target) target.scrollIntoView({ behavior: 'smooth', block: 'start' });
    });
  });

  // Scroll-spy: highlight tab for visible section
  const sectionIds = ['kpi-section', 'timeline-container', 'heatmap-container', 'linechart-container', 'notes-container'];
  const observer = new IntersectionObserver(entries => {
    entries.forEach(entry => {
      if (entry.isIntersecting) {
        const id = entry.target.id;
        tabs.forEach(t => t.classList.toggle('active', t.dataset.target === id));
      }
    });
  }, { rootMargin: '-30% 0px -60% 0px' });

  sectionIds.forEach(id => {
    const el = document.getElementById(id);
    if (el) observer.observe(el);
  });
}

// ── FAB ───────────────────────────────────────────────
function initFab(modalHandlers) {
  const fabMain = document.getElementById('fab-main');
  const fabMenu = document.getElementById('fab-menu');
  if (!fabMain) return;

  let isOpen = false;

  const toggle = () => {
    isOpen = !isOpen;
    fabMain.classList.toggle('open', isOpen);
    fabMenu.classList.toggle('open', isOpen);
  };

  const close = () => {
    isOpen = false;
    fabMain.classList.remove('open');
    fabMenu.classList.remove('open');
  };

  fabMain.addEventListener('click', e => { e.stopPropagation(); toggle(); });

  document.getElementById('fab-new-goal')?.addEventListener('click', () => {
    close();
    if (!state.currentUser) { window.location.href = '/login.html'; return; }
    modalHandlers.openCreateGoalModal();
  });

  document.getElementById('fab-new-note')?.addEventListener('click', () => {
    close();
    const notesEl = document.getElementById('notes-container');
    if (notesEl) {
      notesEl.scrollIntoView({ behavior: 'smooth', block: 'start' });
      setTimeout(() => notesEl.querySelector('#btn-open-note-form')?.click(), 500);
    }
  });

  document.addEventListener('click', e => {
    if (isOpen && !e.target.closest('.fab-container')) close();
  });
}

// ── Year Selector ─────────────────────────────────────
function initYearSelector() {
  const select = document.getElementById('year-select');
  if (!select) return;

  const currentYear = new Date().getFullYear();
  for (let y = currentYear - 1; y <= currentYear + 2; y++) {
    const opt = document.createElement('option');
    opt.value = y;
    opt.textContent = `${y}년`;
    if (y === currentYear) opt.selected = true;
    select.appendChild(opt);
  }
}

// ── Auth Header ───────────────────────────────────────
function updateAuthHeader(user) {
  const container = document.getElementById('auth-header-container');
  if (!container) return;

  if (user) {
    const initial = user.username ? user.username.charAt(0).toUpperCase() : 'U';
    container.innerHTML = `
      <div class="user-greeting">
        <span class="avatar-circle">${initial}</span>
        <span><strong>${user.username}</strong>님</span>
      </div>
      <button type="button" class="btn btn-secondary btn-sm" id="logout-btn">로그아웃</button>
    `;

    document.getElementById('logout-btn')?.addEventListener('click', () => {
      tokenStorage.clearToken();
      state.setCurrentUser(null);
      state.setGoals([]);
      window.location.replace('/login.html');
    });
  } else {
    container.innerHTML = `
      <a href="/login.html" class="btn btn-secondary btn-sm">로그인</a>
    `;
  }
}

// ── Guest View ────────────────────────────────────────
function renderGuestView() {
  const timelineEl = document.getElementById('timeline-container');
  const todayEl    = document.getElementById('today-checkin-container');
  const heatmapEl  = document.getElementById('heatmap-container');

  if (timelineEl) {
    timelineEl.innerHTML = `
      <div class="empty-state" style="padding: 72px 24px;">
        <span class="empty-state-icon">🔒</span>
        <h3>로그인이 필요한 서비스입니다</h3>
        <p>연간 목표를 세우고 타임라인, 일일 체크인,<br>실천 잔디로 당신의 성장을 기록하세요.</p>
        <a href="/login.html" class="btn btn-primary" style="padding: 10px 24px;">
          로그인하고 시작하기
        </a>
      </div>
    `;
  }

  if (todayEl) {
    todayEl.innerHTML = `
      <div class="empty-state" style="padding: 40px 16px;">
        <span class="empty-state-icon" style="font-size:1.8rem;">⚡</span>
        <p style="font-size:0.84rem;">로그인 후 오늘의 실천을 기록할 수 있습니다.</p>
      </div>
    `;
  }

  if (heatmapEl) {
    heatmapEl.innerHTML = `
      <div style="padding: 20px 24px; color: var(--text-muted); font-size: 0.84rem;">
        🌱 로그인 후 실천 잔디를 확인할 수 있습니다.
      </div>
    `;
  }
}

// ── Init ──────────────────────────────────────────────
async function initApp() {
  initYearSelector();

  const modalContainer  = document.getElementById('modal-container');
  const timelineEl      = document.getElementById('timeline-container');
  const todayEl         = document.getElementById('today-checkin-container');

  const modalHandlers = setupModals(modalContainer, reloadData, showToast);

  document.getElementById('add-goal-btn')?.addEventListener('click', () => {
    if (!state.currentUser) {
      window.location.href = '/login.html';
      return;
    }
    modalHandlers.openCreateGoalModal();
  });

  initSectionNav();
  initFab(modalHandlers);

  state.subscribe(() => {
    renderTimeline(timelineEl, {
      onSelect:    goal => modalHandlers.openGoalDetailModal(goal),
      onEdit:      goal => modalHandlers.openEditGoalModal(goal),
      onAiSuggest: goal => modalHandlers.openGoalDetailModal(goal, { autoAi: true }),
    });
    renderTodayCheckIn(todayEl, reloadData, showToast);
  });

  window.addEventListener('auth:unauthorized', () => {
    tokenStorage.clearToken();
    window.location.replace('/login.html');
  });

  const token = tokenStorage.getToken();
  if (token) {
    try {
      const user = await api.auth.getMe();
      state.setCurrentUser(user);
      updateAuthHeader(user);
      await reloadData();
    } catch {
      tokenStorage.clearToken();
      window.location.replace('/login.html');
    }
  } else {
    window.location.replace('/login.html');
  }
}

if (document.readyState === 'loading') {
  document.addEventListener('DOMContentLoaded', initApp);
} else {
  initApp();
}
