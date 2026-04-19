// ─── Vector icon builder (Material Icons paths, 24×24 grid) ─────────────
const ICON_PATHS = {
  home:    "M 10 20 L 10 14 L 14 14 L 14 20 L 19 20 L 19 12 L 22 12 L 12 3 L 2 12 L 5 12 L 5 20 Z",
  search:  "M 15.5 14 L 14.71 14 L 14.43 13.73 C 15.41 12.59 16 11.11 16 9.5 C 16 5.91 13.09 3 9.5 3 C 5.91 3 3 5.91 3 9.5 C 3 13.09 5.91 16 9.5 16 C 11.11 16 12.59 15.41 13.73 14.43 L 14 14.71 L 14 15.5 L 19 20.49 L 20.49 19 Z M 9.5 14 C 7.01 14 5 11.99 5 9.5 C 5 7.01 7.01 5 9.5 5 C 11.99 5 14 7.01 14 9.5 C 14 11.99 11.99 14 9.5 14 Z",
  history: "M 13 3 C 8.03 3 4 7.03 4 12 L 1 12 L 4.89 15.89 L 4.96 16.03 L 9 12 L 6 12 C 6 8.13 9.13 5 13 5 C 16.87 5 20 8.13 20 12 C 20 15.87 16.87 19 13 19 C 11.07 19 9.32 18.21 8.06 16.94 L 6.64 18.36 C 8.27 19.99 10.51 21 13 21 C 17.97 21 22 16.97 22 12 C 22 7.03 17.97 3 13 3 Z M 12 8 L 12 13 L 16.28 15.54 L 17 14.33 L 13.5 12.25 L 13.5 8 Z",
  person:  "M 12 12 C 14.21 12 16 10.21 16 8 C 16 5.79 14.21 4 12 4 C 9.79 4 8 5.79 8 8 C 8 10.21 9.79 12 12 12 Z M 12 14 C 9.33 14 4 15.34 4 18 L 4 20 L 20 20 L 20 18 C 20 15.34 14.67 14 12 14 Z",
  fingerprint: "M 17.81 4.47 C 17.73 4.47 17.65 4.45 17.58 4.41 C 15.66 3.42 14 3 12.01 3 C 10.03 3 8.15 3.47 6.44 4.41 C 6.2 4.54 5.9 4.45 5.76 4.21 C 5.63 3.97 5.72 3.67 5.96 3.53 C 7.82 2.52 9.86 2 12.01 2 C 14.14 2 16 2.47 17.62 3.41 C 17.86 3.54 17.95 3.84 17.81 4.08 Z M 12 7 C 10.9 7 10 7.9 10 9 L 10 10 C 10 10.55 9.55 11 9 11 C 8.45 11 8 10.55 8 10 L 8 9 C 8 6.79 9.79 5 12 5 C 14.21 5 16 6.79 16 9 C 16 11.95 14.02 14.44 11.27 15.18 C 11.1 15.22 10.93 15.14 10.85 14.98 C 10.77 14.82 10.83 14.63 10.97 14.54 C 13.22 13.19 14 11.5 14 9 C 14 7.9 13.1 7 12 7 Z M 12 11 C 11.45 11 11 10.55 11 10 L 11 9 C 11 8.45 11.45 8 12 8 C 12.55 8 13 8.45 13 9 L 13 10 C 13 10.55 12.55 11 12 11 Z M 9 14 C 9 13.45 8.55 13 8 13 C 7.45 13 7 13.45 7 14 C 7 16.76 8.38 19.22 10.58 20.65 C 10.76 20.76 10.98 20.71 11.1 20.54 C 11.22 20.37 11.18 20.14 11.01 20.02 C 9.08 18.77 8 16.92 8 15 L 8 14 Z",
};

function buildIcon(iconName, size, color) {
  const v = figma.createVector();
  v.name = `Icon · ${iconName}`;
  v.resize(size, size);
  const d = ICON_PATHS[iconName];
  if (d) v.vectorPaths = [{ windingRule: "NONZERO", data: d }];
  v.fills = [{ type: "SOLID", color }];
  v.strokes = [];
  return v;
}

// ─── Navigation Bar (M3: 80dp, indicator 64×32 r=16) ─────────────────────
function buildNavBar(activeIdx) {
  const comp = figma.createComponent();
  comp.name = `Навигация · вкладка ${activeIdx + 1}`;
  comp.layoutMode = "HORIZONTAL";
  comp.primaryAxisSizingMode = "FIXED";
  comp.counterAxisSizingMode = "FIXED";
  comp.resize(W, mh.navBar);
  comp.paddingTop = 12;
  comp.paddingBottom = 16;
  comp.paddingLeft = 8;
  comp.paddingRight = 8;
  comp.itemSpacing = 0;
  comp.fills = tokenFill("surface");
  comp.strokes = [{ type: "SOLID", color: c.outline }];
  comp.strokeTopWeight = 1;
  comp.strokeBottomWeight = 0;
  comp.strokeLeftWeight = 0;
  comp.strokeRightWeight = 0;

  const tabs = ["Главная", "Поиск", "История"];
  for (let i = 0; i < 3; i++) {
    const cell = figma.createFrame();
    cell.name = `Вкладка · ${tabs[i]}`;
    cell.layoutMode = "VERTICAL";
    cell.itemSpacing = 4;
    cell.primaryAxisAlignItems = "CENTER";
    cell.counterAxisAlignItems = "CENTER";
    cell.fills = [];
    cell.layoutGrow = 1;
    cell.primaryAxisSizingMode = "FIXED";
    cell.counterAxisSizingMode = "AUTO";

    // M3: active indicator 88×32, cornerRadius = shape.large (16)
    const indicator = figma.createFrame();
    indicator.name = "Индикатор";
    indicator.resize(88, 32);
    indicator.cornerRadius = shape.large;
    indicator.layoutMode = "HORIZONTAL";
    indicator.primaryAxisAlignItems = "CENTER";
    indicator.counterAxisAlignItems = "CENTER";
    indicator.fills = i === activeIdx ? tokenFill("primaryContainer") : [];

    const iconNames = ["home", "search", "history"];
    const iconColor = i === activeIdx ? c.primary : c.onSurfaceVariant;
    const icon = buildIcon(iconNames[i], 20, iconColor);
    indicator.appendChild(icon);
    icon.layoutSizingHorizontal = "FIXED";
    icon.layoutSizingVertical = "FIXED";

    cell.appendChild(indicator);
    indicator.layoutSizingHorizontal = "FIXED";
    indicator.layoutSizingVertical = "FIXED";

    const label = txt(
      tabs[i], 12,
      i === activeIdx ? "semi" : "reg",
      i === activeIdx ? c.primary : c.onSurfaceVariant,
      "Подпись"
    );
    cell.appendChild(label);
    label.layoutSizingHorizontal = "HUG";
    label.layoutSizingVertical = "HUG";

    comp.appendChild(cell);
    cell.layoutSizingHorizontal = "FILL";
    cell.layoutSizingVertical = "HUG";
  }
  return comp;
}

