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

// ─── Заявки (как брокерская плашка) ───────────────────────────────────────
const ordersCard = figma.createFrame();
ordersCard.name = "Заявки · Плашка";
ordersCard.layoutMode = "HORIZONTAL";
ordersCard.primaryAxisSizingMode = "FIXED";
ordersCard.counterAxisSizingMode = "AUTO";
ordersCard.resize(W - PAD * 2, 1);
ordersCard.paddingLeft = 16; ordersCard.paddingRight = 12;
ordersCard.paddingTop = 14; ordersCard.paddingBottom = 14;
ordersCard.itemSpacing = 8;
ordersCard.cornerRadius = shape.medium;
ordersCard.fills = tokenFill("surface");
ordersCard.strokes = [{ type: "SOLID", color: c.outline }];
ordersCard.strokeWeight = 1;
ordersCard.counterAxisAlignItems = "CENTER";
const ordersLeft = figma.createFrame();
ordersLeft.name = "Тексты заявок";
ordersLeft.layoutMode = "VERTICAL";
ordersLeft.itemSpacing = 2;
ordersLeft.fills = [];
ordersLeft.primaryAxisSizingMode = "AUTO";
ordersLeft.counterAxisSizingMode = "AUTO";
ordersLeft.appendChild(txt("Заявки", 12, "reg", c.onSurfaceVariant, "Лейбл"));
ordersLeft.children[0].layoutSizingHorizontal = "HUG";
ordersLeft.children[0].layoutSizingVertical = "HUG";
ordersLeft.appendChild(txt("Активные и завершённые", 16, "semi", c.onSurface, "Подзаголовок"));
ordersLeft.children[1].layoutSizingHorizontal = "HUG";
ordersLeft.children[1].layoutSizingVertical = "HUG";
ordersCard.appendChild(ordersLeft);
ordersLeft.layoutSizingHorizontal = "FILL";
ordersLeft.layoutSizingVertical = "HUG";
const ordersChevron = figma.createVector();
ordersChevron.name = "Шеврон заявки";
ordersChevron.resize(20, 20);
ordersChevron.vectorPaths = [{
  windingRule: "NONZERO",
  data: "M 7 4 L 14 10 L 7 16"
}];
ordersChevron.strokes = [{ type: "SOLID", color: c.onSurfaceVariant }];
ordersChevron.strokeWeight = 2;
ordersChevron.strokeCap = "ROUND";
ordersChevron.strokeJoin = "ROUND";
ordersChevron.fills = [];
ordersCard.appendChild(ordersChevron);
ordersChevron.layoutSizingHorizontal = "FIXED";
ordersChevron.layoutSizingVertical = "FIXED";
bodyMain.appendChild(ordersCard);
ordersCard.layoutSizingHorizontal = "FILL";
ordersCard.layoutSizingVertical = "HUG";
created.push(ordersCard.id);

bodyMain.appendChild(txt("Активы", 16, "semi", c.onSurface, "Секция"));
bodyMain.children[bodyMain.children.length - 1].layoutSizingHorizontal = "HUG";
appendInstance(bodyMain, assetSber);
appendInstance(bodyMain, assetLkoh);
appendInstance(scrMain, nav1);
scrMain.children[scrMain.children.length - 1].layoutSizingHorizontal = "FILL";
scrMain.children[scrMain.children.length - 1].layoutSizingVertical = "HUG";
