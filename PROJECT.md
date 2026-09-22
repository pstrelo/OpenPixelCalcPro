# Project: Google Math Solver Redesign (Calcolatrice-Pixel)

## Architecture
The application redesign refactors the Advanced/Scientific Calculator screen in Jetpack Compose following MVI (Model-View-Intent) architecture with unidirectional data flow:
- **UI Layer (`ui/advanced/`)**:
  - `AdvancedCalculatorScreen.kt`: Container composable managing view modes (`CALCULATOR_ONLY`, `SPLIT_SCREEN`, `FULLSCREEN_GRAPH`).
  - `AdvancedFormulaDisplay.kt`: Displays multi-equation card list with colored badges, 2D formatted math expressions, and vertical square matrix solution brackets `[ x = -3 ; x = 3 ]`. Includes detached Graph button in top action bar and "+ Aggiungi equazione".
  - `AdvancedKeypad.kt`: 4 thematic tabs (Algebra `#A855F7`, Trigonometry `#10B981`, Calculus `#EC4899`, Alphabet `#F97316`) + persistent bottom edit bar (`[ ← ]`, `[ → ]`, `[ ⌫ DEL ]`, `[ AC ]`, `[ FORMAT ]`, `[ ➤ ]`) with dynamic accent color on `[ ➤ ]`.
  - `AdvancedGraphCanvas.kt`: 2D Cartesian Canvas with mathematical coordinate transformation, pan, centroid-anchored zoom, multi-curve rendering, root/intercept snap inspection `(x; y)`, and inequality shading.
- **State & ViewModel (`ui/advanced/`)**:
  - `AdvancedCalculatorState.kt`: Immutable state containing equation list, active equation index, cursor position, active tab, format mode (`DEC`, `FRAC`, `ENG`, `DMS`, `POL`), graph view mode, and mathematical viewport (`centerMathX`, `centerMathY`, `scaleX`, `scaleY`).
  - `AdvancedCalculatorViewModel.kt`: Handles events (insert character, move cursor, delete, clear, toggle format, solve, toggle graph, pan/zoom).
- **Core Engine Layer (`core/advanced/`)**:
  - `AdvancedMathEngine.kt`: Parsing, evaluation, Brent-Dekker equation root solver, inequality interval solver, numerical calculus (Simpson integral, derivative stencil), format transformations (`DEC`, `FRAC`, `ENG`, `DMS`, `POL`), and function sampling.

## Feature Inventory
| # | Feature | Description | Milestone | Source |
|---|---------|-------------|-----------|--------|
| 1 | M3 Dark Palette | Material 3 Expressive Dark Theme `#1E212D` with squircle/pill buttons | M2 | R1 |
| 2 | 4 Thematic Tabs | Algebra `#A855F7`, Trigonometry `#10B981`, Calculus `#EC4899`, Alphabet `#F97316` | M2 | R1 |
| 3 | Dynamic [ ➤ ] Accent | Execution button color dynamically matches active tab accent color | M2 | R1 |
| 4 | Bottom Edit Bar | Persistent 6-key row: `[ ← ]`, `[ → ]`, `[ ⌫ DEL ]`, `[ AC ]`, `[ FORMAT ]`, `[ ➤ ]` | M2 | R1 |
| 5 | Cursor Navigation | `[ ← ]` and `[ → ]` navigate insertion point within the active equation | M1 | R1 |
| 6 | '=' Character Insertion | Pressing `=` inserts `" = "` into expression without evaluating | M1 | R2 |
| 7 | '➤' Execution Trigger | Pressing `➤` executes solving/evaluating the equation or expression | M1 | R2 |
| 8 | Instant FORMAT Switch | `FORMAT` cycles active result instantly: DEC, FRAC, ENG, DMS, POL | M1 | R3 |
| 9 | Multi-Equation List | Desmos/GeoGebra style equation card list with "+ Aggiungi equazione" | M3 | R4 |
| 10 | Colored Badges | Distinct color badge for each equation matching its curve on the graph | M3 | R4 |
| 11 | Matrix Solution Brackets | Vertical square bracket formatting `[ x = -3 ; x = 3 ]` for multiple roots | M3 | R4 |
| 12 | Detached Graph Button | Button at the top action bar next to formula box toggling graph view | M3 | R5 |
| 13 | Graph View Modes | Three-state view: `CALCULATOR_ONLY`, `SPLIT_SCREEN`, and `FULLSCREEN_GRAPH` | M3 | R5 |
| 14 | Invariant Math Viewport | Graph coordinate model (`centerMathX`, `centerMathY`, `scaleX`, `scaleY`) | M3 | R5 |
| 15 | Pan & Centroid Zoom | Multi-touch drag and centroid-anchored pinch-to-zoom on Canvas | M3 | R5 |
| 16 | Multi-Curve Rendering | Draws curves for all valid equations using their assigned badge colors | M3 | R5 |
| 17 | Root & Intercept Tracing | Touch-to-inspect snaps to roots/intercepts with coordinate bubble `(x; y)` | M3 | R5 |
| 18 | Inequality Shading | Semi-transparent area shading for inequalities (`<`, `>`, `<=`, `>=`) | M3 | R5 |
| 19 | Test Suite & Build Pass | All unit tests pass and `./gradlew assembleDebug` compiles cleanly | M4 | AC |
| 20 | Git Auto-Push | Automatic commit and push to origin per repository rules | M4 | User Rule |

