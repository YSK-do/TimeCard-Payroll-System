const breakQuestion = document.querySelector(".break-check");
const breakVisibilityInput = document.querySelector("#show-break-question");
const breakAnswerInputs = document.querySelectorAll('input[name="tookBreak"]');

const BREAK_VISIBILITY_KEY = "timecard.showBreakQuestion";
const BREAK_ANSWER_KEY = "timecard.lastBreakAnswer";

function applyBreakQuestionVisibility() {
  const shouldShow = breakVisibilityInput.checked;
  breakQuestion.hidden = !shouldShow;
  localStorage.setItem(BREAK_VISIBILITY_KEY, String(shouldShow));

  if (!shouldShow) {
    breakAnswerInputs.forEach((input) => {
      input.required = false;
    });
  }
}

function restoreBreakPreference() {
  const savedVisibility = localStorage.getItem(BREAK_VISIBILITY_KEY);
  breakVisibilityInput.checked = savedVisibility !== "false";

  const savedAnswer = localStorage.getItem(BREAK_ANSWER_KEY);
  if (savedAnswer) {
    const input = document.querySelector(
      `input[name="tookBreak"][value="${savedAnswer}"]`
    );
    if (input) {
      input.checked = true;
    }
  }

  applyBreakQuestionVisibility();
}

breakVisibilityInput.addEventListener("change", applyBreakQuestionVisibility);

breakAnswerInputs.forEach((input) => {
  input.addEventListener("change", () => {
    localStorage.setItem(BREAK_ANSWER_KEY, input.value);
  });
});

restoreBreakPreference();
