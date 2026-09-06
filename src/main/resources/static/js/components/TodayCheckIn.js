/**
 * Today's Quick Check-In Component
 */
import { state } from '../state.js';
import { api } from '../api.js';

export function renderTodayCheckIn(container, onCheckInSuccess, showToast) {
  const today = new Date();
  const options = { year: 'numeric', month: 'long', day: 'numeric', weekday: 'short' };
  const dateStr = today.toLocaleDateString('ko-KR', options);
  const isoDate = today.toISOString().split('T')[0];

  const subTasks = state.subTasks;

  let itemsHtml = '';
  if (subTasks.length === 0) {
    itemsHtml = `
      <div class="empty-state">
        <div class="empty-state-icon">⚡</div>
        <p>오늘 체크인할 하위 태스크가 없습니다.<br>목표를 클릭하여 <strong>하위 태스크(SubTask)</strong>를 먼저 추가해보세요!</p>
      </div>
    `;
  } else {
    itemsHtml = subTasks
      .map((st) => {
        return `
          <div class="checkin-card" data-subtask-id="${st.id}">
            <div class="checkin-card-header">
              <div class="checkin-title-box">
                <span class="checkin-goal-tag">🎯 ${st.goalTitle}</span>
                <span class="checkin-task-title">${st.title}</span>
              </div>
              <div class="checkin-status-toggle">
                <button type="button" class="status-toggle-btn active success" data-status="SUCCESS">완료</button>
                <button type="button" class="status-toggle-btn" data-status="FAIL">미완료</button>
              </div>
            </div>

            <div class="slider-row">
              <input type="range" min="0" max="100" value="100" class="checkin-rate-slider">
              <span class="slider-value">100%</span>
            </div>

            <div class="memo-input-row">
              <input type="text" class="memo-input" placeholder="오늘의 실천 한 줄 메모 남기기...">
              <button type="button" class="btn btn-primary btn-sm save-checkin-btn">체크인</button>
            </div>
          </div>
        `;
      })
      .join('');
  }

  container.innerHTML = `
    <div class="section-header">
      <div class="section-title-group">
        <h2><span>⚡</span> 오늘의 빠른 체크인</h2>
      </div>
      <span class="date-pill">${dateStr}</span>
    </div>

    <div class="checkin-list">
      ${itemsHtml}
    </div>
  `;

  // Attach interactive events
  container.querySelectorAll('.checkin-card').forEach((card) => {
    const subTaskId = Number(card.dataset.subtaskId);
    const slider = card.querySelector('.checkin-rate-slider');
    const sliderVal = card.querySelector('.slider-value');
    const memoInput = card.querySelector('.memo-input');
    const saveBtn = card.querySelector('.save-checkin-btn');
    const toggleBtns = card.querySelectorAll('.status-toggle-btn');

    let selectedStatus = 'SUCCESS';

    // Toggle status buttons
    toggleBtns.forEach((btn) => {
      btn.addEventListener('click', (e) => {
        toggleBtns.forEach((b) => b.classList.remove('active', 'success', 'fail'));
        const status = e.currentTarget.dataset.status;
        selectedStatus = status;
        if (status === 'SUCCESS') {
          e.currentTarget.classList.add('active', 'success');
          slider.value = 100;
          sliderVal.textContent = '100%';
        } else {
          e.currentTarget.classList.add('active', 'fail');
          slider.value = 0;
          sliderVal.textContent = '0%';
        }
      });
    });

    // Slider change
    slider.addEventListener('input', (e) => {
      sliderVal.textContent = `${e.target.value}%`;
    });

    // Save check-in
    saveBtn.addEventListener('click', async () => {
      const progressRate = Number(slider.value);
      const memo = memoInput.value.trim();

      saveBtn.disabled = true;
      saveBtn.textContent = '저장중...';

      try {
        await api.createCheckIn(subTaskId, {
          checkInDate: isoDate,
          status: selectedStatus,
          progressRate: progressRate,
          memo: memo || '오늘 실천 완료',
        });
        showToast('체크인이 성공적으로 기록되었습니다! 🎉');
        saveBtn.textContent = '완료됨 ✓';
        saveBtn.classList.remove('btn-primary');
        saveBtn.classList.add('btn-secondary');
        if (onCheckInSuccess) onCheckInSuccess();
      } catch (err) {
        // If already checked in today (Conflict 409), fetch existing and inform
        if (err.message && err.message.includes('이미')) {
          showToast('오늘 이미 체크인한 기록이 있습니다. 수정 반영을 시도합니다.', 'info');
          // Try to get check-ins for this subtask and update
          try {
            const checkIns = await api.getCheckIns(subTaskId);
            const todayCheckIn = checkIns.find((c) => c.checkInDate === isoDate);
            if (todayCheckIn) {
              await api.updateCheckIn(todayCheckIn.id, {
                status: selectedStatus,
                progressRate: progressRate,
                memo: memo || todayCheckIn.memo,
              });
              showToast('오늘자 체크인이 업데이트되었습니다! 👍');
              saveBtn.textContent = '수정됨 ✓';
              if (onCheckInSuccess) onCheckInSuccess();
            }
          } catch (updateErr) {
            showToast(updateErr.message || '체크인 업데이트 실패', 'error');
            saveBtn.textContent = '체크인';
            saveBtn.disabled = false;
          }
        } else {
          showToast(err.message || '체크인 저장 중 오류 발생', 'error');
          saveBtn.textContent = '체크인';
          saveBtn.disabled = false;
        }
      }
    });
  });
}
