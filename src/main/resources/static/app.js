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
  return minutes === 0 ? `${hours}<span>時間</span>` : `${hours}<span>時間</span>${minutes}<span>分</span>`;
}

function refreshSummary() {
  const elapsedMinutes = minutesFromTime(endInput.value) - minutesFromTime(startInput.value);
  const workMinutes = elapsedMinutes - 60;

  if (workMinutes < 0) {
    errorMessage.textContent = "退勤時刻は出勤時刻より後にしてください。";
    errorMessage.hidden = false;
    return false;
  }

  errorMessage.hidden = true;
  document.querySelector("#work-hours").innerHTML = formatDuration(workMinutes);
  document.querySelector("#overtime-hours").innerHTML = formatDuration(Math.max(0, workMinutes - 480));
  return true;
}

function updateMonthRange() {
  const today = new Date();
  const selectedMonth = new Date(today.getFullYear(), today.getMonth() - (monthSelect.value === "previous" ? 1 : 0), 1);
  const year = selectedMonth.getFullYear();
  const month = String(selectedMonth.getMonth() + 1).padStart(2, "0");
  const finalDay = new Date(year, selectedMonth.getMonth() + 1, 0).getDate();

  dateInput.min = `${year}-${month}-01`;
  dateInput.max = `${year}-${month}-${finalDay}`;
  if (!dateInput.value.startsWith(`${year}-${month}`)) {
    dateInput.value = monthSelect.value === "current"
      ? `${year}-${month}-${String(Math.min(today.getDate(), finalDay)).padStart(2, "0")}`
      : `${year}-${month}-${String(finalDay).padStart(2, "0")}`;
  }
}

monthSelect.addEventListener("change", updateMonthRange);
startInput.addEventListener("change", refreshSummary);
endInput.addEventListener("change", refreshSummary);

updateMonthRange();

form.addEventListener("submit", (event) => {
  event.preventDefault();
  if (!refreshSummary()) return;

  saveStatus.hidden = false;
  saveStatus.animate(
    [{ opacity: 0, transform: "translateY(-4px)" }, { opacity: 1, transform: "translateY(0)" }],
    { duration: 250, easing: "ease-out" }
  );
});
