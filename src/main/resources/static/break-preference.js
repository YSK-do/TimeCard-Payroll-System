const breakPreferenceToggle = document.querySelector("#show-break-check");
const breakCheckField = document.querySelector("#break-check-field");
const breakAnswerInputs = [...document.querySelectorAll('input[name="tookBreak"]')];
const breakPreferenceForm = document.querySelector("#settings-form");
const breakAttendanceForm = document.querySelector("#attendance-form");
const breakEndTimeInput = document.querySelector("#end-time");
const breakSubmitButton = breakAttendanceForm.querySelector('button[type="submit"]');
const optionalBreakCard = document.querySelector("#break-card");

const SHOW_BREAK_CHECK_KEY = "timecard.showBreakCheck";
const LAST_BREAK_ANSWER_KEY = "timecard.lastBreakAnswer";

const preferenceStyle = document.createElement("style");
preferenceStyle.textContent = `
  .preference-toggle { display:flex; align-items:flex-start; gap:12px; padding:14px; background:#f8fbfd; border:1px solid #dce7ef; border-radius:8px; cursor:pointer; }
  .preference-toggle input { width:20px; height:20px; flex:none; margin:1px 0 0; padding:0; }
  .preference-toggle span { display:flex; flex-direction:column; gap:4px; }
  .preference-toggle strong { color:var(--text); font-size:13px; }
  .preference-toggle small, .break-purpose { color:var(--muted); font-size:11px; font-weight:400; line-height:1.5; }
  .break-purpose { margin:0 0 10px; }
  .optional-label { margin-left:6px; padding:2px 6px; color:var(--blue); background:var(--blue-soft); border-radius:10px; font-size:10px; font-weight:500; }
`;
document.head.appendChild(preferenceStyle);

function applyBreakCheckVisibility() {
  const shouldShow = breakPreferenceToggle.checked;
  breakCheckField.hidden = !shouldShow;
  localStorage.setItem(SHOW_BREAK_CHECK_KEY, String(shouldShow));

  if (!shouldShow) {
    optionalBreakCard.hidden = true;
  }
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

breakEndTimeInput.addEventListener("keydown", (event) => {
  if (event.key === "Enter" && breakCheckField.hidden) {
    queueMicrotask(() => breakSubmitButton.focus());
  }
});

breakAttendanceForm.addEventListener("submit", () => {
  const selectedAnswer = breakAnswerInputs.find((input) => input.checked);
  if (selectedAnswer) {
    localStorage.setItem(LAST_BREAK_ANSWER_KEY, selectedAnswer.value);
  }

  if (breakCheckField.hidden) {
    const observer = new MutationObserver(() => {
      optionalBreakCard.hidden = true;
    });
    observer.observe(optionalBreakCard, { attributes: true, attributeFilter: ["hidden"] });
    setTimeout(() => observer.disconnect(), 3000);
  }
});

restoreBreakPreference();