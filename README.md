# MineSweeper

A fully-featured Minesweeper clone built in **Java** with a custom dark-themed UI, a probability-based AI solver, BFS flood-fill reveals, and dynamic tile scaling.

<video src="https://github.com/user-attachments/assets/5ca71202-ed3b-4767-a503-db45b24adbb2" controls width="100%"></video>

## Features

- **Custom rendered board** — 3D bevel tiles drawn with `Graphics2D`, distinct highlight/shadow edges for unrevealed vs. revealed states
- **Probability-based AI solver** — constraint propagation fixpoint loop deduces guaranteed mines; remaining cells ranked by local mine probability; random tie-breaking when probabilities are equal
- **Auto Solve mode** — checkbox triggers the AI on a 500 ms Swing Timer; runs on the EDT without blocking the UI
- **Configurable grid** — dialog lets you set columns, rows, and mine count; tile size auto-scales to fill the screen while keeping tiles between 20–60 px
- **Restart** — resets board, AI state, and mine counter without reopening the setup dialog
- **Instructions dialog** — in-game "How to Play" reference
- **Dynamic mine counter** — tracks remaining unflagged mines in real time
- **Platform-aware icon** — programmatically generated app icon reusing the same `drawRaised` / `drawMine` helpers; set on the macOS dock via the `Taskbar` API

## How It Works

The game logic, AI, and UI are kept in separate classes (`MineSweeper`, `AI`, `Main`). Revealing an empty cell triggers a **BFS flood-fill** that cascades outward through all connected zero-count cells, opening their neighbours in one sweep.

The AI solver runs a **constraint-propagation fixpoint loop**: it repeatedly scans every numbered cell, and whenever a cell's number equals its count of adjacent unknowns it flags those as guaranteed mines (and conversely opens cells that are provably safe), iterating until no new deductions are possible. When pure deduction stalls, it falls back to **probabilistic reasoning** — ranking the remaining unknown cells by their local mine probability and breaking ties randomly — so it can keep making the statistically best move. Auto-solve mode steps this engine on a 500 ms Swing `Timer`, entirely on the Event Dispatch Thread so the UI never freezes.

## Skills Demonstrated

- Object-oriented design — game logic, AI solver, and UI in separate classes
- Event-driven programming — Swing mouse and window listeners, timer-driven auto-solve
- 2D rendering — `Graphics2D` 3D-bevel tiles with highlight/shadow edges
- BFS flood-fill — cascading reveal of connected zero-count cells
- Constraint-propagation solver — fixpoint deduction of guaranteed mines and safe cells
- Probabilistic reasoning — local mine-probability ranking with random tie-breaking
- Algorithm design — combined deterministic + probabilistic AI strategy
- Dynamic UI scaling — tile size auto-scales (20–60 px) to grid and screen size
- Procedural graphics — programmatically generated app icon and dock integration via `Taskbar`
- Input handling — mouse listener for reveal/flag, window listener for lifecycle
- Layout management — Swing layout managers and `SpringUtilities`
- JAR packaging — distributed as a standalone runnable JAR

## Tech Stack

- Java 17
- Java Swing / AWT (`JFrame`, `JPanel`, `Graphics2D`, `javax.swing.Timer`, layout managers)
- `Taskbar` API for the macOS dock icon
- Packaged as a standalone runnable JAR (`MineSweeper.jar`)

## Demo & Links

- ⬇️ [Download the latest build](https://github.com/TheYellowDuck/minesweeper/releases)

## Getting Started

Download `MineSweeper.jar` from [Releases](https://github.com/TheYellowDuck/minesweeper/releases) and run:

```bash
java -jar MineSweeper.jar
```

Requires Java 17 or later.
