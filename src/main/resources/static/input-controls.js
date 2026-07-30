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
