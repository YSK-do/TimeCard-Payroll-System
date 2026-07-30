document.querySelectorAll("[data-number-control]").forEach((control) => {
  const input = control.querySelector('input[type="number"]');
  const decreaseButton = control.querySelector('[data-action="decrease"]');
  const increaseButton = control.querySelector('[data-action="increase"]');

  if (!input || !decreaseButton || !increaseButton) {
    return;
  }

  function changeValue(direction) {
    if (direction > 0) {
      input.stepUp();
    } else {
      input.stepDown();
    }

    input.dispatchEvent(new Event("input", { bubbles: true }));
    input.focus();
  }

  decreaseButton.addEventListener("click", () => changeValue(-1));
  increaseButton.addEventListener("click", () => changeValue(1));
});

const mobileDevice = window.matchMedia("(pointer: coarse)").matches;

if (mobileDevice) {
  document.querySelectorAll('input[type="date"], input[type="time"]').forEach((input) => {
    input.addEventListener("click", () => {
      if (typeof input.showPicker === "function") {
        input.showPicker();
      }
    });
  });
}

const calendarDateInput = document.querySelector("#work-date");
const calendarMonthSelect = document.querySelector("#target-month");
const calendarStartInput = document.querySelector("#start-time");
const calendarSaveStatus = document.querySelector("#save-status");
const calendarSettingsStatus = document.querySelector("#settings-status");

if (calendarDateInput && calendarMonthSelect) {
  const dateField = calendarDateInput.closest(".field");
  const calendarSection = document.createElement("section");
  calendarSection.className = "work-calendar";
  calendarSection.setAttribute("aria-labelledby", "work-calendar-title");
  calendarSection.innerHTML = `
    <div class="work-calendar-heading">
      <div>
        <strong id="work-calendar-title">勤務日を選択</strong>
        <span id="work-calendar-month"></span>
      </div>
      <div class="work-calendar-legend" aria-label="カレンダーの凡例">
        <span><i class="legend-today"></i>今日</span>
        <span><i class="legend-registered"></i>登録済み</span>
      </div>
    </div>
    <div class="work-calendar-weekdays" aria-hidden="true">
      <span>日</span><span>月</span><span>火</span><span>水</span><span>木</span><span>金</span><span>土</span>
    </div>
    <div id="work-calendar-grid" class="work-calendar-grid"></div>
    <p id="work-calendar-status" class="work-calendar-status" aria-live="polite"></p>
  `;
  dateField.parentElement.insertBefore(calendarSection, dateField);

  const monthLabel = calendarSection.querySelector("#work-calendar-month");
  const calendarGrid = calendarSection.querySelector("#work-calendar-grid");
  const calendarStatus = calendarSection.querySelector("#work-calendar-status");

  function selectedMonthText() {
    if (calendarDateInput.min) {
      return calendarDateInput.min.slice(0, 7);
    }
    return calendarDateInput.value.slice(0, 7);
  }

  async function loadRegisteredDates(monthText) {
    try {
      const response = await fetch(
        `/api/attendances/dates?month=${encodeURIComponent(monthText)}`
      );
      if (!response.ok) {
        return new Set();
      }
      return new Set(await response.json());
    } catch (error) {
      return new Set();
    }
  }

  async function renderWorkCalendar() {
    const monthText = selectedMonthText();
    if (!monthText) {
      return;
    }

    const [year, month] = monthText.split("-").map(Number);
    const firstDay = new Date(year, month - 1, 1);
    const finalDay = new Date(year, month, 0).getDate();
    const firstWeekday = firstDay.getDay();
    const todayText = new Date().toLocaleDateString("sv-SE");
    const registeredDates = await loadRegisteredDates(monthText);

    monthLabel.textContent = `${year}年${month}月`;
    calendarGrid.replaceChildren();

    for (let index = 0; index < firstWeekday; index += 1) {
      const blank = document.createElement("span");
      blank.className = "calendar-blank";
      calendarGrid.appendChild(blank);
    }

    for (let day = 1; day <= finalDay; day += 1) {
      const dateText = `${monthText}-${String(day).padStart(2, "0")}`;
      const button = document.createElement("button");
      button.type = "button";
      button.className = "calendar-day";
      button.textContent = day;
      button.setAttribute("aria-label", `${year}年${month}月${day}日`);

      if (dateText === todayText) {
        button.classList.add("is-today");
        button.setAttribute("aria-current", "date");
      }
      if (registeredDates.has(dateText)) {
        button.classList.add("is-registered");
        button.setAttribute("aria-label", `${button.getAttribute("aria-label")} 登録済み`);
      }
      if (dateText === calendarDateInput.value) {
        button.classList.add("is-selected");
      }

      button.addEventListener("click", () => {
        calendarDateInput.value = dateText;
        calendarDateInput.dispatchEvent(new Event("change", { bubbles: true }));
        calendarStatus.textContent = `${year}年${month}月${day}日を選択しました。`;
        renderWorkCalendar();
        if (calendarStartInput) {
          calendarStartInput.focus();
        }
      });

      calendarGrid.appendChild(button);
    }
  }

  calendarMonthSelect.addEventListener("change", () => {
    window.setTimeout(renderWorkCalendar, 0);
  });
  calendarDateInput.addEventListener("change", renderWorkCalendar);

  [calendarSaveStatus, calendarSettingsStatus].forEach((statusElement) => {
    if (!statusElement) {
      return;
    }
    new MutationObserver(() => {
      if (!statusElement.hidden) {
        renderWorkCalendar();
      }
    }).observe(statusElement, {
      attributes: true,
      childList: true,
      subtree: true
    });
  });

  window.setTimeout(renderWorkCalendar, 0);
}
