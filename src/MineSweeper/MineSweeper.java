// SPDX-License-Identifier: PolyForm-Noncommercial-1.0.0
// Required Notice: Copyright (c) 2026 George Zhang — https://github.com/TheYellowDuck

package MineSweeper;

import java.util.LinkedList;
import java.util.Queue;

public class MineSweeper {
	
	static int XLen, YLen, mineCount;
	int incorrect = 0;
	int correct = 0;
	static Mine[] mines;
	static boolean[][] visited;
	MineSweeper MS = this;
	
	MineSweeper() {
		Main.clickcount = 0;
		XLen = 10;
		YLen = 10;
		Main.totalCells = XLen * YLen;
		mineCount = (int) (Main.totalCells/10);
		Main.map = new int[XLen][YLen];
		Main.clickedOn = new int[XLen][YLen];
		visited = new boolean[XLen][YLen];
		mines = new Mine[mineCount];
		generate();
		printMap();
	}

	MineSweeper(int XLen1, int YLen1) {
		Main.clickcount = 0;
		XLen = XLen1;
		YLen = YLen1;
		Main.totalCells = XLen * YLen;
		mineCount = (int) (Main.totalCells/10);
		Main.map = new int[XLen][YLen];
		Main.clickedOn = new int[XLen][YLen];
		visited = new boolean[XLen][YLen];
		mines = new Mine[mineCount];
		generate();
		printMap();
	}
	
	MineSweeper(int XLen1, int YLen1, int mineCount1) {
		Main.clickcount = 0;
		XLen = XLen1;
		YLen = YLen1;
		Main.totalCells = XLen * YLen;
		mineCount = (int) (mineCount1 > Main.totalCells ? Main.totalCells : mineCount1);
		Main.map = new int[XLen][YLen];
		Main.clickedOn = new int[XLen][YLen];
		visited = new boolean[XLen][YLen];
		mines = new Mine[mineCount];
		generate();
		printMap();
	}
	
	public static void printMap() {
		for (int[] i : Main.map) {
			for (int j : i)
				System.out.printf("%-3d", j);
			System.out.println();
		}
	}
	
	public void game(int x, int y) {
		int v = Main.map[x][y];
		switch (v) {
		case -1:
			clickedMine();
			break;
		case 0:
			clickedZero(x, y);
			break;
		default:
			Main.clickedOn[x][y] = 1;
			if (Main.ai != null) Main.ai.remove(x, y);
			Main.clickcount ++;
			break;
		}
	}
	
	public static void generate() {
		int count = 0;
		while (count < mineCount && count < Main.totalCells) {
			int x = (int) (Math.random() * XLen);
			int y = (int) (Math.random() * YLen);
			if (Main.map[x][y] != -1) {
				Main.map[x][y] = -1;
				mines[count] = new Mine(x, y);
				calculateCells(x, y);
				count ++;
			}
		}
	}
	
	public static void calculateCells(int x, int y) {
		for (int i = -1; i <= 1; i ++) 
			if (x + i >= 0 && x + i < XLen) 
				for (int j = -1; j <= 1; j ++) 
					if (y + j >= 0 && y + j < YLen) 
						if (Main.map[x + i][y + j] != -1)
							Main.map[x + i][y + j] ++;
	}
	
	public static void clickedZero(int x, int y) {
		Queue<Mine> Q = new LinkedList<>();
		Mine s = new Mine(x, y);
		Q.add(s);
		while (!Q.isEmpty()) {
			Mine m = Q.poll();
			Main.clickedOn[m.x][m.y] = 1;
			visited[m.x][m.y] = true;
			if (Main.ai != null) Main.ai.remove(m.x, m.y);
			Main.clickcount++;
			if (Main.map[m.x][m.y] == 0) {
				for (int i = -1; i <= 1; i ++)
					if (m.x + i >= 0 && m.x + i < XLen)
						for (int j = -1; j <= 1; j ++)
							if (m.y + j >= 0 && m.y + j < YLen)
									if (!visited[m.x + i][m.y + j]) {
										Q.add(new Mine(m.x + i, m.y + j));
										visited[m.x + i][m.y + j] = true;
									}
			}
		}
	}
	
	public void clickedMine() {
		if (!Main.win) {
			Main.gameOver = true;
			Main.setStatus("Game Over!", Main.STATUS_GAMEOVER);
		}
		for (Mine m : mines)
			Main.clickedOn[m.x][m.y] = 3;
		Main.M.repaint();
	}
	
	static class Mine {
		int x, y;
		Mine (int x, int y) {
			this.x = x;
			this.y = y;
		}
	}
	
}