// ─── Summary Card (M3 ElevatedCard: cornerRadius = shape.medium) ──────────
function buildSummaryCard() {
  const comp = figma.createComponent();
  comp.name = "Карточка · Сводка портфеля";
  comp.layoutMode = "VERTICAL";
  comp.primaryAxisSizingMode = "AUTO";
  comp.counterAxisSizingMode = "FIXED";
  comp.resize(W - PAD * 2, 1);
  comp.paddingLeft = 16;
  comp.paddingRight = 16;
  comp.paddingTop = 16;
  comp.paddingBottom = 16;
  comp.itemSpacing = 6;
  comp.cornerRadius = shape.medium;
  comp.fills = tokenFill("surface");
  comp.effects = shadowCard;
  comp.appendChild(txt("Цена портфеля", 12, "reg", c.onSurfaceVariant, "Лейбл"));
  comp.appendChild(txt("128 400 ₽", 22, "semi", c.onSurface, "Значение"));
  comp.appendChild(txt("Суммарный прирост", 12, "reg", c.onSurfaceVariant, "Лейбл"));
  comp.appendChild(txt("+2,1% · +2 640 ₽", 14, "med", c.positive, "Значение"));
  comp.appendChild(txt("Убыток (отн. и абс.)", 12, "reg", c.onSurfaceVariant, "Лейбл"));
  comp.appendChild(txt("—", 14, "med", c.onSurfaceVariant, "Значение"));
  for (const n of comp.children) {
    if (n.type === "TEXT") {
      n.layoutSizingHorizontal = "FILL";
      n.layoutSizingVertical = "HUG";
    }
  }
  return comp;
}

// ─── Asset Row (M3 2-line List Item: 72dp, paddingH 16) ──────────────────
function buildAssetRow(ticker, meta, value, delta) {
  const comp = figma.createComponent();
  comp.name = `Строка · Актив · ${ticker}`;
  comp.layoutMode = "HORIZONTAL";
  comp.primaryAxisSizingMode = "FIXED";
  comp.counterAxisSizingMode = "FIXED";
  comp.resize(W - PAD * 2, mh.listItem2);
  comp.paddingLeft = 16;
  comp.paddingRight = 16;
  comp.paddingTop = 14;
  comp.paddingBottom = 14;
  comp.itemSpacing = 16;
  comp.primaryAxisAlignItems = "CENTER";
  comp.cornerRadius = shape.medium;
  comp.fills = tokenFill("surface");
  comp.strokes = [{ type: "SOLID", color: c.outline }];
  comp.strokeWeight = 1;

  const left = figma.createFrame();
  left.name = "Слева";
  left.layoutMode = "VERTICAL";
  left.itemSpacing = 4;
  left.fills = [];
  left.appendChild(txt(ticker, 14, "semi", c.onSurface, "Тикер"));
  left.appendChild(txt(meta, 12, "reg", c.onSurfaceVariant, "Мета"));
  comp.appendChild(left);
  left.layoutSizingHorizontal = "FILL";
  left.layoutSizingVertical = "HUG";

  const right = figma.createFrame();
  right.name = "Справа";
  right.layoutMode = "VERTICAL";
  right.itemSpacing = 2;
  right.primaryAxisAlignItems = "MAX";
  right.counterAxisAlignItems = "MAX";
  right.fills = [];
  right.appendChild(txt(value, 14, "semi", c.onSurface, "Стоимость"));
  right.appendChild(txt(delta, 12, "med", c.positive, "Изменение"));
  comp.appendChild(right);
  right.layoutSizingHorizontal = "HUG";
  right.layoutSizingVertical = "HUG";
  return comp;
}

// ─── Filter Chip (M3: 32dp, cornerRadius = shape.small) ──────────────────
function buildChipFixed(label, selected) {
  const comp = figma.createComponent();
  comp.name = `Чип · ${label}`;
  comp.layoutMode = "HORIZONTAL";
  comp.paddingLeft = 16;
  comp.paddingRight = 16;
  comp.paddingTop = 6;
  comp.paddingBottom = 6;
  comp.cornerRadius = shape.small;
  comp.primaryAxisSizingMode = "AUTO";
  comp.counterAxisSizingMode = "FIXED";
  comp.resize(1, mh.chip);
  comp.primaryAxisAlignItems = "CENTER";
  comp.counterAxisAlignItems = "CENTER";
  comp.fills = tokenFill(selected ? "primaryContainer" : "surfaceContainer");
  if (!selected) {
    comp.strokes = [{ type: "SOLID", color: c.outline }];
    comp.strokeWeight = 1;
  }
  comp.appendChild(txt(label, 14, selected ? "semi" : "med",
    selected ? c.positive : c.onSurfaceVariant, "Текст"));
  comp.children[0].layoutSizingHorizontal = "HUG";
  comp.children[0].layoutSizingVertical = "HUG";
  return comp;
}

