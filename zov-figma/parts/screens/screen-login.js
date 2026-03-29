const scrLogin = androidScreen("Вход");
statusStrip(scrLogin);

const loginBody = scrollBody(scrLogin);
loginBody.primaryAxisAlignItems = "CENTER";
loginBody.counterAxisAlignItems = "CENTER";
loginBody.paddingTop = 48;
loginBody.paddingBottom = 32;
loginBody.itemSpacing = 24;

// ─── Приветствие ─────────────────────────────────────────────────────────────
const greetBlock = figma.createFrame();
greetBlock.name = "Приветствие";
greetBlock.layoutMode = "VERTICAL";
greetBlock.primaryAxisAlignItems = "CENTER";
greetBlock.counterAxisAlignItems = "CENTER";
greetBlock.itemSpacing = 6;
greetBlock.fills = [];
greetBlock.primaryAxisSizingMode = "AUTO";
greetBlock.counterAxisSizingMode = "AUTO";
greetBlock.appendChild(txt("Добро пожаловать", 22, "semi", c.onSurface, "Заголовок"));
greetBlock.children[0].layoutSizingHorizontal = "HUG";
greetBlock.children[0].layoutSizingVertical = "HUG";
greetBlock.appendChild(txt("Введите PIN-код для входа", 14, "reg", c.onSurfaceVariant, "Подзаголовок"));
greetBlock.children[1].layoutSizingHorizontal = "HUG";
greetBlock.children[1].layoutSizingVertical = "HUG";
loginBody.appendChild(greetBlock);
greetBlock.layoutSizingHorizontal = "HUG";
greetBlock.layoutSizingVertical = "HUG";

// ─── PIN-точки ───────────────────────────────────────────────────────────────
const dotsInst = appendInstance(loginBody, pinDots);
dotsInst.layoutSizingHorizontal = "HUG";
dotsInst.layoutSizingVertical = "HUG";

// ─── PIN-клавиатура (FILL = растягивается на ширину тела) ─────────────────────
appendInstance(loginBody, pinPad);

// ─── Кнопка биометрии ────────────────────────────────────────────────────────
const bioBtn = figma.createFrame();
bioBtn.name = "Кнопка · Биометрия";
bioBtn.layoutMode = "HORIZONTAL";
bioBtn.primaryAxisAlignItems = "CENTER";
bioBtn.counterAxisAlignItems = "CENTER";
bioBtn.primaryAxisSizingMode = "FIXED";
bioBtn.counterAxisSizingMode = "FIXED";
bioBtn.resize(W - PAD * 2, mh.button);
bioBtn.paddingLeft = 24;
bioBtn.paddingRight = 24;
bioBtn.itemSpacing = 8;
bioBtn.cornerRadius = shape.full;
bioBtn.fills = tokenFill("primaryContainer");

const fpIcon = buildIcon("fingerprint", 20, c.primary);
bioBtn.appendChild(fpIcon);
fpIcon.layoutSizingHorizontal = "FIXED";
fpIcon.layoutSizingVertical = "FIXED";

bioBtn.appendChild(txt("Войти по биометрии", 14, "med", c.primary, "Текст"));
bioBtn.children[bioBtn.children.length - 1].layoutSizingHorizontal = "HUG";
bioBtn.children[bioBtn.children.length - 1].layoutSizingVertical = "HUG";

loginBody.appendChild(bioBtn);
bioBtn.layoutSizingHorizontal = "FILL";
bioBtn.layoutSizingVertical = "FIXED";
