const breakPreferenceToggle = document.querySelector("#show-break-check");
const breakCheckField = document.querySelector("#break-check-field");
const breakAnswerInputs = [...document.querySelectorAll('input[name="tookBreak"]')];
const breakPreferenceForm = document.querySelector("#settings-form");
const breakAttendanceForm = document.querySelector("#attendance-form");

const SHOW_BREAK_CHECK_KEY = "timecard.showBreakCheck";
const LAST_BREAK_ANSWER_KEY = "timecard.lastBreakAnswer";

function applyBreakCheckVisibility() {
  const shouldShow = breakPreferenceToggle.checked;
  breakCheckField.hidden = !shouldShow;
  localStorage.setItem(SHOW_BREAK_CHECK_KEY, String(shouldShow));
}

function restoreBreakPreference() {
  const savedVisibility = localStorage.getItem(SHOW_BREAK_CHECK_KEY);
  breakPreferenceToggle.checked = savedVisibility !== "false";

  const savedAnswer = localStorage.getItem(LAST_BREAK_ANSWER_KEY) || "yes";
  const matchingInput = breakAnswerInputs.find((input) => input.value === savedAnswer);
  if (matchingInput) {
    matchingInput.checked = true;
  }

  applyBreakCheckVisibility();
}

breakPreferenceToggle.addEventListener("change", applyBreakCheckVisibility);

breakAnswerInputs.forEach((input) => {
  input.addEventListener("change", () => {
    if (input.checked) {
      localStorage.setItem(LAST_BREAK_ANSWER_KEY, input.value);
    }
  });
});

breakPreferenceForm.addEventListener("submit", applyBreakCheckVisibility);

breakAttendanceForm.addEventListener("submit", () => {
  const selectedAnswer = breakAnswerInputs.find((input) => input.checked);
  if (selectedAnswer) {
    localStorage.setItem(LAST_BREAK_ANSWER_KEY, selectedAnswer.value);
  }
});

restoreBreakPreference();