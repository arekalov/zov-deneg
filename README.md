# ЗОВ денег

**ЗОВ денег** — учебный/продуктовый проект **мобильного брокерского приложения**: торговля ценными бумагами, портфель, баланс, заявки и связанный бэкенд по [ТЗ](docs/TZ.md). В репозитории лежат **контракт REST API**, **документация**, **макеты в Figma** и **нативное Android-приложение** (Jetpack Compose).

## Монорепозиторий

Это **один Git-репозиторий** без вложенных `.git` и без **git submodule**: все подпроекты — обычные каталоги в одном дереве коммитов. Общий CI, одна история изменений, проще согласовывать API (`openapi.yaml`) с клиентом.

Сюда по мере разработки добавляются другие части стека из ТЗ (например **Ktor-бэкенд**, **React Native**, сервисы на **Go** и т.д.) — каждый в своей папке в корне репозитория.

**Git hooks:** в **`.githooks/`** лежит `pre-commit` с **Detekt** для `zov-android/` (см. [`zov-android/docs/android-development-process.md`](zov-android/docs/android-development-process.md)). Один раз из корня репозитория: **`git config core.hooksPath .githooks`**.

## Подпроекты и что в них лежит

| Путь | Назначение |
|------|------------|
| **`openapi.yaml`** | Контракт **OpenAPI 3**: JWT, пользователи, ценные бумаги, портфель, заявки, транзакции, баланс (пополнение / вывод), денежные суммы и цены как **строки**, время — **Unix timestamp**. Публикуется в Swagger UI через GitHub Actions. |
| **`docs/`** | Тексты: **`TZ.md`** — техническое задание; **`roles.md`** — роли и доступ к API. |
| **`zov-figma/`** | **Dev-плагин Figma**: из `parts/` собирает страницы «Компоненты» и «Экраны» (макеты **360×800**, Android Compact). Подробности — [`zov-figma/README.md`](zov-figma/README.md). |
| **`zov-android/`** | **Нативное Android-приложение** (Gradle, `:app`, Compose). Быстрый старт — [`zov-android/README.md`](zov-android/README.md); детали — [`zov-android/docs/`](zov-android/docs/). |
| **`.github/workflows/`** | CI: например публикация **Swagger UI** на GitHub Pages при изменении `openapi.yaml`. |

### Каталог `zov-figma/` (кратко)

| Путь | Назначение |
|------|------------|
| **`parts/`** | Исходники плагина: токены, компоненты, хелперы; **`parts/screens/`** — экраны по файлам. |
| **`build.mjs`** | Сборка: склеивает `parts/` в один скрипт. |
| **`dist/`** | Сгенерированный **`screens.assembled.js`** (отладка / Figma MCP). |
| **`plugin/`** | **`manifest.json`** и **`code.js`** — папка для импорта плагина в Figma. |

Сборка плагина из корня:

```bash
node zov-figma/build.mjs
```

После правок в `zov-figma/parts/` команду повторяйте. Нужны **Node.js** и шрифт **Inter** в Figma.

### Каталог `zov-android/` (кратко)

Сборка и запуск — стандартно для Android Studio / Gradle из каталога `zov-android/` (`./gradlew assembleDebug` и т.д.). Локальные пути и кэши IDE не коммитьте (см. корневой **`.gitignore`**).

## Полезные ссылки

- Спецификация API: файл **`openapi.yaml`** в корне (или задеплоенный Swagger, если настроены GitHub Pages).
- Дизайн: запуск плагина — **`zov-figma/README.md`**.
