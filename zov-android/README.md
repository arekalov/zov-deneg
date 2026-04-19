# ЗОВ денег — Android

Клиент брокерского приложения: **Jetpack Compose**, **Material 3**, контракт API в [`../openapi.yaml`](../openapi.yaml), макеты — плагин [`../zov-figma/`](../zov-figma/).

## Требования

- **JDK 17+** (для Gradle/AGP; `compileOptions` модуля — 11).
- **Android Studio** или CLI: из каталога **`zov-android/`** выполнять Gradle.

## Сборка

```bash
./gradlew :app:assembleDebug
./gradlew :app:assembleRelease   # нужна своя подпись release
```

## Качество

```bash
./gradlew detekt                 # или из корня монорепы: ./gradlew detekt
```

**Pre-commit (Detekt):** в корне монорепы выполни один раз `git config core.hooksPath .githooks` — при коммите с изменениями под `zov-android/` запускается `./gradlew detekt`. Подробнее в [`docs/android-development-process.md`](docs/android-development-process.md).

## Документация

| Файл | Содержание |
|------|------------|
| [`docs/android-stack.md`](docs/android-stack.md) | Стек (Compose, Hilt, Ktor, Navigation, CI). |
| [`docs/android-development-process.md`](docs/android-development-process.md) | Процесс: Figma → слои → OpenAPI. |

## CI (монорепа)

- **`master`** + изменения `zov-android/**` → Detekt ([`../.github/workflows/android-master-detekt.yml`](../.github/workflows/android-master-detekt.yml)).
- Ветка **`android-release`** + изменения `zov-android/**` → debug APK в артефактах ([`../.github/workflows/android-release-apk.yml`](../.github/workflows/android-release-apk.yml)).
