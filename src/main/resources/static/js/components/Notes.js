import { api } from '../api.js';

const TYPE_LABELS = {
  MEMO: { label: '메모', emoji: '📝' },
  IDEA: { label: '아이디어', emoji: '💡' },
  WORK_LOG: { label: '업무기록', emoji: '💼' },
};

let currentFilter = null;
let editingNoteId = null;
let goals = [];

export async function renderNotes(container, stateGoals, showToast) {
  goals = stateGoals || [];
  container.innerHTML = buildNotesHTML();
  await loadAndRenderNoteList(container, showToast);
  bindEvents(container, showToast);
}

function buildNotesHTML() {
  return `
    <div class="notes-header">
      <div class="notes-title-group">
        <h3>🗒️ 노트</h3>
        <p>메모, 아이디어, 업무기록을 한 곳에</p>
      </div>
      <button class="btn btn-primary btn-sm" id="btn-open-note-form">+ 새 노트</button>
    </div>

    <!-- 필터 탭 -->
    <div class="notes-filter-tabs">
      <button class="note-tab active" data-type="">전체</button>
      <button class="note-tab" data-type="MEMO">📝 메모</button>
      <button class="note-tab" data-type="IDEA">💡 아이디어</button>
      <button class="note-tab" data-type="WORK_LOG">💼 업무기록</button>
    </div>

    <!-- 빠른 작성 폼 -->
    <div class="note-quick-form" id="note-quick-form" style="display:none;">
      <div class="note-form-row">
        <div class="note-form-type-select">
          <select id="note-type-select" class="form-control">
            <option value="MEMO">📝 메모</option>
            <option value="IDEA">💡 아이디어</option>
            <option value="WORK_LOG">💼 업무기록</option>
          </select>
        </div>
        <input type="text" id="note-title-input" class="form-control" placeholder="제목 (선택)">
      </div>
      <div class="note-form-goal-row">
        <select id="note-goal-select" class="form-control">
          <option value="">목표 연결 (선택)</option>
          ${goals.map(g => `<option value="${g.id}">${g.title}</option>`).join('')}
        </select>
        <input type="date" id="note-date-input" class="form-control" placeholder="날짜 (업무기록용)">
      </div>
      <textarea id="note-content-input" class="form-control note-textarea" placeholder="내용을 입력하세요..." rows="4"></textarea>
      <div class="note-form-actions">
        <button class="btn btn-secondary btn-sm" id="btn-cancel-note">취소</button>
        <button class="btn btn-primary btn-sm" id="btn-save-note">저장</button>
      </div>
    </div>

    <!-- 노트 목록 -->
    <div class="notes-list" id="notes-list">
      <div class="notes-loading">불러오는 중...</div>
    </div>
  `;
}

async function loadAndRenderNoteList(container, showToast) {
  const listEl = container.querySelector('#notes-list');
  if (!listEl) return;

  try {
    const notes = await api.notes.getAll(currentFilter || null);
    if (notes.length === 0) {
      listEl.innerHTML = `
        <div class="empty-state" style="padding: 32px 16px;">
          <span class="empty-state-icon" style="font-size:1.8rem;">🗒️</span>
          <p style="font-size:0.84rem;">아직 노트가 없습니다.<br>새 노트를 작성해보세요.</p>
        </div>
      `;
      return;
    }

    listEl.innerHTML = notes.map(note => buildNoteCard(note)).join('');
    bindNoteCardEvents(container, showToast);
  } catch (err) {
    listEl.innerHTML = `<div style="color:var(--text-muted);padding:16px;font-size:0.84rem;">노트를 불러오지 못했습니다.</div>`;
  }
}

function buildNoteCard(note) {
  const typeInfo = TYPE_LABELS[note.type] || { label: note.type, emoji: '📄' };
  const date = note.workDate || note.createdAt?.substring(0, 10);
  const displayDate = date ? formatDate(date) : '';
  const pinIcon = note.isPinned ? '📌 ' : '';

  return `
    <div class="note-card ${note.isPinned ? 'pinned' : ''}" data-note-id="${note.id}">
      <div class="note-card-header">
        <div class="note-card-meta">
          <span class="note-type-badge note-type-${note.type.toLowerCase()}">${typeInfo.emoji} ${typeInfo.label}</span>
          ${note.goalTitle ? `<span class="note-goal-link">🎯 ${note.goalTitle}</span>` : ''}
        </div>
        <div class="note-card-actions">
          <span class="note-date">${displayDate}</span>
          <button class="note-action-btn" data-action="pin" data-id="${note.id}" data-pinned="${note.isPinned}" title="${note.isPinned ? '핀 해제' : '핀 고정'}">
            ${note.isPinned ? '📌' : '📍'}
          </button>
          <button class="note-action-btn" data-action="edit" data-id="${note.id}" title="수정">✏️</button>
          <button class="note-action-btn" data-action="delete" data-id="${note.id}" title="삭제">🗑️</button>
        </div>
      </div>
      ${note.title ? `<div class="note-card-title">${escapeHtml(note.title)}</div>` : ''}
      <div class="note-card-content">${escapeHtml(note.content)}</div>
    </div>
  `;
}

