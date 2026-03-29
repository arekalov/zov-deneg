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

