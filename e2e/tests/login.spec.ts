import { test, expect } from '@playwright/test';

test.describe('로그인 페이지', () => {
  test('로그인 페이지가 정상적으로 렌더링된다', async ({ page }) => {
    await page.goto('/login.html');

    await expect(page).toHaveTitle(/Yearly Goal Tracker/);
    await expect(page.locator('.brand-name')).toHaveText('Yearly Goal Tracker');
    await expect(page.locator('#btn-google-login')).toBeVisible();
  });

  test('인증되지 않은 상태로 대시보드 접근 시 로그인 페이지로 리다이렉트된다', async ({ page }) => {
    await page.goto('/index.html');
    await page.waitForURL('**/login.html');
    await expect(page.locator('#btn-google-login')).toBeVisible();
  });

  test('테마 토글 버튼 클릭 시 data-theme 속성이 변경된다', async ({ page }) => {
    await page.goto('/login.html');
    const html = page.locator('html');
    const before = await html.getAttribute('data-theme');

    await page.locator('#theme-toggle-btn').click();

    await expect(async () => {
      const after = await html.getAttribute('data-theme');
      expect(after).not.toBe(before);
    }).toPass();
  });
});
