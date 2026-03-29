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
