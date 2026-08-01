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
const registrationResult = document.querySelector("#registration-result");
const attendanceSubmitButton = attendanceForm.querySelector("button[type='submit']");
const attendanceSubmitButtonText = attendanceSubmitButton.textContent;

let currentSettings = null;
let currentYearMonth = "";
let attendanceButtonResetTimer = null;

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

function formatCurrency(amount) {
  return amount.toLocaleString("ja-JP");
}

function calculatePayroll(workMinutes, overtimeMinutes) {
  const basePay = Math.floor(
    workMinutes * currentSettings.hourlyWage / 60
  );
  const overtimePay = Math.floor(
    overtimeMinutes * currentSettings.hourlyWage * 25 / (60 * 100)
  );
  return {
    basePay,
    overtimePay,
    totalPay: basePay + overtimePay
  };
}

function formatDurationText(totalMinutes) {
  const hours = Math.floor(totalMinutes / 60);
  const minutes = totalMinutes % 60;
  return minutes === 0
    ? `${hours}時間`
    : `${hours}時間${minutes}分`;
}

function showActiveSettings(settings) {
  const text = settings.employeeName
    ? `${settings.employeeName}さん・休憩${settings.breakMinutes}分・標準${settings.standardWorkMinutes / 60}時間`
    : "先に基本設定を保存してください";
  document.querySelector("#active-settings").textContent = text;
}

function validateAttendance() {
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
    await loadMonthlySummary();
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
    await loadMonthlySummary();
  } catch (error) {
    settingsError.textContent = error.message;
    settingsError.hidden = false;
  }
});

function selectedYearMonth() {
  return currentYearMonth;
}

async function loadMonthlySummary() {
  if (!currentSettings || !currentSettings.employeeName) {
    return;
  }

  const monthlyError = document.querySelector("#monthly-error");
  monthlyError.hidden = true;

  try {
    const response = await fetch(
      `/api/attendances/summary?month=${selectedYearMonth()}`
    );
    const result = await response.json();
    if (!response.ok) {
      throw new Error(result.message || "月間集計の読み込みに失敗しました。");
    }

    document.querySelector("#monthly-label").textContent =
      `${result.month} の合計`;
    document.querySelector("#monthly-work-days").innerHTML =
      `${result.workDays}<span>日</span>`;
    document.querySelector("#monthly-work-hours").innerHTML =
      formatDuration(result.workMinutes);
    document.querySelector("#monthly-overtime-hours").innerHTML =
      formatDuration(result.overtimeMinutes);
    document.querySelector("#monthly-base-pay").textContent =
      `${formatCurrency(result.basePay)}円`;
    document.querySelector("#monthly-overtime-pay").textContent =
      `${formatCurrency(result.overtimePay)}円`;
    document.querySelector("#monthly-total-pay").textContent =
      `${formatCurrency(result.totalPay)}円`;
  } catch (error) {
    monthlyError.textContent = error.message;
    monthlyError.hidden = false;
  }
}

function yearMonthFromSelect() {
  const today = new Date();
  const selectedMonth = new Date(
    today.getFullYear(),
    today.getMonth() - (monthSelect.value === "previous" ? 1 : 0),
    1
  );
  const year = selectedMonth.getFullYear();
  const month = String(selectedMonth.getMonth() + 1).padStart(2, "0");
  return `${year}-${month}`;
}

function syncMonthSelect(yearMonth) {
  const today = new Date();
  const currentMonth = `${today.getFullYear()}-${String(today.getMonth() + 1).padStart(2, "0")}`;
  const previousDate = new Date(today.getFullYear(), today.getMonth() - 1, 1);
  const previousMonth = `${previousDate.getFullYear()}-${String(previousDate.getMonth() + 1).padStart(2, "0")}`;

  if (yearMonth === currentMonth) {
    monthSelect.value = "current";
  } else if (yearMonth === previousMonth) {
    monthSelect.value = "previous";
  }
}

function updateMonthRange(yearMonth = yearMonthFromSelect()) {
  currentYearMonth = yearMonth;
  syncMonthSelect(currentYearMonth);

  const [year, month] = currentYearMonth.split("-").map(Number);
  const finalDay = new Date(year, month, 0).getDate();
  const today = new Date();

  dateInput.min = `${currentYearMonth}-01`;
  dateInput.max = `${currentYearMonth}-${String(finalDay).padStart(2, "0")}`;

  if (!dateInput.value.startsWith(currentYearMonth)) {
    const isCurrentMonth = monthSelect.value === "current";
    const day = isCurrentMonth ? Math.min(today.getDate(), finalDay) : finalDay;
    dateInput.value = `${currentYearMonth}-${String(day).padStart(2, "0")}`;
  }
}

monthSelect.addEventListener("change", async () => {
  updateMonthRange(yearMonthFromSelect());
  await loadMonthlySummary();
});

dateInput.addEventListener("change", async () => {
  const selectedMonth = dateInput.value.slice(0, 7);
  if (!selectedMonth || selectedMonth === currentYearMonth) {
    return;
  }

  updateMonthRange(selectedMonth);
  await loadMonthlySummary();
});

updateMonthRange();

attendanceForm.addEventListener("submit", async (event) => {
  event.preventDefault();

  if (!validateAttendance()) {
    return;
  }

  if (attendanceButtonResetTimer) {
    clearTimeout(attendanceButtonResetTimer);
  }
  attendanceSubmitButton.disabled = true;
  attendanceSubmitButton.textContent = "登録中...";
  registrationResult.hidden = true;

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

    errorMessage.hidden = true;
    registrationResult.textContent =
      `登録しました：実働${formatDurationText(result.workMinutes)}・${formatCurrency(result.totalPay)}円`;
    registrationResult.hidden = false;
    attendanceSubmitButton.textContent = "✓ 登録済み";
    await loadMonthlySummary();

    attendanceButtonResetTimer = setTimeout(() => {
      attendanceSubmitButton.textContent = attendanceSubmitButtonText;
      attendanceSubmitButton.disabled = false;
      attendanceButtonResetTimer = null;
    }, 2000);
  } catch (error) {
    errorMessage.textContent = error.message;
    errorMessage.hidden = false;
    attendanceSubmitButton.textContent = attendanceSubmitButtonText;
    attendanceSubmitButton.disabled = false;
  }
});

loadSettings().catch((error) => {
  const settingsError = document.querySelector("#settings-error");
  settingsError.textContent = error.message;
  settingsError.hidden = false;
});
