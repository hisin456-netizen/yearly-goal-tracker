/**
 * AuthModal.js - Login & Signup Modal Component
 */
import { api, tokenStorage } from '../api.js';

export function setupAuthModal(container, onAuthSuccess, showToast) {
  let isLoginMode = true;

  function renderModal() {
    container.innerHTML = `
      <div class="modal-backdrop" id="auth-modal-backdrop">
        <div class="modal-dialog auth-modal" role="dialog" aria-modal="true">
          <div class="modal-header">
            <div class="auth-header-brand">
              <span class="auth-logo">🚀</span>
              <h3>Yearly Goal Tracker</h3>
            </div>
            <button class="modal-close-btn" id="auth-close-btn" aria-label="닫기">×</button>
          </div>

          <div class="modal-body">
            <!-- Tabs -->
            <div class="auth-tabs">
              <button type="button" class="auth-tab ${isLoginMode ? 'active' : ''}" id="tab-login">로그인</button>
              <button type="button" class="auth-tab ${!isLoginMode ? 'active' : ''}" id="tab-signup">회원가입</button>
            </div>

            <!-- Demo Fast-Pass Alert -->
            <div class="auth-demo-banner" id="demo-account-box">
              <div class="demo-info">
                <strong>💡 데모 체험 계정</strong>
                <span>huesuk4720@naver.com</span>
              </div>
              <button type="button" class="btn btn-secondary btn-sm" id="demo-login-btn">원클릭 로그인</button>
            </div>

            <!-- Form -->
            <form id="auth-form" class="auth-form">
              ${!isLoginMode ? `
                <div class="form-group">
                  <label for="auth-username">이름 / 닉네임</label>
                  <input type="text" id="auth-username" class="form-control" placeholder="예: 정회석" required autocomplete="name">
                </div>
              ` : ''}

              <div class="form-group">
                <label for="auth-email">이메일</label>
                <input type="email" id="auth-email" class="form-control" placeholder="name@example.com" required autocomplete="email">
              </div>

              <div class="form-group">
                <label for="auth-password">비밀번호</label>
                <input type="password" id="auth-password" class="form-control" placeholder="6자리 이상 비밀번호" required autocomplete="current-password">
              </div>

              <button type="submit" class="btn btn-primary btn-block" id="auth-submit-btn" style="margin-top: 1.5rem; width: 100%;">
                ${isLoginMode ? '로그인 시작하기 🚀' : '계정 생성하고 시작하기 ✨'}
              </button>
            </form>
          </div>
        </div>
      </div>
    `;

    bindEvents();
  }

  function bindEvents() {
    const backdrop = document.getElementById('auth-modal-backdrop');
    const closeBtn = document.getElementById('auth-close-btn');
    const tabLogin = document.getElementById('tab-login');
    const tabSignup = document.getElementById('tab-signup');
    const demoBtn = document.getElementById('demo-login-btn');
    const form = document.getElementById('auth-form');

    closeBtn?.addEventListener('click', closeModal);
    backdrop?.addEventListener('click', (e) => {
      if (e.target === backdrop) closeModal();
    });

    tabLogin?.addEventListener('click', () => {
      if (!isLoginMode) {
        isLoginMode = true;
        renderModal();
      }
    });

    tabSignup?.addEventListener('click', () => {
      if (isLoginMode) {
        isLoginMode = false;
        renderModal();
      }
    });

    demoBtn?.addEventListener('click', async () => {
      try {
        const res = await api.auth.login({
          email: 'huesuk4720@naver.com',
          password: 'password123!',
        });
        tokenStorage.setToken(res.token);
        closeModal();
        showToast(`환영합니다, ${res.user.username}님! 👋`);
        onAuthSuccess(res.user);
      } catch (err) {
        showToast(`데모 로그인 실패: ${err.message}`, 'error');
      }
    });

    form?.addEventListener('submit', async (e) => {
      e.preventDefault();
      const email = document.getElementById('auth-email').value.trim();
      const password = document.getElementById('auth-password').value;

      if (!email || !password) {
        showToast('이메일과 비밀번호를 입력해주세요.', 'error');
        return;
      }

      if (password.length < 6) {
        showToast('비밀번호는 최소 6자 이상이어야 합니다.', 'error');
        return;
      }

      try {
        if (isLoginMode) {
          const res = await api.auth.login({ email, password });
          tokenStorage.setToken(res.token);
          closeModal();
          showToast(`환영합니다, ${res.user.username}님! 👋`);
          onAuthSuccess(res.user);
        } else {
          const username = document.getElementById('auth-username').value.trim();
          if (!username) {
            showToast('이름을 입력해주세요.', 'error');
            return;
          }
          await api.auth.signup({ email, username, password });
          showToast('회원가입이 완료되었습니다! 로그인합니다.');
          const loginRes = await api.auth.login({ email, password });
          tokenStorage.setToken(loginRes.token);
          closeModal();
          onAuthSuccess(loginRes.user);
        }
      } catch (err) {
        showToast(err.message || '인증 처리에 실패했습니다.', 'error');
      }
    });
  }

  function openModal(mode = 'login') {
    isLoginMode = mode === 'login';
    renderModal();
  }

  function closeModal() {
    container.innerHTML = '';
  }

  return {
    openModal,
    closeModal,
  };
}
