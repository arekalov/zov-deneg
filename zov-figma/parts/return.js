// Пост-обработка: min-height = H, nav всегда внизу
// Обходим все ряды в screensShell (structure: screensShell → sec → row "Экраны" → screens)
function postProcessScreen(scr) {
  const finalH = Math.max(H, Math.round(scr.height));
  scr.primaryAxisSizingMode = "FIXED";
  scr.resize(W, finalH);
  scr.layoutSizingVertical = "FIXED";
  for (const ch of scr.children) {
    if (ch.name === "Контент") {
      ch.layoutSizingVertical = "FILL";
      ch.layoutGrow = 1;
      break;
    }
  }
}

for (const sec of screensShell.children) {
  if (sec.type !== "FRAME") continue;
  for (const child of sec.children) {
    if (child.name === "Экраны") {
      for (const scr of child.children) postProcessScreen(scr);
    }
  }
}

return { createdNodeIds: created, message: "Компоненты + экраны 360×H" };
