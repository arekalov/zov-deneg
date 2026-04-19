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
    { role: "Label Medium",    size: 12, weight: "med",  sample: "Уведомления · Тема" },
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
