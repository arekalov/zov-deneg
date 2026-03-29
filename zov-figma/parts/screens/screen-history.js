makeScreensRow("История транзакций");
const scrHist = androidScreen("История транзакций");
statusStrip(scrHist);
appendInstance(scrHist, hdrHistory);
scrHist.children[1].layoutSizingHorizontal = "FILL";
scrHist.children[1].layoutSizingVertical = "HUG";
const bodyHist = scrollBody(scrHist);

// ─── Чипы фильтрации ─────────────────────────────────────────────────────────
const chipRow = figma.createFrame();
chipRow.name = "Чипсы фильтрации";
chipRow.layoutMode = "HORIZONTAL";
chipRow.itemSpacing = 8;
chipRow.fills = [];
chipRow.primaryAxisSizingMode = "AUTO";
chipRow.counterAxisSizingMode = "AUTO";
bodyHist.appendChild(chipRow);
chipRow.layoutSizingHorizontal = "FILL";
chipRow.layoutSizingVertical = "HUG";

for (const chipComp of [chipAll, chipBuy, chipSell]) {
  const inst = chipComp.createInstance();
  chipRow.appendChild(inst);
  inst.layoutSizingHorizontal = "HUG";
  inst.layoutSizingVertical = "HUG";
}

// ─── Транзакции ──────────────────────────────────────────────────────────────
const txData = [
  { type: "Покупка",    ticker: "SBER", time: "28 марта 2026, 14:32", details: "10 шт. · цена 298,12 ₽", amount: "+2 981 ₽",  pos: true  },
  { type: "Продажа",   ticker: "LKOH", time: "27 марта 2026, 10:14", details: "2 шт. · цена 6 540 ₽",   amount: "−13 080 ₽", pos: false },
  { type: "Покупка",   ticker: "YNDX", time: "25 марта 2026, 16:45", details: "1 шт. · цена 3 210 ₽",   amount: "+3 210 ₽",  pos: true  },
  { type: "Дивиденды", ticker: "SBER", time: "20 марта 2026, 09:00", details: "2,50 ₽ × 10 шт.",        amount: "+25 ₽",     pos: true  },
];

for (const tx of txData) {
  const row = figma.createFrame();
  row.name = `Транзакция · ${tx.type} · ${tx.ticker}`;
  row.layoutMode = "VERTICAL";
  row.paddingLeft = 16;
  row.paddingRight = 16;
  row.paddingTop = 14;
  row.paddingBottom = 14;
  row.itemSpacing = 4;
  row.cornerRadius = shape.medium;
  row.fills = tokenFill("surface");
  row.effects = shadowCard;
  row.primaryAxisSizingMode = "AUTO";
  row.counterAxisSizingMode = "FIXED";
  row.resize(W - PAD * 2, 1);

  const header = figma.createFrame();
  header.name = "Шапка";
  header.layoutMode = "HORIZONTAL";
  header.fills = [];
  header.primaryAxisSizingMode = "AUTO";
  header.counterAxisSizingMode = "AUTO";
  const typeLabel = txt(`${tx.type} · ${tx.ticker}`, 14, "semi", c.onSurface, "Тип");
  header.appendChild(typeLabel);
  typeLabel.layoutSizingHorizontal = "FILL";
  typeLabel.layoutSizingVertical = "HUG";
  const amountLabel = txt(tx.amount, 14, "semi", tx.pos ? c.positive : c.negative, "Сумма");
  header.appendChild(amountLabel);
  amountLabel.layoutSizingHorizontal = "HUG";
  amountLabel.layoutSizingVertical = "HUG";
  row.appendChild(header);
  header.layoutSizingHorizontal = "FILL";
  header.layoutSizingVertical = "HUG";

  const timeLabel = txt(tx.time, 12, "reg", c.onSurfaceVariant, "Время");
  row.appendChild(timeLabel);
  timeLabel.layoutSizingHorizontal = "FILL";
  timeLabel.layoutSizingVertical = "HUG";

  const detailsLabel = txt(tx.details, 13, "reg", c.onSurface, "Детали");
  row.appendChild(detailsLabel);
  detailsLabel.layoutSizingHorizontal = "FILL";
  detailsLabel.layoutSizingVertical = "HUG";

  bodyHist.appendChild(row);
  row.layoutSizingHorizontal = "FILL";
  row.layoutSizingVertical = "HUG";
  created.push(row.id);
}

appendInstance(scrHist, nav3);
scrHist.children[scrHist.children.length - 1].layoutSizingHorizontal = "FILL";
scrHist.children[scrHist.children.length - 1].layoutSizingVertical = "HUG";
