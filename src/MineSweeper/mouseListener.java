// SPDX-License-Identifier: PolyForm-Noncommercial-1.0.0
// Required Notice: Copyright (c) 2026 George Zhang — https://github.com/TheYellowDuck

package MineSweeper;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

public class mouseListener implements MouseListener {

    @Override
    public void mouseClicked(MouseEvent e) {
        if (Main.gameOver || Main.win) return;

        // Coordinates are relative to Main (M) since the listener is on M, not the frame
        int row = e.getY() / Main.scale;
        int col = e.getX() / Main.scale;

        if (row < 0 || row >= Main.XSquares || col < 0 || col >= Main.YSquares) return;

        if (e.getButton() == MouseEvent.BUTTON1) {
            Main.MS.game(row, col);
        } else if (e.getButton() == MouseEvent.BUTTON3) {
            int state = Main.clickedOn[row][col];
            if (state == 0 || state == 2) {
                Main.clickedOn[row][col] = (state + 2) % 4;
                if (Main.clickedOn[row][col] == 0) {
                    if (Main.map[row][col] == -1) Main.MS.correct--;
                    else                          Main.MS.incorrect--;
                } else {
                    if (Main.map[row][col] == -1) Main.MS.correct++;
                    else                          Main.MS.incorrect++;
                }
                Main.updateMineCount();
            }
        }

        winCondition();
        Main.M.repaint();
    }

    public void winCondition() {
        if (Main.clickcount == Main.totalCells - Main.Mines
                || (Main.MS.correct == Main.Mines && Main.MS.incorrect == 0)) {
            Main.win = true;
            Main.setStatus("You Win!", Main.STATUS_WIN);
            Main.MS.clickedMine();
        }
    }

    @Override public void mousePressed(MouseEvent e)  {}
    @Override public void mouseReleased(MouseEvent e) {}
    @Override public void mouseEntered(MouseEvent e)  {}
    @Override public void mouseExited(MouseEvent e)   {}
}
