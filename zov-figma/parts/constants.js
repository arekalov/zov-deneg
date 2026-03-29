await figma.loadFontAsync({ family: "Inter", style: "Regular" });
await figma.loadFontAsync({ family: "Inter", style: "Medium" });
await figma.loadFontAsync({ family: "Inter", style: "Semi Bold" });

const W = 360;
const H = 800;
const PAD = 16;
const GAP_SCREENS = 40;

// M3 Shape tokens (corner radius, dp)
const shape = {
  none:       0,
  extraSmall: 4,
  small:      8,
  medium:     12,
  large:      16,
  extraLarge: 28,
  full:       200,
};

// M3 component heights (dp)
const mh = {
  button:    40,
  chip:      32,
  navBar:    80,
  topBar:    64,
  listItem1: 56,
  listItem2: 72,
  input:     56,
};
const N_SCREENS = 5;
const PAD_SCREEN_SHELL = 48;
const PAD_WORK_STACK = 56;
const STRIP_PAD_X = 40;
const DOC_ROW_W = N_SCREENS * W + (N_SCREENS - 1) * GAP_SCREENS;
const WORK_DOC_W = 1400; // ширина колонки документации (компоненты + палитра + типо)
const SCREENS_OFFSET_X = WORK_DOC_W + 80; // позиция секции экранов по горизонтали
const PALETTE_ROW_W = WORK_DOC_W - 2 * PAD_WORK_STACK - 2 * STRIP_PAD_X;

const c = {
  bg: { r: 0.96, g: 0.97, b: 0.98 },
  surface: { r: 1, g: 1, b: 1 },
  surfaceContainer: { r: 0.93, g: 0.94, b: 0.95 },
  primary: { r: 0.13, g: 0.59, b: 0.22 },
  onSurface: { r: 0.09, g: 0.1, b: 0.11 },
  onSurfaceVariant: { r: 0.45, g: 0.46, b: 0.48 },
  outline: { r: 0.88, g: 0.89, b: 0.9 },
  positive: { r: 0.05, g: 0.55, b: 0.28 },
  negative: { r: 0.72, g: 0.16, b: 0.14 },
  onPrimary: { r: 1, g: 1, b: 1 },
  primaryContainer: { r: 0.85, g: 0.94, b: 0.86 },
};

const fill = (color) => [{ type: "SOLID", color }];

const shadowCard = [
  {
    type: "DROP_SHADOW",
    color: { r: 0, g: 0, b: 0, a: 0.07 },
    offset: { x: 0, y: 2 },
    radius: 10,
    spread: 0,
    visible: true,
    blendMode: "NORMAL",
  },
];

function txt(content, size, weight, color, layerName) {
  const t = figma.createText();
  t.name = layerName;
  t.characters = content;
  t.fontSize = size;
  t.fontName =
    weight === "semi"
      ? { family: "Inter", style: "Semi Bold" }
      : weight === "med"
        ? { family: "Inter", style: "Medium" }
        : { family: "Inter", style: "Regular" };
  t.fills = fill(color);
  return t;
}
