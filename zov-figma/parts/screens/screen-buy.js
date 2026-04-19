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

bodyBuy.appendChild(txt("Только рыночная заявка · исполнение по лучшей доступной цене", 12, "reg", c.onSurfaceVariant, "Тип заявки"));
bodyBuy.children[bodyBuy.children.length - 1].layoutSizingHorizontal = "FILL";
bodyBuy.children[bodyBuy.children.length - 1].layoutSizingVertical = "HUG";

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
