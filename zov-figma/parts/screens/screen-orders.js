makeScreensRow("Заявки");
// ═══════════════════════════════════════════════════════════════════════════
// Список заявок
// ═══════════════════════════════════════════════════════════════════════════
const scrOrdersList = androidScreen("Заявки");
statusStrip(scrOrdersList);
appendInstance(scrOrdersList, hdrOrders);
scrOrdersList.children[1].layoutSizingHorizontal = "FILL";
scrOrdersList.children[1].layoutSizingVertical = "HUG";
const bodyOrders = scrollBody(scrOrdersList);

const ord1 = figma.createFrame();
ord1.name = "Заявка · SBER · в очереди";
ord1.layoutMode = "VERTICAL";
ord1.paddingLeft = 16; ord1.paddingRight = 16; ord1.paddingTop = 14; ord1.paddingBottom = 14;
ord1.itemSpacing = 6;
ord1.cornerRadius = shape.medium;
ord1.fills = tokenFill("surface");
ord1.effects = shadowCard;
ord1.primaryAxisSizingMode = "AUTO"; ord1.counterAxisSizingMode = "FIXED";
ord1.resize(W - PAD * 2, 1);
ord1.appendChild(txt("SBER · покупка · 10 шт.", 14, "semi", c.onSurface, "Заголовок"));
ord1.children[0].layoutSizingHorizontal = "FILL"; ord1.children[0].layoutSizingVertical = "HUG";
ord1.appendChild(txt("26 апр. 2026, 12:00", 12, "reg", c.onSurfaceVariant, "Время"));
ord1.children[1].layoutSizingHorizontal = "FILL"; ord1.children[1].layoutSizingVertical = "HUG";
ord1.appendChild(txt("В очереди", 14, "semi", c.primary, "Статус"));
ord1.children[2].layoutSizingHorizontal = "FILL"; ord1.children[2].layoutSizingVertical = "HUG";
bodyOrders.appendChild(ord1);
ord1.layoutSizingHorizontal = "FILL"; ord1.layoutSizingVertical = "HUG";

const ord2 = figma.createFrame();
ord2.name = "Заявка · SBER · исполнена";
ord2.layoutMode = "VERTICAL";
ord2.paddingLeft = 16; ord2.paddingRight = 16; ord2.paddingTop = 14; ord2.paddingBottom = 14;
ord2.itemSpacing = 6;
ord2.cornerRadius = shape.medium;
ord2.fills = tokenFill("surface");
ord2.effects = shadowCard;
ord2.primaryAxisSizingMode = "AUTO"; ord2.counterAxisSizingMode = "FIXED";
ord2.resize(W - PAD * 2, 1);
ord2.appendChild(txt("SBER · покупка · 10 шт.", 14, "semi", c.onSurface, "Заголовок"));
ord2.children[0].layoutSizingHorizontal = "FILL"; ord2.children[0].layoutSizingVertical = "HUG";
ord2.appendChild(txt("13 марта 2026, 10:40", 12, "reg", c.onSurfaceVariant, "Время"));
ord2.children[1].layoutSizingHorizontal = "FILL"; ord2.children[1].layoutSizingVertical = "HUG";
ord2.appendChild(txt("Исполнена", 14, "semi", c.onSurface, "Статус"));
ord2.children[2].layoutSizingHorizontal = "FILL"; ord2.children[2].layoutSizingVertical = "HUG";
bodyOrders.appendChild(ord2);
ord2.layoutSizingHorizontal = "FILL"; ord2.layoutSizingVertical = "HUG";

appendInstance(scrOrdersList, nav1);
scrOrdersList.children[scrOrdersList.children.length - 1].layoutSizingHorizontal = "FILL";
scrOrdersList.children[scrOrdersList.children.length - 1].layoutSizingVertical = "HUG";

// ═══════════════════════════════════════════════════════════════════════════
// Детали заявки (отмена)
// ═══════════════════════════════════════════════════════════════════════════
const scrOrderDetail = androidScreen("Детали заявки");
statusStrip(scrOrderDetail);
appendInstance(scrOrderDetail, hdrOrderDetail);
scrOrderDetail.children[1].layoutSizingHorizontal = "FILL";
scrOrderDetail.children[1].layoutSizingVertical = "HUG";
const bodyDetail = scrollBody(scrOrderDetail);
bodyDetail.appendChild(txt("SBER · покупка · 10 шт.", 20, "semi", c.onSurface, "Заголовок"));
bodyDetail.children[bodyDetail.children.length - 1].layoutSizingHorizontal = "FILL";
bodyDetail.appendChild(txt("Создана: 26 апр. 2026, 12:00", 14, "reg", c.onSurfaceVariant, "Подпись"));
bodyDetail.children[bodyDetail.children.length - 1].layoutSizingHorizontal = "FILL";
bodyDetail.appendChild(txt("Направление\nпокупка", 13, "reg", c.onSurface, "Поле"));
bodyDetail.children[bodyDetail.children.length - 1].layoutSizingHorizontal = "FILL";
bodyDetail.appendChild(txt("Статус\nВ очереди", 13, "reg", c.onSurface, "Поле"));
bodyDetail.children[bodyDetail.children.length - 1].layoutSizingHorizontal = "FILL";

const cancelBtn = buildButtonSecondary("Отменить заявку");
cancelBtn.resize(W - PAD * 2, mh.button);
bodyDetail.appendChild(cancelBtn);
cancelBtn.layoutSizingHorizontal = "FILL";
cancelBtn.layoutSizingVertical = "HUG";

appendInstance(scrOrderDetail, nav1);
scrOrderDetail.children[scrOrderDetail.children.length - 1].layoutSizingHorizontal = "FILL";
scrOrderDetail.children[scrOrderDetail.children.length - 1].layoutSizingVertical = "HUG";