// ─── Transaction Row (M3 3-line List: cornerRadius = shape.medium) ────────
function buildTransactionRow() {
  const comp = figma.createComponent();
  comp.name = "Строка · Транзакция";
  comp.layoutMode = "VERTICAL";
  comp.primaryAxisSizingMode = "AUTO";
  comp.counterAxisSizingMode = "FIXED";
  comp.resize(W - PAD * 2, 1);
  comp.paddingLeft = 16;
  comp.paddingRight = 16;
  comp.paddingTop = 14;
  comp.paddingBottom = 14;
  comp.itemSpacing = 4;
  comp.cornerRadius = shape.medium;
  comp.fills = tokenFill("surface");
  comp.effects = shadowCard;
  comp.appendChild(txt("Покупка · SBER", 14, "semi", c.onSurface, "Тип"));
  comp.appendChild(txt("28 марта 2026, 14:32", 12, "reg", c.onSurfaceVariant, "Время"));
  comp.appendChild(txt("10 шт. · цена 298,12 ₽", 14, "reg", c.onSurface, "Детали"));
  const actions = figma.createFrame();
  actions.name = "Действия";
  actions.layoutMode = "HORIZONTAL";
  actions.fills = [];
  actions.paddingTop = 4;
  actions.appendChild(txt("Обжаловать", 14, "med", c.primary, "Ссылка"));
  actions.children[0].layoutSizingHorizontal = "HUG";
  comp.appendChild(actions);
  actions.layoutSizingHorizontal = "FILL";
  actions.layoutSizingVertical = "HUG";
  for (const n of comp.children) {
    if (n.type === "TEXT") {
      n.layoutSizingHorizontal = "FILL";
      n.layoutSizingVertical = "HUG";
    }
  }
  return comp;
}

// ─── Setting Row (M3 1-line List Item: 56dp, paddingH 16) ────────────────
function buildSettingRow(title) {
  const comp = figma.createComponent();
  comp.name = `Строка · Настройка · ${title}`;
  comp.layoutMode = "HORIZONTAL";
  comp.primaryAxisSizingMode = "FIXED";
  comp.counterAxisSizingMode = "FIXED";
  comp.resize(W - PAD * 2, mh.listItem1);
  comp.paddingLeft = 16;
  comp.paddingRight = 16;
  comp.paddingTop = 18;
  comp.paddingBottom = 18;
  comp.primaryAxisAlignItems = "CENTER";
  comp.itemSpacing = 8;
  comp.cornerRadius = shape.medium;
  comp.fills = tokenFill("surface");
  comp.strokes = [{ type: "SOLID", color: c.outline }];
  comp.strokeWeight = 1;
  comp.appendChild(txt(title, 14, "reg", c.onSurface, "Название"));
  comp.appendChild(txt("›", 16, "reg", c.onSurfaceVariant, "Шеврон"));
  comp.children[0].layoutSizingHorizontal = "FILL";
  comp.children[0].layoutSizingVertical = "HUG";
  comp.children[1].layoutSizingHorizontal = "HUG";
  comp.children[1].layoutSizingVertical = "HUG";
  return comp;
}

// ─── Top App Bar with Profile (M3 Small Top App Bar: 64dp) ───────────────
function buildHeaderWithProfile(titleText) {
  const comp = figma.createComponent();
  comp.name = "Шапка · Заголовок + Профиль";
  comp.layoutMode = "HORIZONTAL";
  comp.primaryAxisSizingMode = "FIXED";
  comp.counterAxisSizingMode = "FIXED";
  comp.resize(W, mh.topBar);
  comp.paddingLeft = PAD;
  comp.paddingRight = 12;
  comp.paddingTop = 0;
  comp.paddingBottom = 0;
  comp.itemSpacing = 8;
  comp.primaryAxisAlignItems = "CENTER";
  comp.counterAxisAlignItems = "CENTER";
  comp.fills = tokenFill("bg");

  // M3 Title Large: 22sp
  const title = txt(titleText, 22, "semi", c.onSurface, "Заголовок");
  comp.appendChild(title);
  title.layoutSizingHorizontal = "FILL";
  title.layoutSizingVertical = "HUG";

  // M3 Icon Button: 40×40 круг с иконкой person
  const iconBtn = figma.createFrame();
  iconBtn.name = "Кнопка · Профиль";
  iconBtn.resize(40, 40);
  iconBtn.cornerRadius = shape.full;
  iconBtn.layoutMode = "HORIZONTAL";
  iconBtn.primaryAxisAlignItems = "CENTER";
  iconBtn.counterAxisAlignItems = "CENTER";
  iconBtn.fills = tokenFill("surfaceContainer");
  const personIcon = buildIcon("person", 20, c.primary);
  iconBtn.appendChild(personIcon);
  personIcon.layoutSizingHorizontal = "FIXED";
  personIcon.layoutSizingVertical = "FIXED";
  comp.appendChild(iconBtn);
  iconBtn.layoutSizingHorizontal = "FIXED";
  iconBtn.layoutSizingVertical = "FIXED";
  return comp;
}

// ─── Top App Bar simple (M3 Small Top App Bar: 64dp) ─────────────────────
function buildHeaderSimple(titleText) {
  const comp = figma.createComponent();
  comp.name = `Шапка · ${titleText}`;
  comp.layoutMode = "HORIZONTAL";
  comp.primaryAxisSizingMode = "FIXED";
  comp.counterAxisSizingMode = "FIXED";
  comp.resize(W, mh.topBar);
  comp.paddingLeft = PAD;
  comp.paddingRight = PAD;
  comp.paddingTop = 0;
  comp.paddingBottom = 0;
  comp.counterAxisAlignItems = "CENTER";
  comp.fills = tokenFill("bg");
  comp.appendChild(txt(titleText, 22, "semi", c.onSurface, "Заголовок"));
  comp.children[0].layoutSizingHorizontal = "FILL";
  comp.children[0].layoutSizingVertical = "HUG";
  return comp;
}

