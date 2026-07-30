const historyMonthInput = document.querySelector("#history-month");
const historyPreviousButton = document.querySelector("#history-previous");
const historyNextButton = document.querySelector("#history-next");
const historyCurrentButton = document.querySelector("#history-current");
const historyExportButton = document.querySelector("#history-export");
const allowPastEditInput = document.querySelector("#allow-past-edit");
const historyStatus = document.querySelector("#history-status");
const historyEditMessage = document.querySelector("#history-edit-message");
const historyDateInput = document.querySelector("#work-date");
const historyAttendanceForm = document.querySelector("#attendance-form");
const historySubmitButton = historyAttendanceForm.querySelector('button[type="submit"]');
const historyStartInput = document.querySelector("#start-time");
const historyEndInput = document.querySelector("#end-time");
const historyBreakInputs = [...document.querySelectorAll('input[name="tookBreak"]')];

const ALLOW_PAST_EDIT_KEY = "timecard.allowPastEdit";

function currentMonthText() {
  const today = new Date();
  return `${today.getFullYear()}-${String(today.getMonth() + 1).padStart(2, "0")}`;
}

function shiftMonth(monthText, amount) {
  const [year, month] = monthText.split("-").map(Number);
  const shifted = new Date(year, month - 1 + amount, 1);
  return `${shifted.getFullYear()}-${String(shifted.getMonth() + 1).padStart(2, "0")}`;
}

function selectedDayForMonth(monthText) {
  const [year, month] = monthText.split("-").map(Number);
  const finalDay = new Date(year, month, 0).getDate();
  const today = new Date();
  const day = monthText === currentMonthText()
    ? Math.min(today.getDate(), finalDay)
    : finalDay;
  return `${monthText}-${String(day).padStart(2, "0")}`;
}

function updateNavigationState() {
  historyNextButton.disabled = historyMonthInput.value >= currentMonthText();
}

function moveToMonth(monthText) {
  if (!/^\d{4}-\d{2}$/.test(monthText)) {
    return;
  }

  const safeMonth = monthText > currentMonthText() ? currentMonthText() : monthText;
  historyMonthInput.value = safeMonth;
  historyDateInput.value = selectedDayForMonth(safeMonth);
  historyDateInput.dispatchEvent(new Event("change", { bubbles: true }));
  updatePastEditState();
  updateNavigationState();
  historyStatus.textContent = `${safeMonth} の勤務・給与履歴を表示しています。`;
}

function updatePastEditState() {
  const viewedMonth = historyMonthInput.value || historyDateInput.value.slice(0, 7);
  const isPastMonth = viewedMonth && viewedMonth < currentMonthText();
  const allowEdit = allowPastEditInput.checked;
  const readOnly = isPastMonth && !allowEdit;

  [historyDateInput, historyStartInput, historyEndInput, ...historyBreakInputs]
    .forEach((input) => {
      input.disabled = readOnly;
    });
  historySubmitButton.disabled = readOnly;
  historyEditMessage.hidden = !readOnly;

  localStorage.setItem(ALLOW_PAST_EDIT_KEY, String(allowEdit));
}

function csvCell(value) {
  return `"${String(value ?? "").replaceAll('"', '""')}"`;
}

async function exportSelectedMonth() {
  const monthText = historyMonthInput.value;
  if (!monthText) {
    historyStatus.textContent = "出力する年月を選択してください。";
    return;
  }

  historyExportButton.disabled = true;
  historyStatus.textContent = "CSVを作成しています。";

  try {
    const datesResponse = await fetch(
      `/api/attendances/dates?month=${encodeURIComponent(monthText)}`
    );
    const dates = datesResponse.ok ? await datesResponse.json() : [];
    const rows = [[
      "勤務日", "出勤", "退勤", "実働分", "残業分",
      "基本給与", "残業手当", "支給金額"
    ]];

    for (const date of dates) {
      const detailResponse = await fetch(
        `/api/attendances/detail?date=${encodeURIComponent(date)}`
      );
      if (!detailResponse.ok) {
        continue;
      }
      const detail = await detailResponse.json();
      rows.push([
        detail.workDate,
        detail.startTime,
        detail.endTime,
        detail.workMinutes,
        detail.overtimeMinutes,
        detail.basePay,
        detail.overtimePay,
        detail.totalPay
      ]);
    }

    const csv = "\uFEFF" + rows.map((row) => row.map(csvCell).join(",")).join("\r\n");
    const blob = new Blob([csv], { type: "text/csv;charset=utf-8" });
    const link = document.createElement("a");
    link.href = URL.createObjectURL(blob);
    link.download = `timecard-${monthText}.csv`;
    link.click();
    URL.revokeObjectURL(link.href);
    historyStatus.textContent = `${monthText} のCSVを出力しました。Excelでも開けます。`;
  } catch (error) {
    historyStatus.textContent = "CSVの作成に失敗しました。";
  } finally {
    historyExportButton.disabled = false;
  }
}

historyPreviousButton.addEventListener("click", () => {
  moveToMonth(shiftMonth(historyMonthInput.value, -1));
});
historyNextButton.addEventListener("click", () => {
  moveToMonth(shiftMonth(historyMonthInput.value, 1));
});
historyCurrentButton.addEventListener("click", () => moveToMonth(currentMonthText()));
historyMonthInput.addEventListener("change", () => moveToMonth(historyMonthInput.value));
historyExportButton.addEventListener("click", exportSelectedMonth);
allowPastEditInput.addEventListener("change", updatePastEditState);
historyDateInput.addEventListener("change", () => {
  const monthText = historyDateInput.value.slice(0, 7);
  if (monthText) {
    historyMonthInput.value = monthText;
  }
  updatePastEditState();
  updateNavigationState();
});

allowPastEditInput.checked = localStorage.getItem(ALLOW_PAST_EDIT_KEY) === "true";
historyMonthInput.max = currentMonthText();
moveToMonth(historyDateInput.value.slice(0, 7) || currentMonthText());