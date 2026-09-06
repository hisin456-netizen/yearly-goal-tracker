import { api } from '../api.js';

let chartInstance = null;

export async function renderLineChart(container) {
  try {
    const heatmapData = await api.dashboard.getHeatmap(30);
    const points = heatmapData.points || [];

    const sorted = [...points].sort((a, b) => a.date.localeCompare(b.date));
    const labels = sorted.map(p => {
      const d = new Date(p.date);
      return `${d.getMonth() + 1}/${d.getDate()}`;
    });
    const counts = sorted.map(p => p.count);

    // 7일 이동평균
    const movingAvg = counts.map((_, i) => {
      const slice = counts.slice(Math.max(0, i - 3), i + 4);
      return Math.round((slice.reduce((a, b) => a + b, 0) / slice.length) * 10) / 10;
    });

    container.innerHTML = `
      <div class="linechart-header">
        <div class="linechart-title-group">
          <h3>📊 최근 30일 체크인 추이</h3>
          <p>일별 체크인 수 및 7일 이동평균</p>
        </div>
        <div class="linechart-legend">
          <span class="legend-item"><span class="legend-dot dot-bar"></span>일별 체크인</span>
          <span class="legend-item"><span class="legend-dot dot-line"></span>7일 평균</span>
        </div>
      </div>
      <div class="linechart-canvas-wrap">
        <canvas id="activity-line-chart"></canvas>
      </div>
    `;

    const canvas = document.getElementById('activity-line-chart');
    if (!canvas || typeof Chart === 'undefined') return;

    if (chartInstance) {
      chartInstance.destroy();
      chartInstance = null;
    }

    const ctx = canvas.getContext('2d');

    // 그라데이션 채우기
    const gradient = ctx.createLinearGradient(0, 0, 0, 200);
    gradient.addColorStop(0, 'rgba(32, 201, 151, 0.25)');
    gradient.addColorStop(1, 'rgba(32, 201, 151, 0)');

    chartInstance = new Chart(ctx, {
      type: 'bar',
      data: {
        labels,
        datasets: [
          {
            type: 'bar',
            label: '일별 체크인',
            data: counts,
            backgroundColor: 'rgba(124, 111, 250, 0.35)',
            borderColor: 'rgba(124, 111, 250, 0.7)',
            borderWidth: 1,
            borderRadius: 4,
            order: 2,
          },
          {
            type: 'line',
            label: '7일 이동평균',
            data: movingAvg,
            borderColor: '#20c997',
            backgroundColor: gradient,
            borderWidth: 2.5,
            pointRadius: 0,
            pointHoverRadius: 5,
            pointHoverBackgroundColor: '#20c997',
            tension: 0.4,
            fill: true,
            order: 1,
          },
        ],
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        interaction: { mode: 'index', intersect: false },
        plugins: {
          legend: { display: false },
          tooltip: {
            backgroundColor: '#1a1a28',
            borderColor: 'rgba(255,255,255,0.08)',
            borderWidth: 1,
            titleColor: '#f0f0f5',
            bodyColor: '#8a8a9e',
            padding: 12,
            callbacks: {
              title: (items) => `${items[0].label}`,
              label: (item) => {
                if (item.datasetIndex === 0) return `  체크인: ${item.raw}회`;
                return `  7일 평균: ${item.raw}회`;
              },
            },
          },
        },
        scales: {
          x: {
            grid: { color: 'rgba(255,255,255,0.04)', drawBorder: false },
            ticks: {
              color: '#4a4a60',
              font: { family: 'Pretendard, sans-serif', size: 11 },
              maxTicksLimit: 10,
            },
          },
          y: {
            grid: { color: 'rgba(255,255,255,0.04)', drawBorder: false },
            ticks: {
              color: '#4a4a60',
              font: { family: 'Pretendard, sans-serif', size: 11 },
              stepSize: 1,
            },
            beginAtZero: true,
          },
        },
      },
    });

  } catch (err) {
    container.innerHTML = `
      <div style="padding: 24px; color: var(--text-muted); font-size: 0.84rem;">
        차트 데이터를 불러오지 못했습니다.
      </div>
    `;
  }
}
