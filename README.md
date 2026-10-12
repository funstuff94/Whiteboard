# WhiteBoard for Android & ChromeOS

A simple, fast, and responsive Whiteboard application built with **Jetpack Compose** and **Material 3** for Android and ChromeOS devices (including Chromebooks and Android laptops).

---

## ✨ Features

- 🌙 **Dark Mode by Default**: High-contrast dark canvas (`#0F0F12`) with a crisp, glowing white marker (`#FFFFFF`).
- ☀️ **Light Mode Toggle**: Seamlessly switch between dark slate and classic whiteboard backgrounds with one tap.
- 🎨 **Color Palettes**:
  - **Dark Mode**: Pure White, Neon Yellow, Electric Cyan, Vivid Green, Coral Pink, and Sunset Orange.
  - **Light Mode**: Charcoal Black, Royal Blue, Crimson Red, Forest Green, Purple, and Orange.
- ✍️ **Smooth Freeform Drawing**: Real-time quadratic Bezier path smoothing for natural pen, touch, and trackpad drawing.
- 🧹 **Marker & Eraser Tools**: Easily toggle between marker and eraser modes with configurable stroke sizes.
- 📏 **Stroke Widths**: 4 quick presets: Fine (4dp), Medium (8dp), Bold (16dp), and Thick (28dp).
- ↩️ **Undo & Redo**: Full action history stack to step backward and forward through your strokes.
- 📁 **Multi-Whiteboard Management**:
  - Save multiple whiteboards locally.
  - Browse all saved boards with creation dates, stroke counts, and theme indicators.
  - Open, switch, rename, or delete whiteboards anytime.
- 💾 **Real Persistence**: Lightweight, dependency-free local JSON file storage (`board_<id>.json` and `index.json`) ensures zero data loss across app sessions.

---

## 🛠️ Architecture & Tech Stack

- **UI Framework**: [Jetpack Compose](https://developer.android.com/jetpack/compose) with Material 3
- **Language**: Kotlin 2.x
- **Architecture**: MVVM (Model-View-ViewModel) + Repository Pattern
- **Target SDK**: Android 14+ (API 34/36)
- **Minimum SDK**: Android 8.0 (API 26)
- **Testing**: JUnit 4 unit tests covering ViewModel state, stroke undo/redo logic, JSON serialization, and repository operations

---

## 🚀 Building & Running

### Prerequisites
- JDK 17 or higher
- Android SDK Platform 34+

### Build Debug APK
```bash
./gradlew assembleDebug
```
The compiled APK will be located at:
```
app/build/outputs/apk/debug/WhiteBoard-debug.apk
```

### Run Tests
```bash
./gradlew testDebugUnitTest
```
