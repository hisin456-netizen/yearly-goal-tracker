import { api, tokenStorage } from './api.js';
import { initThemeToggle } from './theme.js';

const authError = document.getElementById('auth-error');
initThemeToggle(document.getElementById('theme-toggle-btn'));

// 이미 로그인된 경우 대시보드로 이동
(async () => {
  const token = tokenStorage.getToken();
  if (token) {
    try {
      await api.auth.getMe();
      window.location.replace('/');
      return;
    } catch {
      tokenStorage.clearToken();
    }
  }

  const params = new URLSearchParams(location.search);
  if (params.get('error')) {
    authError.textContent = params.get('error') === 'signup_not_allowed'
      ? '가입이 허용되지 않은 계정입니다.'
      : '로그인에 실패했습니다. 다시 시도해주세요.';
    authError.style.display = 'block';
  }
})();
