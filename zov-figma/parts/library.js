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
const setTheme = buildSettingRow("Тема");
const setBio = buildSettingRow("Вход по отпечатку");
dropRow(setTheme);
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
const chipDeposit = buildChipFixed("Пополнения", false);
const chipWithdraw = buildChipFixed("Выводы", false);
dropChip(chipAll);
dropChip(chipBuy);
dropChip(chipSell);
dropChip(chipDeposit);
dropChip(chipWithdraw);

// ─── Шапки ─────────────────────────────────────────────────────────────────
const { dropComp: dropHdr } = makeSection("Шапки");
const hdrMain = buildHeaderWithProfile("Главная");
const hdrSearch = buildHeaderWithProfile("Поиск актива");
const hdrHistory = buildHeaderSimple("История транзакций");
const hdrOrders = buildHeaderSimple("Заявки");
const hdrOrderDetail = buildHeaderSimple("Заявка");
const hdrProfile = buildHeaderSimple("Профиль и настройки");
const hdrSber = buildHeaderSimple("SBER · Сбербанк");
const hdrBuyComp = buildHeaderSimple("Купить SBER");
const hdrEditProfile = buildHeaderSimple("Редактирование профиля");
const hdrChangePin = buildHeaderSimple("Смена PIN-кода");
const hdrRegister = buildHeaderSimple("Регистрация");
const hdrBrokerAccountComp = buildHeaderSimple("Брокерский счёт");
dropHdr(hdrMain);
dropHdr(hdrSearch);
dropHdr(hdrHistory);
dropHdr(hdrOrders);
dropHdr(hdrOrderDetail);
dropHdr(hdrProfile);
dropHdr(hdrSber);
dropHdr(hdrBuyComp);
dropHdr(hdrEditProfile);
dropHdr(hdrChangePin);
dropHdr(hdrRegister);
dropHdr(hdrBrokerAccountComp);

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
