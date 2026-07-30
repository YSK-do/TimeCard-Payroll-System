const keyboardAttendanceForm = document.querySelector("#attendance-form");
const keyboardDateInput = document.querySelector("#work-date");
const keyboardStartInput = document.querySelector("#start-time");
const keyboardEndInput = document.querySelector("#end-time");
const keyboardBreakInputs = [...document.querySelectorAll('input[name="tookBreak"]')];
const keyboardSubmitButton = keyboardAttendanceForm.querySelector('button[type="submit"]');

const LAST_START_TIME_KEY = "timecard.lastStartTime";
const LAST_END_TIME_KEY = "timecard.lastEndTime";
const digitBuffers = new WeakMap();

function normalizeTimeDigits(value) {
  const digits = value.replace(/\D/g, "");
  if (digits.length !== 4) {
    return null;
  }

  const hours = Number(digits.slice(0, 2));
  const minutes = Number(digits.slice(2));
  if (hours > 23 || minutes > 59) {
    return null;
  }

  return `${digits.slice(0, 2)}:${digits.slice(2)}`;
}

function rememberTimes() {
  if (keyboardStartInput.value) {
    localStorage.setItem(LAST_START_TIME_KEY, keyboardStartInput.value);
  }
  if (keyboardEndInput.value) {
    localStorage.setItem(LAST_END_TIME_KEY, keyboardEndInput.value);
  }
}

function restoreTimes() {
  const lastStartTime = localStorage.getItem(LAST_START_TIME_KEY);
  const lastEndTime = localStorage.getItem(LAST_END_TIME_KEY);

  if (lastStartTime) {
    keyboardStartInput.value = lastStartTime;
  }
  if (lastEndTime) {
    keyboardEndInput.value = lastEndTime;
  }
}

function handleFourDigitTimeInput(input, event) {
  if (!/^\d$/.test(event.key)) {
    return false;
  }

  event.preventDefault();
  const previousDigits = digitBuffers.get(input) || "";
  const nextDigits = `${previousDigits}${event.key}`.slice(-4);
  digitBuffers.set(input, nextDigits);

  if (nextDigits.length === 4) {
    const normalizedTime = normalizeTimeDigits(nextDigits);
    if (normalizedTime) {
      input.value = normalizedTime;
      input.dispatchEvent(new Event("change", { bubbles: true }));
      digitBuffers.set(input, "");

      if (input === keyboardStartInput) {
        keyboardEndInput.focus();
      }
    }
  }

  return true;
}

function moveFocusOnEnter(input, nextElement) {
  input.addEventListener("keydown", (event) => {
    if (handleFourDigitTimeInput(input, event)) {
      return;
    }

    if (event.key === "Backspace" || event.key === "Delete") {
      digitBuffers.set(input, "");
      return;
    }

    if (event.key === "Enter") {
      event.preventDefault();
      digitBuffers.set(input, "");
      nextElement.focus();
    }
  });
}

restoreTimes();

keyboardDateInput.addEventListener("keydown", (event) => {
  if (event.key === "Enter") {
    event.preventDefault();
    keyboardStartInput.focus();
  }
});

moveFocusOnEnter(keyboardStartInput, keyboardEndInput);
moveFocusOnEnter(keyboardEndInput, keyboardBreakInputs[0]);

keyboardBreakInputs.forEach((input) => {
  input.addEventListener("keydown", (event) => {
    if (event.key === "Enter") {
      event.preventDefault();
      input.checked = true;
      keyboardSubmitButton.focus();
    }
  });
});

keyboardStartInput.addEventListener("change", rememberTimes);
keyboardEndInput.addEventListener("change", rememberTimes);
keyboardAttendanceForm.addEventListener("submit", rememberTimes);
