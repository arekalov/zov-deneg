# ZOV денег — контекст разработки

## Что это

Figma-плагин, генерирующий дизайн-документ инвестиционного приложения «ZOV денег» (Android Compact 360×800).
Запускается через Figma Desktop → Plugins → Development → Run code.js.

## Сборка

```bash
node zov-figma/build.mjs
```

Собирает `parts/` в порядке из `ORDER` в `build.mjs` → `dist/screens.assembled.js` + `plugin/code.js`.

---

## Структура файлов

```
parts/
  constants.js       — размеры, цвета, shape/mh токены, txt()
  tokens.js          — Figma Variables, tokenFill(), buildPaletteStrip(), buildTypographyShowcase()
  components.js      — все buildXxx() функции компонентов
  library.js         — страница "ZOV денег", три колонки: палитра+компоненты+типо (workStack),
                       экраны (screensShell) — справа от workStack на x = SCREENS_OFFSET_X = 1480
  screen-helpers.js  — androidScreen(), statusStrip(), scrollBody(), appendInstance()
  screens/
    screen-login.js
    screen-main.js
    screen-search.js
    screen-history.js
    screen-profile.js
  return.js          — return { createdNodeIds: created, message: "..." }
```

---

## Константы (constants.js)

```js
W = 360, H = 800, PAD = 16, GAP_SCREENS = 40
shape = { none:0, extraSmall:4, small:8, medium:12, large:16, extraLarge:28, full:200 }
mh = { button:40, chip:32, navBar:80, topBar:64, listItem1:56, listItem2:72, input:56 }
WORK_DOC_W = 1400        // ширина колонки документации
SCREENS_OFFSET_X = 1480  // x-позиция секции экранов
c = { bg, surface, surfaceContainer, primary, onSurface, onSurfaceVariant,
      outline, positive, negative, onPrimary, primaryContainer }
```

---

## Компоненты (components.js) — доступные переменные

После выполнения library.js в scope есть:
- `nav1/nav2/nav3` — buildNavBar(0/1/2)
- `sumComp` — buildSummaryCard()
- `assetSber, assetLkoh` — buildAssetRow(...)
- `txRow` — buildTransactionRow()
- `setNotif, setTheme, setCurr, setBio` — buildSettingRow(...)
- `inputPhone, inputPass` — buildInputField(...)
- `btnPrimary, btnSecondary` — buildButtonPrimary/Secondary(...)
- `pinDots` — buildPinDotsComp()
- `pinPad` — buildPinPad() — FIXED W-2*PAD=328, 4 ряда по 64px, кнопки layoutGrow=1
- `chipAll, chipBuy, chipSell` — buildChipFixed(...)
- `hdrMain, hdrSearch` — buildHeaderWithProfile(...)
- `hdrHistory, hdrProfile` — buildHeaderSimple(...)
- `buySellBar` — buildBuySellBar()

Функции-строители доступны глобально:
`buildNavBar`, `buildSummaryCard`, `buildAssetRow`, `buildChipFixed`,
`buildTransactionRow`, `buildSettingRow`, `buildInputField`,
`buildButtonPrimary`, `buildButtonSecondary`, `buildPinDotsComp`, `buildPinPad`,
`buildHeaderWithProfile`, `buildHeaderSimple`, `buildBuySellBar`, `buildSparklineCard`,
`buildIcon`, `txt`, `tokenFill`, `strokeOutline`, `appendInstance`, `fill`

ICON_PATHS: `home, search, history, person, fingerprint`

---

## Текущие экраны

### screen-login.js ✅ (переписан)
- statusStrip, loginBody (CENTER CENTER)
- Блок «Добро пожаловать / Введите PIN-код»
- pinDots instance (HUG)
- pinPad instance (FILL)
- bioBtn: FIXED 328×40, primaryContainer, fingerprint icon + text

### screen-main.js ✅
- statusStrip, hdrMain (FILL), scrollBody
- sumComp instance (FILL)
- «Активы» label
- assetSber, assetLkoh instances
- nav1 (FILL)

### screen-search.js ✅ (переписан)
- statusStrip, hdrSearch (FILL), scrollBody
- Поле поиска 48dp с иконкой search (surfaceContainer)
- filterRow: 4 inline-чипа (Все/Акции/Облигации/ETF), первый active
- «Популярные акции» label
- 5 inline-фреймов рыночных активов: GAZP, YNDX, VTBR, NVTK, ROSN
  (plain frames, не components, с цветом дельты positive/negative)
- nav2 (FILL)

### screen-history.js ✅ (переписан)
- statusStrip, hdrHistory (FILL), scrollBody
- chipRow: 3 chip instances (chipAll/chipBuy/chipSell), primaryAxisSizingMode=AUTO
- 4 inline-фрейма транзакций с header-строкой (тип слева, сумма справа)
  Покупка SBER, Продажа LKOH, Покупка YNDX, Дивиденды SBER
- nav3 (FILL)

### screen-profile.js ✅ (переписан)
- statusStrip, hdrProfile (FILL), scrollBody
- userCard: HORIZONTAL, аватар (48×48 circle, primaryContainer) + имя/email
- «Настройки» label
- 4 instance настроек (setNotif, setTheme, setCurr, setBio)
- logoutBtn: outlined красная кнопка
- nav1 (FILL)

---

## Что нужно сделать (задачи на следующую сессию)

### 1. Экран деталей по бумаге (НОВЫЙ) — `screens/screen-detail.js`

Добавить в `build.mjs` ORDER после screen-search.js.
Итого станет 6 экранов (расширить N_SCREENS=6 в constants.js).

