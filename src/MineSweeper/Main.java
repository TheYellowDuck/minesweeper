// SPDX-License-Identifier: PolyForm-Noncommercial-1.0.0
// Required Notice: Copyright (c) 2026 George Zhang — https://github.com/TheYellowDuck

package MineSweeper;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.function.IntConsumer;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class Main extends JPanel {

    static int scale     = 34;
    static int XSize     = scale * 10;
    static int YSize     = scale * 10;
    static int XSquares  = 10;
    static int YSquares  = 10;
    static int Mines     = 10;
    static int spacing   = 3;
    static int clickcount = 0;
    static long totalCells;
    static boolean gameOver = false, win = false;

    static Main M;
    static MineSweeper MS;
    static AI ai;
    static int[][] map;
    static int[][] clickedOn;

    static JLabel    mineCountLabel;
    static JLabel    statusLabel;
    static JCheckBox autoCheckbox;

    // ── palette ──────────────────────────────────────────────────────────────
    static final Color BG_DARK      = new Color(20,  22,  26);   // near-black gap
    static final Color HEADER_BG    = new Color(38,  42,  50);   // dark header/footer
    static final Color TILE_HIDDEN  = new Color(72,  92,  122);  // steel blue – unrevealed
    static final Color TILE_LIGHT   = new Color(112, 138, 172);  // lighter blue – bevel highlight
    static final Color TILE_SHADOW  = new Color(34,  44,  60);   // dark blue – bevel shadow
    static final Color TILE_REVEAL  = new Color(210, 205, 192);  // warm off-white – revealed
    static final Color TILE_MINE_BG = new Color(200, 52,  48);   // red – mine
    static final Color TILE_WIN     = new Color(68,  182, 82);   // green – win
    static final Color FLAG_COLOR   = new Color(232, 72,  36);   // bright orange-red flag

    static final Color STATUS_NORMAL   = Color.WHITE;
    static final Color STATUS_GAMEOVER = new Color(255, 85,  85);
    static final Color STATUS_WIN      = new Color(85,  220, 105);

    // number colours: index 1-8
    static final Color[] NUM_COLOR = {
        null,
        new Color(25,  118, 210),  // 1 – blue
        new Color(46,  138, 50),   // 2 – green
        new Color(211, 47,  47),   // 3 – red
        new Color(26,  35,  126),  // 4 – dark blue
        new Color(136, 14,  79),   // 5 – purple
        new Color(0,   131, 143),  // 6 – cyan
        new Color(33,  33,  33),   // 7 – near-black
        new Color(80,  80,  80),   // 8 – grey
    };

    // ── constructor ──────────────────────────────────────────────────────────
    public Main() {
        setPreferredSize(new Dimension(YSize, XSize));
        setBackground(BG_DARK);
    }

    // ── static helpers ───────────────────────────────────────────────────────
    static void setStatus(String text, Color color) {
        if (statusLabel == null) return;
        statusLabel.setText(text);
        statusLabel.setForeground(color);
    }

    static void updateMineCount() {
        if (mineCountLabel == null || MS == null) return;
        int flagged = MS.correct + MS.incorrect;
        mineCountLabel.setText("Mines: " + (Mines - flagged));
    }

    static void initGame() {
        gameOver   = false;
        win        = false;
        clickcount = 0;
        MS = new MineSweeper(XSquares, YSquares, Mines);
        ai = new AI(XSquares, YSquares);
        if (autoCheckbox != null) autoCheckbox.setSelected(false);
        setStatus("Good luck!", STATUS_NORMAL);
        updateMineCount();
        if (M != null) M.repaint();
    }

    // ── entry point ──────────────────────────────────────────────────────────
    public static void main(String[] args) {

        // Set dock icon before any window is created (macOS requires this early)
        System.setProperty("apple.awt.application.name", "MineSweeper");
        Image appIcon = createAppIcon();
        try {
            Taskbar.getTaskbar().setIconImage(appIcon);
        } catch (UnsupportedOperationException | SecurityException ignored) {}

        // ── setup dialog ─────────────────────────────────────────────────────
        JFrame input = new JFrame("MineSweeper – New Game");
        input.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        input.setResizable(false);

        JPanel wrap = new JPanel(new BorderLayout(0, 12));
        wrap.setBackground(HEADER_BG);
        wrap.setBorder(new EmptyBorder(16, 20, 16, 20));

        JLabel title = new JLabel("New Game", SwingConstants.CENTER);
        title.setFont(new Font("Monospaced", Font.BOLD, 18));
        title.setForeground(Color.WHITE);
        wrap.add(title, BorderLayout.NORTH);

        JPanel fields = new JPanel(new SpringLayout());
        fields.setBackground(HEADER_BG);
        String[] lblTxt = {"Columns (X):", "Rows (Y):", "Mines:"};
        JTextField[] tfs = {new JTextField(6), new JTextField(6), new JTextField(6)};
        for (int k = 0; k < 3; k++) {
            JLabel lbl = new JLabel(lblTxt[k]);
            lbl.setForeground(Color.LIGHT_GRAY);
            lbl.setFont(new Font("Monospaced", Font.PLAIN, 13));
            fields.add(lbl);
            styleTextField(tfs[k]);
            fields.add(tfs[k]);
        }
        SpringUtilities.makeCompactGrid(fields, 3, 2, 6, 6, 8, 6);
        wrap.add(fields, BorderLayout.CENTER);

        JButton startBtn = makeButton("Start Game", new Color(30, 110, 200));
        wrap.add(startBtn, BorderLayout.SOUTH);
        input.setContentPane(wrap);

        startBtn.addActionListener(evt -> {
            parseField(tfs[0], true,  v -> YSquares = v);  // Columns → width
            parseField(tfs[1], true,  v -> XSquares = v);  // Rows → height
            if (tfs[2].getText().trim().isEmpty()) {
                Mines = Math.max(1, XSquares * YSquares / 10);
            } else {
                parseField(tfs[2], false, v -> Mines = Math.min(v, XSquares * YSquares));
            }
            XSize = XSquares * scale;
            YSize = YSquares * scale;
            input.dispose();
        });
        input.getRootPane().setDefaultButton(startBtn);

        windowListener WL = new windowListener();
        input.addWindowListener(WL);
        input.pack();
        input.setLocationRelativeTo(null);
        input.setVisible(true);
        try { WL.sem.acquire(); } catch (InterruptedException e) { e.printStackTrace(); }

        // ── game window ───────────────────────────────────────────────────────
        JFrame F = new JFrame("MineSweeper");
        F.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        F.getContentPane().setBackground(HEADER_BG);
        F.setIconImage(appIcon);

        // header
        JPanel header = new JPanel(new BorderLayout(12, 0));
        header.setBackground(HEADER_BG);
        header.setBorder(new EmptyBorder(8, 14, 8, 14));

        mineCountLabel = new JLabel("Mines: " + Mines);
        mineCountLabel.setFont(new Font("Monospaced", Font.BOLD, 14));
        mineCountLabel.setForeground(new Color(255, 100, 100));

        statusLabel = new JLabel("Good luck!", SwingConstants.CENTER);
        statusLabel.setFont(new Font("Monospaced", Font.BOLD, 14));
        statusLabel.setForeground(STATUS_NORMAL);

        JButton restartBtn = makeButton("↺  Restart", new Color(65, 120, 75));
        restartBtn.addActionListener(e -> initGame());

        header.add(mineCountLabel, BorderLayout.WEST);
        header.add(statusLabel,    BorderLayout.CENTER);
        header.add(restartBtn,     BorderLayout.EAST);

        // board
        M = new Main();
        M.addMouseListener(new mouseListener());

        // footer
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 6));
        footer.setBackground(HEADER_BG);
        autoCheckbox = new JCheckBox("Auto Solve");
        autoCheckbox.setBackground(HEADER_BG);
        autoCheckbox.setForeground(Color.LIGHT_GRAY);
        autoCheckbox.setFocusPainted(false);
        autoCheckbox.setFont(new Font("Monospaced", Font.PLAIN, 13));
        JButton helpBtn = makeButton("?  How to Play", new Color(70, 70, 100));
        helpBtn.addActionListener(e -> showInstructions(F));
        footer.add(autoCheckbox);
        footer.add(helpBtn);

        F.add(header, BorderLayout.NORTH);
        F.add(M,      BorderLayout.CENTER);
        F.add(footer, BorderLayout.SOUTH);
        F.setResizable(false);
        F.pack();
        F.setLocationRelativeTo(null);
        F.setVisible(true);

        MS = new MineSweeper(XSquares, YSquares, Mines);
        ai = new AI(XSquares, YSquares);
        updateMineCount();

        javax.swing.Timer aiTimer = new javax.swing.Timer(500, evt -> {
            if (autoCheckbox.isSelected() && !gameOver && !win) {
                ai.step();
                if (!gameOver && clickcount >= totalCells - Mines) {
                    win = true;
                    setStatus("You Win!", STATUS_WIN);
                    MS.clickedMine();
                }
                M.repaint();
            }
        });
        aiTimer.start();

        M.repaint();
    }

    // ── painting ─────────────────────────────────────────────────────────────
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (clickedOn == null) return;

        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setFont(new Font("Monospaced", Font.BOLD, 14));

        for (int i = 0; i < XSquares; i++) {
            for (int j = 0; j < YSquares; j++) {
                int tx    = j * scale + spacing;
                int ty    = i * scale + spacing;
                int tw    = scale - spacing * 2;
                int th    = scale - spacing * 2;
                int state = clickedOn[i][j];

                if (win) {
                    drawFlat(g2, tx, ty, tw, th, TILE_WIN);
                    if (state == 3)
                        drawMine(g2, tx, ty, tw, th, new Color(30, 30, 30));
                } else {
                    switch (state) {
                    case 0:
                        drawRaised(g2, tx, ty, tw, th);
                        break;
                    case 1:
                        drawFlat(g2, tx, ty, tw, th, TILE_REVEAL);
                        if (map[i][j] >= 1) drawNumber(g2, tx, ty, tw, th, map[i][j]);
                        break;
                    case 2:
                        drawRaised(g2, tx, ty, tw, th);
                        drawFlag(g2, tx, ty, tw, th);
                        break;
                    case 3:
                        drawFlat(g2, tx, ty, tw, th, TILE_MINE_BG);
                        drawMine(g2, tx, ty, tw, th, Color.BLACK);
                        break;
                    }
                }
            }
        }
    }

    // ── tile renderers ────────────────────────────────────────────────────────
    private static void drawRaised(Graphics2D g, int x, int y, int w, int h) {
        g.setColor(TILE_HIDDEN);
        g.fillRect(x, y, w, h);
        // highlight (top + left) – 3 px wide for a strong 3-D look
        g.setColor(TILE_LIGHT);
        g.fillRect(x, y, w, 3);
        g.fillRect(x, y, 3, h);
        // shadow (bottom + right)
        g.setColor(TILE_SHADOW);
        g.fillRect(x, y + h - 3, w, 3);
        g.fillRect(x + w - 3, y, 3, h);
    }

    private static void drawFlat(Graphics2D g, int x, int y, int w, int h, Color c) {
        // No border needed – the dark gap between tiles provides separation.
        g.setColor(c);
        g.fillRect(x, y, w, h);
    }

    private static void drawNumber(Graphics2D g, int x, int y, int w, int h, int n) {
        g.setColor(n >= 1 && n <= 8 ? NUM_COLOR[n] : Color.BLACK);
        String s = String.valueOf(n);
        FontMetrics fm = g.getFontMetrics();
        g.drawString(s,
            x + (w - fm.stringWidth(s)) / 2,
            y + (h + fm.getAscent() - fm.getDescent()) / 2);
    }

    private static void drawMine(Graphics2D g, int x, int y, int w, int h, Color c) {
        int cx = x + w / 2, cy = y + h / 2;
        int r  = w / 5;
        int sp = w / 4;
        g.setColor(c);
        g.fillOval(cx - r, cy - r, r * 2, r * 2);
        Stroke saved = g.getStroke();
        g.setStroke(new BasicStroke(1.5f));
        g.drawLine(cx - sp, cy,      cx + sp, cy);
        g.drawLine(cx,      cy - sp, cx,      cy + sp);
        int d = (int)(sp * 0.65);
        g.drawLine(cx - d, cy - d, cx + d, cy + d);
        g.drawLine(cx + d, cy - d, cx - d, cy + d);
        g.setStroke(saved);
    }

    private static void drawFlag(Graphics2D g, int x, int y, int w, int h) {
        int px  = x + w / 2 - 1;
        int top = y + h / 6;
        int bot = y + h * 5 / 6;
        Stroke saved = g.getStroke();
        g.setStroke(new BasicStroke(2f));
        g.setColor(new Color(55, 55, 55));
        g.drawLine(px, top, px, bot);
        int[] xs = {px, px + w / 3, px};
        int[] ys = {top, top + h / 5, top + h * 2 / 5};
        g.setColor(FLAG_COLOR);
        g.fillPolygon(xs, ys, 3);
        g.setStroke(saved);
    }

    // ── ui helpers ────────────────────────────────────────────────────────────
    private static void showInstructions(JFrame parent) {
        JDialog dlg = new JDialog(parent, "How to Play", true);
        dlg.setResizable(false);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(HEADER_BG);
        panel.setBorder(new EmptyBorder(18, 24, 18, 24));

        String[][] sections = {
            { "Goal",
              "Reveal every tile that is NOT a mine.\n" +
              "Avoid clicking on mines!" },
            { "Controls",
              "Left-click  — reveal a tile\n" +
              "Right-click — place / remove a flag" },
            { "Numbers",
              "A revealed number shows how many of\n" +
              "the 8 surrounding tiles contain mines.\n" +
              "Use these clues to deduce safe tiles." },
            { "Flags",
              "Right-click an unrevealed tile to mark\n" +
              "it as a suspected mine (shown in red).\n" +
              "Right-click again to remove the flag." },
            { "Winning",
              "You win when all non-mine tiles are\n" +
              "revealed, OR all mines are correctly\n" +
              "flagged (and no safe tiles flagged)." },
            { "Auto Solve",
              "Tick 'Auto Solve' to let the AI play.\n" +
              "It calculates the tile with the lowest\n" +
              "mine probability and clicks it every\n" +
              "500 ms until the game ends." },
        };

        for (String[] section : sections) {
            JLabel heading = new JLabel(section[0]);
            heading.setFont(new Font("Monospaced", Font.BOLD, 13));
            heading.setForeground(new Color(130, 180, 255));
            heading.setAlignmentX(LEFT_ALIGNMENT);
            panel.add(heading);

            for (String line : section[1].split("\n")) {
                JLabel body = new JLabel("  " + line);
                body.setFont(new Font("Monospaced", Font.PLAIN, 12));
                body.setForeground(new Color(210, 210, 210));
                body.setAlignmentX(LEFT_ALIGNMENT);
                panel.add(body);
            }
            panel.add(Box.createVerticalStrut(10));
        }

        JButton close = makeButton("Close", new Color(80, 80, 80));
        close.setAlignmentX(CENTER_ALIGNMENT);
        close.addActionListener(e -> dlg.dispose());
        panel.add(close);

        dlg.setContentPane(panel);
        dlg.pack();
        dlg.setLocationRelativeTo(parent);
        dlg.setVisible(true);
    }

    private static Image createAppIcon() {
        int sz = 64;
        BufferedImage img = new BufferedImage(sz, sz, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        drawRaised(g, 0, 0, sz, sz);
        drawMine(g, 0, 0, sz, sz, new Color(220, 220, 220));
        g.dispose();
        return img;
    }

    private static JButton makeButton(String text, Color bg) {
        JButton b = new JButton(text);
        b.setBackground(bg);
        b.setForeground(Color.WHITE);
        b.setFont(new Font("Monospaced", Font.BOLD, 12));
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setOpaque(true);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return b;
    }

    private static void styleTextField(JTextField tf) {
        tf.setBackground(new Color(68, 68, 68));
        tf.setForeground(Color.WHITE);
        tf.setCaretColor(Color.WHITE);
        tf.setBorder(BorderFactory.createLineBorder(new Color(90, 90, 90)));
    }

    private static void parseField(JTextField tf, boolean requirePositive, IntConsumer setter) {
        String t = tf.getText().trim();
        if (t.isEmpty()) return;
        try {
            int v = Integer.parseInt(t.length() > 4 ? t.substring(0, 4) : t);
            if (!requirePositive || v > 0) setter.accept(v);
        } catch (NumberFormatException ignored) {}
    }
}
