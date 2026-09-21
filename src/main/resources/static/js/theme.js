const THEME_KEY = 'ygt_theme';

export function initThemeToggle(buttonEl) {
  const apply = (theme) => {
    document.documentElement.setAttribute('data-theme', theme);
    try { localStorage.setItem(THEME_KEY, theme); } catch {}
    if (buttonEl) buttonEl.textContent = theme === 'light' ? '🌙' : '☀️';
  };

  const current = document.documentElement.getAttribute('data-theme') || 'dark';
  if (buttonEl) {
    buttonEl.textContent = current === 'light' ? '🌙' : '☀️';
    buttonEl.addEventListener('click', () => {
      const now = document.documentElement.getAttribute('data-theme') === 'light' ? 'dark' : 'light';
      apply(now);
    });
  }
}
