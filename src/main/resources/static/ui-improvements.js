const settingsDetails = document.querySelector(".settings-card details");
const settingsAction = document.querySelector(".summary-action");
const settingsFormPanel = document.querySelector("#settings-form");
const pickerInputs = document.querySelectorAll(
  '#attendance-form input[type="date"], #attendance-form input[type="time"]'
);

function updateSettingsToggleLabel() {
  if (!settingsDetails || !settingsAction) {
    return;
  }

  settingsAction.textContent = settingsDetails.open
    ? "▲ 基本設定を閉じる"
    : "▼ 基本設定を開く";
}

function animateSettingsPanel() {
  if (!settingsDetails?.open || !settingsFormPanel) {
    return;
  }

  settingsFormPanel.animate(
    [
      { opacity: 0, transform: "translateY(-6px)" },
      { opacity: 1, transform: "translateY(0)" }
    ],
    { duration: 180, easing: "ease-out" }
  );
}

if (settingsDetails) {
  updateSettingsToggleLabel();
  settingsDetails.addEventListener("toggle", () => {
    updateSettingsToggleLabel();
    animateSettingsPanel();
  });
}

pickerInputs.forEach((input) => {
  input.addEventListener(
    "wheel",
    (event) => {
      if (document.activeElement === input) {
        event.preventDefault();
      }
    },
    { passive: false }
  );
});