Содержимое:
```
statusStrip
buildHeaderSimple("SBER · Сбербанк")
Табы: [Детали] [Стакан]  ← inline TabBar (2 таба)

--- Вкладка «Детали» ---
Блок цены:
  - Большая цена "298,45 ₽" (28sp semi)
  - Дельта за день "+4,6% · +13,12 ₽" (зелёный)
  - Кол-во в портфеле "10 шт. · ср. 285 ₽"

График динамики цены (buildPricePlot):
  - Линейный или area-chart (ломаная SVG path или бары)
  - Выбор периода для пользователя: inline pill-chips **[1Д] [1Н] [1М] [1Г]**; запрос к API — **`from` / `to`** (Unix, сек), клиент сам переводит выбор в интервал
  - Высота 120dp

Блок «О компании»:
  - Название, сектор, описание 2-3 строки

buySellBar instance (FILL, прибит к низу экрана)
```

### 2. Вкладка «Стакан» на экране деталей — buildOrderBook()

Добавить функцию в `components.js`:
```js
function buildOrderBook() {
  // Компонент: два столбца bid/ask
  // Бид (покупки) — зелёные строки с ценой + объёмом
  // Аск (продажи) — красные строки с ценой + объёмом
  // Горизонтальный разделитель посередине = текущая цена
  // 5-7 строк с каждой стороны
  // Бары заливки пропорциональны объёму (от правого/левого края)
}
```

### 3. Экран покупки (НОВЫЙ) — `screens/screen-buy.js`

Итого 7 экранов → N_SCREENS=7.

Содержимое:
```
statusStrip
buildHeaderSimple("Купить SBER")
scrollBody:
  Блок актива: тикер-бейдж + цена + дельта

  «Количество» label
  Счётчик: [−] [10 шт.] [+]  — inline frame (не component)
  «Итого: 2 984 ₽» — secondary text

  «Тип заявки» label
  Только рыночная заявка (подсказка под итогом / дисклеймер)

  Блок «Из портфеля»: доступно 45 320 ₽

  buildButtonPrimary("Подтвердить покупку") instance
  Disclaimer text (12sp, onSurfaceVariant)
```

### 4. Экран настроек (переработка screen-profile.js)

Убрать pattern «строка → ведёт куда-то». Заменить на inline:

```
Профиль (встроен):
  Аватар + имя/email + [Редактировать] кнопка

Разделитель

«Внешний вид»:
  Тема — Radio inline: ● Светлая  ○ Тёмная  ○ Системная

Разделитель

«Безопасность»:
  Toggle-строка: Вход по отпечатку пальца

Разделитель

Кнопка «Выйти из аккаунта» (outlined, красная)
```

---

## Важные паттерны кода

### Создание inline-компонентов на экране (plain frames, не figma.createComponent)
```js
// Для вещей уникальных для конкретного экрана — frame, не component
const row = figma.createFrame();
row.primaryAxisSizingMode = "AUTO";
row.counterAxisSizingMode = "FIXED";
row.resize(W - PAD * 2, 1);
// ... добавить в body с FILL
bodyXxx.appendChild(row);
row.layoutSizingHorizontal = "FILL";
row.layoutSizingVertical = "HUG";
created.push(row.id);
```

### Использование существующего компонента как instance
```js
const inst = appendInstance(parent, someComponent);
inst.layoutSizingHorizontal = "FILL";
inst.layoutSizingVertical = "HUG";
```

### TabBar (2 таба, inline)
```js
const tabBar = figma.createFrame();
tabBar.layoutMode = "HORIZONTAL";
tabBar.primaryAxisSizingMode = "FIXED";
tabBar.counterAxisSizingMode = "FIXED";
tabBar.resize(W, 48);
tabBar.fills = tokenFill("surface");
tabBar.strokes = [{ type: "SOLID", color: c.outline }];
tabBar.strokeBottomWeight = 1; // только нижняя линия
// ... два таба FILL каждый, active = underline + semi text
```

### buildPricePlot() — линейный график (нужно создать в components.js)
```js
// SVG path через figma.createVector() или серия прямоугольников/линий
// Данные — фиксированный массив из ~20 точек имитирующий рост
// Ось X — время, ось Y — цена
// Заливка area под линией (прямоугольники убывающей прозрачности) или
// просто ломаная линия через vectorPaths
```

### buildOrderBook() — стакан (нужно создать в components.js)
```js
// VERTICAL frame, W - PAD*2
// 5 строк ask (красные, цена убывает сверху вниз)
// разделитель с текущей ценой
// 5 строк bid (зелёные, цена убывает)
// Каждая строка: HORIZONTAL, цена + объём + бар заливки (rect с opacity 0.15)
```

---

## Известные ограничения / багфиксы из прошлых сессий

- `figma.createComponent()` требует явного `primaryAxisSizingMode = "AUTO"` и `counterAxisSizingMode = "AUTO"` иначе не растягивается в auto-layout
- `layoutWrap = "WRAP"` требует `primaryAxisSizingMode = "FIXED"` с явной шириной
- `brandBlock.removeChild(logoText)` после `logoCircle.appendChild(logoText)` — бросает исключение (logoText уже перемещён). Паттерн уже исправлен, не повторять.
- `screensShell` — отдельный фрейм на странице (не child workStack), x = SCREENS_OFFSET_X = 1480
- buildPinPad: кнопки используют `layoutGrow = 1`, строки FIXED 328×64
- Все экранные фреймы: `androidScreen()` создаёт FIXED 360×800 и добавляет в screensRow