// ─── Outlined TextField (M3: 56dp, cornerRadius = shape.extraSmall) ───────
function buildInputField(labelText, placeholderText) {
  const comp = figma.createComponent();
  comp.name = `Поле ввода · ${labelText}`;
  comp.layoutMode = "VERTICAL";
  comp.itemSpacing = 4;
  comp.primaryAxisSizingMode = "AUTO";
  comp.counterAxisSizingMode = "FIXED";
  comp.resize(W - PAD * 2, 1);
  comp.fills = [];

  const label = txt(labelText, 12, "med", c.onSurfaceVariant, "Лейбл");
  comp.appendChild(label);
  label.layoutSizingHorizontal = "HUG";
  label.layoutSizingVertical = "HUG";

  const field = figma.createFrame();
  field.name = "Поле";
  field.layoutMode = "HORIZONTAL";
  field.paddingLeft = 16;
  field.paddingRight = 16;
  field.paddingTop = 16;
  field.paddingBottom = 16;
  // M3 Outlined TextField: shape.extraSmall = 4
  field.cornerRadius = shape.extraSmall;
  field.fills = tokenFill("surface");
  field.strokes = [{ type: "SOLID", color: c.outline }];
  field.strokeWeight = 1;
  field.primaryAxisSizingMode = "FIXED";
  field.counterAxisSizingMode = "FIXED";
  field.resize(W - PAD * 2, mh.input);
  field.primaryAxisAlignItems = "CENTER";
  field.appendChild(txt(placeholderText, 16, "reg", c.onSurfaceVariant, "Плейсхолдер"));
  field.children[0].layoutSizingHorizontal = "FILL";
  field.children[0].layoutSizingVertical = "HUG";
  comp.appendChild(field);
  field.layoutSizingHorizontal = "FILL";
  field.layoutSizingVertical = "FIXED";

  return comp;
}

// ─── Filled Button (M3: 40dp, cornerRadius = shape.full, Label Large 14sp)
function buildButtonPrimary(labelText) {
  const comp = figma.createComponent();
  comp.name = `Кнопка · Основная · ${labelText}`;
  comp.layoutMode = "HORIZONTAL";
  comp.primaryAxisAlignItems = "CENTER";
  comp.counterAxisAlignItems = "CENTER";
  comp.paddingLeft = 24;
  comp.paddingRight = 24;
  comp.paddingTop = 10;
  comp.paddingBottom = 10;
  comp.cornerRadius = shape.full;
  comp.primaryAxisSizingMode = "FIXED";
  comp.counterAxisSizingMode = "FIXED";
  comp.resize(W - PAD * 2, mh.button);
  comp.fills = tokenFill("primary");
  comp.effects = shadowCard;
  const t = txt(labelText, 14, "med", c.onPrimary, "Текст");
  comp.appendChild(t);
  t.layoutSizingHorizontal = "HUG";
  t.layoutSizingVertical = "HUG";
  return comp;
}

// ─── Outlined Button (M3: 40dp, cornerRadius = shape.full) ───────────────
function buildButtonSecondary(labelText) {
  const comp = figma.createComponent();
  comp.name = `Кнопка · Вторичная · ${labelText}`;
  comp.layoutMode = "HORIZONTAL";
  comp.primaryAxisAlignItems = "CENTER";
  comp.counterAxisAlignItems = "CENTER";
  comp.paddingLeft = 24;
  comp.paddingRight = 24;
  comp.paddingTop = 10;
  comp.paddingBottom = 10;
  comp.cornerRadius = shape.full;
  comp.primaryAxisSizingMode = "FIXED";
  comp.counterAxisSizingMode = "FIXED";
  comp.resize(W - PAD * 2, mh.button);
  comp.fills = tokenFill("surface");
  comp.strokes = [{ type: "SOLID", color: c.outline }];
  comp.strokeWeight = 1;
  const t = txt(labelText, 14, "med", c.primary, "Текст");
  comp.appendChild(t);
  t.layoutSizingHorizontal = "HUG";
  t.layoutSizingVertical = "HUG";
  return comp;
}

// ─── PIN: точки ──────────────────────────────────────────────────────────
function buildPinDotsComp() {
  const comp = figma.createComponent();
  comp.name = "PIN · Точки (2 из 4)";
  comp.layoutMode = "HORIZONTAL";
  comp.itemSpacing = 20;
  comp.primaryAxisAlignItems = "CENTER";
  comp.counterAxisAlignItems = "CENTER";
  comp.paddingTop = 8;
  comp.paddingBottom = 8;
  comp.primaryAxisSizingMode = "AUTO";
  comp.counterAxisSizingMode = "AUTO";
  comp.fills = [];
  for (let i = 0; i < 4; i++) {
    const dot = figma.createEllipse();
    dot.name = i < 2 ? "Заполнен" : "Пустой";
    dot.resize(18, 18);
    dot.fills = tokenFill(i < 2 ? "primary" : "surface");
    if (i >= 2) {
      dot.strokes = [{ type: "SOLID", color: c.outline }];
      dot.strokeWeight = 2;
    }
    comp.appendChild(dot);
    dot.layoutSizingHorizontal = "FIXED";
    dot.layoutSizingVertical = "FIXED";
  }
  return comp;
}

