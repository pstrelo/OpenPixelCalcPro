# E2E Test Infra: Google Math Solver Redesign

## Test Philosophy
- Opaque-box, requirement-driven testing based on user specifications in `ORIGINAL_REQUEST.md`.
- No reliance on internal implementation details; tests verify external contracts, state transitions, mathematical correctness, formatting, and UI behavior.
- Systematic 4-tier methodology: Category-Partition + Boundary Value Analysis + Pairwise Combinatorial Testing + Real-World Workload Testing.

## Feature Inventory & Test Coverage Goals
| # | Feature | Requirement Source | Tier 1 (>=5) | Tier 2 (>=5) | Tier 3 (Pairwise) | Tier 4 (Real-World) |
|---|---------|-------------------|:------------:|:------------:|:-----------------:|:-------------------:|
| 1 | M3 Expressive Theme & Styling | R1 | 5 | 5 | ✓ | ✓ |
| 2 | 4 Thematic Tabs & Keypads | R1 | 5 | 5 | ✓ | ✓ |
| 3 | Dynamic [ ➤ ] Accent Color | R1 | 5 | 5 | ✓ | ✓ |
| 4 | Bottom 6-Key Edit Bar | R1 | 5 | 5 | ✓ | ✓ |
| 5 | Cursor Navigation ([ ← ], [ → ]) | R1 | 5 | 5 | ✓ | ✓ |
| 6 | '=' Character Insertion | R2 | 5 | 5 | ✓ | ✓ |
| 7 | '➤' Execution / Solve Trigger | R2 | 5 | 5 | ✓ | ✓ |
| 8 | FORMAT Cycling (DEC, FRAC, ENG, DMS, POL) | R3 | 5 | 5 | ✓ | ✓ |
| 9 | Multi-Equation List & "+ Add" | R4 | 5 | 5 | ✓ | ✓ |
| 10 | Colored Badges Matching Graph | R4 | 5 | 5 | ✓ | ✓ |
| 11 | Solution Matrix Brackets `[ x = -3 ; x = 3 ]` | R4 | 5 | 5 | ✓ | ✓ |
| 12 | Detached Top Graph Button | R5 | 5 | 5 | ✓ | ✓ |
| 13 | Graph View Modes (Collapsed/Split/Fullscreen) | R5 | 5 | 5 | ✓ | ✓ |
| 14 | Invariant Math Viewport & Coordinates | R5 | 5 | 5 | ✓ | ✓ |
| 15 | Pan & Centroid Zoom Gestures | R5 | 5 | 5 | ✓ | ✓ |
| 16 | Multi-Curve Rendering | R5 | 5 | 5 | ✓ | ✓ |
| 17 | Root & Intercept Snap Tracing `(x; y)` | R5 | 5 | 5 | ✓ | ✓ |
| 18 | Inequality Shading & Boundary Curves | R5 | 5 | 5 | ✓ | ✓ |

## Test Architecture
- Test Runner: Gradle unit test runner (`./gradlew testDebugUnitTest`).
- Test location: `app/src/test/java/com/google/pixel/calculator/e2e/`.
- Test case format: JUnit4 tests evaluating state flow, engine evaluations, format switches, cursor movements, and multi-equation systems.

## Real-World Application Scenarios (Tier 4)
| # | Scenario | Features Exercised | Expected Outcome |
|---|----------|--------------------|------------------|
| 1 | Quadratic with dual roots $x^2 - 9 = 0$ | F6, F7, F8, F11, F16, F17 | Result formatted as `[ x = -3 ; x = 3 ]`, curve passes through (-3,0) and (3,0) |
| 2 | Trigonometric wave $y = \sin(x)$ with DMS format | F2, F7, F8, F16, F17 | Result converted to DMS, roots at multiples of $\pi$ |
| 3 | Polynomial intersection system $y = x^2$ and $y = 2x + 3$ | F9, F10, F16, F17 | Multi-equation display, colored badges, intersections at $x = -1, 3$ |
| 4 | Calculus derivative & integral $\int_0^\pi \sin(x)dx$ | F2, F7, F8 | Numerical evaluation yields 2.0, convertible to FRAC/ENG |
| 5 | Inequality region $y \le 2x - 1$ | F6, F7, F18 | Inequality intervals solved and shaded on Cartesian grid |

## Coverage Thresholds
- Tier 1: >= 90 test cases (>= 5 per feature across 18 features)
- Tier 2: >= 90 boundary & edge test cases
- Tier 3: >= 18 pairwise interaction tests
- Tier 4: >= 9 realistic application scenarios
- Total Target: >= 200 comprehensive automated assertions
