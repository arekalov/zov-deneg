makeScreensRow("Брокерский счёт");
// ═══════════════════════════════════════════════════════════════════════════
// Брокерский счёт: пополнение и вывод (вкладки, как «Детали / Стакан»)
// ═══════════════════════════════════════════════════════════════════════════

function appendMoneyTabBar(parent, activeIndex) {
  const tabBar = figma.createFrame();
  tabBar.name = "TabBar";
  tabBar.layoutMode = "HORIZONTAL";
  tabBar.primaryAxisSizingMode = "FIXED";
  tabBar.counterAxisSizingMode = "FIXED";
  tabBar.resize(W, 48);
  tabBar.fills = [];
  tabBar.paddingTop = 16;
  tabBar.strokes = [{ type: "SOLID", color: c.outline }];
  tabBar.strokeBottomWeight = 1;
  tabBar.strokeTopWeight = 0;
  tabBar.strokeLeftWeight = 0;
  tabBar.strokeRightWeight = 0;
  parent.appendChild(tabBar);
  tabBar.layoutSizingHorizontal = "FILL";
  tabBar.layoutSizingVertical = "FIXED";

  const labels = ["Пополнение", "Вывод"];
  for (let i = 0; i < 2; i++) {
    const active = i === activeIndex;
    const tab = figma.createFrame();
    tab.name = active ? `Таб · ${labels[i]} (активный)` : `Таб · ${labels[i]}`;
    tab.layoutMode = "VERTICAL";
    tab.primaryAxisAlignItems = "CENTER";
    tab.counterAxisAlignItems = "CENTER";
    tab.fills = [];
    tab.layoutGrow = 1;
    tab.primaryAxisSizingMode = "FIXED";
    tab.counterAxisSizingMode = "AUTO";
    if (active) {
      tab.strokes = [{ type: "SOLID", color: c.primary }];
      tab.strokeBottomWeight = 2;
      tab.strokeTopWeight = 0;
      tab.strokeLeftWeight = 0;
      tab.strokeRightWeight = 0;
    }
    tab.appendChild(txt(labels[i], 14, active ? "semi" : "reg",
      active ? c.primary : c.onSurfaceVariant, "Текст"));
    tab.children[0].layoutSizingHorizontal = "HUG";
    tab.children[0].layoutSizingVertical = "HUG";
    tabBar.appendChild(tab);
    tab.layoutSizingHorizontal = "FILL";
    tab.layoutSizingVertical = "FILL";
  }
}

// ─── Вкладка «Пополнение» ─────────────────────────────────────────────────
{
  const scrDep = androidScreen("Брокерский счёт · Пополнение");
  statusStrip(scrDep);
  appendInstance(scrDep, hdrBrokerAccountComp);
  appendMoneyTabBar(scrDep, 0);
  const bodyDep = scrollBody(scrDep);

  bodyDep.appendChild(txt("Сумма пополнения", 14, "semi", c.onSurface, "Секция"));
  bodyDep.children[bodyDep.children.length - 1].layoutSizingHorizontal = "HUG";
  bodyDep.children[bodyDep.children.length - 1].layoutSizingVertical = "HUG";

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
  bodyDep.appendChild(amountField);
  amountField.layoutSizingHorizontal = "FILL";
  amountField.layoutSizingVertical = "HUG";
  created.push(amountField.id);

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
  bodyDep.appendChild(quickRow);
  quickRow.layoutSizingHorizontal = "HUG";
  quickRow.layoutSizingVertical = "HUG";
  created.push(quickRow.id);

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
  bodyDep.appendChild(balRow);
  balRow.layoutSizingHorizontal = "FILL";
  balRow.layoutSizingVertical = "HUG";
  created.push(balRow.id);

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
  bodyDep.appendChild(confirmDepBtn);
  confirmDepBtn.layoutSizingHorizontal = "FILL";
  confirmDepBtn.layoutSizingVertical = "FIXED";
  created.push(confirmDepBtn.id);

  bodyDep.appendChild(txt(
    "Средства поступят на счёт мгновенно и будут доступны для торговли.",
    11, "reg", c.onSurfaceVariant, "Подсказка"
  ));
  bodyDep.children[bodyDep.children.length - 1].layoutSizingHorizontal = "FILL";
  bodyDep.children[bodyDep.children.length - 1].layoutSizingVertical = "HUG";

  appendInstance(scrDep, nav1);
  scrDep.children[scrDep.children.length - 1].layoutSizingHorizontal = "FILL";
  scrDep.children[scrDep.children.length - 1].layoutSizingVertical = "HUG";

  const scrDepDone = scrDep.clone();
  scrDepDone.name = `Android Compact ${W}×${H} — Брокерский счёт · Пополнение · Успех`;
  screensRow.appendChild(scrDepDone);
  scrDepDone.layoutSizingHorizontal = "FIXED";
  scrDepDone.layoutSizingVertical = "HUG";
  created.push(scrDepDone.id);
  addSnackbar(scrDepDone, "success", "Счёт пополнен");
}

