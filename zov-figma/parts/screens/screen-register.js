// ═══════════════════════════════════════════════════════════════════════════
// Экраны регистрации — 4 шага
// ═══════════════════════════════════════════════════════════════════════════

// Вспомогательная функция: прогресс-бар регистрации (4 сегмента)
function buildRegProgress(activeStep) {
  const row = figma.createFrame();
  row.name = "Прогресс регистрации";
  row.layoutMode = "HORIZONTAL";
  row.itemSpacing = 6;
  row.fills = [];
  row.primaryAxisSizingMode = "FIXED";
  row.counterAxisSizingMode = "AUTO";
  row.resize(W - PAD * 2, 1);

  for (let i = 0; i < 4; i++) {
    const seg = figma.createFrame();
    seg.name = `Сегмент ${i + 1}`;
    seg.layoutMode = "HORIZONTAL";
    seg.primaryAxisSizingMode = "AUTO";
    seg.counterAxisSizingMode = "FIXED";
    seg.resize(1, 4);
    seg.cornerRadius = 2;
    seg.layoutGrow = 1;
    seg.fills = [{
      type: "SOLID",
      color: i < activeStep ? c.primary : i === activeStep ? c.primary : c.outline,
      opacity: i < activeStep ? 0.45 : 1,
    }];
    row.appendChild(seg);
    seg.layoutSizingHorizontal = "FILL";
    seg.layoutSizingVertical = "FIXED";
  }
  return row;
}

// Вспомогательная функция: поле ввода для формы регистрации
function buildRegField(parent, label, value, isLast) {
  const wrap = figma.createFrame();
  wrap.name = `Поле · ${label}`;
  wrap.layoutMode = "VERTICAL";
  wrap.itemSpacing = 4;
  wrap.fills = [];
  wrap.primaryAxisSizingMode = "AUTO";
  wrap.counterAxisSizingMode = "FIXED";
  wrap.resize(W - PAD * 2, 1);

  const lbl = txt(label, 12, "med", c.onSurfaceVariant, "Лейбл");
  wrap.appendChild(lbl);
  lbl.layoutSizingHorizontal = "HUG";
  lbl.layoutSizingVertical = "HUG";

  const field = figma.createFrame();
  field.name = "Поле";
  field.layoutMode = "HORIZONTAL";
  field.counterAxisAlignItems = "CENTER";
  field.paddingLeft = 14;
  field.paddingRight = 14;
  field.primaryAxisSizingMode = "FIXED";
  field.counterAxisSizingMode = "FIXED";
  field.resize(W - PAD * 2, 48);
  field.cornerRadius = shape.medium;
  field.fills = tokenFill("surfaceContainer");
  field.strokes = [{ type: "SOLID", color: isLast ? c.primary : c.outline }];
  field.strokeWeight = isLast ? 2 : 1;

  field.appendChild(txt(value, 14, "reg",
    value ? c.onSurface : c.onSurfaceVariant, "Значение"));
  field.children[0].layoutSizingHorizontal = "FILL";
  field.children[0].layoutSizingVertical = "HUG";

  wrap.appendChild(field);
  field.layoutSizingHorizontal = "FILL";
  field.layoutSizingVertical = "FIXED";

  parent.appendChild(wrap);
  wrap.layoutSizingHorizontal = "FILL";
  wrap.layoutSizingVertical = "HUG";
  created.push(wrap.id);
}

