# Стек Android-клиента «ЗОВ денег»

Нативное приложение в этом каталоге на **Kotlin**. Ниже зафиксирован выбранный стек и оговорки; процесс разработки (Figma + OpenAPI по шагам) — в [`android-development-process.md`](android-development-process.md).

---

## Язык и асинхронность

| Технология | Назначение |
|------------|------------|
| **Kotlin** | Основной язык модуля `:app` и будущих модулей. |
| **Kotlin Coroutines** | Асинхронность в ViewModel, репозиториях и Ktor; при необходимости **Flow** для потоков данных. |

---

## UI

| Технология | Назначение |
|------------|------------|
| **Jetpack Compose** | Декларативный UI. |
| **Navigation for Compose** (`androidx.navigation:navigation-compose`) | Навигация между экранами: граф, **`NavHost`**, при необходимости **type-safe** маршруты (Kotlin DSL / сериализация — по мере внедрения в `libs.versions.toml`). |
| **Material Design 3** (`androidx.compose.material3`) | Компоненты, тема, типографика и цвета в духе M3; выравнивание с макетами из Figma-плагина. |

---

## Архитектура

| Подход | Назначение |
|--------|------------|
| **Чистая архитектура** | Разделение на слои; зависимости направлены от UI к домену и данным. |
| **Репозитории** | Слой `data`: единая точка доступа к сети (и при необходимости к кэшу/локальному хранилищу). |
| **Use case** | Слой `domain`: прикладные сценарии, оркестрация репозиториев, правила без привязки к Android framework. |
| **ViewModel** | Слой представления: состояние экрана, вызов use case-ов, подготовка данных для Compose. |

---

## Сеть и моки

| Технология | Назначение |
|------------|------------|
| **Ktor Client** | HTTP-клиент; запросы/ответы в соответствии с [`openapi.yaml`](../../openapi.yaml) в корне монорепы. |
| **Мок бэкенда** | Тот же Ktor Client с тестовым движком (**MockEngine**) и фикстурами либо заглушкой в репозитории — для разработки UI и домена без живого сервера. |

Конкретные модули Gradle (`ktor-client-android`, сериализация `kotlinx.serialization` или иной формат под OpenAPI) добавляются в `gradle/libs.versions.toml` по мере внедрения.

---

## Внедрение зависимостей (DI)

| Технология | Назначение |
|------------|------------|
| **Hilt** (Dagger) | DI для ViewModel, репозиториев, Ktor `HttpClient`, конфигурации base URL и моков. |

---

## Качество кода

| Инструмент | Статус |
|------------|--------|
| **Detekt** | Линтер для Kotlin: плагин **`io.gitlab.arturbosch.detekt` 1.23.8**, конфиг **[`detekt.yml`](../detekt.yml)** (скопирован из учебного проекта lab1 и дополнен `ignoreAnnotated: Composable` для имён `@Composable`). Плагин и **`detekt-formatting`** подключены в **`:app`**; из корня Gradle доступна задача **`detekt`** (делегирует на `:app:detekt`). **`check`** в `:app` зависит от **`detekt`**. Отчёты: `app/build/reports/detekt/`. Локально перед коммитом — **`pre-commit`** в [`.githooks/`](../../.githooks) монорепы (после `git config core.hooksPath .githooks`). |

При необходимости позже можно явно зафиксировать **ktlint** или форматирование через IDE — пока в стеке основной линтер **Detekt**.

---

## CI / CD

GitHub Actions в корне монорепы [`.github/workflows/`](../../.github/workflows/), срабатывают только при изменениях в **`zov-android/**`** (`paths`).

| Workflow | Ветка | Что делает |
|----------|-------|------------|
| **`android-master-detekt.yml`** | **`master`** | `./gradlew detekt` в `zov-android/`; при падении загружаются отчёты Detekt. **APK не собирается.** |
| **`android-release-apk.yml`** | **`android-release`** | `./gradlew :app:assembleDebug` — **debug APK** (стандартная debug-подпись Gradle, **секреты не нужны**). Артефакт **`app-debug-apk`** (`app/build/outputs/apk/debug/*.apk`). |

**Ветка `android-release`:** создай при необходимости; workflow запустится на `push`, где в коммите есть изменения под **`zov-android/**`**. Для публикации в стор позже настроишь отдельно **`assembleRelease`** и свой keystore вне этого упрощённого сценария.

---

## Связанные документы

- [`android-development-process.md`](android-development-process.md) — шаги от макета до merge.
- [`openapi.yaml`](../../openapi.yaml) — контракт API.
- [`zov-figma/README.md`](../../zov-figma/README.md) — генерация макетов в Figma.
