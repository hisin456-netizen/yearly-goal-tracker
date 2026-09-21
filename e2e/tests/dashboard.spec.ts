import { test, expect } from '@playwright/test';

test.describe('대시보드 (인증됨)', () => {
  test('회원가입 + 로그인 후 대시보드가 정상적으로 렌더링된다', async ({ page, request }) => {
    const email = `e2e-${Date.now()}@example.com`;
    const password = 'test1234';

    const signupRes = await request.post('/api/v1/auth/signup', {
      data: { email, username: 'E2E Tester', password },
    });
    expect(signupRes.ok()).toBeTruthy();

    const loginRes = await request.post('/api/v1/auth/login', {
      data: { email, password },
    });
    expect(loginRes.ok()).toBeTruthy();
    const { data } = await loginRes.json();
    const token: string = data.token;
    expect(token).toBeTruthy();

    // app.js reads the token from localStorage on DOMContentLoaded, so it must
    // be set before the page's own scripts run.
    await page.addInitScript((t) => {
      window.localStorage.setItem('ygt_access_token', t);
    }, token);

    await page.goto('/index.html');

    await expect(page).toHaveURL(/index\.html/);
    await expect(page.locator('#main-header')).toBeVisible();
    await expect(page.locator('#add-goal-btn')).toBeVisible();
    await expect(page.locator('#kpi-annual-progress')).toBeVisible();
  });
});
