const breakQuestion = document.querySelector(".break-check");
const breakVisibilityInput = document.querySelector("#show-break-question");
const breakAnswerInputs = document.querySelectorAll('input[name="tookBreak"]');

const BREAK_VISIBILITY_KEY = "timecard.showBreakQuestion";
const BREAK_ANSWER_KEY = "timecard.lastBreakAnswer";

function applyBreakQuestionVisibility() {
  const shouldShow = breakVisibilityInput.checked;
  breakQuestion.hidden = !shouldShow;
  localStorage.setItem(BREAK_VISIBILITY_KEY, String(shouldShow));

  breakAnswerInputs.forEach((input) => {
    input.required = false;
  });
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

window.showBreakMessage = function () {
  const selectedBreak = document.querySelector(
    'input[name="tookBreak"]:checked'
  );
  const breakCardElement = document.querySelector("#break-card");
  const breakMessageElement = document.querySelector("#break-message");

  if (!selectedBreak || !breakVisibilityInput.checked) {
    breakCardElement.hidden = true;
    return;
  }

  localStorage.setItem(BREAK_ANSWER_KEY, selectedBreak.value);

  breakMessageElement.textContent = selectedBreak.value === "yes"
    ? "素晴らしい！体調管理バッチリですね。明日もその調子で！"
    : "今日もお疲れ様でした。無理しすぎず、ゆっくり休んでくださいね。";
  breakCardElement.hidden = false;
};

breakVisibilityInput.addEventListener("change", applyBreakQuestionVisibility);

breakAnswerInputs.forEach((input) => {
  input.addEventListener("change", () => {
    localStorage.setItem(BREAK_ANSWER_KEY, input.value);
  });
});

restoreBreakPreference();
