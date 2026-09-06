/**
 * Activity Heatmap Component - GitHub Style Streak & CheckIn Grid (Last 15 Weeks)
 */
import { api } from '../api.js';

export async function renderHeatmap(container) {
  if (!container) return;

  try {
    const data = await api.dashboard.getHeatmap(105);
    const points = data.points || [];
    const totalContributions = data.totalContributions || 0;

    // Group points into 15 weeks (each week has 7 days)
    const weeks = [];
    let currentWeek = [];

    // Ensure day of week alignment if needed, or simply chunks of 7
    for (let i = 0; i < points.length; i++) {
      currentWeek.push(points[i]);
      if (currentWeek.length === 7 || i === points.length - 1) {
        weeks.push(currentWeek);
        currentWeek = [];
      }
    }

    // Render 7 rows (one per day of week) across weeks columns
    const dayLabels = ['일', '월', '화', '수', '목', '금', '토'];

    // Construct grid columns
    const weeksColsHtml = weeks
      .map((week) => {
        const cells = week
          .map((pt) => {
            const dateStr = pt.date;
            const tooltip = `${dateStr}: ${pt.count}회 실천 완료`;
            return `
              <div 
                class="heatmap-cell level-${pt.level}" 
                data-date="${dateStr}" 
                data-count="${pt.count}"
                title="${tooltip}"
              ></div>
            `;
          })
          .join('');

        return `<div class="heatmap-week-col" style="display: flex; flex-direction: column; gap: 4px;">${cells}</div>`;
      })
      .join('');

    container.innerHTML = `
      <div class="heatmap-header">
        <div class="heatmap-title-group">
          <h3><span>🌱</span> 일일 실천 잔디 (Activity Heatmap)</h3>
        </div>
        <div style="font-size: 0.82rem; color: var(--color-cyan); font-weight: 600;">
          최근 15주간 총 <strong>${totalContributions}회</strong> 실천 완료! 👏
        </div>
      </div>

      <div class="heatmap-grid-container">
        <div style="display: flex; gap: 6px; align-items: center;">
          <!-- Day Labels (Optional) -->
          <div style="display: flex; flex-direction: column; gap: 4px; font-size: 0.68rem; color: var(--text-muted); padding-right: 4px;">
            <span>일</span>
            <span style="opacity: 0;">월</span>
            <span>화</span>
            <span style="opacity: 0;">목</span>
            <span>금</span>
            <span style="opacity: 0;">토</span>
          </div>

          <!-- Heatmap Weeks Grid -->
          <div style="display: flex; gap: 4px; overflow-x: auto; padding: 4px 0;">
            ${weeksColsHtml}
          </div>
        </div>
      </div>

      <!-- Legend -->
      <div class="heatmap-legend">
        <span>Less</span>
        <div class="heatmap-cell level-0" style="width: 12px; height: 12px;" title="0회"></div>
        <div class="heatmap-cell level-1" style="width: 12px; height: 12px;" title="1회"></div>
        <div class="heatmap-cell level-2" style="width: 12px; height: 12px;" title="2회"></div>
        <div class="heatmap-cell level-3" style="width: 12px; height: 12px;" title="3회"></div>
        <div class="heatmap-cell level-4" style="width: 12px; height: 12px;" title="4회 이상"></div>
        <span>More</span>
      </div>
    `;
  } catch (err) {
    container.innerHTML = `
      <div style="font-size: 0.82rem; color: var(--text-muted); text-align: center; padding: 16px;">
        잔디 데이터를 불러올 수 없습니다. (${err.message})
      </div>
    `;
  }
}
