const originalMonthSelect = document.querySelector("#target-month");
const monthField = originalMonthSelect?.closest(".field");
const workDateInput = document.querySelector("#work-date");
const attendanceInputs = document.querySelectorAll(
  "#attendance-form input, #attendance-form button[type='submit']"
);

const EDIT_PAST_KEY = "timecard.allowPastMonthEditing";

function currentYearMonth() {
  const today = new Date();
  return `${today.getFullYear()}-${String(today.getMonth() + 1).padStart(2, "0")}`;
}

function shiftMonth(yearMonth, amount) {
  const [year, month] = yearMonth.split("-").map(Number);
  const shifted = new Date(year, month - 1 + amount, 1);
  return `${shifted.getFullYear()}-${String(shifted.getMonth() + 1).padStart(2, "0")}`;
}

function applySelectedMonth(yearMonth) {
  const [year, month] = yearMonth.split("-").map(Number);
  const lastDay = new Date(year, month, 0).getDate();
  const today = new Date();
  const todayMonth = currentYearMonth();
  const preferredDay = yearMonth === todayMonth
    ? Math.min(today.getDate(), lastDay)
    : 1;

  workDateInput.min = `${yearMonth}-01`;
  workDateInput.max = `${yearMonth}-${String(lastDay).padStart(2, "0")}`;

  if (!workDateInput.value.startsWith(yearMonth)) {
    workDateInput.value = `${yearMonth}-${String(preferredDay).padStart(2, "0")}`;
  }

  workDateInput.dispatchEvent(new Event("change", { bubbles: true }));
  updateEditingState();

  if (typeof loadMonthlySummary === "function") {
    loadMonthlySummary();
  }
}

function updateEditingState() {
  const monthInput = document.querySelector("#history-month");
  const editPastInput = document.querySelector("#allow-past-editing");
  if (!monthInput || !editPastInput) return;

  const isPastMonth = monthInput.value < currentYearMonth();
  const canEdit = !isPastMonth || editPastInput.checked;

  attendanceInputs.forEach((input) => {
    if (input === workDateInput) return;
    input.disabled = !canEdit;
  });

  const notice = document.querySelector("#past-edit-notice");
  notice.textContent = canEdit
    ? isPastMonth
      ? "過去月の編集を許可しています。登録すると同じ日付の記録を上書きします。"
      : "今月の勤務を入力できます。"
    : "過去月は閲覧専用です。編集する場合は設定を有効にしてください。";
}

function text(id) {
  return document.querySelector(id)?.textContent.trim() || "";
}

function exportMonthlyCsv() {
  const month = document.querySelector("#history-month").value;
  const rows = [
    ["対象月", month],
    ["勤務日数", text("#monthly-work-days")],
    ["実働時間", text("#monthly-work-hours")],
    ["残業時間", text("#monthly-overtime-hours")],
    ["基本給与", text("#monthly-base-pay")],
    ["残業手当", text("#monthly-overtime-pay")],
    ["支給金額", text("#monthly-total-pay")]
  ];

  const csv = "\uFEFF" + rows
    .map((row) => row.map((value) => `"${String(value).replaceAll('"', '""')}"`).join(","))
    .join("\r\n");
  const blob = new Blob([csv], { type: "text/csv;charset=utf-8" });
  const link = document.createElement("a");
  link.href = URL.createObjectURL(blob);
  link.download = `timecard-summary-${month}.csv`;
  link.click();
  URL.revokeObjectURL(link.href);
}

if (monthField && originalMonthSelect) {
  originalMonthSelect.hidden = true;
  originalMonthSelect.removeAttribute("required");

  const controls = document.createElement("div");
  controls.className = "month-history-controls";
  controls.innerHTML = `
    <button type="button" id="previous-history-month" aria-label="前月を表示">‹ 前月</button>
    <input id="history-month" type="month" aria-label="表示する年月">
    <button type="button" id="next-history-month" aria-label="次月を表示">次月 ›</button>
    <button type="button" id="export-month-csv" class="export-month-button">CSV出力</button>
  `;
  monthField.appendChild(controls);

  const editing = document.createElement("div");
  editing.className = "past-edit-setting";
  editing.innerHTML = `
    <label><input id="allow-past-editing" type="checkbox"> 過去月の編集を許可する</label>
    <p id="past-edit-notice"></p>
  `;
  monthField.appendChild(editing);

  const monthInput = controls.querySelector("#history-month");
  const editPastInput = editing.querySelector("#allow-past-editing");
  monthInput.value = currentYearMonth();
  monthInput.max = currentYearMonth();
  editPastInput.checked = localStorage.getItem(EDIT_PAST_KEY) === "true";

  controls.querySelector("#previous-history-month").addEventListener("click", () => {
    monthInput.value = shiftMonth(monthInput.value, -1);
    applySelectedMonth(monthInput.value);
  });

  controls.querySelector("#next-history-month").addEventListener("click", () => {
    const next = shiftMonth(monthInput.value, 1);
    if (next <= currentYearMonth()) {
      monthInput.value = next;
      applySelectedMonth(monthInput.value);
    }
  });

  monthInput.addEventListener("change", () => applySelectedMonth(monthInput.value));
  editPastInput.addEventListener("change", () => {
    localStorage.setItem(EDIT_PAST_KEY, String(editPastInput.checked));
    updateEditingState();
  });
  controls.querySelector("#export-month-csv").addEventListener("click", exportMonthlyCsv);

  applySelectedMonth(monthInput.value);
}