// ─── Вкладка «Вывод» ─────────────────────────────────────────────────────
{
  const scrWd = androidScreen("Брокерский счёт · Вывод");
  statusStrip(scrWd);
  appendInstance(scrWd, hdrBrokerAccountComp);
  appendMoneyTabBar(scrWd, 1);
  const bodyWd = scrollBody(scrWd);

  bodyWd.appendChild(txt("Сумма вывода", 14, "semi", c.onSurface, "Секция"));
  bodyWd.children[bodyWd.children.length - 1].layoutSizingHorizontal = "HUG";
  bodyWd.children[bodyWd.children.length - 1].layoutSizingVertical = "HUG";

  const wdField = figma.createFrame();
  wdField.name = "Поле · Сумма вывода";
  wdField.layoutMode = "VERTICAL";
  wdField.primaryAxisSizingMode = "AUTO";
  wdField.counterAxisSizingMode = "FIXED";
  wdField.resize(W - PAD * 2, 1);
  wdField.paddingLeft = 16; wdField.paddingRight = 16;
  wdField.paddingTop = 14; wdField.paddingBottom = 14;
  wdField.cornerRadius = shape.medium;
  wdField.fills = tokenFill("surfaceContainer");
  wdField.strokes = [{ type: "SOLID", color: c.outline }];
  wdField.strokeWeight = 1;
  wdField.itemSpacing = 4;
  wdField.appendChild(txt("Сумма, ₽", 12, "reg", c.onSurfaceVariant, "Лейбл"));
  wdField.children[0].layoutSizingHorizontal = "HUG";
  wdField.children[0].layoutSizingVertical = "HUG";
  wdField.appendChild(txt("5 000", 20, "semi", c.onSurface, "Значение"));
  wdField.children[1].layoutSizingHorizontal = "FILL";
  wdField.children[1].layoutSizingVertical = "HUG";
  bodyWd.appendChild(wdField);
  wdField.layoutSizingHorizontal = "FILL";
  wdField.layoutSizingVertical = "HUG";
  created.push(wdField.id);

  const quickWd = figma.createFrame();
  quickWd.name = "Быстрый выбор";
  quickWd.layoutMode = "HORIZONTAL";
  quickWd.primaryAxisSizingMode = "AUTO";
  quickWd.counterAxisSizingMode = "AUTO";
  quickWd.itemSpacing = 8;
  quickWd.fills = [];
  for (const amount of ["1 000 ₽", "3 000 ₽", "5 000 ₽", "10 000 ₽"]) {
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
    chip.fills = tokenFill("surfaceContainer");
    chip.strokes = [{ type: "SOLID", color: c.outline }];
    chip.strokeWeight = 1;
    chip.appendChild(txt(amount, 13, "med", c.onSurface, "Текст"));
    chip.children[0].layoutSizingHorizontal = "HUG";
    chip.children[0].layoutSizingVertical = "HUG";
    quickWd.appendChild(chip);
    chip.layoutSizingHorizontal = "HUG";
    chip.layoutSizingVertical = "HUG";
  }
  bodyWd.appendChild(quickWd);
  quickWd.layoutSizingHorizontal = "HUG";
  quickWd.layoutSizingVertical = "HUG";
  created.push(quickWd.id);

  const balWd = figma.createFrame();
  balWd.name = "Доступно к выводу";
  balWd.layoutMode = "HORIZONTAL";
  balWd.fills = [];
  balWd.primaryAxisSizingMode = "AUTO";
  balWd.counterAxisSizingMode = "AUTO";
  balWd.counterAxisAlignItems = "CENTER";
  balWd.itemSpacing = 8;
  balWd.appendChild(txt("Доступно к выводу:", 13, "reg", c.onSurfaceVariant, "Лейбл"));
  balWd.children[0].layoutSizingHorizontal = "HUG";
  balWd.children[0].layoutSizingVertical = "HUG";
  balWd.appendChild(txt("45 320 ₽", 13, "semi", c.onSurface, "Значение"));
  balWd.children[1].layoutSizingHorizontal = "HUG";
  balWd.children[1].layoutSizingVertical = "HUG";
  bodyWd.appendChild(balWd);
  balWd.layoutSizingHorizontal = "FILL";
  balWd.layoutSizingVertical = "HUG";
  created.push(balWd.id);

  const confirmWdBtn = figma.createFrame();
  confirmWdBtn.name = "Кнопка · Вывести";
  confirmWdBtn.layoutMode = "HORIZONTAL";
  confirmWdBtn.primaryAxisAlignItems = "CENTER";
  confirmWdBtn.counterAxisAlignItems = "CENTER";
  confirmWdBtn.primaryAxisSizingMode = "FIXED";
  confirmWdBtn.counterAxisSizingMode = "FIXED";
  confirmWdBtn.resize(W - PAD * 2, mh.button);
  confirmWdBtn.cornerRadius = shape.full;
  confirmWdBtn.fills = tokenFill("primary");
  confirmWdBtn.effects = shadowCard;
  confirmWdBtn.appendChild(txt("Вывести · 5 000 ₽", 14, "med", c.onPrimary, "Текст"));
  confirmWdBtn.children[0].layoutSizingHorizontal = "HUG";
  confirmWdBtn.children[0].layoutSizingVertical = "HUG";
  bodyWd.appendChild(confirmWdBtn);
  confirmWdBtn.layoutSizingHorizontal = "FILL";
  confirmWdBtn.layoutSizingVertical = "FIXED";
  created.push(confirmWdBtn.id);

  bodyWd.appendChild(txt(
    "Заявка обрабатывается в течение 1–3 рабочих дней. Комиссия — по тарифу брокера.",
    11, "reg", c.onSurfaceVariant, "Подсказка"
  ));
  bodyWd.children[bodyWd.children.length - 1].layoutSizingHorizontal = "FILL";
  bodyWd.children[bodyWd.children.length - 1].layoutSizingVertical = "HUG";

  appendInstance(scrWd, nav1);
  scrWd.children[scrWd.children.length - 1].layoutSizingHorizontal = "FILL";
  scrWd.children[scrWd.children.length - 1].layoutSizingVertical = "HUG";

  const scrWdDone = scrWd.clone();
  scrWdDone.name = `Android Compact ${W}×${H} — Брокерский счёт · Вывод · Успех`;
  screensRow.appendChild(scrWdDone);
  scrWdDone.layoutSizingHorizontal = "FIXED";
  scrWdDone.layoutSizingVertical = "HUG";
  created.push(scrWdDone.id);
  addSnackbar(scrWdDone, "success", "Заявка на вывод принята");
}
