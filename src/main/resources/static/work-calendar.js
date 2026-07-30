const workCalendar = document.querySelector("#work-calendar");
const workCalendarTitle = document.querySelector("#work-calendar-title");
const workDateInput = document.querySelector("#work-date");
const targetMonthSelect = document.querySelector("#target-month");
const attendanceFormForCalendar = document.querySelector("#attendance-form");
const saveStatusForCalendar = document.querySelector("#save-status");
const registeredDatesKey = "timecardRegisteredDates";

function loadRegisteredDates() {
  try {
    return new Set(JSON.parse(localStorage.getItem(registeredDatesKey) || "[]"));
  } catch (error) {
    return new Set();
  }
}

function saveRegisteredDate(date) {
  const registeredDates = loadRegisteredDates();
  registeredDates.add(date);
  localStorage.setItem(registeredDatesKey, JSON.stringify([...registeredDates]));
}

function getDisplayedYearMonth() {
  const source = workDateInput.value || workDateInput.min;
  return source ? source.slice(0, 7) : new Date().toISOString().slice(0, 7);
}

function formatDate(year, month, day) {
  return `${year}-${String(month).padStart(2, "0")}-${String(day).padStart(2, "0")}`;
}

function renderWorkCalendar() {
  if (!workCalendar || !workCalendarTitle) {
    return;
  }

  const [year, month] = getDisplayedYearMonth().split("-").map(Number);
  const firstWeekday = new Date(year, month - 1, 1).getDay();
  const finalDay = new Date(year, month, 0).getDate();
  const today = new Date();
  const todayText = formatDate(today.getFullYear(), today.getMonth() + 1, today.getDate());
  const registeredDates = loadRegisteredDates();

  workCalendarTitle.textContent = `${year}年${month}月`;
  workCalendar.innerHTML = "";

  ["日", "月", "火", "水", "木", "金", "土"].forEach((weekday) => {
    const label = document.createElement("span");
    label.className = "calendar-weekday";
    label.textContent = weekday;
    workCalendar.appendChild(label);
  });

  for (let index = 0; index < firstWeekday; index += 1) {
    const blank = document.createElement("span");
    blank.className = "calendar-blank";
    workCalendar.appendChild(blank);
  }

  for (let day = 1; day <= finalDay; day += 1) {
    const date = formatDate(year, month, day);
    const button = document.createElement("button");
    button.type = "button";
    button.className = "calendar-day";
    button.textContent = day;
    button.dataset.date = date;
    button.setAttribute("aria-label", `${month}月${day}日を選択`);

    if (date === todayText) {
      button.classList.add("is-today");
      button.title = "今日";
    }
    if (date === workDateInput.value) {
      button.classList.add("is-selected");
    }
    if (registeredDates.has(date)) {
      button.classList.add("is-registered");
      button.title = button.title ? `${button.title}・登録済み` : "登録済み";
    }

    button.addEventListener("click", () => {
      workDateInput.value = date;
      workDateInput.dispatchEvent(new Event("change", { bubbles: true }));
      renderWorkCalendar();
      document.querySelector("#start-time")?.focus();
    });

    workCalendar.appendChild(button);
  }
}

workDateInput?.addEventListener("change", renderWorkCalendar);
targetMonthSelect?.addEventListener("change", () => setTimeout(renderWorkCalendar, 0));

if (attendanceFormForCalendar && saveStatusForCalendar) {
  const observer = new MutationObserver(() => {
    if (!saveStatusForCalendar.hidden && saveStatusForCalendar.textContent.includes("✓")) {
      saveRegisteredDate(workDateInput.value);
      renderWorkCalendar();
    }
  });
  observer.observe(saveStatusForCalendar, {
    attributes: true,
    childList: true,
    subtree: true,
    attributeFilter: ["hidden"]
  });
}

setTimeout(renderWorkCalendar, 0);