// ─────────────────────────────────────────────────────────────────────────────
// Шаг 1 — Личные данные
// ─────────────────────────────────────────────────────────────────────────────
{
  const scr = androidScreen("Регистрация · Данные");
  statusStrip(scr);
  appendInstance(scr, hdrRegister);

  const body = scrollBody(scr);
  body.paddingTop = 20;
  body.itemSpacing = 14;

  const prog1 = buildRegProgress(0);
  body.appendChild(prog1);
  prog1.layoutSizingHorizontal = "FILL";
  prog1.layoutSizingVertical = "HUG";
  created.push(prog1.id);

  const heading1 = figma.createFrame();
  heading1.name = "Заголовок";
  heading1.layoutMode = "VERTICAL";
  heading1.itemSpacing = 4;
  heading1.fills = [];
  heading1.primaryAxisSizingMode = "AUTO";
  heading1.counterAxisSizingMode = "AUTO";
  heading1.appendChild(txt("Личные данные", 20, "semi", c.onSurface, "Заголовок"));
  heading1.children[0].layoutSizingHorizontal = "HUG";
  heading1.children[0].layoutSizingVertical = "HUG";
  heading1.appendChild(txt("Шаг 1 из 4", 13, "reg", c.onSurfaceVariant, "Шаг"));
  heading1.children[1].layoutSizingHorizontal = "HUG";
  heading1.children[1].layoutSizingVertical = "HUG";
  body.appendChild(heading1);
  heading1.layoutSizingHorizontal = "HUG";
  heading1.layoutSizingVertical = "HUG";

  buildRegField(body, "Имя", "Иван", false);
  buildRegField(body, "Фамилия", "Иванов", false);
  buildRegField(body, "Телефон", "+7 900 ···-··-··", false);
  buildRegField(body, "E-mail", "ivan@example.com", true); // активное поле

  const hint = txt("Мы отправим код подтверждения на указанный номер",
    12, "reg", c.onSurfaceVariant, "Подсказка");
  body.appendChild(hint);
  hint.layoutSizingHorizontal = "FILL";
  hint.layoutSizingVertical = "HUG";

  const btnNext1 = figma.createFrame();
  btnNext1.name = "Кнопка · Далее";
  btnNext1.layoutMode = "HORIZONTAL";
  btnNext1.primaryAxisAlignItems = "CENTER";
  btnNext1.counterAxisAlignItems = "CENTER";
  btnNext1.primaryAxisSizingMode = "FIXED";
  btnNext1.counterAxisSizingMode = "FIXED";
  btnNext1.resize(W - PAD * 2, mh.button);
  btnNext1.cornerRadius = shape.full;
  btnNext1.fills = tokenFill("primary");
  btnNext1.effects = shadowCard;
  btnNext1.appendChild(txt("Далее", 14, "med", c.onPrimary, "Текст"));
  btnNext1.children[0].layoutSizingHorizontal = "HUG";
  btnNext1.children[0].layoutSizingVertical = "HUG";
  body.appendChild(btnNext1);
  btnNext1.layoutSizingHorizontal = "FILL";
  btnNext1.layoutSizingVertical = "FIXED";
  created.push(btnNext1.id);

  const loginRow = figma.createFrame();
  loginRow.name = "Уже есть аккаунт";
  loginRow.layoutMode = "HORIZONTAL";
  loginRow.itemSpacing = 4;
  loginRow.fills = [];
  loginRow.primaryAxisSizingMode = "AUTO";
  loginRow.counterAxisSizingMode = "AUTO";
  loginRow.primaryAxisAlignItems = "CENTER";
  loginRow.appendChild(txt("Уже есть аккаунт?", 13, "reg", c.onSurfaceVariant, "Текст"));
  loginRow.children[0].layoutSizingHorizontal = "HUG";
  loginRow.children[0].layoutSizingVertical = "HUG";
  loginRow.appendChild(txt("Войти", 13, "semi", c.primary, "Ссылка"));
  loginRow.children[1].layoutSizingHorizontal = "HUG";
  loginRow.children[1].layoutSizingVertical = "HUG";
  body.appendChild(loginRow);
  loginRow.layoutSizingHorizontal = "HUG";
  loginRow.layoutSizingVertical = "HUG";
  created.push(loginRow.id);
}

// ─────────────────────────────────────────────────────────────────────────────
// Шаг 2 — Создание PIN-кода
// ─────────────────────────────────────────────────────────────────────────────
{
  const scr = androidScreen("Регистрация · PIN");
  statusStrip(scr);
  appendInstance(scr, hdrRegister);

  const body = scrollBody(scr);
  body.primaryAxisAlignItems = "CENTER";
  body.counterAxisAlignItems = "CENTER";
  body.paddingTop = 20;
  body.itemSpacing = 28;

  const prog2 = buildRegProgress(1);
  body.appendChild(prog2);
  prog2.layoutSizingHorizontal = "FILL";
  prog2.layoutSizingVertical = "HUG";
  created.push(prog2.id);

  const instrPin = figma.createFrame();
  instrPin.name = "Инструкция";
  instrPin.layoutMode = "VERTICAL";
  instrPin.primaryAxisAlignItems = "CENTER";
  instrPin.counterAxisAlignItems = "CENTER";
  instrPin.itemSpacing = 6;
  instrPin.fills = [];
  instrPin.primaryAxisSizingMode = "AUTO";
  instrPin.counterAxisSizingMode = "AUTO";
  instrPin.appendChild(txt("Создайте PIN-код", 20, "semi", c.onSurface, "Заголовок"));
  instrPin.children[0].layoutSizingHorizontal = "HUG";
  instrPin.children[0].layoutSizingVertical = "HUG";
  instrPin.appendChild(txt("Шаг 2 из 4 · запомните этот код", 13, "reg", c.onSurfaceVariant, "Подзаголовок"));
  instrPin.children[1].layoutSizingHorizontal = "HUG";
  instrPin.children[1].layoutSizingVertical = "HUG";
  body.appendChild(instrPin);
  instrPin.layoutSizingHorizontal = "HUG";
  instrPin.layoutSizingVertical = "HUG";
  created.push(instrPin.id);

  // PIN-точки (0 введено)
  const dotsRow2 = figma.createFrame();
  dotsRow2.name = "PIN-точки";
  dotsRow2.layoutMode = "HORIZONTAL";
  dotsRow2.itemSpacing = 16;
  dotsRow2.fills = [];
  dotsRow2.primaryAxisSizingMode = "AUTO";
  dotsRow2.counterAxisSizingMode = "AUTO";
  dotsRow2.counterAxisAlignItems = "CENTER";
  for (let i = 0; i < 4; i++) {
    const dot = figma.createEllipse();
    dot.name = "Пустой"; dot.resize(14, 14);
    dot.fills = [];
    dot.strokes = [{ type: "SOLID", color: c.outline }]; dot.strokeWeight = 1.5;
    dotsRow2.appendChild(dot);
    dot.layoutSizingHorizontal = "FIXED"; dot.layoutSizingVertical = "FIXED";
  }
  body.appendChild(dotsRow2);
  dotsRow2.layoutSizingHorizontal = "HUG";
  dotsRow2.layoutSizingVertical = "HUG";
  created.push(dotsRow2.id);

  appendInstance(body, pinPad);
}

