// ═══════════════════════════════════════════════════════════════════════════
// Экран пополнения брокерского счёта
// ═══════════════════════════════════════════════════════════════════════════
const scrDeposit = androidScreen("Пополнение счёта");
statusStrip(scrDeposit);

appendInstance(scrDeposit, hdrDepositComp);

const bodyDeposit = scrollBody(scrDeposit);

// ─── Сумма пополнения ─────────────────────────────────────────────────────
bodyDeposit.appendChild(txt("Сумма пополнения", 14, "semi", c.onSurface, "Секция"));
bodyDeposit.children[bodyDeposit.children.length - 1].layoutSizingHorizontal = "HUG";
bodyDeposit.children[bodyDeposit.children.length - 1].layoutSizingVertical = "HUG";

// Поле ввода суммы
const amountField = figma.createFrame();
amountField.name = "Поле · Сумма";
amountField.layoutMode = "VERTICAL";
amountField.primaryAxisSizingMode = "AUTO";
amountField.counterAxisSizingMode = "FIXED";
amountField.resize(W - PAD * 2, 1);
amountField.paddingLeft = 16; amountField.paddingRight = 16;
amountField.paddingTop = 14; amountField.paddingBottom = 14;
amountField.cornerRadius = shape.medium;
amountField.fills = tokenFill("surfaceContainer");
amountField.strokes = [{ type: "SOLID", color: c.primary }];
amountField.strokeWeight = 2;
amountField.itemSpacing = 4;

amountField.appendChild(txt("Сумма, ₽", 12, "reg", c.primary, "Лейбл"));
amountField.children[0].layoutSizingHorizontal = "HUG";
amountField.children[0].layoutSizingVertical = "HUG";

amountField.appendChild(txt("10 000", 20, "semi", c.onSurface, "Значение"));
amountField.children[1].layoutSizingHorizontal = "FILL";
amountField.children[1].layoutSizingVertical = "HUG";

bodyDeposit.appendChild(amountField);
amountField.layoutSizingHorizontal = "FILL";
amountField.layoutSizingVertical = "HUG";
created.push(amountField.id);

// ─── Быстрый выбор суммы ──────────────────────────────────────────────────
const quickRow = figma.createFrame();
quickRow.name = "Быстрый выбор";
quickRow.layoutMode = "HORIZONTAL";
quickRow.primaryAxisSizingMode = "AUTO";
quickRow.counterAxisSizingMode = "AUTO";
quickRow.itemSpacing = 8;
quickRow.fills = [];

for (const amount of ["1 000 ₽", "5 000 ₽", "10 000 ₽", "50 000 ₽"]) {
  const chip = figma.createFrame();
  chip.name = amount;
  chip.layoutMode = "HORIZONTAL";
  chip.primaryAxisAlignItems = "CENTER";
  chip.counterAxisAlignItems = "CENTER";
  chip.primaryAxisSizingMode = "AUTO";
  chip.counterAxisSizingMode = "AUTO";
  chip.paddingLeft = 12; chip.paddingRight = 12;
  chip.paddingTop = 8; chip.paddingBottom = 8;
  chip.cornerRadius = shape.full;
  chip.fills = tokenFill("primaryContainer");
  chip.strokes = [{ type: "SOLID", color: c.outline }];
  chip.strokeWeight = 1;
  chip.appendChild(txt(amount, 13, "med", c.primary, "Текст"));
  chip.children[0].layoutSizingHorizontal = "HUG";
  chip.children[0].layoutSizingVertical = "HUG";
  quickRow.appendChild(chip);
  chip.layoutSizingHorizontal = "HUG";
  chip.layoutSizingVertical = "HUG";
}

bodyDeposit.appendChild(quickRow);
quickRow.layoutSizingHorizontal = "HUG";
quickRow.layoutSizingVertical = "HUG";
created.push(quickRow.id);

// ─── Текущий баланс ───────────────────────────────────────────────────────
const balRow = figma.createFrame();
balRow.name = "Текущий баланс";
balRow.layoutMode = "HORIZONTAL";
balRow.fills = [];
balRow.primaryAxisSizingMode = "AUTO";
balRow.counterAxisSizingMode = "AUTO";
balRow.counterAxisAlignItems = "CENTER";
balRow.itemSpacing = 8;

balRow.appendChild(txt("Текущий баланс:", 13, "reg", c.onSurfaceVariant, "Лейбл"));
balRow.children[0].layoutSizingHorizontal = "HUG";
balRow.children[0].layoutSizingVertical = "HUG";

balRow.appendChild(txt("45 320 ₽", 13, "semi", c.onSurface, "Значение"));
balRow.children[1].layoutSizingHorizontal = "HUG";
balRow.children[1].layoutSizingVertical = "HUG";

bodyDeposit.appendChild(balRow);
balRow.layoutSizingHorizontal = "FILL";
balRow.layoutSizingVertical = "HUG";
created.push(balRow.id);

// ─── Кнопка подтверждения ────────────────────────────────────────────────
const confirmDepBtn = figma.createFrame();
confirmDepBtn.name = "Кнопка · Пополнить";
confirmDepBtn.layoutMode = "HORIZONTAL";
confirmDepBtn.primaryAxisAlignItems = "CENTER";
confirmDepBtn.counterAxisAlignItems = "CENTER";
confirmDepBtn.primaryAxisSizingMode = "FIXED";
confirmDepBtn.counterAxisSizingMode = "FIXED";
confirmDepBtn.resize(W - PAD * 2, mh.button);
confirmDepBtn.cornerRadius = shape.full;
confirmDepBtn.fills = tokenFill("primary");
confirmDepBtn.effects = shadowCard;
confirmDepBtn.appendChild(txt("Пополнить · 10 000 ₽", 14, "med", c.onPrimary, "Текст"));
confirmDepBtn.children[0].layoutSizingHorizontal = "HUG";
confirmDepBtn.children[0].layoutSizingVertical = "HUG";
bodyDeposit.appendChild(confirmDepBtn);
confirmDepBtn.layoutSizingHorizontal = "FILL";
confirmDepBtn.layoutSizingVertical = "FIXED";
created.push(confirmDepBtn.id);

// Подсказка
bodyDeposit.appendChild(txt(
  "Средства поступят на счёт мгновенно и будут доступны для торговли.",
  11, "reg", c.onSurfaceVariant, "Подсказка"
));
bodyDeposit.children[bodyDeposit.children.length - 1].layoutSizingHorizontal = "FILL";
bodyDeposit.children[bodyDeposit.children.length - 1].layoutSizingVertical = "HUG";

appendInstance(scrDeposit, nav1);
scrDeposit.children[scrDeposit.children.length - 1].layoutSizingHorizontal = "FILL";
scrDeposit.children[scrDeposit.children.length - 1].layoutSizingVertical = "HUG";

// Состояние «после пополнения» со снекбаром
{
  const scrDepositDone = scrDeposit.clone();
  scrDepositDone.name = `Android Compact ${W}×${H} — Пополнение · Успех`;
  screensRow.appendChild(scrDepositDone);
  scrDepositDone.layoutSizingHorizontal = "FIXED";
  scrDepositDone.layoutSizingVertical = "HUG";
  created.push(scrDepositDone.id);
  addSnackbar(scrDepositDone, "success", "Счёт пополнен");
}
