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
