const attendanceForm = document.querySelector("#attendance-form");
const settingsForm = document.querySelector("#settings-form");
const employeeNameInput = document.querySelector("#employee-name");
const hourlyWageInput = document.querySelector("#hourly-wage");
const standardWorkHoursInput = document.querySelector("#standard-work-hours");
const breakMinutesInput = document.querySelector("#break-minutes");
const dateInput = document.querySelector("#work-date");
const monthSelect = document.querySelector("#target-month");
const startInput = document.querySelector("#start-time");
const endInput = document.querySelector("#end-time");
const errorMessage = document.querySelector("#form-error");
const saveStatus = document.querySelector("#save-status");

let currentSettings = null;

function minutesFromTime(value) {
  const [hours, minutes] = value.split(":").map(Number);
  return hours * 60 + minutes;
}

function formatDuration(totalMinutes) {
  const hours = Math.floor(totalMinutes / 60);
  const minutes = totalMinutes % 60;
  return minutes === 0
    ? `${hours}<span>時間</span>`
    : `${hours}<span>時間</span>${minutes}<span>分</span>`;
}

function showSummary(workMinutes, overtimeMinutes) {
  document.querySelector("#work-hours").innerHTML =
    formatDuration(workMinutes);
  document.querySelector("#overtime-hours").innerHTML =
    formatDuration(overtimeMinutes);
}

function showActiveSettings(settings) {
  const text = settings.employeeName
    ? `${settings.employeeName}さん・休憩${settings.breakMinutes}分・標準${settings.standardWorkMinutes / 60}時間`
    : "先に基本設定を保存してください";
  document.querySelector("#active-settings").textContent = text;
}

function refreshSummary() {
  if (!currentSettings || !currentSettings.employeeName) {
    errorMessage.textContent = "先に基本設定を保存してください。";
    errorMessage.hidden = false;
    return false;
  }

  const elapsedMinutes =
    minutesFromTime(endInput.value) - minutesFromTime(startInput.value);
  const workMinutes = elapsedMinutes - currentSettings.breakMinutes;

  if (elapsedMinutes <= 0) {
    errorMessage.textContent =
      "退勤時刻は出勤時刻より後にしてください。";
    errorMessage.hidden = false;
    return false;
  }

  if (workMinutes < 0) {
    errorMessage.textContent =
      "勤務時間は休憩時間より長くしてください。";
    errorMessage.hidden = false;
    return false;
  }

  errorMessage.hidden = true;
  showSummary(
    workMinutes,
    Math.max(0, workMinutes - currentSettings.standardWorkMinutes)
  );
  return true;
}

async function loadSettings() {
  const response = await fetch("/api/settings");
  const settings = await response.json();

  if (!response.ok) {
    throw new Error(settings.message || "設定の読み込みに失敗しました。");
  }

  currentSettings = settings;
  employeeNameInput.value = settings.employeeName;
  hourlyWageInput.value = settings.hourlyWage;
  standardWorkHoursInput.value = settings.standardWorkMinutes / 60;
  breakMinutesInput.value = settings.breakMinutes;
  showActiveSettings(settings);

  if (settings.employeeName) {
    refreshSummary();
  }
}

settingsForm.addEventListener("submit", async (event) => {
  event.preventDefault();

  const settingsError = document.querySelector("#settings-error");
  const settingsStatus = document.querySelector("#settings-status");
  settingsError.hidden = true;
  settingsStatus.hidden = true;

  try {
    const response = await fetch("/api/settings", {
      method: "PUT",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({
        employeeName: employeeNameInput.value.trim(),
        hourlyWage: Number(hourlyWageInput.value),
        standardWorkMinutes:
          Math.round(Number(standardWorkHoursInput.value) * 60),
        breakMinutes: Number(breakMinutesInput.value)
      })
    });

    const result = await response.json();
    if (!response.ok) {
      throw new Error(result.message || "設定の保存に失敗しました。");
    }

    currentSettings = result;
    showActiveSettings(result);
    settingsStatus.textContent = "✓ 基本設定を保存しました。";
    settingsStatus.hidden = false;
    refreshSummary();
  } catch (error) {
    settingsError.textContent = error.message;
    settingsError.hidden = false;
  }
});

function updateMonthRange() {
  const today = new Date();
  const selectedMonth = new Date(
    today.getFullYear(),
    today.getMonth() - (monthSelect.value === "previous" ? 1 : 0),
    1
  );
  const year = selectedMonth.getFullYear();
  const month = String(selectedMonth.getMonth() + 1).padStart(2, "0");
  const finalDay = new Date(year, selectedMonth.getMonth() + 1, 0).getDate();

  dateInput.min = `${year}-${month}-01`;
  dateInput.max = `${year}-${month}-${finalDay}`;

  if (!dateInput.value.startsWith(`${year}-${month}`)) {
    const day = monthSelect.value === "current"
      ? Math.min(today.getDate(), finalDay)
      : finalDay;
    dateInput.value =
      `${year}-${month}-${String(day).padStart(2, "0")}`;
  }
}

monthSelect.addEventListener("change", updateMonthRange);
startInput.addEventListener("change", refreshSummary);
endInput.addEventListener("change", refreshSummary);
updateMonthRange();

attendanceForm.addEventListener("submit", async (event) => {
  event.preventDefault();

  if (!refreshSummary()) {
    return;
  }

  saveStatus.hidden = true;

  try {
    const response = await fetch("/api/attendances", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({
        employeeName: currentSettings.employeeName,
        workDate: dateInput.value,
        startTime: startInput.value,
        endTime: endInput.value
      })
    });

    const result = await response.json();
    if (!response.ok) {
      throw new Error(result.message || "勤怠の登録に失敗しました。");
    }

    showSummary(result.workMinutes, result.overtimeMinutes);
    errorMessage.hidden = true;
    saveStatus.textContent = `✓ ${result.message}`;
    saveStatus.hidden = false;
  } catch (error) {
    errorMessage.textContent = error.message;
    errorMessage.hidden = false;
  }
});

loadSettings().catch((error) => {
  const settingsError = document.querySelector("#settings-error");
  settingsError.textContent = error.message;
  settingsError.hidden = false;
});
