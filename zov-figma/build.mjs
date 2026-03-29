#!/usr/bin/env node
import fs from "fs";
import path from "path";
import { fileURLToPath } from "url";

const root = path.dirname(fileURLToPath(import.meta.url));
const partsDir = path.join(root, "parts");
const pluginDir = path.join(root, "plugin");
const distDir = path.join(root, "dist");

const ORDER = [
  "constants.js",
  "tokens.js",
  "components.js",
  "library.js",
  "screen-helpers.js",
  "screens/screen-login.js",
  "screens/screen-register.js",
  "screens/screen-main.js",
  "screens/screen-deposit.js",
  "screens/screen-search.js",
  "screens/screen-history.js",
  "screens/screen-detail.js",
  "screens/screen-buy.js",
  "screens/screen-profile.js",
  "screens/screen-edit-profile.js",
  "screens/screen-change-pin.js",
  "return.js",
];

let assembled =
  "// Собрано: node zov-figma/build.mjs · Android 360×800 · ZOV денег\n\n";

for (const name of ORDER) {
  const p = path.join(partsDir, name);
  if (!fs.existsSync(p)) {
    console.error("Нет файла:", p);
    process.exit(1);
  }
  assembled += fs.readFileSync(p, "utf8").trimEnd() + "\n\n";
}

fs.mkdirSync(distDir, { recursive: true });
const outDist = path.join(distDir, "screens.assembled.js");
fs.writeFileSync(outDist, assembled, "utf8");
console.log("dist/screens.assembled.js");

const replaced = assembled.replace(
  /return\s*\{\s*createdNodeIds:\s*created\s*,\s*message:\s*"[^"]*"\s*\}\s*;\s*$/,
  `figma.notify("Готово: узлов — " + created.length);`
);

if (replaced === assembled) {
  console.error("Нет финального return { createdNodeIds: created, ... }");
  process.exit(1);
}

const pluginCode = `/* zov-figma/build.mjs */
(async function () {
  try {
${replaced}
  } catch (e) {
    figma.notify("Ошибка: " + (e && e.message ? e.message : String(e)));
  } finally {
    figma.closePlugin();
  }
})();
`;

fs.mkdirSync(pluginDir, { recursive: true });
fs.writeFileSync(path.join(pluginDir, "code.js"), pluginCode, "utf8");
console.log("plugin/code.js");