// ─────────────────────────────────────────────────────────────────────────────
// Шаг 3 — Подтверждение PIN-кода
// ─────────────────────────────────────────────────────────────────────────────
{
  const scr = androidScreen("Регистрация · Подтверждение PIN");
  statusStrip(scr);
  appendInstance(scr, hdrRegister);

  const body = scrollBody(scr);
  body.primaryAxisAlignItems = "CENTER";
  body.counterAxisAlignItems = "CENTER";
  body.paddingTop = 20;
  body.itemSpacing = 28;

  const prog3 = buildRegProgress(2);
  body.appendChild(prog3);
  prog3.layoutSizingHorizontal = "FILL";
  prog3.layoutSizingVertical = "HUG";
  created.push(prog3.id);

  const instrConfirm = figma.createFrame();
  instrConfirm.name = "Инструкция";
  instrConfirm.layoutMode = "VERTICAL";
  instrConfirm.primaryAxisAlignItems = "CENTER";
  instrConfirm.counterAxisAlignItems = "CENTER";
  instrConfirm.itemSpacing = 6;
  instrConfirm.fills = [];
  instrConfirm.primaryAxisSizingMode = "AUTO";
  instrConfirm.counterAxisSizingMode = "AUTO";
  instrConfirm.appendChild(txt("Повторите PIN-код", 20, "semi", c.onSurface, "Заголовок"));
  instrConfirm.children[0].layoutSizingHorizontal = "HUG";
  instrConfirm.children[0].layoutSizingVertical = "HUG";
  instrConfirm.appendChild(txt("Шаг 3 из 4 · введите тот же код ещё раз", 13, "reg", c.onSurfaceVariant, "Подзаголовок"));
  instrConfirm.children[1].layoutSizingHorizontal = "HUG";
  instrConfirm.children[1].layoutSizingVertical = "HUG";
  body.appendChild(instrConfirm);
  instrConfirm.layoutSizingHorizontal = "HUG";
  instrConfirm.layoutSizingVertical = "HUG";
  created.push(instrConfirm.id);

  // PIN-точки (все 4 заполнены)
  const dotsRow3 = figma.createFrame();
  dotsRow3.name = "PIN-точки";
  dotsRow3.layoutMode = "HORIZONTAL";
  dotsRow3.itemSpacing = 16;
  dotsRow3.fills = [];
  dotsRow3.primaryAxisSizingMode = "AUTO";
  dotsRow3.counterAxisSizingMode = "AUTO";
  dotsRow3.counterAxisAlignItems = "CENTER";
  for (let i = 0; i < 4; i++) {
    const dot = figma.createEllipse();
    dot.name = "Заполнен"; dot.resize(14, 14);
    dot.fills = tokenFill("primary"); dot.strokes = [];
    dotsRow3.appendChild(dot);
    dot.layoutSizingHorizontal = "FIXED"; dot.layoutSizingVertical = "FIXED";
  }
  body.appendChild(dotsRow3);
  dotsRow3.layoutSizingHorizontal = "HUG";
  dotsRow3.layoutSizingVertical = "HUG";
  created.push(dotsRow3.id);

  appendInstance(body, pinPad);
}