function bindEvents(container, showToast) {
  // 필터 탭
  container.querySelectorAll('.note-tab').forEach(tab => {
    tab.addEventListener('click', async () => {
      container.querySelectorAll('.note-tab').forEach(t => t.classList.remove('active'));
      tab.classList.add('active');
      currentFilter = tab.dataset.type || null;
      await loadAndRenderNoteList(container, showToast);
    });
  });

  // 새 노트 버튼
  container.querySelector('#btn-open-note-form')?.addEventListener('click', () => {
    const form = container.querySelector('#note-quick-form');
    form.style.display = form.style.display === 'none' ? 'block' : 'none';
    editingNoteId = null;
    resetForm(container);
  });

  // 취소
  container.querySelector('#btn-cancel-note')?.addEventListener('click', () => {
    container.querySelector('#note-quick-form').style.display = 'none';
    editingNoteId = null;
    resetForm(container);
  });

  // 저장
  container.querySelector('#btn-save-note')?.addEventListener('click', async () => {
    const type = container.querySelector('#note-type-select').value;
    const title = container.querySelector('#note-title-input').value.trim();
    const content = container.querySelector('#note-content-input').value.trim();
    const goalId = container.querySelector('#note-goal-select').value || null;
    const workDate = container.querySelector('#note-date-input').value || null;

    if (!content) { showToast('내용을 입력해주세요.', 'error'); return; }

    try {
      if (editingNoteId) {
        await api.notes.update(editingNoteId, { title, content, workDate });
        showToast('노트가 수정되었습니다.');
      } else {
        await api.notes.create({ type, title, content, goalId: goalId ? Number(goalId) : null, workDate });
        showToast('노트가 저장되었습니다.');
      }
      container.querySelector('#note-quick-form').style.display = 'none';
      editingNoteId = null;
      resetForm(container);
      await loadAndRenderNoteList(container, showToast);
    } catch (err) {
      showToast(err.message || '저장에 실패했습니다.', 'error');
    }
  });
}

function bindNoteCardEvents(container, showToast) {
  container.querySelectorAll('.note-action-btn').forEach(btn => {
    btn.addEventListener('click', async (e) => {
      e.stopPropagation();
      const action = btn.dataset.action;
      const id = Number(btn.dataset.id);

      if (action === 'delete') {
        if (!confirm('이 노트를 삭제하시겠어요?')) return;
        try {
          await api.notes.delete(id);
          showToast('노트가 삭제되었습니다.');
          await loadAndRenderNoteList(container, showToast);
        } catch (err) {
          showToast('삭제에 실패했습니다.', 'error');
        }
      }

      if (action === 'pin') {
        const isPinned = btn.dataset.pinned === 'true';
        try {
          await api.notes.update(id, { isPinned: !isPinned });
          await loadAndRenderNoteList(container, showToast);
        } catch (err) {
          showToast('수정에 실패했습니다.', 'error');
        }
      }

      if (action === 'edit') {
        const card = btn.closest('.note-card');
        const noteId = Number(card.dataset.noteId);
        const notes = await api.notes.getAll(currentFilter || null);
        const note = notes.find(n => n.id === noteId);
        if (!note) return;

        editingNoteId = noteId;
        const form = container.querySelector('#note-quick-form');
        form.style.display = 'block';
        container.querySelector('#note-type-select').value = note.type;
        container.querySelector('#note-title-input').value = note.title || '';
        container.querySelector('#note-content-input').value = note.content || '';
        container.querySelector('#note-goal-select').value = note.goalId || '';
        container.querySelector('#note-date-input').value = note.workDate || '';
        container.querySelector('#btn-save-note').textContent = '수정 완료';
        form.scrollIntoView({ behavior: 'smooth', block: 'nearest' });
      }
    });
  });
}

function resetForm(container) {
  container.querySelector('#note-type-select').value = 'MEMO';
  container.querySelector('#note-title-input').value = '';
  container.querySelector('#note-content-input').value = '';
  container.querySelector('#note-goal-select').value = '';
  container.querySelector('#note-date-input').value = '';
  container.querySelector('#btn-save-note').textContent = '저장';
}

function formatDate(dateStr) {
  const d = new Date(dateStr);
  return `${d.getMonth() + 1}/${d.getDate()}`;
}

function escapeHtml(str) {
  if (!str) return '';
  return str.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;').replace(/\n/g, '<br>');
}
