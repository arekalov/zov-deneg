function androidScreen(title) {
  const root = figma.createFrame();
  root.name = `Android Compact ${W}×${H} — ${title}`;
  root.resize(W, 1);
  root.layoutMode = "VERTICAL";
  root.itemSpacing = 0;
  root.primaryAxisSizingMode = "AUTO";   // растягивается под контент
  root.counterAxisSizingMode = "FIXED";
  root.fills = tokenFill("bg");
  root.clipsContent = false;
  screensRow.appendChild(root);
  root.layoutSizingHorizontal = "FIXED";
  root.layoutSizingVertical = "HUG";
  created.push(root.id);
  return root;
}

// Android status bar: время слева, батарея+сигнал справа
function statusStrip(parent) {
  const s = figma.createFrame();
  s.name = "Status Bar";
  s.layoutMode = "HORIZONTAL";
  s.primaryAxisSizingMode = "FIXED";
  s.counterAxisSizingMode = "FIXED";
  s.resize(W, 24);
  s.paddingLeft = PAD;
  s.paddingRight = PAD;
  s.primaryAxisAlignItems = "CENTER";
  s.fills = tokenFill("bg");

  // Время
  const time = txt("9:41", 12, "semi", c.onSurface, "Время");
  s.appendChild(time);
  time.layoutSizingHorizontal = "FILL";
  time.layoutSizingVertical = "HUG";

  // Правая панель: сигнал + батарея
  const right = figma.createFrame();
  right.name = "Индикаторы";
  right.layoutMode = "HORIZONTAL";
  right.itemSpacing = 6;
  right.counterAxisAlignItems = "CENTER";
  right.fills = [];
  right.primaryAxisSizingMode = "AUTO";
  right.counterAxisSizingMode = "AUTO";

  // Сигнал: 4 бара разной высоты
  const sigFrame = figma.createFrame();
  sigFrame.name = "Сигнал";
  sigFrame.layoutMode = "HORIZONTAL";
  sigFrame.itemSpacing = 2;
  sigFrame.counterAxisAlignItems = "MAX";
  sigFrame.fills = [];
  sigFrame.primaryAxisSizingMode = "AUTO";
  sigFrame.counterAxisSizingMode = "FIXED";
  sigFrame.resize(1, 12);
  for (const h of [4, 6, 9, 12]) {
    const bar = figma.createRectangle();
    bar.resize(2, h);
    bar.cornerRadius = 1;
    bar.fills = [{ type: "SOLID", color: c.onSurface }];
    sigFrame.appendChild(bar);
    bar.layoutSizingHorizontal = "FIXED";
    bar.layoutSizingVertical = "FIXED";
  }
  right.appendChild(sigFrame);
  sigFrame.layoutSizingHorizontal = "HUG";
  sigFrame.layoutSizingVertical = "FIXED";

  // Батарея: прямоугольник-корпус + заливка
  const batOuter = figma.createFrame();
  batOuter.name = "Батарея";
  batOuter.layoutMode = "HORIZONTAL";
  batOuter.counterAxisAlignItems = "CENTER";
  batOuter.paddingLeft = 2;
  batOuter.paddingTop = 2;
  batOuter.paddingBottom = 2;
  batOuter.primaryAxisSizingMode = "FIXED";
  batOuter.counterAxisSizingMode = "FIXED";
  batOuter.resize(20, 11);
  batOuter.cornerRadius = 2;
  batOuter.fills = [];
  batOuter.strokes = [{ type: "SOLID", color: c.onSurface }];
  batOuter.strokeWeight = 1.5;

  const batFill = figma.createRectangle();
  batFill.resize(13, 7);
  batFill.cornerRadius = 1;
  batFill.fills = [{ type: "SOLID", color: c.onSurface }];
  batOuter.appendChild(batFill);
  batFill.layoutSizingHorizontal = "FIXED";
  batFill.layoutSizingVertical = "FIXED";

  right.appendChild(batOuter);
  batOuter.layoutSizingHorizontal = "FIXED";
  batOuter.layoutSizingVertical = "FIXED";

  s.appendChild(right);
  right.layoutSizingHorizontal = "HUG";
  right.layoutSizingVertical = "HUG";

  parent.appendChild(s);
  s.layoutSizingHorizontal = "FILL";
  s.layoutSizingVertical = "FIXED";
}

function scrollBody(parent) {
  const body = figma.createFrame();
  body.name = "Контент";
  body.layoutMode = "VERTICAL";
  body.itemSpacing = 12;
  body.paddingLeft = PAD;
  body.paddingRight = PAD;
  body.paddingTop = 16;
  body.paddingBottom = 16;
  body.fills = [];
  body.primaryAxisSizingMode = "AUTO";
  body.counterAxisSizingMode = "FIXED";
  parent.appendChild(body);
  body.layoutSizingHorizontal = "FILL";
  body.layoutSizingVertical = "HUG";
  return body;
}

function appendInstance(parent, mainComp) {
  const inst = mainComp.createInstance();
  parent.appendChild(inst);
  inst.layoutSizingHorizontal = "FILL";
  inst.layoutSizingVertical = "HUG";
  return inst;
}

// Снекбар — абсолютно поверх экрана, над nav-баром
// type: "success" | "error"  bottomOffset: дополнительный отступ снизу (для экранов с доп. панелями)
function addSnackbar(screenFrame, type, message, bottomOffset) {
  const SNACK_H   = 48;
  const NAV_H     = mh.navBar;   // 80px (из constants.js)
  const MARGIN    = 12;
  const extra     = bottomOffset || 0;

  const bar = figma.createFrame();
  bar.name = `Снекбар · ${message}`;
  bar.layoutMode = "HORIZONTAL";
  bar.counterAxisAlignItems = "CENTER";
  bar.itemSpacing = 10;
  bar.paddingLeft = 16; bar.paddingRight = 16;
  bar.paddingTop = 0;   bar.paddingBottom = 0;
  bar.cornerRadius = shape.medium;
  bar.primaryAxisSizingMode = "FIXED";
  bar.counterAxisSizingMode = "FIXED";
  bar.resize(W - PAD * 2, SNACK_H);
  const clr = type === "success" ? c.positive : c.negative;
  bar.fills = tokenFill("surface");
  bar.strokes = [{ type: "SOLID", color: clr }];
  bar.strokeWeight = 1.5;
  bar.effects = shadowCard;

  // Иконка
  const icon = txt(type === "success" ? "✓" : "✕", 15, "semi", clr, "Иконка");
  bar.appendChild(icon);
  icon.layoutSizingHorizontal = "HUG"; icon.layoutSizingVertical = "HUG";

  // Текст
  const msg = txt(message, 13, "reg", c.onSurface, "Текст");
  bar.appendChild(msg);
  msg.layoutSizingHorizontal = "FILL"; msg.layoutSizingVertical = "HUG";

  screenFrame.appendChild(bar);
  bar.layoutPositioning = "ABSOLUTE";
  bar.x = PAD;
  bar.y = H - NAV_H - MARGIN - SNACK_H - extra;

  created.push(bar.id);
  return bar;
}