// ─────────────────────────────────────────────────────────────────────────────
// Шаг 4 — Биометрия
// ─────────────────────────────────────────────────────────────────────────────
{
  const scr = androidScreen("Регистрация · Биометрия");
  statusStrip(scr);
  appendInstance(scr, hdrRegister);

  const body = scrollBody(scr);
  body.primaryAxisAlignItems = "CENTER";
  body.counterAxisAlignItems = "CENTER";
  body.paddingTop = 20;
  body.itemSpacing = 32;

  const prog4 = buildRegProgress(3);
  body.appendChild(prog4);
  prog4.layoutSizingHorizontal = "FILL";
  prog4.layoutSizingVertical = "HUG";
  created.push(prog4.id);

  // Иконка биометрии
  const fpCircle = figma.createFrame();
  fpCircle.name = "Иконка биометрии";
  fpCircle.layoutMode = "HORIZONTAL";
  fpCircle.primaryAxisAlignItems = "CENTER";
  fpCircle.counterAxisAlignItems = "CENTER";
  fpCircle.primaryAxisSizingMode = "FIXED";
  fpCircle.counterAxisSizingMode = "FIXED";
  fpCircle.resize(88, 88);
  fpCircle.cornerRadius = 44;
  fpCircle.fills = tokenFill("primaryContainer");
  const fpIco = buildIcon("fingerprint", 44, c.primary);
  fpCircle.appendChild(fpIco);
  fpIco.layoutSizingHorizontal = "FIXED";
  fpIco.layoutSizingVertical = "FIXED";
  body.appendChild(fpCircle);
  fpCircle.layoutSizingHorizontal = "HUG";
  fpCircle.layoutSizingVertical = "HUG";
  created.push(fpCircle.id);

  // Текст
  const bioText = figma.createFrame();
  bioText.name = "Текст";
  bioText.layoutMode = "VERTICAL";
  bioText.primaryAxisAlignItems = "CENTER";
  bioText.counterAxisAlignItems = "CENTER";
  bioText.itemSpacing = 10;
  bioText.fills = [];
  bioText.primaryAxisSizingMode = "AUTO";
  bioText.counterAxisSizingMode = "AUTO";
  bioText.appendChild(txt("Вход по биометрии", 20, "semi", c.onSurface, "Заголовок"));
  bioText.children[0].layoutSizingHorizontal = "HUG";
  bioText.children[0].layoutSizingVertical = "HUG";
  bioText.appendChild(txt(
    "Используйте отпечаток пальца для быстрого и безопасного входа в приложение",
    14, "reg", c.onSurfaceVariant, "Описание"
  ));
  bioText.children[1].layoutSizingHorizontal = "HUG";
  bioText.children[1].layoutSizingVertical = "HUG";
  bioText.appendChild(txt("Шаг 4 из 4", 13, "reg", c.onSurfaceVariant, "Шаг"));
  bioText.children[2].layoutSizingHorizontal = "HUG";
  bioText.children[2].layoutSizingVertical = "HUG";
  body.appendChild(bioText);
  bioText.layoutSizingHorizontal = "FILL";
  bioText.layoutSizingVertical = "HUG";
  created.push(bioText.id);

  // Кнопки
  const allowBtn = figma.createFrame();
  allowBtn.name = "Кнопка · Разрешить";
  allowBtn.layoutMode = "HORIZONTAL";
  allowBtn.primaryAxisAlignItems = "CENTER";
  allowBtn.counterAxisAlignItems = "CENTER";
  allowBtn.primaryAxisSizingMode = "FIXED";
  allowBtn.counterAxisSizingMode = "FIXED";
  allowBtn.resize(W - PAD * 2, mh.button);
  allowBtn.cornerRadius = shape.full;
  allowBtn.fills = tokenFill("primary");
  allowBtn.effects = shadowCard;
  allowBtn.appendChild(txt("Разрешить", 14, "med", c.onPrimary, "Текст"));
  allowBtn.children[0].layoutSizingHorizontal = "HUG";
  allowBtn.children[0].layoutSizingVertical = "HUG";
  body.appendChild(allowBtn);
  allowBtn.layoutSizingHorizontal = "FILL";
  allowBtn.layoutSizingVertical = "FIXED";
  created.push(allowBtn.id);

  const skipBtn = figma.createFrame();
  skipBtn.name = "Кнопка · Пропустить";
  skipBtn.layoutMode = "HORIZONTAL";
  skipBtn.primaryAxisAlignItems = "CENTER";
  skipBtn.counterAxisAlignItems = "CENTER";
  skipBtn.primaryAxisSizingMode = "FIXED";
  skipBtn.counterAxisSizingMode = "FIXED";
  skipBtn.resize(W - PAD * 2, mh.button);
  skipBtn.cornerRadius = shape.full;
  skipBtn.fills = [];
  skipBtn.strokes = [{ type: "SOLID", color: c.outline }];
  skipBtn.strokeWeight = 1;
  skipBtn.appendChild(txt("Пропустить", 14, "med", c.onSurfaceVariant, "Текст"));
  skipBtn.children[0].layoutSizingHorizontal = "HUG";
  skipBtn.children[0].layoutSizingVertical = "HUG";
  body.appendChild(skipBtn);
  skipBtn.layoutSizingHorizontal = "FILL";
  skipBtn.layoutSizingVertical = "FIXED";
  created.push(skipBtn.id);
}
