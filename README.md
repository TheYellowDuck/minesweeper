# MineSweeper

A fully-featured Minesweeper clone built in Java with a custom dark-themed UI, a probability-based AI solver, and dynamic tile scaling.

<video src="https://github.com/user-attachments/assets/5ca71202-ed3b-4767-a503-db45b24adbb2" controls width="100%"></video>

## Features

- **Custom rendered board** — 3D bevel tiles drawn with `Graphics2D`, distinct highlight/shadow edges for unrevealed vs. revealed states
- **Probability-based AI solver** — constraint propagation fixpoint loop deduces guaranteed mines; remaining cells ranked by local mine probability; random tie-breaking when probabilities are equal
- **Auto Solve mode** — checkbox triggers the AI on a 500 ms Swing Timer; runs on the EDT without blocking the UI
- **Configurable grid** — dialog lets you set columns, rows, and mine count; tile size auto-scales to fill the screen while keeping tiles between 20–60 px
- **Restart** — resets board, AI state, and mine counter without reopening the setup dialog
- **Instructions dialog** — in-game "How to Play" reference
- **Dynamic mine counter** — tracks remaining unflagged mines in real time
- **Platform-aware icon** — programmatically generated app icon reusing the same `drawRaised` / `drawMine` helpers; set on the macOS dock via `Taskbar` API

## Tech Stack

- **Java 17** — compiled and packaged as a standalone JAR
- **Java Swing** — `JFrame`, `JPanel`, `Graphics2D`, `javax.swing.Timer`, layout managers
- **OOP** — separate classes for game logic (`MineSweeper`), AI (`AI`), and UI (`Main`)
- **Algorithms** — BFS flood-fill for zero-cell reveal; constraint propagation + probabilistic ranking for the AI

## Skills Demonstrated

`Java` `Swing / GUI` `2D Graphics` `Algorithm Design` `Constraint Propagation` `Probability` `BFS` `OOP` `Event-Driven Programming` `JAR Packaging` `Git / GitHub`

## Running

Download `MineSweeper.jar` from [Releases](https://github.com/TheYellowDuck/minesweeper/releases) and run:

```bash
java -jar MineSweeper.jar
```

Requires Java 17 or later.