// ─── PIN: клавиатура ─────────────────────────────────────────────────────
function buildPinPad() {
  const comp = figma.createComponent();
  comp.name = "PIN · Клавиатура";
  comp.layoutMode = "VERTICAL";
  comp.itemSpacing = 8;
  comp.primaryAxisSizingMode = "AUTO";
  comp.counterAxisSizingMode = "FIXED";
  comp.resize(W - PAD * 2, 1);
  comp.fills = [];

  const rows = [["1","2","3"], ["4","5","6"], ["7","8","9"], ["⌫","0","OK"]];
  for (const row of rows) {
    const rowFrame = figma.createFrame();
    rowFrame.name = `Ряд · ${row.join("")}`;
    rowFrame.layoutMode = "HORIZONTAL";
    rowFrame.itemSpacing = 8;
    rowFrame.fills = [];
    rowFrame.primaryAxisSizingMode = "FIXED";
    rowFrame.counterAxisSizingMode = "FIXED";
    rowFrame.resize(W - PAD * 2, 64);
    for (const key of row) {
      const btn = figma.createFrame();
      btn.name = key;
      btn.cornerRadius = shape.large;
      btn.layoutMode = "HORIZONTAL";
      btn.primaryAxisAlignItems = "CENTER";
      btn.counterAxisAlignItems = "CENTER";

      if (key === "OK") {
        btn.fills = tokenFill("primary");
        btn.effects = shadowCard;
        btn.appendChild(txt("OK", 16, "semi", c.onPrimary, "Символ"));
      } else if (key === "⌫") {
        btn.fills = tokenFill("surfaceContainer");
        btn.appendChild(txt("⌫", 20, "med", c.onSurfaceVariant, "Символ"));
      } else {
        btn.fills = tokenFill("surface");
        btn.effects = shadowCard;
        btn.appendChild(txt(key, 24, "semi", c.onSurface, "Символ"));
      }
      btn.children[0].layoutSizingHorizontal = "HUG";
      btn.children[0].layoutSizingVertical = "HUG";

      rowFrame.appendChild(btn);
      btn.layoutGrow = 1;
      btn.layoutSizingVertical = "FILL";
    }
    comp.appendChild(rowFrame);
    rowFrame.layoutSizingHorizontal = "FILL";
    rowFrame.layoutSizingVertical = "FIXED";
  }
  return comp;
}

// ─── Sparkline Card (M3 ElevatedCard: cornerRadius = shape.medium) ────────
function buildSparklineCard(ticker, name, price, delta, isPositive) {
  const comp = figma.createComponent();
  comp.name = `Карточка · Спарклайн · ${ticker}`;
  comp.layoutMode = "VERTICAL";
  comp.primaryAxisSizingMode = "AUTO";
  comp.counterAxisSizingMode = "FIXED";
  comp.resize(W - PAD * 2, 1);
  comp.paddingLeft = 16;
  comp.paddingRight = 16;
  comp.paddingTop = 16;
  comp.paddingBottom = 16;
  comp.itemSpacing = 10;
  comp.cornerRadius = shape.medium;
  comp.fills = tokenFill("surface");
  comp.effects = shadowCard;

  const accentColor = isPositive ? c.positive : c.negative;
  const badgeBg = isPositive
    ? { r: 0.85, g: 0.96, b: 0.88 }
    : { r: 0.97, g: 0.89, b: 0.88 };

  // шапка: тикер-бейдж + название + дельта
  const topRow = figma.createFrame();
  topRow.name = "Шапка";
  topRow.layoutMode = "HORIZONTAL";
  topRow.itemSpacing = 8;
  topRow.primaryAxisAlignItems = "CENTER";
  topRow.fills = [];

  const tickerBadge = figma.createFrame();
  tickerBadge.name = "Тикер-бейдж";
  tickerBadge.layoutMode = "HORIZONTAL";
  tickerBadge.paddingLeft = 10;
  tickerBadge.paddingRight = 10;
  tickerBadge.paddingTop = 5;
  tickerBadge.paddingBottom = 5;
  tickerBadge.cornerRadius = shape.small;
  tickerBadge.fills = tokenFill("primaryContainer");
  tickerBadge.appendChild(txt(ticker, 13, "semi", c.positive, "Текст"));
  tickerBadge.children[0].layoutSizingHorizontal = "HUG";
  tickerBadge.children[0].layoutSizingVertical = "HUG";
  topRow.appendChild(tickerBadge);
  tickerBadge.layoutSizingHorizontal = "HUG";
  tickerBadge.layoutSizingVertical = "HUG";

  const nameT = txt(name, 13, "reg", c.onSurfaceVariant, "Название");
  topRow.appendChild(nameT);
  nameT.layoutSizingHorizontal = "FILL";
  nameT.layoutSizingVertical = "HUG";

  const deltaBadge = figma.createFrame();
  deltaBadge.name = "Дельта";
  deltaBadge.layoutMode = "HORIZONTAL";
  deltaBadge.paddingLeft = 8;
  deltaBadge.paddingRight = 8;
  deltaBadge.paddingTop = 5;
  deltaBadge.paddingBottom = 5;
  deltaBadge.cornerRadius = shape.small;
  deltaBadge.fills = [{ type: "SOLID", color: badgeBg }];
  deltaBadge.appendChild(txt(delta, 12, "semi", accentColor, "Текст"));
  deltaBadge.children[0].layoutSizingHorizontal = "HUG";
  deltaBadge.children[0].layoutSizingVertical = "HUG";
  topRow.appendChild(deltaBadge);
  deltaBadge.layoutSizingHorizontal = "HUG";
  deltaBadge.layoutSizingVertical = "HUG";

  comp.appendChild(topRow);
  topRow.layoutSizingHorizontal = "FILL";
  topRow.layoutSizingVertical = "HUG";

  const priceT = txt(price, 24, "semi", c.onSurface, "Цена");
  comp.appendChild(priceT);
  priceT.layoutSizingHorizontal = "HUG";
  priceT.layoutSizingVertical = "HUG";

  // спарклайн — бары выровнены по низу
  const barHeights = isPositive
    ? [10, 14, 12, 18, 14, 22, 17, 26, 20, 30, 25, 36]
    : [36, 28, 32, 22, 28, 18, 24, 14, 20, 12, 16, 10];

  const chartFrame = figma.createFrame();
  chartFrame.name = "Спарклайн";
  chartFrame.layoutMode = "HORIZONTAL";
  chartFrame.itemSpacing = 4;
  chartFrame.counterAxisAlignItems = "MAX";
  chartFrame.fills = [];
  chartFrame.resize(1, 44);
  chartFrame.primaryAxisSizingMode = "FIXED";
  chartFrame.counterAxisSizingMode = "FIXED";

  for (const h of barHeights) {
    const bar = figma.createRectangle();
    bar.name = "Бар";
    bar.resize(18, h);
    bar.cornerRadius = 3;
    bar.fills = [{ type: "SOLID", color: accentColor, opacity: 0.75 }];
    chartFrame.appendChild(bar);
    bar.layoutGrow = 1;
    bar.layoutSizingVertical = "FIXED";
  }

  comp.appendChild(chartFrame);
  chartFrame.layoutSizingHorizontal = "FILL";
  chartFrame.layoutSizingVertical = "FIXED";

  return comp;
}

