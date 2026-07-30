const dashboardMonthSelect = document.querySelector("#target-month");
const dashboardDateInput = document.querySelector("#work-date");
const dashboardStartInput = document.querySelector("#start-time");
const dashboardEndInput = document.querySelector("#end-time");
const dashboardAttendanceForm = document.querySelector("#attendance-form");
const dashboardMonthlyCard = document.querySelector(".monthly-card");
const dashboardSaveStatus = document.querySelector("#save-status");

const DAILY_RECORDS_KEY = "timecard.dailyRecords";

function readDailyRecords() {
  try {
    return JSON.parse(localStorage.getItem(DAILY_RECORDS_KEY)) || {};
  } catch (error) {
    return {};
  }
}

function selectedDashboardMonth() {
  return dashboardDateInput.min
    ? dashboardDateInput.min.slice(0, 7)
    : dashboardDateInput.value.slice(0, 7);
}

function formatSelectedDate(dateText) {
  if (!dateText) {
    return "日付を選択してください";
  }
  const [year, month, day] = dateText.split("-");
  return `${year}年${Number(month)}月${Number(day)}日`;
}

function createDashboard() {
  const dashboard = document.createElement("section");
  dashboard.className = "monthly-dashboard";
  dashboard.setAttribute("aria-label", "月間勤怠ダッシュボード");
  dashboard.innerHTML = `
    <section class="dashboard-panel">
      <div class="dashboard-panel-heading">
        <h2>月間カレンダー</h2>
        <span id="dashboard-month-label" class="dashboard-month-label"></span>
      </div>
      <div class="dashboard-calendar">
        <div class="dashboard-weekdays" aria-hidden="true">
          <span>日</span><span>月</span><span>火</span><span>水</span><span>木</span><span>金</span><span>土</span>
        </div>
        <div id="dashboard-days" class="dashboard-days"></div>
      </div>
      <p class="dashboard-legend">青：選択中　枠線：今日　緑の印：このブラウザで登録済み</p>
    </section>
    <div class="dashboard-right">
      <section class="dashboard-panel">
        <div class="dashboard-panel-heading">
          <h3>選択日の勤務内容</h3>
          <span id="selected-day-label" class="dashboard-month-label"></span>
        </div>
        <div id="selected-day-content" class="selected-day-content"></div>
      </section>
    </div>
  `;

  dashboardMonthlyCard.parentNode.insertBefore(dashboard, dashboardMonthlyCard);
  dashboard.querySelector(".dashboard-right").appendChild(dashboardMonthlyCard);
}

function renderSelectedDay() {
  const records = readDailyRecords();
  const record = records[dashboardDateInput.value];
  const label = document.querySelector("#selected-day-label");
  const content = document.querySelector("#selected-day-content");

  label.textContent = formatSelectedDate(dashboardDateInput.value);

  if (!record) {
    content.innerHTML = '<p class="selected-day-empty">この日に、このブラウザで登録した勤務記録はありません。</p>';
    return;
  }

  content.innerHTML = `
    <dl>
      <div><dt>出勤時刻</dt><dd>${record.startTime}</dd></div>
      <div><dt>退勤時刻</dt><dd>${record.endTime}</dd></div>
      <div><dt>実働時間</dt><dd>${record.workHours}</dd></div>
      <div><dt>残業時間</dt><dd>${record.overtimeHours}</dd></div>
      <div><dt>基本給与</dt><dd>${record.basePay}</dd></div>
      <div><dt>支給金額</dt><dd>${record.totalPay}</dd></div>
    </dl>
  `;
}

function renderDashboardCalendar() {
  const monthText = selectedDashboardMonth();
  if (!monthText) {
    return;
  }

  const [year, month] = monthText.split("-").map(Number);
  const firstWeekday = new Date(year, month - 1, 1).getDay();
  const finalDay = new Date(year, month, 0).getDate();
  const today = new Date();
  const todayText = [
    today.getFullYear(),
    String(today.getMonth() + 1).padStart(2, "0"),
    String(today.getDate()).padStart(2, "0")
  ].join("-");
  const records = readDailyRecords();
  const days = document.querySelector("#dashboard-days");

  document.querySelector("#dashboard-month-label").textContent =
    `${year}年${month}月`;
  days.innerHTML = "";

  for (let index = 0; index < firstWeekday; index += 1) {
    const empty = document.createElement("span");
    empty.className = "dashboard-day is-empty";
    days.appendChild(empty);
  }

  for (let day = 1; day <= finalDay; day += 1) {
    const dateText = `${year}-${String(month).padStart(2, "0")}-${String(day).padStart(2, "0")}`;
    const button = document.createElement("button");
    button.type = "button";
    button.className = "dashboard-day";
    button.textContent = day;
    button.setAttribute("aria-label", `${month}月${day}日を選択`);

    if (dateText === todayText) button.classList.add("is-today");
    if (dateText === dashboardDateInput.value) button.classList.add("is-selected");
    if (records[dateText]) button.classList.add("has-record");

    button.addEventListener("click", () => {
      dashboardDateInput.value = dateText;
      dashboardDateInput.dispatchEvent(new Event("change", { bubbles: true }));
      renderDashboardCalendar();
      renderSelectedDay();
      dashboardStartInput.focus();
    });

    days.appendChild(button);
  }
}

function saveCurrentDayRecord() {
  const records = readDailyRecords();
  records[dashboardDateInput.value] = {
    startTime: dashboardStartInput.value,
    endTime: dashboardEndInput.value,
    workHours: document.querySelector("#work-hours").textContent,
    overtimeHours: document.querySelector("#overtime-hours").textContent,
    basePay: document.querySelector("#base-pay").textContent,
    totalPay: document.querySelector("#payment").textContent
  };
  localStorage.setItem(DAILY_RECORDS_KEY, JSON.stringify(records));
  renderDashboardCalendar();
  renderSelectedDay();
}

createDashboard();
renderDashboardCalendar();
renderSelectedDay();

dashboardDateInput.addEventListener("change", () => {
  renderDashboardCalendar();
  renderSelectedDay();
});

dashboardMonthSelect.addEventListener("change", () => {
  window.setTimeout(() => {
    renderDashboardCalendar();
    renderSelectedDay();
  }, 0);
});

const statusObserver = new MutationObserver(() => {
  if (!dashboardSaveStatus.hidden && dashboardSaveStatus.textContent.includes("✓")) {
    saveCurrentDayRecord();
  }
});

statusObserver.observe(dashboardSaveStatus, {
  childList: true,
  characterData: true,
  subtree: true,
  attributes: true,
  attributeFilter: ["hidden"]
});
