const form = document.querySelector("#attendance-form");
const dateInput = document.querySelector("#work-date");
const monthSelect = document.querySelector("#target-month");
const startInput = document.querySelector("#start-time");
const endInput = document.querySelector("#end-time");
const errorMessage = document.querySelector("#form-error");
const saveStatus = document.querySelector("#save-status");

function minutesFromTime(value) {
  const [hours, minutes] = value.split(":").map(Number);
  return hours * 60 + minutes;
}

function formatDuration(totalMinutes) {
  const hours = Math.floor(totalMinutes / 60);
  const minutes = totalMinutes % 60;

  if (minutes === 0) {
    return `${hours}<span>時間</span>`;
  }

  return `${hours}<span>時間</span>${minutes}<span>分</span>`;
}

function showSummary(workMinutes, overtimeMinutes) {
  document.querySelector("#work-hours").innerHTML =
    formatDuration(workMinutes);

  document.querySelector("#overtime-hours").innerHTML =
    formatDuration(overtimeMinutes);
}

function refreshSummary() {
  const elapsedMinutes =
    minutesFromTime(endInput.value)
    - minutesFromTime(startInput.value);

  const workMinutes = elapsedMinutes - 60;

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
    Math.max(0, workMinutes - 480)
  );

  return true;
}

function updateMonthRange() {
  const today = new Date();

  const selectedMonth = new Date(
    today.getFullYear(),
    today.getMonth()
      - (monthSelect.value === "previous" ? 1 : 0),
    1
  );

  const year = selectedMonth.getFullYear();

  const month = String(
    selectedMonth.getMonth() + 1
  ).padStart(2, "0");

  const finalDay = new Date(
    year,
    selectedMonth.getMonth() + 1,
    0
  ).getDate();

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

form.addEventListener("submit", async (event) => {
  event.preventDefault();

  if (!refreshSummary()) {
    return;
  }

  saveStatus.hidden = true;

  try {
    const response = await fetch("/api/attendances", {
      method: "POST",
      headers: {
        "Content-Type": "application/json"
      },
      body: JSON.stringify({
        workDate: dateInput.value,
        startTime: startInput.value,
        endTime: endInput.value
      })
    });

    const result = await response.json();

    if (!response.ok) {
      throw new Error(
        result.message || "勤怠の登録に失敗しました。"
      );
    }

    showSummary(
      result.workMinutes,
      result.overtimeMinutes
    );

    errorMessage.hidden = true;
    saveStatus.textContent = `✓ ${result.message}`;
    saveStatus.hidden = false;

  } catch (error) {
    errorMessage.textContent = error.message;
    errorMessage.hidden = false;
  }
});