// ─── Price Plot (линейный график; на макете — понятные периоды 1Д/1Н/…; в API клиент шлёт from/to) ─
function buildPricePlot() {
  const comp = figma.createComponent();
  comp.name = "График · Динамика цены";
  comp.layoutMode = "VERTICAL";
  comp.itemSpacing = 6;
  comp.paddingTop = 8;
  comp.primaryAxisSizingMode = "AUTO";
  comp.counterAxisSizingMode = "FIXED";
  comp.resize(W - PAD * 2, 1);
  comp.fills = [];

  // ─── Данные ──────────────────────────────────────────────────────────────
  const yAxisW  = 38;
  const chartAreaW = (W - PAD * 2) - yAxisW;  // ширина области графика
  const chartH = 130;
  const raw = [2913,2907,2903,2910,2918,2925,2921,2928,2935,2938,2940,2940,2937,2926,2929,2935,2932,2937,2940,2936,2931,2928,2924,2929,2933,2927,2919,2910,2904,2900];
  const minV = Math.min(...raw);
  const maxV = Math.max(...raw);
  const rng   = maxV - minV;
  const n     = raw.length;
  const vpad   = 8;                       // внутренний отступ сверху/снизу в chartBox
  const innerH = chartH - vpad * 2;      // высота области рисования линии

  // xs — по всей ширине, ys — в пространстве chartInner (0..innerH)
  const xs = raw.map((_, i) => Math.round(i * (chartAreaW - 1) / (n - 1)));
  const ys = raw.map(v => Math.round((1 - (v - minV) / rng) * innerH));

  // ─── Строка «График + Ось Y» ─────────────────────────────────────────────
  const chartRow = figma.createFrame();
  chartRow.name = "График+ОсьY";
  chartRow.layoutMode = "HORIZONTAL";
  chartRow.itemSpacing = 0;
  chartRow.fills = [];
  chartRow.primaryAxisSizingMode = "FIXED";
  chartRow.counterAxisSizingMode = "FIXED";
  chartRow.resize(W - PAD * 2, chartH);

  // ─── Область графика (без auto-layout — абсолютное позиционирование) ─────
  const chartBox = figma.createFrame();
  chartBox.name = "Поле графика";
  chartBox.resize(chartAreaW, chartH);
  chartBox.fills = [{ type: "SOLID", color: c.surfaceContainer }];
  chartBox.clipsContent = true;
  chartBox.cornerRadius = shape.small;

  // chartInner: внутренний фрейм со смещением vpad — линия гарантированно не касается края
  const chartInner = figma.createFrame();
  chartInner.name = "Контент графика";
  chartInner.resize(chartAreaW, innerH);
  chartInner.fills = [];
  chartInner.clipsContent = false;
  chartBox.appendChild(chartInner);
  chartInner.x = 0; chartInner.y = vpad;

  // Заливка под линией (закрывается по нижнему краю chartInner)
  let areaD = "M " + xs[0] + " " + ys[0];
  for (let i = 1; i < n; i++) areaD += " L " + xs[i] + " " + ys[i];
  areaD += " L " + xs[n-1] + " " + innerH + " L " + xs[0] + " " + innerH + " Z";
  const areaVec = figma.createVector();
  areaVec.name = "Заливка";
  areaVec.vectorPaths = [{ windingRule: "NONZERO", data: areaD }];
  areaVec.fills = [{ type: "SOLID", color: c.primary, opacity: 0.12 }];
  areaVec.strokes = [];
  chartInner.appendChild(areaVec);
  areaVec.x = 0; areaVec.y = 0;

  // Линия графика
  let lineD = "M " + xs[0] + " " + ys[0];
  for (let i = 1; i < n; i++) lineD += " L " + xs[i] + " " + ys[i];
  const lineVec = figma.createVector();
  lineVec.name = "Линия";
  lineVec.vectorPaths = [{ windingRule: "NONE", data: lineD }];
  lineVec.fills = [];
  lineVec.strokes = [{ type: "SOLID", color: c.primary }];
  lineVec.strokeWeight = 2;
  lineVec.strokeCap = "ROUND";
  lineVec.strokeJoin = "ROUND";
  chartInner.appendChild(lineVec);
  lineVec.x = 0; lineVec.y = 0;

  // Вертикальная линия курсора (индекс 21 ≈ точка «29 мар»)
  const cursorIdx = 21;
  const cursorX   = xs[cursorIdx];
  const cursorY   = ys[cursorIdx];   // в системе chartInner (0..innerH)

  const vLine = figma.createRectangle();
  vLine.name = "Курсор · линия";
  vLine.resize(1, chartH);
  vLine.x = cursorX; vLine.y = 0;
  vLine.fills = [{ type: "SOLID", color: c.onSurfaceVariant, opacity: 0.3 }];
  vLine.strokes = [];
  chartBox.appendChild(vLine);

  // Точка: chartInner смещён на vpad → визуальный y = vpad + cursorY
  const dot = figma.createEllipse();
  dot.name = "Курсор · точка";
  dot.resize(10, 10);
  dot.x = cursorX - 5; dot.y = vpad + cursorY - 5;
  dot.fills = [{ type: "SOLID", color: c.primary }];
  dot.strokes = [{ type: "SOLID", color: c.onPrimary }];
  dot.strokeWeight = 2;
  chartBox.appendChild(dot);

  chartRow.appendChild(chartBox);
  chartBox.layoutSizingHorizontal = "FILL"; chartBox.layoutSizingVertical = "FILL";

  // ─── Ось Y (справа) ──────────────────────────────────────────────────────
  const yAxis = figma.createFrame();
  yAxis.name = "Ось Y";
  yAxis.layoutMode = "VERTICAL";
  yAxis.primaryAxisSizingMode = "FIXED";
  yAxis.counterAxisSizingMode = "FIXED";
  yAxis.resize(yAxisW, chartH);
  yAxis.fills = []; yAxis.itemSpacing = 0;
  yAxis.paddingLeft = 4;
  yAxis.primaryAxisAlignItems = "SPACE_BETWEEN";
  yAxis.counterAxisAlignItems = "MIN";
  for (const label of ["2940","2930","2920","2910","2900"]) {
    const t = txt(label, 9, "reg", c.onSurfaceVariant, label);
    yAxis.appendChild(t);
    t.layoutSizingHorizontal = "HUG"; t.layoutSizingVertical = "HUG";
  }
  chartRow.appendChild(yAxis);
  yAxis.layoutSizingHorizontal = "FIXED"; yAxis.layoutSizingVertical = "FILL";

  comp.appendChild(chartRow);
  chartRow.layoutSizingHorizontal = "FILL"; chartRow.layoutSizingVertical = "FIXED";

  // ─── Ось X (внизу) ───────────────────────────────────────────────────────
  const xAxis = figma.createFrame();
  xAxis.name = "Ось X";
  xAxis.layoutMode = "HORIZONTAL";
  xAxis.primaryAxisSizingMode = "FIXED"; xAxis.counterAxisSizingMode = "AUTO";
  xAxis.primaryAxisAlignItems = "SPACE_BETWEEN";
  xAxis.resize(W - PAD * 2, 1);
  xAxis.paddingRight = yAxisW;   // выравниваем с chartAreaW
  xAxis.fills = [];
  for (const label of ["12:00","15:00","18:00","21:00","29","09:00"]) {
    const t = txt(label, 9, "reg", c.onSurfaceVariant, label);
    xAxis.appendChild(t);
    t.layoutSizingHorizontal = "HUG"; t.layoutSizingVertical = "HUG";
  }
  comp.appendChild(xAxis);
  xAxis.layoutSizingHorizontal = "FILL"; xAxis.layoutSizingVertical = "HUG";

  // ─── Чипсы периода (под осью X; для пользователя — кратко; клиент сам считает from/to для API) ─
  const periodRow = figma.createFrame();
  periodRow.name = "Периоды";
  periodRow.layoutMode = "HORIZONTAL";
  periodRow.itemSpacing = 4;
  periodRow.fills = [];
  periodRow.primaryAxisSizingMode = "AUTO";
  periodRow.counterAxisSizingMode = "AUTO";
  for (let i = 0; i < 4; i++) {
    const label = ["1Д", "1Н", "1М", "1Г"][i];
    const pill = figma.createFrame();
    pill.name = `Период · ${label}`;
    pill.layoutMode = "HORIZONTAL";
    pill.paddingLeft = 14; pill.paddingRight = 14;
    pill.paddingTop = 6;   pill.paddingBottom = 6;
    pill.cornerRadius = shape.full;
    pill.primaryAxisSizingMode = "AUTO"; pill.counterAxisSizingMode = "AUTO";
    pill.primaryAxisAlignItems = "CENTER"; pill.counterAxisAlignItems = "CENTER";
    pill.fills = i === 0 ? tokenFill("primaryContainer") : [];
    pill.appendChild(txt(label, 12, i === 0 ? "semi" : "reg",
      i === 0 ? c.primary : c.onSurfaceVariant, "Текст"));
    pill.children[0].layoutSizingHorizontal = "HUG";
    pill.children[0].layoutSizingVertical = "HUG";
    periodRow.appendChild(pill);
    pill.layoutSizingHorizontal = "HUG"; pill.layoutSizingVertical = "HUG";
  }
  comp.appendChild(periodRow);
  periodRow.layoutSizingHorizontal = "HUG"; periodRow.layoutSizingVertical = "HUG";

  return comp;
}

