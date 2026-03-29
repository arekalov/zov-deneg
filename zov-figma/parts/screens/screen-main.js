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
