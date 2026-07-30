const dashboardStyle = document.createElement("link");
dashboardStyle.rel = "stylesheet";
dashboardStyle.href = "monthly-dashboard.css";
document.head.appendChild(dashboardStyle);

const dailyDetailTitle = document.querySelector("#daily-detail-title");
const dailyDetailStatus = document.querySelector("#daily-detail-status");
const dailyDetailMessage = document.querySelector("#daily-detail-message");
const dailyStartTime = document.querySelector("#daily-start-time");
const dailyEndTime = document.querySelector("#daily-end-time");
const dailyWorkTime = document.querySelector("#daily-work-time");
const dailyOvertimeTime = document.querySelector("#daily-overtime-time");
const dailyBasePay = document.querySelector("#daily-base-pay");
const dailyOvertimePay = document.querySelector("#daily-overtime-pay");
const dailyTotalPay = document.querySelector("#daily-total-pay");
const dashboardDateInput = document.querySelector("#work-date");
const dashboardSaveStatus = document.querySelector("#save-status");

function formatDashboardDuration(totalMinutes) {
  const hours = Math.floor(totalMinutes / 60);
  const minutes = totalMinutes % 60;
  return minutes === 0 ? `${hours}時間` : `${hours}時間${minutes}分`;
}

function formatDashboardCurrency(amount) {
  return `${Number(amount).toLocaleString("ja-JP")}円`;
}

function formatJapaneseDate(dateText) {
  const [year, month, day] = dateText.split("-").map(Number);
  return `${year}年${month}月${day}日`;
}

function clearDailyDetail(dateText, message) {
  dailyDetailTitle.textContent = formatJapaneseDate(dateText);
  dailyDetailStatus.textContent = "未登録";
  dailyDetailStatus.classList.remove("is-registered");
  dailyStartTime.textContent = "—";
  dailyEndTime.textContent = "—";
  dailyWorkTime.textContent = "—";
  dailyOvertimeTime.textContent = "—";
  dailyBasePay.textContent = "—円";
  dailyOvertimePay.textContent = "—円";
  dailyTotalPay.textContent = "—円";
  dailyDetailMessage.textContent = message;
}

async function loadDailyDetail(dateText) {
  if (!dateText || !dailyDetailTitle) {
    return;
  }

  dailyDetailTitle.textContent = formatJapaneseDate(dateText);
  dailyDetailStatus.textContent = "読込中";
  dailyDetailStatus.classList.remove("is-registered");
  dailyDetailMessage.textContent = "勤務内容を読み込んでいます。";

  try {
    const response = await fetch(
      `/api/attendances/detail?date=${encodeURIComponent(dateText)}`
    );

    if (!response.ok) {
      throw new Error("勤務内容の読み込みに失敗しました。");
    }

    const detail = await response.json();
    if (!detail) {
      clearDailyDetail(dateText, "この日はまだ勤怠が登録されていません。");
      return;
    }

    dailyDetailStatus.textContent = "登録済み";
    dailyDetailStatus.classList.add("is-registered");
    dailyStartTime.textContent = detail.startTime;
    dailyEndTime.textContent = detail.endTime;
    dailyWorkTime.textContent = formatDashboardDuration(detail.workMinutes);
    dailyOvertimeTime.textContent = formatDashboardDuration(detail.overtimeMinutes);
    dailyBasePay.textContent = formatDashboardCurrency(detail.basePay);
    dailyOvertimePay.textContent = formatDashboardCurrency(detail.overtimePay);
    dailyTotalPay.textContent = formatDashboardCurrency(detail.totalPay);
    dailyDetailMessage.textContent = "登録済みの勤務内容です。同じ日付で再登録すると上書きされます。";
  } catch (error) {
    clearDailyDetail(dateText, error.message);
  }
}

document.addEventListener("timecard:calendar-date-selected", (event) => {
  loadDailyDetail(event.detail.date);
});

if (dashboardDateInput) {
  dashboardDateInput.addEventListener("change", () => {
    loadDailyDetail(dashboardDateInput.value);
  });
}

if (dashboardSaveStatus) {
  new MutationObserver(() => {
    if (!dashboardSaveStatus.hidden && dashboardDateInput.value) {
      loadDailyDetail(dashboardDateInput.value);
    }
  }).observe(dashboardSaveStatus, {
    attributes: true,
    childList: true,
    subtree: true
  });
}

window.setTimeout(() => {
  if (dashboardDateInput && dashboardDateInput.value) {
    loadDailyDetail(dashboardDateInput.value);
  }
}, 0);