// ─── Order Book / Стакан ──────────────────────────────────────────────────
function buildOrderBook() {
  const OBW = W - PAD * 2;
  const ROW_H = 28;

  function addOBRow(parent, price, vol, isBid) {
    const row = figma.createFrame();
    row.name = `${isBid ? "Бид" : "Аск"} · ${price}`;
    row.layoutMode = "HORIZONTAL";
    row.primaryAxisSizingMode = "FIXED"; row.counterAxisSizingMode = "FIXED";
    row.resize(OBW, ROW_H);
    row.paddingLeft = 8; row.paddingRight = 8;
    row.counterAxisAlignItems = "CENTER";
    row.fills = [{ type: "SOLID", color: isBid ? c.positive : c.negative, opacity: 0.08 }];
    const volT = txt(vol, 12, "reg", c.onSurface, "Объём");
    row.appendChild(volT); volT.layoutSizingHorizontal = "FILL"; volT.layoutSizingVertical = "HUG";
    const priceT = txt(price, 12, "semi", isBid ? c.positive : c.negative, "Цена");
    row.appendChild(priceT); priceT.layoutSizingHorizontal = "FILL"; priceT.layoutSizingVertical = "HUG";
    const volT2 = txt(isBid ? "" : vol, 12, "reg", c.onSurface, "Объём2");
    row.appendChild(volT2); volT2.layoutSizingHorizontal = "FILL"; volT2.layoutSizingVertical = "HUG";
    parent.appendChild(row);
    row.layoutSizingHorizontal = "FILL"; row.layoutSizingVertical = "FIXED";
  }

  const comp = figma.createComponent();
  comp.name = "Стакан · Спрос и предложение";
  comp.layoutMode = "VERTICAL";
  comp.primaryAxisSizingMode = "AUTO"; comp.counterAxisSizingMode = "FIXED";
  comp.resize(OBW, 1);
  comp.itemSpacing = 0;
  comp.fills = [];

  // Заголовок колонок
  const hdr = figma.createFrame();
  hdr.name = "Заголовки"; hdr.layoutMode = "HORIZONTAL";
  hdr.primaryAxisSizingMode = "FIXED"; hdr.counterAxisSizingMode = "FIXED";
  hdr.resize(OBW, 24); hdr.fills = []; hdr.paddingLeft = 8; hdr.paddingRight = 8;
  hdr.counterAxisAlignItems = "CENTER";
  for (const label of ["Объём", "Цена", "Объём"]) {
    const t = txt(label, 11, "med", c.onSurfaceVariant, "Заголовок");
    hdr.appendChild(t); t.layoutSizingHorizontal = "FILL"; t.layoutSizingVertical = "HUG";
  }
  comp.appendChild(hdr); hdr.layoutSizingHorizontal = "FILL"; hdr.layoutSizingVertical = "FIXED";

  // Аск-строки (продавцы): высшая цена сверху → низшая снизу (ближе к спреду)
  const asks = [
    { price: "305,00", vol: "1 200" },
    { price: "303,50", vol: "3 500" },
    { price: "302,00", vol: "5 000" },
    { price: "300,80", vol: "2 100" },
    { price: "299,20", vol:   "800" },
  ];
  for (const a of asks) addOBRow(comp, a.price, a.vol, false);

  // Спред — 2 строки: лучший Ask сверху, лучший Bid снизу
  function makeSpreadRow(label, price, color, bgColor) {
    const row = figma.createFrame();
    row.name = label;
    row.layoutMode = "HORIZONTAL";
    row.primaryAxisSizingMode = "FIXED"; row.counterAxisSizingMode = "FIXED";
    row.resize(OBW, 28);
    row.paddingLeft = 8; row.paddingRight = 8;
    row.counterAxisAlignItems = "CENTER";
    row.primaryAxisAlignItems = "SPACE_BETWEEN";
    row.fills = [{ type: "SOLID", color: bgColor, opacity: 0.15 }];

    const lbl = txt(label, 11, "med", color, "Лейбл");
    row.appendChild(lbl); lbl.layoutSizingHorizontal = "FILL"; lbl.layoutSizingVertical = "HUG";

    const val = txt(price, 12, "semi", color, "Цена");
    row.appendChild(val); val.layoutSizingHorizontal = "HUG"; val.layoutSizingVertical = "HUG";

    comp.appendChild(row); row.layoutSizingHorizontal = "FILL"; row.layoutSizingVertical = "FIXED";
  }

  makeSpreadRow("Лучший Ask", "299,20 ₽", c.negative, c.negative);
  makeSpreadRow("Лучший Bid", "298,00 ₽", c.positive, c.positive);

  // Бид-строки (покупатели)
  const bids = [
    { price: "298,00", vol: "4 200" }, { price: "297,50", vol: "6 800" },
    { price: "296,80", vol: "2 900" }, { price: "295,00", vol: "1 500" },
    { price: "293,20", vol:   "600" },
  ];
  for (const b of bids) addOBRow(comp, b.price, b.vol, true);

  return comp;
}

