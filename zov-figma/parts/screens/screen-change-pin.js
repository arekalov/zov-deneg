// ═══════════════════════════════════════════════════════════════════════════
// Экраны смены PIN-кода — 3 шага
// ═══════════════════════════════════════════════════════════════════════════

// stepIndex: 0 = текущий, 1 = новый, 2 = подтверждение
// filledDots: сколько точек заполнено для иллюстрации состояния
function buildPinChangeStep(stepIndex, instrTitle, instrSub, filledDots, showForgot) {

  const labels = ["Текущий PIN", "Новый PIN", "Подтверждение"];
  const scr = androidScreen(`Смена PIN · ${labels[stepIndex]}`);
  statusStrip(scr);
  appendInstance(scr, hdrChangePin);

  const body = scrollBody(scr);
  body.primaryAxisAlignItems = "CENTER";
  body.counterAxisAlignItems = "CENTER";
  body.paddingTop = 32;
  body.paddingBottom = 24;
  body.itemSpacing = 28;

  // ── Индикатор шагов ──────────────────────────────────────────────────
  const stepsRow = figma.createFrame();
  stepsRow.name = "Шаги";
  stepsRow.layoutMode = "HORIZONTAL";
  stepsRow.itemSpacing = 8;
  stepsRow.fills = [];
  stepsRow.primaryAxisSizingMode = "AUTO";
  stepsRow.counterAxisSizingMode = "AUTO";
  stepsRow.counterAxisAlignItems = "CENTER";

  for (let i = 0; i < 3; i++) {
    const isActive = i === stepIndex;
    const isDone   = i < stepIndex;
    const seg = figma.createRectangle();
    seg.name = `Шаг ${i + 1}`;
    seg.resize(isActive ? 36 : 20, 4);
    seg.cornerRadius = 2;
    seg.fills = [{
      type: "SOLID",
      color: isDone ? c.primary : isActive ? c.primary : c.outline,
      opacity: isDone ? 0.45 : 1,
    }];
    stepsRow.appendChild(seg);
    seg.layoutSizingHorizontal = "FIXED";
    seg.layoutSizingVertical = "FIXED";
  }

  body.appendChild(stepsRow);
  stepsRow.layoutSizingHorizontal = "HUG";
  stepsRow.layoutSizingVertical = "HUG";
  created.push(stepsRow.id);

  // ── Инструкция ───────────────────────────────────────────────────────
  const instrBlock = figma.createFrame();
  instrBlock.name = "Инструкция";
  instrBlock.layoutMode = "VERTICAL";
  instrBlock.primaryAxisAlignItems = "CENTER";
  instrBlock.counterAxisAlignItems = "CENTER";
  instrBlock.itemSpacing = 6;
  instrBlock.fills = [];
  instrBlock.primaryAxisSizingMode = "AUTO";
  instrBlock.counterAxisSizingMode = "AUTO";

  instrBlock.appendChild(txt(instrTitle, 20, "semi", c.onSurface, "Заголовок"));
  instrBlock.children[0].layoutSizingHorizontal = "HUG";
  instrBlock.children[0].layoutSizingVertical = "HUG";

  instrBlock.appendChild(txt(instrSub, 13, "reg", c.onSurfaceVariant, "Подзаголовок"));
  instrBlock.children[1].layoutSizingHorizontal = "HUG";
  instrBlock.children[1].layoutSizingVertical = "HUG";

  body.appendChild(instrBlock);
  instrBlock.layoutSizingHorizontal = "HUG";
  instrBlock.layoutSizingVertical = "HUG";
  created.push(instrBlock.id);

  // ── PIN-точки (inline, показываем filledDots заполненными) ───────────
  const dotsRow = figma.createFrame();
  dotsRow.name = "PIN-точки";
  dotsRow.layoutMode = "HORIZONTAL";
  dotsRow.itemSpacing = 16;
  dotsRow.fills = [];
  dotsRow.primaryAxisSizingMode = "AUTO";
  dotsRow.counterAxisSizingMode = "AUTO";
  dotsRow.counterAxisAlignItems = "CENTER";

  for (let i = 0; i < 4; i++) {
    const filled = i < filledDots;
    const dot = figma.createEllipse();
    dot.name = filled ? "Заполнен" : "Пустой";
    dot.resize(14, 14);
    dot.fills = filled ? tokenFill("primary") : [];
    dot.strokes = [{ type: "SOLID", color: filled ? c.primary : c.outline }];
    dot.strokeWeight = filled ? 0 : 1.5;
    dotsRow.appendChild(dot);
    dot.layoutSizingHorizontal = "FIXED";
    dot.layoutSizingVertical = "FIXED";
  }

  body.appendChild(dotsRow);
  dotsRow.layoutSizingHorizontal = "HUG";
  dotsRow.layoutSizingVertical = "HUG";
  created.push(dotsRow.id);

  // ── PIN-клавиатура ───────────────────────────────────────────────────
  appendInstance(body, pinPad);

  // ── Кнопка «Забыл PIN» (только на шаге 1) ───────────────────────────
  if (showForgot) {
    const forgotBtn = figma.createFrame();
    forgotBtn.name = "Кнопка · Забыл PIN";
    forgotBtn.layoutMode = "HORIZONTAL";
    forgotBtn.primaryAxisAlignItems = "CENTER";
    forgotBtn.counterAxisAlignItems = "CENTER";
    forgotBtn.primaryAxisSizingMode = "AUTO";
    forgotBtn.counterAxisSizingMode = "AUTO";
    forgotBtn.fills = [];
    forgotBtn.paddingTop = 4;
    forgotBtn.paddingBottom = 4;
    forgotBtn.appendChild(txt("Забыл текущий PIN", 13, "med", c.primary, "Текст"));
    forgotBtn.children[0].layoutSizingHorizontal = "HUG";
    forgotBtn.children[0].layoutSizingVertical = "HUG";
    body.appendChild(forgotBtn);
    forgotBtn.layoutSizingHorizontal = "HUG";
    forgotBtn.layoutSizingVertical = "HUG";
    created.push(forgotBtn.id);
  }

  appendInstance(scr, nav1);
  scr.children[scr.children.length - 1].layoutSizingHorizontal = "FILL";
  scr.children[scr.children.length - 1].layoutSizingVertical = "HUG";
  return scr;
}

// ── Шаг 1: текущий PIN (2 точки введено) ─────────────────────────────────
buildPinChangeStep(0, "Введите текущий PIN-код", "Шаг 1 из 3 · подтвердите личность", 2, true);

// ── Шаг 2: новый PIN (0 точек — только начали) ───────────────────────────
buildPinChangeStep(1, "Придумайте новый PIN-код", "Шаг 2 из 3 · не используйте простые комбинации", 0, false);

// ── Шаг 3: подтверждение (4 точки — все введены) ─────────────────────────
{
  const scrStep3 = buildPinChangeStep(2, "Повторите новый PIN-код", "Шаг 3 из 3 · введите код ещё раз", 4, false);

  // Дополнительный экран — состояние «PIN изменён» со снекбаром
  const scrPinDone = scrStep3.clone();
  scrPinDone.name = `Android Compact ${W}×${H} — Смена PIN · Успех`;
  screensRow.appendChild(scrPinDone);
  scrPinDone.layoutSizingHorizontal = "FIXED";
  scrPinDone.layoutSizingVertical = "HUG";
  created.push(scrPinDone.id);
  addSnackbar(scrPinDone, "success", "PIN-код успешно изменён");
}
