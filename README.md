# OpenPixelCalcPro 📐✨

[![Download APK](https://img.shields.io/badge/📲_Download_APK-OpenPixelCalcPro.apk-2ea44f?style=for-the-badge&logo=android)](https://github.com/pstrelo/OpenPixelCalcPro/releases/latest)

[![License: GPL v3](https://img.shields.io/badge/License-GPLv3-blue.svg)](https://www.gnu.org/licenses/gpl-3.0)
[![Android Min SDK](https://img.shields.io/badge/Min%20SDK-34-brightgreen.svg)](https://developer.android.com/about/versions/14)
[![Android Target SDK](https://img.shields.io/badge/Target%20SDK-35-green.svg)](https://developer.android.com/about/versions/15)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.0.0-purple.svg)](https://kotlinlang.org)
[![Compose](https://img.shields.io/badge/Jetpack%20Compose-Material%203-blueviolet.svg)](https://developer.android.com/jetpack/compose)

A modern, open-source Android calculator inspired by the clean aesthetic of the default Google Pixel Calculator, redesigned and supercharged with advanced features. Built from scratch for Android 14+ (API 34 & 35) using **Jetpack Compose** and Material 3 Expressive, it combines advanced numerical math, real-time 2D Cartesian graphing, offline unit & currency conversion, and hardware sensor tools into a single, cohesive experience.

Born out of personal necessity: the stock calculator felt too limited for daily engineering and quick utility needs, so I designed the exact calculator I always wanted to have on my phone.

*On device launcher, the app displays cleanly as **"Calculator"**.*

---

## 📥 Direct APK Download

Ready to install immediately on your Android 14+ device:

👉 **[Download OpenPixelCalcPro.apk (Latest Release)](https://github.com/pstrelo/OpenPixelCalcPro/releases/latest)**

*(Simply enable "Install unknown apps" when prompted by your Android system).*

---

## 💡 About This Project & Vibe Coding

OpenPixelCalcPro is my first project built entirely through **vibe coding with Antigravity**. While the underlying code was generated and implemented through agentic AI, I personally drove the entire product lifecycle curating the creative vision, interface design, graphic assets, user experience, and feature workflows from the ground up. 

Although I used my programming background and prior familiarity with Android Studio to review, test, and verify the codebase for structural consistency, the application remains fully AI-synthesized. While it has been thoroughly tested locally and shouldn't present any obvious issues, **it is provided "as is" and I assume no legal or technical liability regarding software security, edge cases, or potential vulnerabilities**.

**The project is completely free, open source under the GPLv3 license, and shared with the developer community in the spirit of exploration. I’d love for you to test it, inspect the architecture, and contribute suggestions or pull requests for future improvements!**

---

## ✨ Features

### 1. Standard & Scientific Calculator
- **Instant Mathematical Evaluation**: Real-time expression parsing with support for parentheses balancing, standard operators, percentage calculations, powers, and roots.
- **Persistent Calculation History**: Local history storage organized by timestamp with one-tap recall and clear actions.
- **Angle Modes**: Seamless switching between Degrees (`DEG`) and Radians (`RAD`).
- **Tactile Feedback**: Configurable haptic feedback on keypress.

### 2. Advanced Math Solver & 2D Graphs
Inspired by modern math solvers and graphing utilities:
- **4 Thematic Keypad Tabs**:
  -  **Algebra**: Powers, roots, log/ln, absolute values, factorials, permutations, and combinatorics.
  -  **Trigonometry**: Sine, cosine, tangent, secant, cosecant, cotangent, hyperbolic functions, and inverses.
  -  **Calculus**: Numerical derivatives, definite integrals (Simpson's rule), limits, and summations.
  -  **Alphabet & Constants**: Mathematical and physical constants ($\pi$, $e$, $\varphi$, $c$, $h$, $G$), SI prefixes, Greek letters, and custom functions ($f, g, h, x, y$).
- **Multi-Equation 2D Graphing**:
  - Interactive Cartesian coordinate system with smooth pan and centroid-anchored pinch-to-zoom.
  - Color-coded equation badges matching curve strokes on canvas.
  - Root, intercept, and intersection snap detection.
  - Asymptote and singularity break handling without spurious connecting lines.
  - 1D and 2D inequality interval solving with dynamic region shading ($y > f(x)$, $y \le f(x)$, $x > a$).
- **Brent-Dekker Equation Solver**: Robust root finding for non-linear equations with exact interval and bracketed solution matrices.
- **Flexible Format Modes**: Quickly cycle results through Decimal (`DEC`), Fractions (`FRAC`), Engineering notation (`ENG`), Degrees/Minutes/Seconds (`DMS`), and Polar coordinates (`POL`).

### 3. Comprehensive Unit & Currency Converter
- **17 Offline Categories**:
  - Length, Mass / Weight, Temperature, Area, Volume, Speed, Time, Pressure, Energy, Power, Digital Data, Angle, Fuel Consumption, Cooking / Recipes, Typography, Frequency, and Currencies.
- **Offline & Online Currency Exchange**: Pre-loaded offline exchange rates with support for automatic and manual updates.
- **Quick Search & Swap**: Fast unit lookup with instant reciprocal conversion.


### 4. Pixel Hardware Sensor Toolbox
Integrated utility tools leveraging device sensors:
-  **Digital Compass**: Real-time 360° magnetic heading, true north calculation, cardinal direction indicators, GPS coordinates (DMS and Decimal), altitude, and an interactive 8-figure calibration wizard.
-  **Digital Bubble Level**: Dual-axis surface tilt measurement powered by the accelerometer with roll & pitch readouts and zero-angle alignment haptics.
-  **Calibrated Digital Ruler**: On-screen ruler with millimeter and inch scales physically calibrated to Pixel display metrics, complete with tick toggle and custom accent colors.

### 5. Material You & Dynamic App Icons
- **Dynamic Theming**: Full support for Android Dynamic Color (Material You), System Auto, Standard Light/Dark, and vibrant custom themes (**Candy Pop** and **Retro Cyber**).
- **Dynamic Launcher Icon**: Automatically synchronizes the app launcher icon on your home screen to match your selected theme palette.

---

## 🛠️ Architecture & Tech Stack

- **UI Layer**: 100% Jetpack Compose with Material 3 Expressive components, custom Canvas rendering for graphs and gauges.
- **Architecture**: MVI (Model-View-Intent) with immutable states and unidirectional data flow.
- **Language**: Kotlin 2.0 with Kotlin Compose compiler plugin.
- **Minimum Requirements**: Android 14 (API level 34).
- **Target SDK**: Android 15 (API level 35).
- **Sensors**: Hardware sensor managers using `SensorManager` (Accelerometer, Magnetic Field, Rotation Vector) and `LocationManager`.
- **Zero Heavy Dependencies**: Pure Kotlin numerical solvers and parsing engine without external heavyweight math engines.

---

## 🚀 Building from Source

### Prerequisites
- **Android Studio** Ladybug (2024.2.1+) or newer.
- **JDK**: Java Development Kit 17.
- **Android SDK**: Platforms 34 & 35 installed.

### Build Steps

1. **Clone the repository**:
   ```bash
   git clone https://github.com/pstrelo/OpenPixelCalcPro.git
   cd OpenPixelCalcPro
   ```

2. **Assemble Release APK**:
   ```bash
   ./gradlew assembleRelease
   ```
   The generated APK will be located at:
   `app/build/outputs/apk/release/OpenPixelCalcPro.apk`

3. **Install on a connected device / emulator**:
   ```bash
   ./gradlew installRelease
   ```

---

## 📂 Project Structure

```text
OpenPixelCalcPro/
├── app/
│   ├── build.gradle.kts           # Module build configuration (APK: OpenPixelCalcPro.apk)
│   ├── proguard-rules.pro         # Proguard/R8 rules for release
│   └── src/main/
│       ├── AndroidManifest.xml    # App manifest with dynamic launcher aliases
│       ├── java/com/google/pixel/calculator/
│       │   ├── MainActivity.kt    # Single activity entry point
│       │   ├── core/
│       │   │   ├── advanced/      # AdvancedMathEngine (Brent-Dekker, calculus)
│       │   │   ├── math/          # ExpressionParser, MathEvaluator, FastDoubleEvaluator
│       │   │   ├── sensors/       # Compass, Level & Location managers
│       │   │   └── utils/         # UnitConverter, Currency rates, Ruler calibration
│       │   ├── data/              # Storage, Theme & Converter preferences
│       │   └── ui/                # Compose screens, widgets, keypads, and themes
│       └── res/                   # Drawables, mipmaps, fonts, and localization
├── gradle/wrapper/                # Gradle wrapper binaries & properties
├── build.gradle.kts               # Root Gradle configuration
├── settings.gradle.kts            # Project settings & repositories
├── LICENSE                        # GNU General Public License v3.0
└── README.md                      # Project documentation
```

---

## 📄 License

This project is licensed under the terms of the **GNU General Public License v3.0** (GPL-3.0). See the [LICENSE](LICENSE) file for complete details.

---

## 👤 Author

Developed by **Lorenzo Trinati**.  
Contributions, suggestions, and pull requests are warmly welcome!