// ─── Buy/Sell Bar (M3 Filled/Tonal Buttons: 40dp, shape.full) ─────────────
function buildBuySellBar() {
  const comp = figma.createComponent();
  comp.name = "Бар · Купить / Продать";
  comp.layoutMode = "HORIZONTAL";
  comp.primaryAxisSizingMode = "FIXED";
  comp.counterAxisSizingMode = "AUTO";
  comp.resize(W, 1);
  comp.paddingLeft = PAD;
  comp.paddingRight = PAD;
  comp.paddingTop = 12;
  comp.paddingBottom = 24;
  comp.itemSpacing = 12;
  comp.fills = tokenFill("surface");
  comp.strokes = [{ type: "SOLID", color: c.outline }];
  comp.strokeTopWeight = 1;
  comp.strokeBottomWeight = 0;
  comp.strokeLeftWeight = 0;
  comp.strokeRightWeight = 0;

  const buyBtn = figma.createFrame();
  buyBtn.name = "Купить";
  buyBtn.layoutMode = "HORIZONTAL";
  buyBtn.primaryAxisAlignItems = "CENTER";
  buyBtn.counterAxisAlignItems = "CENTER";
  buyBtn.paddingTop = 10;
  buyBtn.paddingBottom = 10;
  buyBtn.cornerRadius = shape.full;
  buyBtn.fills = tokenFill("primary");
  buyBtn.primaryAxisSizingMode = "FIXED";
  buyBtn.counterAxisSizingMode = "FIXED";
  buyBtn.resize(1, mh.button);
  buyBtn.appendChild(txt("Купить", 14, "med", c.onPrimary, "Текст"));
  buyBtn.children[0].layoutSizingHorizontal = "HUG";
  buyBtn.children[0].layoutSizingVertical = "HUG";
  comp.appendChild(buyBtn);
  buyBtn.layoutGrow = 1;
  buyBtn.layoutSizingVertical = "FIXED";

  const sellBtn = figma.createFrame();
  sellBtn.name = "Продать";
  sellBtn.layoutMode = "HORIZONTAL";
  sellBtn.primaryAxisAlignItems = "CENTER";
  sellBtn.counterAxisAlignItems = "CENTER";
  sellBtn.paddingTop = 10;
  sellBtn.paddingBottom = 10;
  sellBtn.cornerRadius = shape.full;
  sellBtn.fills = [{ type: "SOLID", color: { r: 0.98, g: 0.91, b: 0.90 } }];
  sellBtn.strokes = [{ type: "SOLID", color: { r: 0.92, g: 0.78, b: 0.77 } }];
  sellBtn.strokeWeight = 1;
  sellBtn.primaryAxisSizingMode = "FIXED";
  sellBtn.counterAxisSizingMode = "FIXED";
  sellBtn.resize(1, mh.button);
  sellBtn.appendChild(txt("Продать", 14, "med", c.negative, "Текст"));
  sellBtn.children[0].layoutSizingHorizontal = "HUG";
  sellBtn.children[0].layoutSizingVertical = "HUG";
  comp.appendChild(sellBtn);
  sellBtn.layoutGrow = 1;
  sellBtn.layoutSizingVertical = "FIXED";

  return comp;
}
