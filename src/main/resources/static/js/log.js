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

const params     = new URLSearchParams(location.search);
const stId       = Number(params.get('stId'));
const stTitle    = params.get('stTitle')   || '서브태스크';
const goalTitle  = params.get('goalTitle') || '목표';

let selectedCorrect = '';

async function init() {
  if (!tokenStorage.getToken()) { window.location.replace('/login.html'); return; }
  try { await api.auth.getMe(); } catch { window.location.replace('/login.html'); return; }
  if (!stId) { window.location.replace('/'); return; }

  document.getElementById('goal-title-label').textContent = goalTitle;
  document.getElementById('st-title-label').textContent   = stTitle;
  document.title = `${stTitle} — 작업 기록`;
  document.getElementById('log-date').value = new Date().toISOString().split('T')[0];

  await loadLogs();
  bindEvents();
}

function bindEvents() {
  document.getElementById('back-btn').addEventListener('click', () => {
    window.history.length > 1 ? window.history.back() : window.location.replace('/');
  });

  // Image drop area
  const dropArea    = document.getElementById('image-drop-area');
  const imageInput  = document.getElementById('image-input');
  const preview     = document.getElementById('image-preview');
  const placeholder = document.getElementById('image-placeholder');
  const removeBtn   = document.getElementById('image-remove-btn');

  dropArea.addEventListener('click', e => { if (e.target !== removeBtn) imageInput.click(); });
  dropArea.addEventListener('dragover', e => { e.preventDefault(); dropArea.classList.add('drag-over'); });
  dropArea.addEventListener('dragleave', () => dropArea.classList.remove('drag-over'));
  dropArea.addEventListener('drop', e => {
    e.preventDefault();
    dropArea.classList.remove('drag-over');
    const file = e.dataTransfer.files[0];
    if (file?.type.startsWith('image/')) previewImage(file);
  });
  imageInput.addEventListener('change', () => {
    if (imageInput.files[0]) previewImage(imageInput.files[0]);
  });
  removeBtn.addEventListener('click', e => {
    e.stopPropagation();
    imageInput.value = '';
    preview.src = '';
    preview.style.display = 'none';
    placeholder.style.display = '';
    removeBtn.style.display = 'none';
  });

  // Correct buttons
  document.querySelectorAll('.correct-btn').forEach(btn => {
    btn.addEventListener('click', () => {
      selectedCorrect = btn.dataset.val;
      document.querySelectorAll('.correct-btn').forEach(b => b.classList.remove('active'));
      btn.classList.add('active');
    });
  });

  document.getElementById('submit-btn').addEventListener('click', submitLog);
}

function previewImage(file) {
  const reader = new FileReader();
  reader.onload = e => {
    const preview     = document.getElementById('image-preview');
    const placeholder = document.getElementById('image-placeholder');
    const removeBtn   = document.getElementById('image-remove-btn');
    preview.src = e.target.result;
    preview.style.display = 'block';
    placeholder.style.display = 'none';
    removeBtn.style.display = '';
  };
  reader.readAsDataURL(file);
}

async function submitLog() {
  const content   = document.getElementById('log-content').value.trim();
  const date      = document.getElementById('log-date').value;
  const imageFile = document.getElementById('image-input').files[0];

  if (!content && !imageFile) {
    showToast('내용 또는 이미지를 입력해주세요.', 'error');
    return;
  }

  const btn = document.getElementById('submit-btn');
  btn.disabled = true;
  btn.textContent = '저장 중...';

  try {
    let imageUrl = null;
    if (imageFile) {
      const uploaded = await api.upload(imageFile);
      imageUrl = uploaded.url;
    }

    const isCorrect = selectedCorrect === 'true' ? true : selectedCorrect === 'false' ? false : null;

    await api.taskLogs.create(stId, {
      content: content || null,
      imageUrl,
      isCorrect,
      logDate: date || null,
    });

    showToast('기록이 저장되었습니다!');
    resetForm();
    await loadLogs();
  } catch (err) {
    showToast(err.message || '저장에 실패했습니다.', 'error');
  } finally {
    btn.disabled = false;
    btn.textContent = '기록 저장';
  }
}

function resetForm() {
  document.getElementById('log-content').value = '';
  const imageInput = document.getElementById('image-input');
  imageInput.value = '';
  document.getElementById('image-preview').style.display = 'none';
  document.getElementById('image-placeholder').style.display = '';
  document.getElementById('image-remove-btn').style.display = 'none';
  selectedCorrect = '';
  document.querySelectorAll('.correct-btn').forEach(b => b.classList.remove('active'));
  document.querySelector('.correct-btn[data-val=""]').classList.add('active');
  document.getElementById('log-date').value = new Date().toISOString().split('T')[0];
}

async function loadLogs() {
  const listEl = document.getElementById('log-list');
  try {
    const logs = await api.taskLogs.getAll(stId);
    document.getElementById('log-count').textContent = `(${logs.length}개)`;

    if (logs.length === 0) {
      listEl.innerHTML = `
        <div class="log-empty">
          <div class="log-empty-icon">📋</div>
          <p>아직 작업 기록이 없습니다.<br>왼쪽 폼에서 첫 번째 기록을 남겨보세요!</p>
        </div>
      `;
      return;
    }

    listEl.innerHTML = logs.map(log => `
      <div class="log-entry" data-log-id="${log.id}">
        <div class="log-entry-header">
          <div class="log-entry-meta">
            <span class="log-date">${formatDate(log.logDate)}</span>
            ${log.isCorrect === true  ? '<span class="correct-badge-ok">✅ 정답</span>'  : ''}
            ${log.isCorrect === false ? '<span class="correct-badge-fail">❌ 오답</span>' : ''}
          </div>
          <button class="log-delete-btn" data-log-id="${log.id}" title="삭제">🗑️</button>
        </div>
        ${log.imageUrl ? `<img src="${log.imageUrl}" class="log-entry-image" alt="문제 이미지" data-src="${log.imageUrl}">` : ''}
        ${log.content  ? `<div class="log-entry-content">${escapeHtml(log.content)}</div>` : ''}
      </div>
    `).join('');

    // 삭제
    listEl.querySelectorAll('.log-delete-btn').forEach(btn => {
      btn.addEventListener('click', async () => {
        if (!confirm('이 기록을 삭제하시겠어요?')) return;
        try {
          await api.taskLogs.delete(Number(btn.dataset.logId));
          showToast('삭제되었습니다.');
          await loadLogs();
        } catch { showToast('삭제에 실패했습니다.', 'error'); }
      });
    });

    // 이미지 확대 (라이트박스)
    listEl.querySelectorAll('.log-entry-image').forEach(img => {
      img.addEventListener('click', () => {
        const box = document.createElement('div');
        box.className = 'log-lightbox';
        box.innerHTML = `<img src="${img.dataset.src}" alt="확대 이미지">`;
        box.addEventListener('click', () => box.remove());
        document.body.appendChild(box);
      });
    });
  } catch {
    listEl.innerHTML = `<div style="color:var(--color-coral);padding:20px;text-align:center;">기록을 불러오지 못했습니다.</div>`;
  }
}

function formatDate(dateStr) {
  if (!dateStr) return '';
  const d = new Date(dateStr);
  return `${d.getFullYear()}.${String(d.getMonth()+1).padStart(2,'0')}.${String(d.getDate()).padStart(2,'0')}`;
}

function escapeHtml(str) {
  if (!str) return '';
  return str.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;');
}

init();
