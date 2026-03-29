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
