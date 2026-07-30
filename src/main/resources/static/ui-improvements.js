const settingsDetails = document.querySelector(".settings-card details");
const settingsAction = document.querySelector(".summary-action");
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

if (settingsDetails) {
  updateSettingsToggleLabel();
  settingsDetails.addEventListener("toggle", updateSettingsToggleLabel);
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