## Milestones
| # | Name | Scope | Dependencies | Status |
|---|------|-------|-------------|--------|
| M1 | Domain & State Engine Integration | `AdvancedCalculatorState.kt`, `AdvancedCalculatorViewModel.kt`, cursor handling, `=` vs `➤`, FORMAT cycling, multi-equation state | none | PLANNED |
| M2 | Keypad & M3 Tab Redesign | `AdvancedKeypad.kt`, 4 thematic tabs, dynamic `[ ➤ ]` accent, 6-button bottom edit bar | M1 | PLANNED |
| M3 | Display & 2D Cartesian Graph Integration | `AdvancedFormulaDisplay.kt`, `AdvancedGraphCanvas.kt`, `AdvancedCalculatorScreen.kt`, top Graph button, multi-equation card, math viewport, gestures, roots/intercepts, inequality shading | M1, M2 | PLANNED |
| M4 | Final Milestone: 100% E2E Test Pass & Coverage Hardening | Full screen integration, run all E2E & unit tests, build verification (`./gradlew assembleDebug`), adversarial hardening, and Git auto-push | M1, M2, M3 | PLANNED |

## Interface Contracts

### M1 ↔ M2: Keypad Events & Colors
```kotlin
enum class MathSolverTab(val title: String, val accentColor: Color) {
    ALGEBRA("Algebra", Color(0xFFA855F7)),
    TRIGONOMETRY("Trigonometry", Color(0xFF10B981)),
    CALCULUS("Calculus", Color(0xFFEC4899)),
    ALPHABET("Alphabet", Color(0xFFF97316))
}

// Keypad callbacks:
onTabSelected: (MathSolverTab) -> Unit
onInsertChar: (String) -> Unit
onMoveCursorLeft: () -> Unit
onMoveCursorRight: () -> Unit
onDeleteChar: () -> Unit
onClearAll: () -> Unit
onCycleFormat: () -> Unit
onExecute: () -> Unit
```

### M1 ↔ M3: Multi-Equation & Graph Viewport
```kotlin
data class EquationItem(
    val id: String,
    val expression: String,
    val cursorPosition: Int,
    val color: Color,
    val result: SolveResult?,
    val formatMode: FormatMode = FormatMode.DECIMAL,
    val formattedResult: String = ""
)

data class GraphViewport(
    val centerMathX: Double = 0.0,
    val centerMathY: Double = 0.0,
    val scaleX: Float = 40f, // pixels per math unit
    val scaleY: Float = 40f
)
```

## Code Layout
- `app/src/main/java/com/google/pixel/calculator/ui/advanced/`
  - `AdvancedCalculatorScreen.kt`: Main scaffold and view mode management
  - `AdvancedCalculatorViewModel.kt`: State holder and event dispatcher
  - `AdvancedCalculatorState.kt`: State models, `MathSolverTab`, `EquationItem`, `GraphViewport`
  - `AdvancedKeypad.kt`: Tab bar, 4 tab keypads, and bottom edit bar
  - `AdvancedFormulaDisplay.kt`: Top card, detached Graph button, multi-equation list, solution brackets
  - `AdvancedGraphCanvas.kt`: 2D Cartesian graph canvas, pan/zoom, curves, roots/intercepts, shading
- `app/src/main/java/com/google/pixel/calculator/core/advanced/`
  - `AdvancedMathEngine.kt`: Mathematical solver and format conversions
- `app/src/test/java/com/google/pixel/calculator/`
  - Unit tests for engine, ViewModel, state, and formatting
