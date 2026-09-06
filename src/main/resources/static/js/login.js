import { api, tokenStorage } from './api.js';

let isLoginMode = true;

const tabLogin = document.getElementById('tab-login');
const tabSignup = document.getElementById('tab-signup');
const form = document.getElementById('auth-form');
const groupUsername = document.getElementById('group-username');
const inputUsername = document.getElementById('input-username');
const inputEmail = document.getElementById('input-email');
const inputPassword = document.getElementById('input-password');
const btnSubmit = document.getElementById('btn-submit');
const btnLabel = document.getElementById('btn-label');
const btnSwitch = document.getElementById('btn-switch');
const switchLead = document.getElementById('switch-lead');
const authError = document.getElementById('auth-error');
const formHeading = document.getElementById('form-heading');
const pwToggle = document.getElementById('pw-toggle');
const eyeOpen = document.getElementById('eye-open');
const eyeClosed = document.getElementById('eye-closed');

// 이미 로그인된 경우 대시보드로 이동
(async () => {
  const token = tokenStorage.getToken();
  if (token) {
    try {
      await api.auth.getMe();
      window.location.replace('/');
    } catch {
      tokenStorage.clearToken();
    }
  }
})();

function setMode(loginMode) {
  isLoginMode = loginMode;

  tabLogin.classList.toggle('active', loginMode);
  tabLogin.setAttribute('aria-selected', loginMode);
  tabSignup.classList.toggle('active', !loginMode);
  tabSignup.setAttribute('aria-selected', !loginMode);

  groupUsername.style.display = loginMode ? 'none' : 'block';
  if (!loginMode) inputUsername.setAttribute('required', '');
  else inputUsername.removeAttribute('required');

  btnLabel.textContent = loginMode ? '로그인하기' : '회원가입하기';
  switchLead.textContent = loginMode ? '계정이 없으신가요?' : '이미 계정이 있으신가요?';
  btnSwitch.textContent = loginMode ? '회원가입' : '로그인';

  formHeading.querySelector('h2').textContent = loginMode
    ? '다시 돌아오셨군요!'
    : '새 계정 만들기';
  formHeading.querySelector('p').textContent = loginMode
    ? '오늘도 목표를 향해 한 걸음 더.'
    : '목표를 세우고 성장을 기록해보세요.';

  hideError();
  form.reset();
}

tabLogin.addEventListener('click', () => setMode(true));
tabSignup.addEventListener('click', () => setMode(false));
btnSwitch.addEventListener('click', () => setMode(!isLoginMode));

// 비밀번호 표시 토글
pwToggle.addEventListener('click', () => {
  const isPassword = inputPassword.type === 'password';
  inputPassword.type = isPassword ? 'text' : 'password';
  eyeOpen.style.display = isPassword ? 'none' : 'block';
  eyeClosed.style.display = isPassword ? 'block' : 'none';
});

function showError(msg) {
  authError.textContent = msg;
  authError.style.display = 'block';
}

function hideError() {
  authError.style.display = 'none';
}

function setLoading(loading) {
  btnSubmit.disabled = loading;
  btnLabel.textContent = loading
    ? '처리 중...'
    : isLoginMode ? '로그인하기' : '회원가입하기';
}

form.addEventListener('submit', async (e) => {
  e.preventDefault();
  hideError();

  const email = inputEmail.value.trim();
  const password = inputPassword.value;

  if (!email) { showError('이메일을 입력해주세요.'); inputEmail.focus(); return; }
  if (!password) { showError('비밀번호를 입력해주세요.'); inputPassword.focus(); return; }
  if (password.length < 6) { showError('비밀번호는 최소 6자 이상이어야 합니다.'); inputPassword.focus(); return; }

  setLoading(true);

  try {
    if (isLoginMode) {
      const res = await api.auth.login({ email, password });
      tokenStorage.setToken(res.token);
      window.location.replace('/');
    } else {
      const username = inputUsername.value.trim();
      if (!username) { showError('이름 / 닉네임을 입력해주세요.'); inputUsername.focus(); setLoading(false); return; }
      await api.auth.signup({ email, username, password });
      const loginRes = await api.auth.login({ email, password });
      tokenStorage.setToken(loginRes.token);
      window.location.replace('/');
    }
  } catch (err) {
    showError(err.message || '인증에 실패했습니다. 다시 시도해주세요.');
    setLoading(false);
  }
});
