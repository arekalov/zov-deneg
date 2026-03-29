/* zov-figma/build.mjs */
(async function () {
  try {
// Собрано: node zov-figma/build.mjs · Android 360×800 · ZOV денег

await figma.loadFontAsync({ family: "Inter", style: "Regular" });
await figma.loadFontAsync({ family: "Inter", style: "Medium" });
await figma.loadFontAsync({ family: "Inter", style: "Semi Bold" });

const W = 360;
const H = 800;
const PAD = 16;
const GAP_SCREENS = 40;

// M3 Shape tokens (corner radius, dp)
const shape = {
  none:       0,
  extraSmall: 4,
  small:      8,
  medium:     12,
  large:      16,
  extraLarge: 28,
  full:       200,
};

// M3 component heights (dp)
const mh = {
  button:    40,
  chip:      32,
  navBar:    80,
  topBar:    64,
  listItem1: 56,
  listItem2: 72,
  input:     56,
};
const N_SCREENS = 5;
const PAD_SCREEN_SHELL = 48;
const PAD_WORK_STACK = 56;
const STRIP_PAD_X = 40;
const DOC_ROW_W = N_SCREENS * W + (N_SCREENS - 1) * GAP_SCREENS;
const WORK_DOC_W = 1400; // ширина колонки документации (компоненты + палитра + типо)
const SCREENS_OFFSET_X = WORK_DOC_W + 80; // позиция секции экранов по горизонтали
const PALETTE_ROW_W = WORK_DOC_W - 2 * PAD_WORK_STACK - 2 * STRIP_PAD_X;

const c = {
  bg: { r: 0.96, g: 0.97, b: 0.98 },
  surface: { r: 1, g: 1, b: 1 },
  surfaceContainer: { r: 0.93, g: 0.94, b: 0.95 },
  primary: { r: 0.13, g: 0.59, b: 0.22 },
  onSurface: { r: 0.09, g: 0.1, b: 0.11 },
  onSurfaceVariant: { r: 0.45, g: 0.46, b: 0.48 },
  outline: { r: 0.88, g: 0.89, b: 0.9 },
  positive: { r: 0.05, g: 0.55, b: 0.28 },
  negative: { r: 0.72, g: 0.16, b: 0.14 },
  onPrimary: { r: 1, g: 1, b: 1 },
  primaryContainer: { r: 0.85, g: 0.94, b: 0.86 },
};

const fill = (color) => [{ type: "SOLID", color }];

const shadowCard = [
  {
    type: "DROP_SHADOW",
    color: { r: 0, g: 0, b: 0, a: 0.07 },
    offset: { x: 0, y: 2 },
    radius: 10,
    spread: 0,
    visible: true,
    blendMode: "NORMAL",
  },
];

function txt(content, size, weight, color, layerName) {
  const t = figma.createText();
  t.name = layerName;
  t.characters = content;
  t.fontSize = size;
  t.fontName =
    weight === "semi"
      ? { family: "Inter", style: "Semi Bold" }
      : weight === "med"
        ? { family: "Inter", style: "Medium" }
        : { family: "Inter", style: "Regular" };
  t.fills = fill(color);
  return t;
}

const tokenVars = {};

async function initColorTokens() {
  const coll = await figma.variables.createVariableCollectionAsync("ZOV палитра");
  const modeId = coll.modes[0].modeId;
  for (const key of Object.keys(c)) {
    const rgb = c[key];
    if (!rgb || typeof rgb.r !== "number") continue;
    const v = figma.variables.createVariable(key, coll, "COLOR");
    v.setValueForMode(modeId, rgb);
    tokenVars[key] = v;
  }
}

function tokenFill(key) {
  const v = tokenVars[key];
  const rgb = c[key];
  if (!v || !rgb || typeof rgb.r !== "number") return fill(rgb);
  const base = { type: "SOLID", color: rgb };
  return [figma.variables.setBoundVariableForPaint(base, "color", v)];
}

function strokeOutline() {
  return [{ type: "SOLID", color: c.outline }];
}

function buildPaletteStrip(parent) {
  const strip = figma.createFrame();
  strip.name = "Палитра · ZOV денег";
  strip.layoutMode = "VERTICAL";
  strip.itemSpacing = 32;
  strip.paddingLeft = 48;
  strip.paddingRight = 48;
  strip.paddingTop = 36;
  strip.paddingBottom = 40;
  strip.cornerRadius = 20;
  strip.fills = tokenFill("surface");
  strip.strokes = strokeOutline();
  strip.strokeWeight = 1;
  strip.effects = [
    {
      type: "DROP_SHADOW",
      color: { r: 0, g: 0, b: 0, a: 0.05 },
      offset: { x: 0, y: 4 },
      radius: 20,
      spread: 0,
      visible: true,
      blendMode: "NORMAL",
    },
  ];

  // Заголовок
  const header = figma.createFrame();
  header.name = "Заголовок";
  header.layoutMode = "VERTICAL";
  header.itemSpacing = 6;
  header.fills = [];
  const title = txt("Палитра · ZOV денег", 18, "semi", c.onSurface, "Заголовок");
  header.appendChild(title);
  title.layoutSizingHorizontal = "HUG";
  title.layoutSizingVertical = "HUG";
  const sub = txt(
    "Все цвета привязаны к локальным переменным — редактируйте в панели Variables",
    13, "reg", c.onSurfaceVariant, "Подзаголовок"
  );
  header.appendChild(sub);
  sub.layoutSizingHorizontal = "HUG";
  sub.layoutSizingVertical = "HUG";
  strip.appendChild(header);
  header.layoutSizingHorizontal = "HUG";
  header.layoutSizingVertical = "HUG";

  // Разделитель
  const divider = figma.createRectangle();
  divider.name = "Разделитель";
  divider.resize(1, 1);
  divider.fills = [{ type: "SOLID", color: c.outline }];
  strip.appendChild(divider);
  divider.layoutSizingHorizontal = "FILL";
  divider.layoutSizingVertical = "FIXED";

  // Группы токенов
  const groups = [
    { label: "Поверхности", keys: ["bg", "surface", "surfaceContainer"] },
    { label: "Акцент",      keys: ["primary", "onPrimary", "primaryContainer"] },
    { label: "Текст",       keys: ["onSurface", "onSurfaceVariant"] },
    { label: "Статус",      keys: ["positive", "negative"] },
    { label: "Граница",     keys: ["outline"] },
  ];

  for (const group of groups) {
    const groupFrame = figma.createFrame();
    groupFrame.name = `Группа · ${group.label}`;
    groupFrame.layoutMode = "VERTICAL";
    groupFrame.itemSpacing = 16;
    groupFrame.fills = [];

    const groupTitle = txt(group.label.toUpperCase(), 11, "semi", c.onSurfaceVariant, "Лейбл группы");
    groupFrame.appendChild(groupTitle);
    groupTitle.layoutSizingHorizontal = "HUG";
    groupTitle.layoutSizingVertical = "HUG";

    const rowFrame = figma.createFrame();
    rowFrame.name = "Образцы";
    rowFrame.layoutMode = "HORIZONTAL";
    rowFrame.itemSpacing = 20;
    rowFrame.fills = [];
    rowFrame.primaryAxisSizingMode = "AUTO";
    rowFrame.counterAxisSizingMode = "AUTO";

    for (const key of group.keys) {
      const col = c[key];
      if (!col || typeof col.r !== "number") continue;
      const cell = figma.createComponent();
      cell.name = `Токен · ${key}`;
      cell.layoutMode = "VERTICAL";
      cell.primaryAxisSizingMode = "AUTO";
      cell.counterAxisSizingMode = "AUTO";
      cell.itemSpacing = 10;
      cell.paddingLeft = 14;
      cell.paddingRight = 14;
      cell.paddingTop = 14;
      cell.paddingBottom = 14;
      cell.cornerRadius = 14;
      cell.fills = tokenFill("surfaceContainer");
      cell.strokes = strokeOutline();
      cell.strokeWeight = 1;

      const sw = figma.createRectangle();
      sw.resize(72, 72);
      sw.cornerRadius = 12;
      sw.fills = tokenFill(key);
      sw.strokes = strokeOutline();
      sw.strokeWeight = 1;
      cell.appendChild(sw);
      sw.layoutSizingHorizontal = "FIXED";
      sw.layoutSizingVertical = "FIXED";

      const labName = txt(key, 12, "semi", c.onSurface, "Токен");
      cell.appendChild(labName);
      labName.layoutSizingHorizontal = "HUG";
      labName.layoutSizingVertical = "HUG";

      rowFrame.appendChild(cell);
      cell.layoutSizingHorizontal = "HUG";
      cell.layoutSizingVertical = "HUG";
    }

    groupFrame.appendChild(rowFrame);
    rowFrame.layoutSizingHorizontal = "HUG";
    rowFrame.layoutSizingVertical = "HUG";

    strip.appendChild(groupFrame);
    groupFrame.layoutSizingHorizontal = "HUG";
    groupFrame.layoutSizingVertical = "HUG";
  }

  parent.appendChild(strip);
  strip.layoutSizingHorizontal = "FILL";
  strip.layoutSizingVertical = "HUG";
}


// ─── Typography showcase ────────────────────────────────────────────────────
function buildTypographyShowcase(parent) {
  const block = figma.createFrame();
  block.name = "Типографика · ZOV денег";
  block.layoutMode = "VERTICAL";
  block.itemSpacing = 32;
  block.paddingLeft = 48;
  block.paddingRight = 48;
  block.paddingTop = 36;
  block.paddingBottom = 40;
  block.cornerRadius = 20;
  block.fills = tokenFill("surface");
  block.strokes = strokeOutline();
  block.strokeWeight = 1;
  block.effects = [
    { type: "DROP_SHADOW", color: { r: 0, g: 0, b: 0, a: 0.05 },
      offset: { x: 0, y: 4 }, radius: 20, spread: 0, visible: true, blendMode: "NORMAL" },
  ];

  const header = txt("Типографика · ZOV денег", 18, "semi", c.onSurface, "Заголовок");
  block.appendChild(header);
  header.layoutSizingHorizontal = "HUG";
  header.layoutSizingVertical = "HUG";

  const divider = figma.createRectangle();
  divider.name = "Разделитель";
  divider.resize(1, 1);
  divider.fills = [{ type: "SOLID", color: c.outline }];
  block.appendChild(divider);
  divider.layoutSizingHorizontal = "FILL";
  divider.layoutSizingVertical = "FIXED";

  const scale = [
    { role: "Headline Large",  size: 32, weight: "reg",  sample: "Мой портфель" },
    { role: "Headline Medium", size: 28, weight: "reg",  sample: "Добро пожаловать" },
    { role: "Title Large",     size: 22, weight: "semi", sample: "История транзакций" },
    { role: "Title Medium",    size: 16, weight: "semi", sample: "Активы · Настройки" },
    { role: "Body Large",      size: 16, weight: "reg",  sample: "Цена закрытия: 297,45 ₽" },
    { role: "Body Medium",     size: 14, weight: "reg",  sample: "Сбербанк · 10 шт. · ср. 285 ₽" },
    { role: "Body Small",      size: 12, weight: "reg",  sample: "28 марта 2026, 14:32" },
    { role: "Label Large",     size: 14, weight: "med",  sample: "Купить · Войти · Продать" },
    { role: "Label Medium",    size: 12, weight: "med",  sample: "Уведомления · Тема · Валюта" },
    { role: "Label Small",     size: 11, weight: "med",  sample: "SBER · LKOH · ВХОД" },
  ];

  for (const item of scale) {
    const row = figma.createFrame();
    row.name = `Типо · ${item.role}`;
    row.layoutMode = "VERTICAL";
    row.itemSpacing = 4;
    row.paddingLeft = 16;
    row.paddingRight = 16;
    row.paddingTop = 14;
    row.paddingBottom = 14;
    row.cornerRadius = shape.small;
    row.fills = tokenFill("surfaceContainer");

    const meta = txt(`${item.role} · ${item.size}sp · ${item.weight === "semi" ? "SemiBold" : item.weight === "med" ? "Medium" : "Regular"}`, 11, "med", c.onSurfaceVariant, "Мета");
    row.appendChild(meta);
    meta.layoutSizingHorizontal = "HUG";
    meta.layoutSizingVertical = "HUG";

    const sample = txt(item.sample, item.size, item.weight, c.onSurface, "Образец");
    row.appendChild(sample);
    sample.layoutSizingHorizontal = "FILL";
    sample.layoutSizingVertical = "HUG";

    block.appendChild(row);
    row.layoutSizingHorizontal = "FILL";
    row.layoutSizingVertical = "HUG";
  }

  parent.appendChild(block);
  block.layoutSizingHorizontal = "FILL";
  block.layoutSizingVertical = "HUG";
}

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

// ─── Price Plot (линейный график с выбором периода) ──────────────────────
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

  // ─── Чипсы периода (под осью X) ──────────────────────────────────────────
  const periodRow = figma.createFrame();
  periodRow.name = "Периоды";
  periodRow.layoutMode = "HORIZONTAL";
  periodRow.itemSpacing = 4;
  periodRow.fills = [];
  periodRow.primaryAxisSizingMode = "AUTO";
  periodRow.counterAxisSizingMode = "AUTO";
  for (let i = 0; i < 4; i++) {
    const label = ["1Д","1Н","1М","1Г"][i];
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

const created = [];

let libraryArea;
let screensRow;

function getOrCreatePage(name) {
  let p = figma.root.children.find((x) => x.name === name);
  if (!p) {
    p = figma.createPage();
    p.name = name;
    created.push(p.id);
  }
  return p;
}

const workPage = getOrCreatePage("ZOV денег");

await figma.setCurrentPageAsync(workPage);
for (const ch of [...workPage.children]) {
  ch.remove();
}

try {
  await initColorTokens();
} catch (e) {
  figma.notify("Переменные не созданы, цвета без привязки: " + (e.message || e));
}

// ─── Корневая подложка ─────────────────────────────────────────────────────
const workStack = figma.createFrame();
workStack.name = "Подложка · документ";
workStack.layoutMode = "VERTICAL";
workStack.itemSpacing = 56;
workStack.paddingLeft = 56;
workStack.paddingRight = 56;
workStack.paddingTop = 48;
workStack.paddingBottom = 80;
workStack.fills = tokenFill("bg");
workStack.primaryAxisSizingMode = "AUTO";
workStack.counterAxisSizingMode = "FIXED";
workStack.resize(WORK_DOC_W, 4000); // высота — placeholder, AUTO пересчитает
workPage.appendChild(workStack);
workStack.x = 0;
workStack.y = 0;
created.push(workStack.id);

// ─── Палитра ───────────────────────────────────────────────────────────────
buildPaletteStrip(workStack);

// ─── Блок «Компоненты» с тенью ────────────────────────────────────────────
libraryArea = figma.createFrame();
libraryArea.name = "Подложка · библиотека компонентов";
libraryArea.layoutMode = "VERTICAL";
libraryArea.itemSpacing = 40;
libraryArea.paddingLeft = 48;
libraryArea.paddingRight = 48;
libraryArea.paddingTop = 40;
libraryArea.paddingBottom = 56;
libraryArea.cornerRadius = 20;
libraryArea.fills = tokenFill("surface");
libraryArea.strokes = strokeOutline();
libraryArea.strokeWeight = 1;
libraryArea.effects = [
  {
    type: "DROP_SHADOW",
    color: { r: 0, g: 0, b: 0, a: 0.05 },
    offset: { x: 0, y: 4 },
    radius: 20,
    spread: 0,
    visible: true,
    blendMode: "NORMAL",
  },
];
libraryArea.primaryAxisSizingMode = "AUTO";
libraryArea.counterAxisSizingMode = "AUTO";

const libTitle = txt("Компоненты · ZOV денег", 18, "semi", c.onSurface, "Заголовок");
libraryArea.appendChild(libTitle);
libTitle.layoutSizingHorizontal = "HUG";
libTitle.layoutSizingVertical = "HUG";

workStack.appendChild(libraryArea);
libraryArea.layoutSizingHorizontal = "FILL";
libraryArea.layoutSizingVertical = "HUG";

// ─── Хелпер: секционный разделитель + враппер ─────────────────────────────
function makeSection(sectionLabel) {
  const divider = figma.createRectangle();
  divider.name = `Разделитель · ${sectionLabel}`;
  divider.resize(1, 1);
  divider.fills = [{ type: "SOLID", color: c.outline }];
  libraryArea.appendChild(divider);
  divider.layoutSizingHorizontal = "FILL";
  divider.layoutSizingVertical = "FIXED";

  const sec = figma.createFrame();
  sec.name = `Секция · ${sectionLabel}`;
  sec.layoutMode = "VERTICAL";
  sec.itemSpacing = 20;
  sec.fills = [];
  sec.primaryAxisSizingMode = "AUTO";
  sec.counterAxisSizingMode = "AUTO";

  const secLabel = txt(sectionLabel.toUpperCase(), 11, "semi", c.onSurfaceVariant, "Метка");
  sec.appendChild(secLabel);
  secLabel.layoutSizingHorizontal = "HUG";
  secLabel.layoutSizingVertical = "HUG";

  // WRAP нужна фиксированная ширина — берём ширину libraryArea минус её паддинги
  const wrapW = WORK_DOC_W - 2 * PAD_WORK_STACK - 2 * 48;
  const wrap = figma.createFrame();
  wrap.name = "Элементы";
  wrap.layoutMode = "HORIZONTAL";
  wrap.layoutWrap = "WRAP";
  wrap.itemSpacing = 20;
  wrap.counterAxisSpacing = 20;
  wrap.fills = [];
  wrap.primaryAxisSizingMode = "FIXED";
  wrap.counterAxisSizingMode = "AUTO";
  wrap.resize(wrapW, 1);
  sec.appendChild(wrap);
  wrap.layoutSizingHorizontal = "FILL";
  wrap.layoutSizingVertical = "HUG";

  libraryArea.appendChild(sec);
  sec.layoutSizingHorizontal = "FILL";
  sec.layoutSizingVertical = "HUG";

  function dropComp(node) {
    wrap.appendChild(node);
    node.layoutSizingHorizontal = "HUG";
    node.layoutSizingVertical = "HUG";
    created.push(node.id);
  }
  return { dropComp };
}

// ─── Навигация ─────────────────────────────────────────────────────────────
const { dropComp: dropNav } = makeSection("Навигация");
const nav1 = buildNavBar(0);
const nav2 = buildNavBar(1);
const nav3 = buildNavBar(2);
dropNav(nav1);
dropNav(nav2);
dropNav(nav3);

// ─── Карточки ──────────────────────────────────────────────────────────────
const { dropComp: dropCard } = makeSection("Карточки");
const sumComp = buildSummaryCard();
dropCard(sumComp);

// ─── Строки ────────────────────────────────────────────────────────────────
const { dropComp: dropRow } = makeSection("Строки");
const assetSber = buildAssetRow("SBER", "10 шт. · ср. 285 ₽", "29 812 ₽", "+1 312 ₽ · +4,6%");
const assetLkoh = buildAssetRow("LKOH", "5 шт. · ср. 6 200 ₽", "32 710 ₽", "+1 710 ₽");
dropRow(assetSber);
dropRow(assetLkoh);
const txRow = buildTransactionRow();
dropRow(txRow);
const setNotif = buildSettingRow("Уведомления");
const setTheme = buildSettingRow("Тема");
const setCurr = buildSettingRow("Валюта отображения");
const setBio = buildSettingRow("Вход по отпечатку");
dropRow(setNotif);
dropRow(setTheme);
dropRow(setCurr);
dropRow(setBio);

// ─── Ввод ──────────────────────────────────────────────────────────────────
const { dropComp: dropInput } = makeSection("Ввод");
const inputPhone = buildInputField("Телефон или e-mail", "user@example.com");
const inputPass = buildInputField("Пароль", "••••••••");
dropInput(inputPhone);
dropInput(inputPass);
const btnPrimary = buildButtonPrimary("Войти");
const btnSecondary = buildButtonSecondary("Войти по биометрии");
dropInput(btnPrimary);
dropInput(btnSecondary);
const pinDots = buildPinDotsComp();
const pinPad = buildPinPad();
dropInput(pinDots);
dropInput(pinPad);

// ─── Фильтры ───────────────────────────────────────────────────────────────
const { dropComp: dropChip } = makeSection("Фильтры");
const chipAll = buildChipFixed("Все", true);
const chipBuy = buildChipFixed("Покупки", false);
const chipSell = buildChipFixed("Продажи", false);
dropChip(chipAll);
dropChip(chipBuy);
dropChip(chipSell);

// ─── Шапки ─────────────────────────────────────────────────────────────────
const { dropComp: dropHdr } = makeSection("Шапки");
const hdrMain = buildHeaderWithProfile("Главная");
const hdrSearch = buildHeaderWithProfile("Поиск актива");
const hdrHistory = buildHeaderSimple("История транзакций");
const hdrProfile = buildHeaderSimple("Профиль и настройки");
const hdrSber = buildHeaderSimple("SBER · Сбербанк");
const hdrBuyComp = buildHeaderSimple("Купить SBER");
const hdrEditProfile = buildHeaderSimple("Редактирование профиля");
const hdrChangePin = buildHeaderSimple("Смена PIN-кода");
const hdrRegister = buildHeaderSimple("Регистрация");
const hdrDepositComp = buildHeaderSimple("Пополнение счёта");
dropHdr(hdrMain);
dropHdr(hdrSearch);
dropHdr(hdrHistory);
dropHdr(hdrProfile);
dropHdr(hdrSber);
dropHdr(hdrBuyComp);
dropHdr(hdrEditProfile);
dropHdr(hdrChangePin);
dropHdr(hdrRegister);
dropHdr(hdrDepositComp);

// ─── Действия ──────────────────────────────────────────────────────────────
const { dropComp: dropAction } = makeSection("Действия");
const buySellBar = buildBuySellBar();
dropAction(buySellBar);

// ─── Графики ───────────────────────────────────────────────────────────────
const { dropComp: dropChart } = makeSection("Графики");
const pricePlot = buildPricePlot();
const orderBook = buildOrderBook();
dropChart(pricePlot);
dropChart(orderBook);

// ─── Типографика ───────────────────────────────────────────────────────────
buildTypographyShowcase(workStack);

// ─── Подложка экранов (рядом с документацией, справа) ─────────────────────
const screensShell = figma.createFrame();
screensShell.name = "Подложка · экраны";
screensShell.layoutMode = "VERTICAL";
screensShell.itemSpacing = 32;
screensShell.paddingLeft = 48;
screensShell.paddingRight = 48;
screensShell.paddingTop = 40;
screensShell.paddingBottom = 56;
screensShell.cornerRadius = 20;
screensShell.fills = tokenFill("surface");
screensShell.strokes = strokeOutline();
screensShell.strokeWeight = 1;
screensShell.primaryAxisSizingMode = "AUTO";
screensShell.counterAxisSizingMode = "AUTO";
screensShell.effects = [
  {
    type: "DROP_SHADOW",
    color: { r: 0, g: 0, b: 0, a: 0.05 },
    offset: { x: 0, y: 4 },
    radius: 20,
    spread: 0,
    visible: true,
    blendMode: "NORMAL",
  },
];

const scrTitle = txt("Экраны · Android Compact 360×800", 18, "semi", c.onSurface, "Заголовок секции");
screensShell.appendChild(scrTitle);
scrTitle.layoutSizingHorizontal = "HUG";
scrTitle.layoutSizingVertical = "HUG";

function makeScreensRow(label) {
  const sec = figma.createFrame();
  sec.name = `Ряд · ${label}`;
  sec.layoutMode = "VERTICAL";
  sec.itemSpacing = 12;
  sec.fills = [];
  sec.primaryAxisSizingMode = "AUTO";
  sec.counterAxisSizingMode = "AUTO";

  const lbl = txt(label.toUpperCase(), 11, "semi", c.onSurfaceVariant, "Метка");
  sec.appendChild(lbl);
  lbl.layoutSizingHorizontal = "HUG";
  lbl.layoutSizingVertical = "HUG";

  const row = figma.createFrame();
  row.name = "Экраны";
  row.layoutMode = "HORIZONTAL";
  row.itemSpacing = GAP_SCREENS;
  row.primaryAxisSizingMode = "AUTO";
  row.counterAxisSizingMode = "AUTO";
  row.fills = [];
  sec.appendChild(row);
  row.layoutSizingHorizontal = "HUG";
  row.layoutSizingVertical = "HUG";

  screensShell.appendChild(sec);
  sec.layoutSizingHorizontal = "HUG";
  sec.layoutSizingVertical = "HUG";

  screensRow = row;
}

makeScreensRow("Онбординг");

workPage.appendChild(screensShell);
screensShell.x = SCREENS_OFFSET_X;
screensShell.y = 0;
created.push(screensShell.id);

await figma.setCurrentPageAsync(workPage);

function androidScreen(title) {
  const root = figma.createFrame();
  root.name = `Android Compact ${W}×${H} — ${title}`;
  root.resize(W, 1);
  root.layoutMode = "VERTICAL";
  root.itemSpacing = 0;
  root.primaryAxisSizingMode = "AUTO";   // растягивается под контент
  root.counterAxisSizingMode = "FIXED";
  root.fills = tokenFill("bg");
  root.clipsContent = false;
  screensRow.appendChild(root);
  root.layoutSizingHorizontal = "FIXED";
  root.layoutSizingVertical = "HUG";
  created.push(root.id);
  return root;
}

// Android status bar: время слева, батарея+сигнал справа
function statusStrip(parent) {
  const s = figma.createFrame();
  s.name = "Status Bar";
  s.layoutMode = "HORIZONTAL";
  s.primaryAxisSizingMode = "FIXED";
  s.counterAxisSizingMode = "FIXED";
  s.resize(W, 24);
  s.paddingLeft = PAD;
  s.paddingRight = PAD;
  s.primaryAxisAlignItems = "CENTER";
  s.fills = tokenFill("bg");

  // Время
  const time = txt("9:41", 12, "semi", c.onSurface, "Время");
  s.appendChild(time);
  time.layoutSizingHorizontal = "FILL";
  time.layoutSizingVertical = "HUG";

  // Правая панель: сигнал + батарея
  const right = figma.createFrame();
  right.name = "Индикаторы";
  right.layoutMode = "HORIZONTAL";
  right.itemSpacing = 6;
  right.counterAxisAlignItems = "CENTER";
  right.fills = [];
  right.primaryAxisSizingMode = "AUTO";
  right.counterAxisSizingMode = "AUTO";

  // Сигнал: 4 бара разной высоты
  const sigFrame = figma.createFrame();
  sigFrame.name = "Сигнал";
  sigFrame.layoutMode = "HORIZONTAL";
  sigFrame.itemSpacing = 2;
  sigFrame.counterAxisAlignItems = "MAX";
  sigFrame.fills = [];
  sigFrame.primaryAxisSizingMode = "AUTO";
  sigFrame.counterAxisSizingMode = "FIXED";
  sigFrame.resize(1, 12);
  for (const h of [4, 6, 9, 12]) {
    const bar = figma.createRectangle();
    bar.resize(2, h);
    bar.cornerRadius = 1;
    bar.fills = [{ type: "SOLID", color: c.onSurface }];
    sigFrame.appendChild(bar);
    bar.layoutSizingHorizontal = "FIXED";
    bar.layoutSizingVertical = "FIXED";
  }
  right.appendChild(sigFrame);
  sigFrame.layoutSizingHorizontal = "HUG";
  sigFrame.layoutSizingVertical = "FIXED";

  // Батарея: прямоугольник-корпус + заливка
  const batOuter = figma.createFrame();
  batOuter.name = "Батарея";
  batOuter.layoutMode = "HORIZONTAL";
  batOuter.counterAxisAlignItems = "CENTER";
  batOuter.paddingLeft = 2;
  batOuter.paddingTop = 2;
  batOuter.paddingBottom = 2;
  batOuter.primaryAxisSizingMode = "FIXED";
  batOuter.counterAxisSizingMode = "FIXED";
  batOuter.resize(20, 11);
  batOuter.cornerRadius = 2;
  batOuter.fills = [];
  batOuter.strokes = [{ type: "SOLID", color: c.onSurface }];
  batOuter.strokeWeight = 1.5;

  const batFill = figma.createRectangle();
  batFill.resize(13, 7);
  batFill.cornerRadius = 1;
  batFill.fills = [{ type: "SOLID", color: c.onSurface }];
  batOuter.appendChild(batFill);
  batFill.layoutSizingHorizontal = "FIXED";
  batFill.layoutSizingVertical = "FIXED";

  right.appendChild(batOuter);
  batOuter.layoutSizingHorizontal = "FIXED";
  batOuter.layoutSizingVertical = "FIXED";

  s.appendChild(right);
  right.layoutSizingHorizontal = "HUG";
  right.layoutSizingVertical = "HUG";

  parent.appendChild(s);
  s.layoutSizingHorizontal = "FILL";
  s.layoutSizingVertical = "FIXED";
}

function scrollBody(parent) {
  const body = figma.createFrame();
  body.name = "Контент";
  body.layoutMode = "VERTICAL";
  body.itemSpacing = 12;
  body.paddingLeft = PAD;
  body.paddingRight = PAD;
  body.paddingTop = 16;
  body.paddingBottom = 16;
  body.fills = [];
  body.primaryAxisSizingMode = "AUTO";
  body.counterAxisSizingMode = "FIXED";
  parent.appendChild(body);
  body.layoutSizingHorizontal = "FILL";
  body.layoutSizingVertical = "HUG";
  return body;
}

function appendInstance(parent, mainComp) {
  const inst = mainComp.createInstance();
  parent.appendChild(inst);
  inst.layoutSizingHorizontal = "FILL";
  inst.layoutSizingVertical = "HUG";
  return inst;
}

// Снекбар — абсолютно поверх экрана, над nav-баром
// type: "success" | "error"  bottomOffset: дополнительный отступ снизу (для экранов с доп. панелями)
function addSnackbar(screenFrame, type, message, bottomOffset) {
  const SNACK_H   = 48;
  const NAV_H     = mh.navBar;   // 80px (из constants.js)
  const MARGIN    = 12;
  const extra     = bottomOffset || 0;

  const bar = figma.createFrame();
  bar.name = `Снекбар · ${message}`;
  bar.layoutMode = "HORIZONTAL";
  bar.counterAxisAlignItems = "CENTER";
  bar.itemSpacing = 10;
  bar.paddingLeft = 16; bar.paddingRight = 16;
  bar.paddingTop = 0;   bar.paddingBottom = 0;
  bar.cornerRadius = shape.medium;
  bar.primaryAxisSizingMode = "FIXED";
  bar.counterAxisSizingMode = "FIXED";
  bar.resize(W - PAD * 2, SNACK_H);
  const clr = type === "success" ? c.positive : c.negative;
  bar.fills = tokenFill("surface");
  bar.strokes = [{ type: "SOLID", color: clr }];
  bar.strokeWeight = 1.5;
  bar.effects = shadowCard;

  // Иконка
  const icon = txt(type === "success" ? "✓" : "✕", 15, "semi", clr, "Иконка");
  bar.appendChild(icon);
  icon.layoutSizingHorizontal = "HUG"; icon.layoutSizingVertical = "HUG";

  // Текст
  const msg = txt(message, 13, "reg", c.onSurface, "Текст");
  bar.appendChild(msg);
  msg.layoutSizingHorizontal = "FILL"; msg.layoutSizingVertical = "HUG";

  screenFrame.appendChild(bar);
  bar.layoutPositioning = "ABSOLUTE";
  bar.x = PAD;
  bar.y = H - NAV_H - MARGIN - SNACK_H - extra;

  created.push(bar.id);
  return bar;
}

const scrLogin = androidScreen("Вход");
statusStrip(scrLogin);

const loginBody = scrollBody(scrLogin);
loginBody.primaryAxisAlignItems = "CENTER";
loginBody.counterAxisAlignItems = "CENTER";
loginBody.paddingTop = 48;
loginBody.paddingBottom = 32;
loginBody.itemSpacing = 24;

// ─── Приветствие ─────────────────────────────────────────────────────────────
const greetBlock = figma.createFrame();
greetBlock.name = "Приветствие";
greetBlock.layoutMode = "VERTICAL";
greetBlock.primaryAxisAlignItems = "CENTER";
greetBlock.counterAxisAlignItems = "CENTER";
greetBlock.itemSpacing = 6;
greetBlock.fills = [];
greetBlock.primaryAxisSizingMode = "AUTO";
greetBlock.counterAxisSizingMode = "AUTO";
greetBlock.appendChild(txt("Добро пожаловать", 22, "semi", c.onSurface, "Заголовок"));
greetBlock.children[0].layoutSizingHorizontal = "HUG";
greetBlock.children[0].layoutSizingVertical = "HUG";
greetBlock.appendChild(txt("Введите PIN-код для входа", 14, "reg", c.onSurfaceVariant, "Подзаголовок"));
greetBlock.children[1].layoutSizingHorizontal = "HUG";
greetBlock.children[1].layoutSizingVertical = "HUG";
loginBody.appendChild(greetBlock);
greetBlock.layoutSizingHorizontal = "HUG";
greetBlock.layoutSizingVertical = "HUG";

// ─── PIN-точки ───────────────────────────────────────────────────────────────
const dotsInst = appendInstance(loginBody, pinDots);
dotsInst.layoutSizingHorizontal = "HUG";
dotsInst.layoutSizingVertical = "HUG";

// ─── PIN-клавиатура (FILL = растягивается на ширину тела) ─────────────────────
appendInstance(loginBody, pinPad);

// ─── Кнопка биометрии ────────────────────────────────────────────────────────
const bioBtn = figma.createFrame();
bioBtn.name = "Кнопка · Биометрия";
bioBtn.layoutMode = "HORIZONTAL";
bioBtn.primaryAxisAlignItems = "CENTER";
bioBtn.counterAxisAlignItems = "CENTER";
bioBtn.primaryAxisSizingMode = "FIXED";
bioBtn.counterAxisSizingMode = "FIXED";
bioBtn.resize(W - PAD * 2, mh.button);
bioBtn.paddingLeft = 24;
bioBtn.paddingRight = 24;
bioBtn.itemSpacing = 8;
bioBtn.cornerRadius = shape.full;
bioBtn.fills = tokenFill("primaryContainer");

const fpIcon = buildIcon("fingerprint", 20, c.primary);
bioBtn.appendChild(fpIcon);
fpIcon.layoutSizingHorizontal = "FIXED";
fpIcon.layoutSizingVertical = "FIXED";

bioBtn.appendChild(txt("Войти по биометрии", 14, "med", c.primary, "Текст"));
bioBtn.children[bioBtn.children.length - 1].layoutSizingHorizontal = "HUG";
bioBtn.children[bioBtn.children.length - 1].layoutSizingVertical = "HUG";

loginBody.appendChild(bioBtn);
bioBtn.layoutSizingHorizontal = "FILL";
bioBtn.layoutSizingVertical = "FIXED";

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

makeScreensRow("Главная");
const scrMain = androidScreen("Главная");
statusStrip(scrMain);
appendInstance(scrMain, hdrMain);
scrMain.children[1].layoutSizingHorizontal = "FILL";
scrMain.children[1].layoutSizingVertical = "HUG";
const bodyMain = scrollBody(scrMain);
appendInstance(bodyMain, sumComp);
bodyMain.children[bodyMain.children.length - 1].layoutSizingHorizontal = "FILL";

// ─── Карточка брокерского счёта ──────────────────────────────────────────
const balanceCard = figma.createFrame();
balanceCard.name = "Брокерский счёт · Плашка";
balanceCard.layoutMode = "HORIZONTAL";
balanceCard.primaryAxisSizingMode = "FIXED";
balanceCard.counterAxisSizingMode = "AUTO";
balanceCard.resize(W - PAD * 2, 1);
balanceCard.paddingLeft = 16; balanceCard.paddingRight = 12;
balanceCard.paddingTop = 14; balanceCard.paddingBottom = 14;
balanceCard.itemSpacing = 8;
balanceCard.cornerRadius = shape.medium;
balanceCard.fills = tokenFill("surface");
balanceCard.strokes = [{ type: "SOLID", color: c.outline }];
balanceCard.strokeWeight = 1;
balanceCard.counterAxisAlignItems = "CENTER";

const balanceLeft = figma.createFrame();
balanceLeft.name = "Левая часть";
balanceLeft.layoutMode = "VERTICAL";
balanceLeft.itemSpacing = 2;
balanceLeft.fills = [];
balanceLeft.primaryAxisSizingMode = "AUTO";
balanceLeft.counterAxisSizingMode = "AUTO";
balanceLeft.appendChild(txt("Брокерский счёт", 12, "reg", c.onSurfaceVariant, "Лейбл"));
balanceLeft.children[0].layoutSizingHorizontal = "HUG";
balanceLeft.children[0].layoutSizingVertical = "HUG";
balanceLeft.appendChild(txt("45 320 ₽", 16, "semi", c.onSurface, "Значение"));
balanceLeft.children[1].layoutSizingHorizontal = "HUG";
balanceLeft.children[1].layoutSizingVertical = "HUG";
balanceCard.appendChild(balanceLeft);
balanceLeft.layoutSizingHorizontal = "FILL";
balanceLeft.layoutSizingVertical = "HUG";

// Стрелка-шеврон
const chevron = figma.createVector();
chevron.name = "Шеврон";
chevron.resize(20, 20);
chevron.vectorPaths = [{
  windingRule: "NONZERO",
  data: "M 7 4 L 14 10 L 7 16"
}];
chevron.strokes = [{ type: "SOLID", color: c.onSurfaceVariant }];
chevron.strokeWeight = 2;
chevron.strokeCap = "ROUND";
chevron.strokeJoin = "ROUND";
chevron.fills = [];
balanceCard.appendChild(chevron);
chevron.layoutSizingHorizontal = "FIXED";
chevron.layoutSizingVertical = "FIXED";

bodyMain.appendChild(balanceCard);
balanceCard.layoutSizingHorizontal = "FILL";
balanceCard.layoutSizingVertical = "HUG";
created.push(balanceCard.id);

bodyMain.appendChild(txt("Активы", 16, "semi", c.onSurface, "Секция"));
bodyMain.children[bodyMain.children.length - 1].layoutSizingHorizontal = "HUG";
appendInstance(bodyMain, assetSber);
appendInstance(bodyMain, assetLkoh);
appendInstance(scrMain, nav1);
scrMain.children[scrMain.children.length - 1].layoutSizingHorizontal = "FILL";
scrMain.children[scrMain.children.length - 1].layoutSizingVertical = "HUG";

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

makeScreensRow("Поиск актива");
const scrSearch = androidScreen("Поиск актива");
statusStrip(scrSearch);
appendInstance(scrSearch, hdrSearch);
scrSearch.children[1].layoutSizingHorizontal = "FILL";
scrSearch.children[1].layoutSizingVertical = "HUG";
const bodySearch = scrollBody(scrSearch);

// ─── Поле поиска ─────────────────────────────────────────────────────────────
const searchField = figma.createFrame();
searchField.name = "Поле поиска";
searchField.layoutMode = "HORIZONTAL";
searchField.paddingLeft = 14;
searchField.paddingRight = 14;
searchField.itemSpacing = 8;
searchField.cornerRadius = shape.medium;
searchField.counterAxisAlignItems = "CENTER";
searchField.fills = tokenFill("surfaceContainer");
searchField.strokes = [{ type: "SOLID", color: c.outline }];
searchField.strokeWeight = 1;
searchField.primaryAxisSizingMode = "FIXED";
searchField.counterAxisSizingMode = "FIXED";
searchField.resize(W - PAD * 2, 48);
const srchIcon = buildIcon("search", 20, c.onSurfaceVariant);
searchField.appendChild(srchIcon);
srchIcon.layoutSizingHorizontal = "FIXED";
srchIcon.layoutSizingVertical = "FIXED";
searchField.appendChild(txt("Название или тикер…", 15, "reg", c.onSurfaceVariant, "Плейсхолдер"));
searchField.children[1].layoutSizingHorizontal = "FILL";
searchField.children[1].layoutSizingVertical = "HUG";
bodySearch.appendChild(searchField);
searchField.layoutSizingHorizontal = "FILL";
searchField.layoutSizingVertical = "FIXED";

// ─── Фильтры ─────────────────────────────────────────────────────────────────
const filterRow = figma.createFrame();
filterRow.name = "Фильтры";
filterRow.layoutMode = "HORIZONTAL";
filterRow.itemSpacing = 8;
filterRow.fills = [];
filterRow.primaryAxisSizingMode = "AUTO";
filterRow.counterAxisSizingMode = "AUTO";
bodySearch.appendChild(filterRow);
filterRow.layoutSizingHorizontal = "FILL";
filterRow.layoutSizingVertical = "HUG";

const filterLabels = ["Все", "Акции", "Облигации", "ETF"];
for (let i = 0; i < filterLabels.length; i++) {
  const chip = figma.createFrame();
  chip.name = `Чип · ${filterLabels[i]}`;
  chip.layoutMode = "HORIZONTAL";
  chip.paddingLeft = 16;
  chip.paddingRight = 16;
  chip.cornerRadius = shape.small;
  chip.primaryAxisSizingMode = "AUTO";
  chip.counterAxisSizingMode = "FIXED";
  chip.resize(1, mh.chip);
  chip.primaryAxisAlignItems = "CENTER";
  chip.counterAxisAlignItems = "CENTER";
  chip.fills = i === 0 ? tokenFill("primaryContainer") : tokenFill("surfaceContainer");
  if (i !== 0) {
    chip.strokes = [{ type: "SOLID", color: c.outline }];
    chip.strokeWeight = 1;
  }
  chip.appendChild(txt(filterLabels[i], 14, i === 0 ? "semi" : "med",
    i === 0 ? c.primary : c.onSurfaceVariant, "Текст"));
  chip.children[0].layoutSizingHorizontal = "HUG";
  chip.children[0].layoutSizingVertical = "HUG";
  filterRow.appendChild(chip);
  chip.layoutSizingHorizontal = "HUG";
  chip.layoutSizingVertical = "FIXED";
}

// ─── Рыночные активы ─────────────────────────────────────────────────────────
bodySearch.appendChild(txt("Популярные акции", 16, "semi", c.onSurface, "Секция"));
bodySearch.children[bodySearch.children.length - 1].layoutSizingHorizontal = "HUG";
bodySearch.children[bodySearch.children.length - 1].layoutSizingVertical = "HUG";

const marketAssets = [
  ["GAZP", "Газпром · энергетика", "167,4 ₽", "+0,8%", true],
  ["YNDX", "Яндекс · технологии", "3 250 ₽", "−1,2%", false],
  ["VTBR", "ВТБ · финансы", "0,026 ₽", "+2,1%", true],
  ["NVTK", "НоваТЭК · энергетика", "812,0 ₽", "+0,4%", true],
  ["ROSN", "Роснефть · энергетика", "534,5 ₽", "−0,6%", false],
];

for (const [ticker, sector, price, delta, isPos] of marketAssets) {
  const row = figma.createFrame();
  row.name = `Актив · ${ticker}`;
  row.layoutMode = "HORIZONTAL";
  row.primaryAxisSizingMode = "FIXED";
  row.counterAxisSizingMode = "FIXED";
  row.resize(W - PAD * 2, mh.listItem2);
  row.paddingLeft = 16;
  row.paddingRight = 16;
  row.paddingTop = 14;
  row.paddingBottom = 14;
  row.itemSpacing = 16;
  row.primaryAxisAlignItems = "CENTER";
  row.cornerRadius = shape.medium;
  row.fills = tokenFill("surface");
  row.strokes = [{ type: "SOLID", color: c.outline }];
  row.strokeWeight = 1;

  const left = figma.createFrame();
  left.name = "Слева";
  left.layoutMode = "VERTICAL";
  left.itemSpacing = 4;
  left.fills = [];
  left.primaryAxisSizingMode = "AUTO";
  left.counterAxisSizingMode = "AUTO";
  left.appendChild(txt(ticker, 14, "semi", c.onSurface, "Тикер"));
  left.children[0].layoutSizingHorizontal = "HUG";
  left.children[0].layoutSizingVertical = "HUG";
  left.appendChild(txt(sector, 12, "reg", c.onSurfaceVariant, "Сектор"));
  left.children[1].layoutSizingHorizontal = "HUG";
  left.children[1].layoutSizingVertical = "HUG";
  row.appendChild(left);
  left.layoutSizingHorizontal = "FILL";
  left.layoutSizingVertical = "HUG";

  const right = figma.createFrame();
  right.name = "Справа";
  right.layoutMode = "VERTICAL";
  right.itemSpacing = 2;
  right.primaryAxisAlignItems = "MAX";
  right.counterAxisAlignItems = "MAX";
  right.fills = [];
  right.primaryAxisSizingMode = "AUTO";
  right.counterAxisSizingMode = "AUTO";
  right.appendChild(txt(price, 14, "semi", c.onSurface, "Цена"));
  right.children[0].layoutSizingHorizontal = "HUG";
  right.children[0].layoutSizingVertical = "HUG";
  right.appendChild(txt(delta, 12, "med", isPos ? c.positive : c.negative, "Дельта"));
  right.children[1].layoutSizingHorizontal = "HUG";
  right.children[1].layoutSizingVertical = "HUG";
  row.appendChild(right);
  right.layoutSizingHorizontal = "HUG";
  right.layoutSizingVertical = "HUG";

  bodySearch.appendChild(row);
  row.layoutSizingHorizontal = "FILL";
  row.layoutSizingVertical = "FIXED";
  created.push(row.id);
}

appendInstance(scrSearch, nav2);
scrSearch.children[scrSearch.children.length - 1].layoutSizingHorizontal = "FILL";
scrSearch.children[scrSearch.children.length - 1].layoutSizingVertical = "HUG";

makeScreensRow("История транзакций");
const scrHist = androidScreen("История транзакций");
statusStrip(scrHist);
appendInstance(scrHist, hdrHistory);
scrHist.children[1].layoutSizingHorizontal = "FILL";
scrHist.children[1].layoutSizingVertical = "HUG";
const bodyHist = scrollBody(scrHist);

// ─── Чипы фильтрации ─────────────────────────────────────────────────────────
const chipRow = figma.createFrame();
chipRow.name = "Чипсы фильтрации";
chipRow.layoutMode = "HORIZONTAL";
chipRow.itemSpacing = 8;
chipRow.fills = [];
chipRow.primaryAxisSizingMode = "AUTO";
chipRow.counterAxisSizingMode = "AUTO";
bodyHist.appendChild(chipRow);
chipRow.layoutSizingHorizontal = "FILL";
chipRow.layoutSizingVertical = "HUG";

for (const chipComp of [chipAll, chipBuy, chipSell]) {
  const inst = chipComp.createInstance();
  chipRow.appendChild(inst);
  inst.layoutSizingHorizontal = "HUG";
  inst.layoutSizingVertical = "HUG";
}

// ─── Транзакции ──────────────────────────────────────────────────────────────
const txData = [
  { type: "Покупка",    ticker: "SBER", time: "28 марта 2026, 14:32", details: "10 шт. · цена 298,12 ₽", amount: "+2 981 ₽",  pos: true  },
  { type: "Продажа",   ticker: "LKOH", time: "27 марта 2026, 10:14", details: "2 шт. · цена 6 540 ₽",   amount: "−13 080 ₽", pos: false },
  { type: "Покупка",   ticker: "YNDX", time: "25 марта 2026, 16:45", details: "1 шт. · цена 3 210 ₽",   amount: "+3 210 ₽",  pos: true  },
  { type: "Дивиденды", ticker: "SBER", time: "20 марта 2026, 09:00", details: "2,50 ₽ × 10 шт.",        amount: "+25 ₽",     pos: true  },
];

for (const tx of txData) {
  const row = figma.createFrame();
  row.name = `Транзакция · ${tx.type} · ${tx.ticker}`;
  row.layoutMode = "VERTICAL";
  row.paddingLeft = 16;
  row.paddingRight = 16;
  row.paddingTop = 14;
  row.paddingBottom = 14;
  row.itemSpacing = 4;
  row.cornerRadius = shape.medium;
  row.fills = tokenFill("surface");
  row.effects = shadowCard;
  row.primaryAxisSizingMode = "AUTO";
  row.counterAxisSizingMode = "FIXED";
  row.resize(W - PAD * 2, 1);

  const header = figma.createFrame();
  header.name = "Шапка";
  header.layoutMode = "HORIZONTAL";
  header.fills = [];
  header.primaryAxisSizingMode = "AUTO";
  header.counterAxisSizingMode = "AUTO";
  const typeLabel = txt(`${tx.type} · ${tx.ticker}`, 14, "semi", c.onSurface, "Тип");
  header.appendChild(typeLabel);
  typeLabel.layoutSizingHorizontal = "FILL";
  typeLabel.layoutSizingVertical = "HUG";
  const amountLabel = txt(tx.amount, 14, "semi", tx.pos ? c.positive : c.negative, "Сумма");
  header.appendChild(amountLabel);
  amountLabel.layoutSizingHorizontal = "HUG";
  amountLabel.layoutSizingVertical = "HUG";
  row.appendChild(header);
  header.layoutSizingHorizontal = "FILL";
  header.layoutSizingVertical = "HUG";

  const timeLabel = txt(tx.time, 12, "reg", c.onSurfaceVariant, "Время");
  row.appendChild(timeLabel);
  timeLabel.layoutSizingHorizontal = "FILL";
  timeLabel.layoutSizingVertical = "HUG";

  const detailsLabel = txt(tx.details, 13, "reg", c.onSurface, "Детали");
  row.appendChild(detailsLabel);
  detailsLabel.layoutSizingHorizontal = "FILL";
  detailsLabel.layoutSizingVertical = "HUG";

  bodyHist.appendChild(row);
  row.layoutSizingHorizontal = "FILL";
  row.layoutSizingVertical = "HUG";
  created.push(row.id);
}

appendInstance(scrHist, nav3);
scrHist.children[scrHist.children.length - 1].layoutSizingHorizontal = "FILL";
scrHist.children[scrHist.children.length - 1].layoutSizingVertical = "HUG";

makeScreensRow("Детальная и покупка");
// ═══════════════════════════════════════════════════════════════════════════
// Экран деталей бумаги — вкладка «Детали»
// ═══════════════════════════════════════════════════════════════════════════
{
  const scrDetail = androidScreen("Детали · SBER");
  statusStrip(scrDetail);

  // Шапка
  appendInstance(scrDetail, hdrSber);

  // ─── TabBar ──────────────────────────────────────────────────────────────
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
  scrDetail.appendChild(tabBar);
  tabBar.layoutSizingHorizontal = "FILL";
  tabBar.layoutSizingVertical = "FIXED";

  for (let i = 0; i < 2; i++) {
    const active = i === 0;
    const tab = figma.createFrame();
    tab.name = active ? "Таб · Детали (активный)" : "Таб · Стакан";
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
    tab.appendChild(txt(["Детали","Стакан"][i], 14, active ? "semi" : "reg",
      active ? c.primary : c.onSurfaceVariant, "Текст"));
    tab.children[0].layoutSizingHorizontal = "HUG";
    tab.children[0].layoutSizingVertical = "HUG";
    tabBar.appendChild(tab);
    tab.layoutSizingHorizontal = "FILL";
    tab.layoutSizingVertical = "FILL";
  }

  // ─── Контент (скролл) ────────────────────────────────────────────────────
  const body = scrollBody(scrDetail);

  // Цена и дельта
  const priceBlock = figma.createFrame();
  priceBlock.name = "Блок цены";
  priceBlock.layoutMode = "VERTICAL";
  priceBlock.itemSpacing = 4;
  priceBlock.fills = [];
  priceBlock.primaryAxisSizingMode = "AUTO";
  priceBlock.counterAxisSizingMode = "AUTO";

  priceBlock.appendChild(txt("298,45 ₽", 28, "semi", c.onSurface, "Цена"));
  priceBlock.children[0].layoutSizingHorizontal = "HUG";
  priceBlock.children[0].layoutSizingVertical = "HUG";

  priceBlock.appendChild(txt("+4,6%  ·  +13,12 ₽  за сегодня", 13, "med", c.positive, "Дельта"));
  priceBlock.children[1].layoutSizingHorizontal = "HUG";
  priceBlock.children[1].layoutSizingVertical = "HUG";

  body.appendChild(priceBlock);
  priceBlock.layoutSizingHorizontal = "FILL"; priceBlock.layoutSizingVertical = "HUG";
  created.push(priceBlock.id);

  // График
  const plotInst = appendInstance(body, pricePlot);
  plotInst.layoutSizingHorizontal = "FILL"; plotInst.layoutSizingVertical = "HUG";

  // Разделитель
  const div1 = figma.createRectangle();
  div1.name = "Разделитель"; div1.resize(1, 1);
  div1.fills = [{ type: "SOLID", color: c.outline }];
  body.appendChild(div1);
  div1.layoutSizingHorizontal = "FILL"; div1.layoutSizingVertical = "FIXED";

  // О компании
  const aboutCard = figma.createFrame();
  aboutCard.name = "О компании";
  aboutCard.layoutMode = "VERTICAL";
  aboutCard.itemSpacing = 6;
  aboutCard.paddingLeft = 16; aboutCard.paddingRight = 16;
  aboutCard.paddingTop = 14; aboutCard.paddingBottom = 14;
  aboutCard.cornerRadius = shape.medium;
  aboutCard.fills = tokenFill("surface");
  aboutCard.effects = shadowCard;
  aboutCard.primaryAxisSizingMode = "AUTO"; aboutCard.counterAxisSizingMode = "FIXED";
  aboutCard.resize(W - PAD * 2, 1);

  aboutCard.appendChild(txt("О компании", 14, "semi", c.onSurface, "Заголовок"));
  aboutCard.children[0].layoutSizingHorizontal = "FILL"; aboutCard.children[0].layoutSizingVertical = "HUG";

  const meta = figma.createFrame();
  meta.name = "Мета"; meta.layoutMode = "HORIZONTAL"; meta.itemSpacing = 12;
  meta.fills = []; meta.primaryAxisSizingMode = "AUTO"; meta.counterAxisSizingMode = "AUTO";
  for (const [label, val] of [["Сектор", "Финансы"], ["Биржа", "MOEX"], ["Лот", "10 шт."]]) {
    const cell = figma.createFrame();
    cell.layoutMode = "VERTICAL"; cell.itemSpacing = 2; cell.fills = [];
    cell.primaryAxisSizingMode = "AUTO"; cell.counterAxisSizingMode = "AUTO";
    cell.appendChild(txt(label, 11, "reg", c.onSurfaceVariant, "Лейбл"));
    cell.children[0].layoutSizingHorizontal = "HUG"; cell.children[0].layoutSizingVertical = "HUG";
    cell.appendChild(txt(val, 13, "semi", c.onSurface, "Значение"));
    cell.children[1].layoutSizingHorizontal = "HUG"; cell.children[1].layoutSizingVertical = "HUG";
    meta.appendChild(cell);
    cell.layoutSizingHorizontal = "HUG"; cell.layoutSizingVertical = "HUG";
  }
  aboutCard.appendChild(meta);
  meta.layoutSizingHorizontal = "HUG"; meta.layoutSizingVertical = "HUG";

  aboutCard.appendChild(txt(
    "Сбербанк — крупнейший банк России и Восточной Европы. Предоставляет полный спектр банковских услуг физическим и юридическим лицам.",
    13, "reg", c.onSurfaceVariant, "Описание"
  ));
  aboutCard.children[2].layoutSizingHorizontal = "FILL"; aboutCard.children[2].layoutSizingVertical = "HUG";

  // В портфеле (перед карточкой "О компании")
  const portfolioChip = figma.createFrame();
  portfolioChip.name = "В портфеле";
  portfolioChip.layoutMode = "HORIZONTAL";
  portfolioChip.paddingLeft = 12; portfolioChip.paddingRight = 12;
  portfolioChip.paddingTop = 6;  portfolioChip.paddingBottom = 6;
  portfolioChip.cornerRadius = shape.small;
  portfolioChip.primaryAxisSizingMode = "AUTO"; portfolioChip.counterAxisSizingMode = "AUTO";
  portfolioChip.fills = tokenFill("primaryContainer");
  portfolioChip.appendChild(txt("В портфеле: 10 шт. · ср. 285 ₽", 12, "med", c.primary, "Текст"));
  portfolioChip.children[0].layoutSizingHorizontal = "HUG";
  portfolioChip.children[0].layoutSizingVertical = "HUG";
  body.appendChild(portfolioChip);
  portfolioChip.layoutSizingHorizontal = "HUG"; portfolioChip.layoutSizingVertical = "HUG";
  created.push(portfolioChip.id);

  body.appendChild(aboutCard);
  aboutCard.layoutSizingHorizontal = "FILL"; aboutCard.layoutSizingVertical = "HUG";
  created.push(aboutCard.id);

  // Кнопки Купить/Продать
  const bsInst = appendInstance(scrDetail, buySellBar);
  bsInst.layoutSizingHorizontal = "FILL"; bsInst.layoutSizingVertical = "HUG";

  appendInstance(scrDetail, nav1);
  scrDetail.children[scrDetail.children.length - 1].layoutSizingHorizontal = "FILL";
  scrDetail.children[scrDetail.children.length - 1].layoutSizingVertical = "HUG";
}

// ═══════════════════════════════════════════════════════════════════════════
// Экран деталей бумаги — вкладка «Стакан»
// ═══════════════════════════════════════════════════════════════════════════
{
  const scrStakan = androidScreen("Стакан · SBER");
  statusStrip(scrStakan);

  appendInstance(scrStakan, hdrSber);

  // TabBar (Стакан активный)
  const tabBar2 = figma.createFrame();
  tabBar2.name = "TabBar";
  tabBar2.layoutMode = "HORIZONTAL";
  tabBar2.primaryAxisSizingMode = "FIXED"; tabBar2.counterAxisSizingMode = "FIXED";
  tabBar2.resize(W, 48);
  tabBar2.fills = [];
  tabBar2.paddingTop = 16;
  tabBar2.strokes = [{ type: "SOLID", color: c.outline }];
  tabBar2.strokeBottomWeight = 1;
  tabBar2.strokeTopWeight = 0; tabBar2.strokeLeftWeight = 0; tabBar2.strokeRightWeight = 0;
  scrStakan.appendChild(tabBar2);
  tabBar2.layoutSizingHorizontal = "FILL"; tabBar2.layoutSizingVertical = "FIXED";

  for (let i = 0; i < 2; i++) {
    const active = i === 1;
    const tab = figma.createFrame();
    tab.name = active ? "Таб · Стакан (активный)" : "Таб · Детали";
    tab.layoutMode = "VERTICAL";
    tab.primaryAxisAlignItems = "CENTER"; tab.counterAxisAlignItems = "CENTER";
    tab.fills = []; tab.layoutGrow = 1;
    tab.primaryAxisSizingMode = "FIXED"; tab.counterAxisSizingMode = "AUTO";
    if (active) {
      tab.strokes = [{ type: "SOLID", color: c.primary }];
      tab.strokeBottomWeight = 2;
      tab.strokeTopWeight = 0; tab.strokeLeftWeight = 0; tab.strokeRightWeight = 0;
    }
    tab.appendChild(txt(["Детали","Стакан"][i], 14, active ? "semi" : "reg",
      active ? c.primary : c.onSurfaceVariant, "Текст"));
    tab.children[0].layoutSizingHorizontal = "HUG"; tab.children[0].layoutSizingVertical = "HUG";
    tabBar2.appendChild(tab);
    tab.layoutSizingHorizontal = "FILL"; tab.layoutSizingVertical = "FILL";
  }

  const bodySt = scrollBody(scrStakan);

  // Текущая цена над стаканом
  const priceRow = figma.createFrame();
  priceRow.name = "Цена";
  priceRow.layoutMode = "HORIZONTAL";
  priceRow.counterAxisAlignItems = "CENTER";
  priceRow.itemSpacing = 10;
  priceRow.fills = [];
  priceRow.primaryAxisSizingMode = "AUTO"; priceRow.counterAxisSizingMode = "AUTO";
  priceRow.appendChild(txt("298,45 ₽", 20, "semi", c.onSurface, "Цена"));
  priceRow.children[0].layoutSizingHorizontal = "HUG"; priceRow.children[0].layoutSizingVertical = "HUG";
  priceRow.appendChild(txt("+4,6%", 13, "med", c.positive, "Дельта"));
  priceRow.children[1].layoutSizingHorizontal = "HUG"; priceRow.children[1].layoutSizingVertical = "HUG";
  bodySt.appendChild(priceRow);
  priceRow.layoutSizingHorizontal = "FILL"; priceRow.layoutSizingVertical = "HUG";
  created.push(priceRow.id);

  // Стакан
  const obInst = appendInstance(bodySt, orderBook);
  obInst.layoutSizingHorizontal = "FILL"; obInst.layoutSizingVertical = "HUG";

  // BuySell
  const bsInst2 = appendInstance(scrStakan, buySellBar);
  bsInst2.layoutSizingHorizontal = "FILL"; bsInst2.layoutSizingVertical = "HUG";

  appendInstance(scrStakan, nav1);
  scrStakan.children[scrStakan.children.length - 1].layoutSizingHorizontal = "FILL";
  scrStakan.children[scrStakan.children.length - 1].layoutSizingVertical = "HUG";
}

makeScreensRow("Покупка");
// ═══════════════════════════════════════════════════════════════════════════
// Экран покупки бумаги
// ═══════════════════════════════════════════════════════════════════════════
const scrBuy = androidScreen("Покупка · SBER");
statusStrip(scrBuy);

appendInstance(scrBuy, hdrBuyComp);

const bodyBuy = scrollBody(scrBuy);

// ─── Карточка актива ─────────────────────────────────────────────────────
const assetCard = figma.createFrame();
assetCard.name = "Актив · Карточка";
assetCard.layoutMode = "HORIZONTAL";
assetCard.primaryAxisSizingMode = "FIXED"; assetCard.counterAxisSizingMode = "AUTO";
assetCard.resize(W - PAD * 2, 1);
assetCard.paddingLeft = 16; assetCard.paddingRight = 16;
assetCard.paddingTop = 14; assetCard.paddingBottom = 14;
assetCard.itemSpacing = 12;
assetCard.cornerRadius = shape.medium;
assetCard.fills = tokenFill("surface");
assetCard.effects = shadowCard;
assetCard.counterAxisAlignItems = "CENTER";

// Тикер-бейдж
const tickerBadge = figma.createFrame();
tickerBadge.name = "Тикер";
tickerBadge.layoutMode = "HORIZONTAL";
tickerBadge.paddingLeft = 10; tickerBadge.paddingRight = 10;
tickerBadge.paddingTop = 6;  tickerBadge.paddingBottom = 6;
tickerBadge.cornerRadius = shape.small;
tickerBadge.primaryAxisSizingMode = "AUTO"; tickerBadge.counterAxisSizingMode = "AUTO";
tickerBadge.fills = tokenFill("primaryContainer");
tickerBadge.appendChild(txt("SBER", 13, "semi", c.primary, "Текст"));
tickerBadge.children[0].layoutSizingHorizontal = "HUG"; tickerBadge.children[0].layoutSizingVertical = "HUG";
assetCard.appendChild(tickerBadge);
tickerBadge.layoutSizingHorizontal = "HUG"; tickerBadge.layoutSizingVertical = "HUG";

const nameBlock = figma.createFrame();
nameBlock.name = "Название"; nameBlock.layoutMode = "VERTICAL"; nameBlock.itemSpacing = 2;
nameBlock.fills = []; nameBlock.primaryAxisSizingMode = "AUTO"; nameBlock.counterAxisSizingMode = "AUTO";
nameBlock.appendChild(txt("Сбербанк", 14, "semi", c.onSurface, "Имя"));
nameBlock.children[0].layoutSizingHorizontal = "HUG"; nameBlock.children[0].layoutSizingVertical = "HUG";
nameBlock.appendChild(txt("298,45 ₽  ·  +4,6%", 12, "reg", c.positive, "Цена"));
nameBlock.children[1].layoutSizingHorizontal = "HUG"; nameBlock.children[1].layoutSizingVertical = "HUG";
assetCard.appendChild(nameBlock);
nameBlock.layoutSizingHorizontal = "FILL"; nameBlock.layoutSizingVertical = "HUG";

bodyBuy.appendChild(assetCard);
assetCard.layoutSizingHorizontal = "FILL"; assetCard.layoutSizingVertical = "HUG";
created.push(assetCard.id);

// ─── Количество ──────────────────────────────────────────────────────────
bodyBuy.appendChild(txt("Количество", 14, "semi", c.onSurface, "Секция"));
bodyBuy.children[bodyBuy.children.length - 1].layoutSizingHorizontal = "HUG";
bodyBuy.children[bodyBuy.children.length - 1].layoutSizingVertical = "HUG";

const counter = figma.createFrame();
counter.name = "Счётчик";
counter.layoutMode = "HORIZONTAL";
counter.primaryAxisSizingMode = "AUTO"; counter.counterAxisSizingMode = "FIXED";
counter.resize(1, 52);
counter.counterAxisAlignItems = "CENTER";
counter.cornerRadius = shape.medium;
counter.fills = tokenFill("surfaceContainer");
counter.strokes = [{ type: "SOLID", color: c.outline }]; counter.strokeWeight = 1;

for (const [label, isCenter] of [["−", false], ["10 шт.", true], ["+", false]]) {
  const btn = figma.createFrame();
  btn.name = label;
  btn.layoutMode = "HORIZONTAL";
  btn.primaryAxisAlignItems = "CENTER"; btn.counterAxisAlignItems = "CENTER";
  btn.fills = isCenter ? tokenFill("surface") : [];
  if (!isCenter) {
    btn.primaryAxisSizingMode = "FIXED"; btn.counterAxisSizingMode = "FIXED";
    btn.resize(52, 52);
    btn.appendChild(txt(label, 24, "semi", c.primary, "Символ"));
  } else {
    btn.primaryAxisSizingMode = "AUTO"; btn.counterAxisSizingMode = "FIXED";
    btn.resize(1, 52);
    btn.paddingLeft = 24; btn.paddingRight = 24;
    btn.strokes = [{ type: "SOLID", color: c.outline }];
    btn.strokeLeftWeight = 1; btn.strokeRightWeight = 1;
    btn.strokeTopWeight = 0; btn.strokeBottomWeight = 0;
    btn.appendChild(txt(label, 16, "semi", c.onSurface, "Значение"));
  }
  btn.children[0].layoutSizingHorizontal = "HUG"; btn.children[0].layoutSizingVertical = "HUG";
  counter.appendChild(btn);
  btn.layoutSizingHorizontal = isCenter ? "HUG" : "FIXED";
  btn.layoutSizingVertical = "FIXED";
}

bodyBuy.appendChild(counter);
counter.layoutSizingHorizontal = "HUG"; counter.layoutSizingVertical = "FIXED";
created.push(counter.id);

// Итого
const totalRow = figma.createFrame();
totalRow.name = "Итого"; totalRow.layoutMode = "HORIZONTAL";
totalRow.fills = []; totalRow.primaryAxisSizingMode = "AUTO"; totalRow.counterAxisSizingMode = "AUTO";
totalRow.counterAxisAlignItems = "CENTER"; totalRow.itemSpacing = 8;
totalRow.appendChild(txt("Итого:", 14, "reg", c.onSurfaceVariant, "Лейбл"));
totalRow.children[0].layoutSizingHorizontal = "HUG"; totalRow.children[0].layoutSizingVertical = "HUG";
totalRow.appendChild(txt("2 984,50 ₽", 16, "semi", c.onSurface, "Сумма"));
totalRow.children[1].layoutSizingHorizontal = "HUG"; totalRow.children[1].layoutSizingVertical = "HUG";
bodyBuy.appendChild(totalRow);
totalRow.layoutSizingHorizontal = "FILL"; totalRow.layoutSizingVertical = "HUG";
created.push(totalRow.id);

// Разделитель
const divBuy = figma.createRectangle();
divBuy.name = "Разделитель"; divBuy.resize(1, 1);
divBuy.fills = [{ type: "SOLID", color: c.outline }];
bodyBuy.appendChild(divBuy);
divBuy.layoutSizingHorizontal = "FILL"; divBuy.layoutSizingVertical = "FIXED";

// ─── Тип заявки (radio) ──────────────────────────────────────────────────
bodyBuy.appendChild(txt("Тип заявки", 14, "semi", c.onSurface, "Секция"));
bodyBuy.children[bodyBuy.children.length - 1].layoutSizingHorizontal = "HUG";
bodyBuy.children[bodyBuy.children.length - 1].layoutSizingVertical = "HUG";

function makeRadio(label, selected) {
  const row = figma.createFrame();
  row.name = `Радио · ${label}`;
  row.layoutMode = "HORIZONTAL"; row.itemSpacing = 12;
  row.counterAxisAlignItems = "CENTER";
  row.fills = []; row.primaryAxisSizingMode = "AUTO"; row.counterAxisSizingMode = "AUTO";
  const dot = figma.createEllipse();
  dot.name = "Точка"; dot.resize(20, 20);
  dot.fills = selected ? tokenFill("primary") : [];
  dot.strokes = [{ type: "SOLID", color: selected ? c.primary : c.outline }];
  dot.strokeWeight = selected ? 6 : 2;
  row.appendChild(dot);
  dot.layoutSizingHorizontal = "FIXED"; dot.layoutSizingVertical = "FIXED";
  row.appendChild(txt(label, 14, "reg", c.onSurface, "Лейбл"));
  row.children[1].layoutSizingHorizontal = "HUG"; row.children[1].layoutSizingVertical = "HUG";
  return row;
}

const radioMarket = makeRadio("Рыночная  ·  по лучшей цене", true);
const radioLimit  = makeRadio("Лимитная  ·  по заданной цене", false);
bodyBuy.appendChild(radioMarket);
radioMarket.layoutSizingHorizontal = "FILL"; radioMarket.layoutSizingVertical = "HUG";
created.push(radioMarket.id);
bodyBuy.appendChild(radioLimit);
radioLimit.layoutSizingHorizontal = "FILL"; radioLimit.layoutSizingVertical = "HUG";
created.push(radioLimit.id);

// Разделитель
const divBuy2 = figma.createRectangle();
divBuy2.name = "Разделитель"; divBuy2.resize(1, 1);
divBuy2.fills = [{ type: "SOLID", color: c.outline }];
bodyBuy.appendChild(divBuy2);
divBuy2.layoutSizingHorizontal = "FILL"; divBuy2.layoutSizingVertical = "FIXED";

// ─── Доступно ────────────────────────────────────────────────────────────
const avRow = figma.createFrame();
avRow.name = "Доступно"; avRow.layoutMode = "HORIZONTAL";
avRow.fills = []; avRow.primaryAxisSizingMode = "AUTO"; avRow.counterAxisSizingMode = "AUTO";
avRow.counterAxisAlignItems = "CENTER"; avRow.itemSpacing = 8;
avRow.appendChild(txt("Доступно:", 13, "reg", c.onSurfaceVariant, "Лейбл"));
avRow.children[0].layoutSizingHorizontal = "HUG"; avRow.children[0].layoutSizingVertical = "HUG";
avRow.appendChild(txt("45 320 ₽", 13, "semi", c.onSurface, "Значение"));
avRow.children[1].layoutSizingHorizontal = "HUG"; avRow.children[1].layoutSizingVertical = "HUG";
bodyBuy.appendChild(avRow);
avRow.layoutSizingHorizontal = "FILL"; avRow.layoutSizingVertical = "HUG";
created.push(avRow.id);

// ─── Кнопка подтверждения ────────────────────────────────────────────────
const confirmBtn = figma.createFrame();
confirmBtn.name = "Кнопка · Подтвердить";
confirmBtn.layoutMode = "HORIZONTAL";
confirmBtn.primaryAxisAlignItems = "CENTER"; confirmBtn.counterAxisAlignItems = "CENTER";
confirmBtn.primaryAxisSizingMode = "FIXED"; confirmBtn.counterAxisSizingMode = "FIXED";
confirmBtn.resize(W - PAD * 2, mh.button);
confirmBtn.cornerRadius = shape.full;
confirmBtn.fills = tokenFill("primary");
confirmBtn.effects = shadowCard;
confirmBtn.appendChild(txt("Подтвердить покупку · 2 984,50 ₽", 14, "med", c.onPrimary, "Текст"));
confirmBtn.children[0].layoutSizingHorizontal = "HUG"; confirmBtn.children[0].layoutSizingVertical = "HUG";
bodyBuy.appendChild(confirmBtn);
confirmBtn.layoutSizingHorizontal = "FILL"; confirmBtn.layoutSizingVertical = "FIXED";
created.push(confirmBtn.id);

// Дисклеймер
bodyBuy.appendChild(txt(
  "Рыночный ордер исполняется по лучшей доступной цене. Итоговая сумма может отличаться.",
  11, "reg", c.onSurfaceVariant, "Дисклеймер"
));
bodyBuy.children[bodyBuy.children.length - 1].layoutSizingHorizontal = "FILL";
bodyBuy.children[bodyBuy.children.length - 1].layoutSizingVertical = "HUG";

appendInstance(scrBuy, nav1);
scrBuy.children[scrBuy.children.length - 1].layoutSizingHorizontal = "FILL";
scrBuy.children[scrBuy.children.length - 1].layoutSizingVertical = "HUG";

// Дополнительный экран — состояние «после покупки» со снекбаром
{
  const scrBuyDone = scrBuy.clone();
  scrBuyDone.name = `Android Compact ${W}×${H} — Покупка · SBER · Успех`;
  screensRow.appendChild(scrBuyDone);
  scrBuyDone.layoutSizingHorizontal = "FIXED";
  scrBuyDone.layoutSizingVertical = "HUG";
  created.push(scrBuyDone.id);
  addSnackbar(scrBuyDone, "success", "Заявка на покупку исполнена");
}

makeScreensRow("Профиль и настройки");
// ═══════════════════════════════════════════════════════════════════════════
// Экран профиля и настроек — всё заинлайнено
// ═══════════════════════════════════════════════════════════════════════════
const scrProf = androidScreen("Профиль и настройки");
statusStrip(scrProf);
appendInstance(scrProf, hdrProfile);
scrProf.children[1].layoutSizingHorizontal = "FILL";
scrProf.children[1].layoutSizingVertical = "HUG";
const bodyProf = scrollBody(scrProf);

// ─── Helpers ─────────────────────────────────────────────────────────────

function makeProfDivider() {
  const d = figma.createRectangle();
  d.name = "Разделитель"; d.resize(1, 1);
  d.fills = [{ type: "SOLID", color: c.outline }];
  bodyProf.appendChild(d);
  d.layoutSizingHorizontal = "FILL"; d.layoutSizingVertical = "FIXED";
}

function makeProfSection(label) {
  const t = txt(label.toUpperCase(), 11, "semi", c.onSurfaceVariant, "Секция");
  bodyProf.appendChild(t);
  t.layoutSizingHorizontal = "HUG"; t.layoutSizingVertical = "HUG";
}

function makeProfRadioRow(label, subtitle, selected) {
  const row = figma.createFrame();
  row.name = `Радио · ${label}`;
  row.layoutMode = "HORIZONTAL"; row.itemSpacing = 12;
  row.fills = []; row.counterAxisAlignItems = "CENTER";
  row.primaryAxisSizingMode = "FIXED"; row.counterAxisSizingMode = "AUTO";
  row.resize(W - PAD * 2, 1);
  row.paddingTop = 4; row.paddingBottom = 4;

  const dot = figma.createEllipse();
  dot.name = "Радио"; dot.resize(20, 20);
  dot.fills = selected ? tokenFill("primary") : [];
  dot.strokes = [{ type: "SOLID", color: selected ? c.primary : c.outline }];
  dot.strokeWeight = selected ? 6 : 2;
  row.appendChild(dot);
  dot.layoutSizingHorizontal = "FIXED"; dot.layoutSizingVertical = "FIXED";

  const labels = figma.createFrame();
  labels.name = "Текст"; labels.layoutMode = "VERTICAL"; labels.itemSpacing = 2;
  labels.fills = []; labels.primaryAxisSizingMode = "AUTO"; labels.counterAxisSizingMode = "AUTO";
  labels.appendChild(txt(label, 14, selected ? "semi" : "reg", c.onSurface, "Лейбл"));
  labels.children[0].layoutSizingHorizontal = "HUG"; labels.children[0].layoutSizingVertical = "HUG";
  if (subtitle) {
    labels.appendChild(txt(subtitle, 12, "reg", c.onSurfaceVariant, "Подпись"));
    labels.children[1].layoutSizingHorizontal = "HUG"; labels.children[1].layoutSizingVertical = "HUG";
  }
  row.appendChild(labels);
  labels.layoutSizingHorizontal = "FILL"; labels.layoutSizingVertical = "HUG";

  bodyProf.appendChild(row);
  row.layoutSizingHorizontal = "FILL"; row.layoutSizingVertical = "HUG";
  created.push(row.id);
}

function makeProfToggleRow(label, isOn) {
  const row = figma.createFrame();
  row.name = `Тоггл · ${label}`;
  row.layoutMode = "HORIZONTAL"; row.itemSpacing = 12;
  row.fills = []; row.counterAxisAlignItems = "CENTER";
  row.primaryAxisSizingMode = "FIXED"; row.counterAxisSizingMode = "FIXED";
  row.resize(W - PAD * 2, 44);

  row.appendChild(txt(label, 14, "reg", c.onSurface, "Лейбл"));
  row.children[0].layoutSizingHorizontal = "FILL"; row.children[0].layoutSizingVertical = "HUG";

  // Toggle switch
  const track = figma.createFrame();
  track.name = "Трек"; track.resize(44, 26); track.cornerRadius = 13;
  track.fills = isOn ? tokenFill("primary") : [{ type: "SOLID", color: c.outline }];
  track.layoutMode = "HORIZONTAL";
  track.primaryAxisAlignItems = isOn ? "MAX" : "MIN";
  track.counterAxisAlignItems = "CENTER";
  track.paddingLeft = 3; track.paddingRight = 3;

  const thumb = figma.createEllipse();
  thumb.name = "Ползунок"; thumb.resize(20, 20);
  thumb.fills = [{ type: "SOLID", color: c.surface }];
  thumb.effects = shadowCard;
  track.appendChild(thumb);
  thumb.layoutSizingHorizontal = "FIXED"; thumb.layoutSizingVertical = "FIXED";

  row.appendChild(track);
  track.layoutSizingHorizontal = "FIXED"; track.layoutSizingVertical = "FIXED";

  bodyProf.appendChild(row);
  row.layoutSizingHorizontal = "FILL"; row.layoutSizingVertical = "FIXED";
  created.push(row.id);
}

// ─── Профиль (встроен) ───────────────────────────────────────────────────
const userCard = figma.createFrame();
userCard.name = "Профиль · Карточка";
userCard.layoutMode = "VERTICAL";
userCard.primaryAxisSizingMode = "AUTO"; userCard.counterAxisSizingMode = "FIXED";
userCard.resize(W - PAD * 2, 1);
userCard.paddingTop = 16; userCard.paddingBottom = 16;
userCard.itemSpacing = 12;
userCard.cornerRadius = shape.large;
userCard.fills = tokenFill("surface");
userCard.effects = shadowCard;

const avatarRow = figma.createFrame();
avatarRow.name = "Аватар + данные"; avatarRow.layoutMode = "HORIZONTAL"; avatarRow.itemSpacing = 14;
avatarRow.fills = []; avatarRow.counterAxisAlignItems = "CENTER";
avatarRow.primaryAxisSizingMode = "FIXED"; avatarRow.counterAxisSizingMode = "AUTO";
avatarRow.resize(W - PAD * 2, 1); avatarRow.paddingLeft = 16; avatarRow.paddingRight = 16;

const av = figma.createEllipse();
av.name = "Аватар"; av.resize(52, 52); av.fills = tokenFill("primaryContainer");
avatarRow.appendChild(av); av.layoutSizingHorizontal = "FIXED"; av.layoutSizingVertical = "FIXED";

const userInfo = figma.createFrame();
userInfo.name = "Данные"; userInfo.layoutMode = "VERTICAL"; userInfo.itemSpacing = 3;
userInfo.fills = []; userInfo.primaryAxisSizingMode = "AUTO"; userInfo.counterAxisSizingMode = "AUTO";
userInfo.appendChild(txt("Иван Иванов", 16, "semi", c.onSurface, "Имя"));
userInfo.children[0].layoutSizingHorizontal = "HUG"; userInfo.children[0].layoutSizingVertical = "HUG";
userInfo.appendChild(txt("ivan@example.com", 13, "reg", c.onSurfaceVariant, "Email"));
userInfo.children[1].layoutSizingHorizontal = "HUG"; userInfo.children[1].layoutSizingVertical = "HUG";
userInfo.appendChild(txt("+7 900 123-45-67", 13, "reg", c.onSurfaceVariant, "Телефон"));
userInfo.children[2].layoutSizingHorizontal = "HUG"; userInfo.children[2].layoutSizingVertical = "HUG";
avatarRow.appendChild(userInfo);
userInfo.layoutSizingHorizontal = "FILL"; userInfo.layoutSizingVertical = "HUG";

userCard.appendChild(avatarRow);
avatarRow.layoutSizingHorizontal = "FILL"; avatarRow.layoutSizingVertical = "HUG";

// Кнопка «Редактировать»
const editDivider = figma.createRectangle();
editDivider.name = "Разделитель"; editDivider.resize(1, 1);
editDivider.fills = [{ type: "SOLID", color: c.outline }];
userCard.appendChild(editDivider);
editDivider.layoutSizingHorizontal = "FILL"; editDivider.layoutSizingVertical = "FIXED";

const editBtn = figma.createFrame();
editBtn.name = "Кнопка · Редактировать"; editBtn.layoutMode = "HORIZONTAL";
editBtn.primaryAxisAlignItems = "CENTER"; editBtn.counterAxisAlignItems = "CENTER";
editBtn.primaryAxisSizingMode = "FIXED"; editBtn.counterAxisSizingMode = "FIXED";
editBtn.resize(W - PAD * 2, 40);
editBtn.paddingLeft = 16; editBtn.paddingRight = 16;
editBtn.appendChild(txt("Редактировать профиль", 14, "med", c.primary, "Текст"));
editBtn.children[0].layoutSizingHorizontal = "HUG"; editBtn.children[0].layoutSizingVertical = "HUG";
userCard.appendChild(editBtn);
editBtn.layoutSizingHorizontal = "FILL"; editBtn.layoutSizingVertical = "FIXED";

bodyProf.appendChild(userCard);
userCard.layoutSizingHorizontal = "FILL"; userCard.layoutSizingVertical = "HUG";
created.push(userCard.id);

// ─── Внешний вид ─────────────────────────────────────────────────────────
makeProfDivider();
makeProfSection("Внешний вид");
makeProfRadioRow("Светлая", "Белый фон, тёмный текст", true);
makeProfRadioRow("Тёмная", "Тёмный фон, светлый текст", false);
makeProfRadioRow("Системная", "Как в настройках Android", false);

// ─── Уведомления ─────────────────────────────────────────────────────────
makeProfDivider();
makeProfSection("Уведомления");
makeProfToggleRow("Уведомления о сделках", true);
makeProfToggleRow("Курсовые уведомления", true);
makeProfToggleRow("Новости по портфелю", false);

// ─── Безопасность ────────────────────────────────────────────────────────
makeProfDivider();
makeProfSection("Безопасность");
makeProfToggleRow("Вход по отпечатку пальца", true);
makeProfToggleRow("Двухфакторная аутентификация", false);

// ─── Валюта отображения ───────────────────────────────────────────────────
makeProfDivider();
makeProfSection("Валюта отображения");
makeProfRadioRow("₽  Российский рубль", null, true);
makeProfRadioRow("$  Доллар США", null, false);
makeProfRadioRow("€  Евро", null, false);

// ─── Выход ───────────────────────────────────────────────────────────────
makeProfDivider();

const logoutBtn = figma.createFrame();
logoutBtn.name = "Кнопка · Выйти";
logoutBtn.layoutMode = "HORIZONTAL";
logoutBtn.primaryAxisAlignItems = "CENTER"; logoutBtn.counterAxisAlignItems = "CENTER";
logoutBtn.primaryAxisSizingMode = "FIXED"; logoutBtn.counterAxisSizingMode = "FIXED";
logoutBtn.resize(W - PAD * 2, mh.button);
logoutBtn.cornerRadius = shape.full;
logoutBtn.fills = [{ type: "SOLID", color: { r: 0.98, g: 0.91, b: 0.90 } }];
logoutBtn.strokes = [{ type: "SOLID", color: c.negative }]; logoutBtn.strokeWeight = 1;
logoutBtn.appendChild(txt("Выйти из аккаунта", 14, "med", c.negative, "Текст"));
logoutBtn.children[0].layoutSizingHorizontal = "HUG"; logoutBtn.children[0].layoutSizingVertical = "HUG";
bodyProf.appendChild(logoutBtn);
logoutBtn.layoutSizingHorizontal = "FILL"; logoutBtn.layoutSizingVertical = "FIXED";
created.push(logoutBtn.id);

appendInstance(scrProf, nav1);
scrProf.children[scrProf.children.length - 1].layoutSizingHorizontal = "FILL";
scrProf.children[scrProf.children.length - 1].layoutSizingVertical = "HUG";

// ═══════════════════════════════════════════════════════════════════════════
// Экран редактирования профиля
// ═══════════════════════════════════════════════════════════════════════════
const scrEditProf = androidScreen("Редактирование профиля");
statusStrip(scrEditProf);
appendInstance(scrEditProf, hdrEditProfile);

const bodyEdit = scrollBody(scrEditProf);

// ─── Аватар ──────────────────────────────────────────────────────────────
const avEdit = figma.createEllipse();
avEdit.name = "Аватар";
avEdit.resize(72, 72);
avEdit.fills = tokenFill("primaryContainer");
bodyEdit.appendChild(avEdit);
avEdit.layoutSizingHorizontal = "FIXED";
avEdit.layoutSizingVertical = "FIXED";
created.push(avEdit.id);

const changeAvBtn = figma.createFrame();
changeAvBtn.name = "Кнопка · Изменить фото";
changeAvBtn.layoutMode = "HORIZONTAL";
changeAvBtn.primaryAxisAlignItems = "CENTER";
changeAvBtn.counterAxisAlignItems = "CENTER";
changeAvBtn.primaryAxisSizingMode = "FIXED";
changeAvBtn.counterAxisSizingMode = "FIXED";
changeAvBtn.resize(W - PAD * 2, 36);
changeAvBtn.cornerRadius = shape.full;
changeAvBtn.fills = tokenFill("primaryContainer");
changeAvBtn.appendChild(txt("Изменить фото", 14, "med", c.primary, "Текст"));
changeAvBtn.children[0].layoutSizingHorizontal = "HUG";
changeAvBtn.children[0].layoutSizingVertical = "HUG";
bodyEdit.appendChild(changeAvBtn);
changeAvBtn.layoutSizingHorizontal = "FILL";
changeAvBtn.layoutSizingVertical = "FIXED";
created.push(changeAvBtn.id);

// ─── Разделитель ─────────────────────────────────────────────────────────
const divEdit1 = figma.createRectangle();
divEdit1.name = "Разделитель";
divEdit1.resize(1, 1);
divEdit1.fills = [{ type: "SOLID", color: c.outline }];
bodyEdit.appendChild(divEdit1);
divEdit1.layoutSizingHorizontal = "FILL";
divEdit1.layoutSizingVertical = "FIXED";

// ─── Вспомогательная функция: поле ввода ─────────────────────────────────
function makeField(label, value) {
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
  field.primaryAxisAlignItems = "MIN";
  field.counterAxisAlignItems = "CENTER";
  field.paddingLeft = 14;
  field.paddingRight = 14;
  field.primaryAxisSizingMode = "FIXED";
  field.counterAxisSizingMode = "FIXED";
  field.resize(W - PAD * 2, 48);
  field.cornerRadius = shape.medium;
  field.fills = tokenFill("surfaceContainer");
  field.strokes = [{ type: "SOLID", color: c.outline }];
  field.strokeWeight = 1;
  field.appendChild(txt(value, 14, "reg", c.onSurface, "Значение"));
  field.children[0].layoutSizingHorizontal = "FILL";
  field.children[0].layoutSizingVertical = "HUG";
  wrap.appendChild(field);
  field.layoutSizingHorizontal = "FILL";
  field.layoutSizingVertical = "FIXED";

  bodyEdit.appendChild(wrap);
  wrap.layoutSizingHorizontal = "FILL";
  wrap.layoutSizingVertical = "HUG";
  created.push(wrap.id);
}

// ─── Поля ────────────────────────────────────────────────────────────────
makeField("Имя", "Иван");
makeField("Фамилия", "Иванов");
makeField("E-mail", "ivan@example.com");
makeField("Телефон", "+7 900 123-45-67");

// ─── Разделитель ─────────────────────────────────────────────────────────
const divEdit2 = figma.createRectangle();
divEdit2.name = "Разделитель";
divEdit2.resize(1, 1);
divEdit2.fills = [{ type: "SOLID", color: c.outline }];
bodyEdit.appendChild(divEdit2);
divEdit2.layoutSizingHorizontal = "FILL";
divEdit2.layoutSizingVertical = "FIXED";

// ─── Смена пароля ─────────────────────────────────────────────────────────
const changePassBtn = figma.createFrame();
changePassBtn.name = "Кнопка · Сменить пароль";
changePassBtn.layoutMode = "HORIZONTAL";
changePassBtn.primaryAxisAlignItems = "SPACE_BETWEEN";
changePassBtn.counterAxisAlignItems = "CENTER";
changePassBtn.paddingLeft = 0;
changePassBtn.paddingRight = 0;
changePassBtn.primaryAxisSizingMode = "FIXED";
changePassBtn.counterAxisSizingMode = "FIXED";
changePassBtn.resize(W - PAD * 2, 44);
changePassBtn.fills = [];
changePassBtn.appendChild(txt("Сменить пароль", 14, "reg", c.onSurface, "Текст"));
changePassBtn.children[0].layoutSizingHorizontal = "HUG";
changePassBtn.children[0].layoutSizingVertical = "HUG";
changePassBtn.appendChild(txt("›", 18, "reg", c.onSurfaceVariant, "Стрелка"));
changePassBtn.children[1].layoutSizingHorizontal = "HUG";
changePassBtn.children[1].layoutSizingVertical = "HUG";
bodyEdit.appendChild(changePassBtn);
changePassBtn.layoutSizingHorizontal = "FILL";
changePassBtn.layoutSizingVertical = "FIXED";
created.push(changePassBtn.id);

// ─── Биометрия ───────────────────────────────────────────────────────────
const divEdit3 = figma.createRectangle();
divEdit3.name = "Разделитель";
divEdit3.resize(1, 1);
divEdit3.fills = [{ type: "SOLID", color: c.outline }];
bodyEdit.appendChild(divEdit3);
divEdit3.layoutSizingHorizontal = "FILL";
divEdit3.layoutSizingVertical = "FIXED";

const bioSecLabel = txt("БЕЗОПАСНОСТЬ", 11, "semi", c.onSurfaceVariant, "Секция");
bodyEdit.appendChild(bioSecLabel);
bioSecLabel.layoutSizingHorizontal = "HUG";
bioSecLabel.layoutSizingVertical = "HUG";

const bioCheckRow = figma.createFrame();
bioCheckRow.name = "Чекбокс · Биометрия";
bioCheckRow.layoutMode = "HORIZONTAL";
bioCheckRow.itemSpacing = 12;
bioCheckRow.counterAxisAlignItems = "CENTER";
bioCheckRow.fills = [];
bioCheckRow.primaryAxisSizingMode = "FIXED";
bioCheckRow.counterAxisSizingMode = "FIXED";
bioCheckRow.resize(W - PAD * 2, 44);

// Чекбокс (отмечен)
const cbBox = figma.createFrame();
cbBox.name = "Чекбокс";
cbBox.layoutMode = "HORIZONTAL";
cbBox.primaryAxisAlignItems = "CENTER";
cbBox.counterAxisAlignItems = "CENTER";
cbBox.primaryAxisSizingMode = "FIXED";
cbBox.counterAxisSizingMode = "FIXED";
cbBox.resize(20, 20);
cbBox.cornerRadius = 4;
cbBox.fills = tokenFill("primary");
cbBox.strokes = [];
// Галочка через текст
const checkMark = txt("✓", 13, "semi", c.onPrimary, "Галочка");
cbBox.appendChild(checkMark);
checkMark.layoutSizingHorizontal = "HUG";
checkMark.layoutSizingVertical = "HUG";
bioCheckRow.appendChild(cbBox);
cbBox.layoutSizingHorizontal = "FIXED";
cbBox.layoutSizingVertical = "FIXED";

// Лейбл + подпись
const bioLabelCol = figma.createFrame();
bioLabelCol.name = "Текст";
bioLabelCol.layoutMode = "VERTICAL";
bioLabelCol.itemSpacing = 2;
bioLabelCol.fills = [];
bioLabelCol.primaryAxisSizingMode = "AUTO";
bioLabelCol.counterAxisSizingMode = "AUTO";
bioLabelCol.appendChild(txt("Разрешить вход по биометрии", 14, "reg", c.onSurface, "Лейбл"));
bioLabelCol.children[0].layoutSizingHorizontal = "HUG";
bioLabelCol.children[0].layoutSizingVertical = "HUG";
bioCheckRow.appendChild(bioLabelCol);
bioLabelCol.layoutSizingHorizontal = "FILL";
bioLabelCol.layoutSizingVertical = "HUG";

bodyEdit.appendChild(bioCheckRow);
bioCheckRow.layoutSizingHorizontal = "FILL";
bioCheckRow.layoutSizingVertical = "FIXED";
created.push(bioCheckRow.id);

// ─── Кнопка сохранить ────────────────────────────────────────────────────
const saveBtn = figma.createFrame();
saveBtn.name = "Кнопка · Сохранить";
saveBtn.layoutMode = "HORIZONTAL";
saveBtn.primaryAxisAlignItems = "CENTER";
saveBtn.counterAxisAlignItems = "CENTER";
saveBtn.primaryAxisSizingMode = "FIXED";
saveBtn.counterAxisSizingMode = "FIXED";
saveBtn.resize(W - PAD * 2, mh.button);
saveBtn.cornerRadius = shape.full;
saveBtn.fills = tokenFill("primary");
saveBtn.effects = shadowCard;
saveBtn.appendChild(txt("Сохранить изменения", 14, "med", c.onPrimary, "Текст"));
saveBtn.children[0].layoutSizingHorizontal = "HUG";
saveBtn.children[0].layoutSizingVertical = "HUG";
bodyEdit.appendChild(saveBtn);
saveBtn.layoutSizingHorizontal = "FILL";
saveBtn.layoutSizingVertical = "FIXED";
created.push(saveBtn.id);

appendInstance(scrEditProf, nav1);
scrEditProf.children[scrEditProf.children.length - 1].layoutSizingHorizontal = "FILL";
scrEditProf.children[scrEditProf.children.length - 1].layoutSizingVertical = "HUG";

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

// Пост-обработка: min-height = H, nav всегда внизу
// Обходим все ряды в screensShell (structure: screensShell → sec → row "Экраны" → screens)
function postProcessScreen(scr) {
  const finalH = Math.max(H, Math.round(scr.height));
  scr.primaryAxisSizingMode = "FIXED";
  scr.resize(W, finalH);
  scr.layoutSizingVertical = "FIXED";
  for (const ch of scr.children) {
    if (ch.name === "Контент") {
      ch.layoutSizingVertical = "FILL";
      ch.layoutGrow = 1;
      break;
    }
  }
}

for (const sec of screensShell.children) {
  if (sec.type !== "FRAME") continue;
  for (const child of sec.children) {
    if (child.name === "Экраны") {
      for (const scr of child.children) postProcessScreen(scr);
    }
  }
}

figma.notify("Готово: узлов — " + created.length);
  } catch (e) {
    figma.notify("Ошибка: " + (e && e.message ? e.message : String(e)));
  } finally {
    figma.closePlugin();
  }
})();
