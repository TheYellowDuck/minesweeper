// SPDX-License-Identifier: PolyForm-Noncommercial-1.0.0
// Required Notice: Copyright (c) 2026 George Zhang — https://github.com/TheYellowDuck

package MineSweeper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class AI {

    Square[][] squares;
    ArrayList<Square> order;

    static class SquareCompare implements Comparator<Square> {
        @Override
        public int compare(Square a, Square b) {
            int c = Double.compare(a.prob, b.prob);
            if (c != 0) return c;
            if (a.x != b.x) return Integer.compare(a.x, b.x);
            return Integer.compare(a.y, b.y);
        }
    }

    AI(int x, int y) {
        squares = new Square[x][y];
        order = new ArrayList<>();
        for (int i = 0; i < x; i++) {
            for (int j = 0; j < y; j++) {
                squares[i][j] = new Square(i, j);
                order.add(squares[i][j]);
            }
        }
    }

    public void remove(int x, int y) {
        order.remove(squares[x][y]);
    }

    private boolean inBounds(int r, int c) {
        return r >= 0 && r < Main.XSquares && c >= 0 && c < Main.YSquares;
    }

    public void updateProbabilities() {
        order.removeIf(s -> Main.clickedOn[s.x][s.y] != 0);

        int unrevealed = order.size();
        if (unrevealed == 0) return;

        // ── Step 1: constraint propagation to find guaranteed mines ──────────
        //
        // If a revealed number tile has (remaining_mines == free_unrevealed_neighbors),
        // every one of those free neighbors must be a mine.  We loop until no new
        // mines are deduced, so each deduction can cascade into the next.
        boolean[][] definitelyMine = new boolean[Main.XSquares][Main.YSquares];
        boolean changed = true;
        while (changed) {
            changed = false;
            for (int i = 0; i < Main.XSquares; i++) {
                for (int j = 0; j < Main.YSquares; j++) {
                    if (Main.clickedOn[i][j] != 1 || Main.map[i][j] <= 0) continue;

                    int flagged = 0, knownMines = 0, free = 0;
                    for (int di = -1; di <= 1; di++) {
                        for (int dj = -1; dj <= 1; dj++) {
                            if (di == 0 && dj == 0) continue;
                            int ni = i + di, nj = j + dj;
                            if (!inBounds(ni, nj)) continue;
                            int st = Main.clickedOn[ni][nj];
                            if (st == 2)                             flagged++;
                            else if (st == 0 && definitelyMine[ni][nj]) knownMines++;
                            else if (st == 0)                        free++;
                        }
                    }
                    int remaining = Main.map[i][j] - flagged - knownMines;
                    if (remaining > 0 && remaining == free) {
                        for (int di = -1; di <= 1; di++) {
                            for (int dj = -1; dj <= 1; dj++) {
                                if (di == 0 && dj == 0) continue;
                                int ni = i + di, nj = j + dj;
                                if (!inBounds(ni, nj)) continue;
                                if (Main.clickedOn[ni][nj] == 0 && !definitelyMine[ni][nj]) {
                                    definitelyMine[ni][nj] = true;
                                    changed = true;
                                }
                            }
                        }
                    }
                }
            }
        }

        // ── Step 2: assign baseline probability to every unrevealed cell ─────
        double baseline = (double) Main.Mines / unrevealed;
        for (Square s : order) {
            s.prob = definitelyMine[s.x][s.y] ? 1.0 : baseline;
        }

        // ── Step 3: refine with local constraint probabilities ───────────────
        //
        // For each revealed number tile recompute remaining mines after
        // subtracting flags AND cells we know are mines from Step 1.
        // Apply localProb = remaining / free as an upper bound (Math.max).
        // When remaining == 0, all free neighbors are definitely safe → prob = 0.
        for (int i = 0; i < Main.XSquares; i++) {
            for (int j = 0; j < Main.YSquares; j++) {
                if (Main.clickedOn[i][j] != 1 || Main.map[i][j] <= 0) continue;

                int flagged = 0, knownMines = 0, free = 0;
                for (int di = -1; di <= 1; di++) {
                    for (int dj = -1; dj <= 1; dj++) {
                        if (di == 0 && dj == 0) continue;
                        int ni = i + di, nj = j + dj;
                        if (!inBounds(ni, nj)) continue;
                        int st = Main.clickedOn[ni][nj];
                        if (st == 2)                             flagged++;
                        else if (st == 0 && definitelyMine[ni][nj]) knownMines++;
                        else if (st == 0)                        free++;
                    }
                }
                int remaining = Math.max(0, Main.map[i][j] - flagged - knownMines);
                if (free == 0) continue;

                double localProb = (double) remaining / free;
                for (int di = -1; di <= 1; di++) {
                    for (int dj = -1; dj <= 1; dj++) {
                        if (di == 0 && dj == 0) continue;
                        int ni = i + di, nj = j + dj;
                        if (!inBounds(ni, nj)) continue;
                        if (Main.clickedOn[ni][nj] != 0 || definitelyMine[ni][nj]) continue;
                        Square s = squares[ni][nj];
                        if (localProb == 0.0) {
                            s.prob = 0.0;                      // definitely safe
                        } else {
                            s.prob = Math.max(s.prob, localProb);
                        }
                    }
                }
            }
        }
    }

    // Picks and clicks the unrevealed cell with the lowest estimated mine probability.
    // When multiple cells share the minimum probability, one is chosen at random.
    public void step() {
        if (Main.gameOver || Main.win) return;
        updateProbabilities();
        if (order.isEmpty()) return;
        Collections.sort(order, new SquareCompare());
        double minProb = order.get(0).prob;
        int tied = 1;
        while (tied < order.size() && Math.abs(order.get(tied).prob - minProb) < 1e-10)
            tied++;
        Square best = order.get((int)(Math.random() * tied));
        Main.MS.game(best.x, best.y);
    }

    class Square {
        int x, y;
        double prob;

        Square(int x, int y) {
            this.x = x;
            this.y = y;
            prob = (double) Main.Mines / (Main.XSquares * Main.YSquares);
        }
    }
}
