import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.Random;
import java.io.File;
import javax.sound.sampled.*;
import java.util.prefs.Preferences;

public class GamePanel extends JPanel implements ActionListener {

    // ================== CONSTANTS ==================
    static final int WIDTH = 500;
    static final int HEIGHT = 500;
    static final int SCORE_HEIGHT = 50;
    static final int GAME_HEIGHT = HEIGHT - SCORE_HEIGHT;
    static final int UNIT_SIZE = 20;
    static final int NUMBER_OF_UNITS = (WIDTH * HEIGHT) / (UNIT_SIZE * UNIT_SIZE);

    // ================== ANIMATION & UI ==================
    private double titleScale = 1.0;
    private boolean scalingUp = true;
    private int hueValue = 0;
    private Image menuBackgroundGif;
    private float snakePulse = 1.0f;
    private boolean snakePulseUp = true;

    // ================== AUDIO ==================
    private Clip menuMusicClip;
    private boolean soundEnabled = true;




    // ================== LOADING SYSTEM ==================
    private static boolean firstTimeLaunch = true;
    private boolean isInitialLoading = false;
    private long loadingStartTime;
    private final int TOTAL_LOADING_TIME = 3000;

    // ================== BIG APPLE SYSTEM ==================
    private Image bigAppleImage;
    private int bigAppleX, bigAppleY;
    private boolean bigAppleActive = false;
    private int bigAppleTimer = 0;
    private int spawnCooldown = 0;

    // ================== SNAKE ==================
    final int x[] = new int[NUMBER_OF_UNITS];
    final int y[] = new int[NUMBER_OF_UNITS];
    int length = 5;
    char direction = 'R';
    boolean running = false;
    boolean paused = false;

    // ================== FOOD & SCORE ==================
    int foodX, foodY;
    int foodEaten;
    private int highScore = 0;
    private Preferences prefs;

    // ================== WALLS ==================
    Rectangle[] wallBlocks;
    Rectangle[] level2ExtraWalls;
    Rectangle[] level3BorderWalls;
    Rectangle[] level4Walls;
    Rectangle level5CenterBox;
    Rectangle[] level6Walls;

    // ================== PERMANENT PROGRESS ==================
    boolean level1Unlocked = true;
    boolean level2Unlocked = false;
    boolean level3Unlocked = false;
    boolean level4Unlocked = false;
    boolean level5Unlocked = false;
    boolean level6Unlocked = false;
    private boolean masterUnlocked = false;

    // ================== CURRENT STATE ==================
    boolean showMenu = false;
    int currentActiveLevel = 0;
    boolean levelLost = false;
    boolean levelWonThisSession = false;

    // ================== ENGINE ==================
    Timer timer;
    Random random;
    Image appleImage;
    JButton restartButton, menuButton, nextLevelButton;
    JButton level1Btn, level2Btn, level3Btn, level4Btn, level5Btn, level6Btn, justPlayBtn;
    JButton unlockToggleButton, soundToggleButton;


    /// ///////////////////////////0001
  /*  private void playClickSound() {
        if (soundEnabled) {
            playSound("click.wav");
        }
    }
    /// ////////////////////////////////////////0001
*/


    public GamePanel() {
        random = new Random();
        appleImage = new ImageIcon("src/apple.png").getImage();
        bigAppleImage = new ImageIcon("src/big_apple.png").getImage();
        menuBackgroundGif = new ImageIcon("src/menu_bg.gif").getImage();







        // High Score Initialization
        prefs = Preferences.userNodeForPackage(GamePanel.class);
        highScore = prefs.getInt("highScore", 0);

        this.setPreferredSize(new Dimension(WIDTH, HEIGHT));
        this.setBackground(Color.WHITE);
        this.setFocusable(true);
        this.addKeyListener(new MyKeyAdapter());

        createExtendedWalls();
        createLevel3Walls();
        createLevel4Walls();
        createLevel5Walls();
        createLevel6Walls();

        timer = new Timer(110, this);
        timer.start();

        if (firstTimeLaunch) {
            startInitialLoading();
        } else {
            showStartMenu();
        }
    }

    private void startInitialLoading() {
        isInitialLoading = true;
        loadingStartTime = System.currentTimeMillis();
    }

    private void playMenuMusic() {
        if (!soundEnabled) return;
        try {
            if (menuMusicClip == null || !menuMusicClip.isRunning()) {
                File f = new File("src/menu_music.wav");
                AudioInputStream audioIn = AudioSystem.getAudioInputStream(f);
                menuMusicClip = AudioSystem.getClip();
                menuMusicClip.open(audioIn);
                menuMusicClip.loop(Clip.LOOP_CONTINUOUSLY);
                menuMusicClip.start();
            }
        } catch (Exception e) {
            System.err.println("Menu music file not found or error.");
        }
    }
    private void stopMenuMusic() {
        if (menuMusicClip != null && menuMusicClip.isRunning()) {
            menuMusicClip.stop();
            menuMusicClip.close();
            menuMusicClip = null;
        }
    }

    private void playSound(String soundFile) {
        if (!soundEnabled) return;
        try {
            File f = new File("src/" + soundFile);
            AudioInputStream audioIn = AudioSystem.getAudioInputStream(f);
            Clip clip = AudioSystem.getClip();
            clip.open(audioIn);
            clip.start();
        } catch (Exception e) {
            System.err.println("Sound file not found: " + soundFile);
        }
    }

    private void createExtendedWalls() {
        int cornerLength = 4;
        wallBlocks = new Rectangle[cornerLength * 4 * 2];
        int index = 0;
        for (int i = 0; i < cornerLength; i++) {
            wallBlocks[index++] = new Rectangle(i * UNIT_SIZE, SCORE_HEIGHT, UNIT_SIZE, UNIT_SIZE);
            wallBlocks[index++] = new Rectangle(0, SCORE_HEIGHT + i * UNIT_SIZE, UNIT_SIZE, UNIT_SIZE);
            wallBlocks[index++] = new Rectangle(i * UNIT_SIZE, HEIGHT - UNIT_SIZE, UNIT_SIZE, UNIT_SIZE);
            wallBlocks[index++] = new Rectangle(0, HEIGHT - UNIT_SIZE - i * UNIT_SIZE, UNIT_SIZE, UNIT_SIZE);
            wallBlocks[index++] = new Rectangle(WIDTH - UNIT_SIZE - i * UNIT_SIZE, SCORE_HEIGHT, UNIT_SIZE, UNIT_SIZE);
            wallBlocks[index++] = new Rectangle(WIDTH - UNIT_SIZE, SCORE_HEIGHT + i * UNIT_SIZE, UNIT_SIZE, UNIT_SIZE);
            wallBlocks[index++] = new Rectangle(WIDTH - UNIT_SIZE - i * UNIT_SIZE, HEIGHT - UNIT_SIZE, UNIT_SIZE, UNIT_SIZE);
            wallBlocks[index++] = new Rectangle(WIDTH - UNIT_SIZE, HEIGHT - UNIT_SIZE - i * UNIT_SIZE, UNIT_SIZE, UNIT_SIZE);
        }
    }
    private void createLevel2Walls() {
        int wallLength = GAME_HEIGHT / 2;
        int startY = SCORE_HEIGHT + (GAME_HEIGHT - wallLength) / 2;
        level2ExtraWalls = new Rectangle[2];
        int centerX = WIDTH / 2;
        level2ExtraWalls[0] = new Rectangle(centerX - 60, startY, UNIT_SIZE, wallLength);
        level2ExtraWalls[1] = new Rectangle(centerX + 60, startY, UNIT_SIZE, wallLength);
    }

    private void createLevel3Walls() {
        int cols = WIDTH / UNIT_SIZE;
        int rows = GAME_HEIGHT / UNIT_SIZE;
        level3BorderWalls = new Rectangle[(cols * 2) + (rows * 2)];
        int index = 0;
        for (int i = 0; i < cols; i++) {
            level3BorderWalls[index++] = new Rectangle(i * UNIT_SIZE, SCORE_HEIGHT, UNIT_SIZE, UNIT_SIZE);
            level3BorderWalls[index++] = new Rectangle(i * UNIT_SIZE, HEIGHT - UNIT_SIZE, UNIT_SIZE, UNIT_SIZE);
        }
        for (int i = 0; i < rows; i++) {
            level3BorderWalls[index++] = new Rectangle(0, SCORE_HEIGHT + i * UNIT_SIZE, UNIT_SIZE, UNIT_SIZE);
            level3BorderWalls[index++] = new Rectangle(WIDTH - UNIT_SIZE, SCORE_HEIGHT + i * UNIT_SIZE, UNIT_SIZE, UNIT_SIZE);
        }
    }

    private void createLevel4Walls() {
        int wallLength = GAME_HEIGHT / 2;
        int startY = SCORE_HEIGHT + (GAME_HEIGHT - wallLength) / 2;
        int centerX = WIDTH / 2;
        level4Walls = new Rectangle[2];
        level4Walls[0] = new Rectangle(centerX - 60, startY, UNIT_SIZE, wallLength);
        level4Walls[1] = new Rectangle(centerX + 60, startY, UNIT_SIZE, wallLength);
    }

    private void createLevel5Walls() {
        int boxW = 200;
        int boxH = 180;
        level5CenterBox = new Rectangle((WIDTH - boxW) / 2, SCORE_HEIGHT + (GAME_HEIGHT - boxH) / 2, boxW, boxH);
    }

    private void createLevel6Walls() {
        int centerX = WIDTH / 2;
        int centerY = SCORE_HEIGHT + (GAME_HEIGHT / 2);
        int h = 240;
        int w = 160;
        level6Walls = new Rectangle[6];
        level6Walls[0] = new Rectangle(centerX - 120, centerY - (h/2), UNIT_SIZE, h);
        level6Walls[1] = new Rectangle(centerX - 120, centerY - (h/2), w/2, UNIT_SIZE);
        level6Walls[2] = new Rectangle(centerX - 120, centerY + (h/2) - UNIT_SIZE, w/2, UNIT_SIZE);
        level6Walls[3] = new Rectangle(centerX + 120 - UNIT_SIZE, centerY - (h/2), UNIT_SIZE, h);
        level6Walls[4] = new Rectangle(centerX + 120 - (w/2), centerY - (h/2), w/2, UNIT_SIZE);
        level6Walls[5] = new Rectangle(centerX + 120 - (w/2), centerY + (h/2) - UNIT_SIZE, w/2, UNIT_SIZE);
    }

    public void showStartMenu() {
        this.removeAll();
        this.setLayout(null);
        showMenu = true;
        currentActiveLevel = -1;
        resetButtonStates();
        playMenuMusic();

        // Unlock Button
        unlockToggleButton = new JButton(masterUnlocked ? "U" : "L");
        unlockToggleButton.setBounds(10, 10, 40, 40);
        unlockToggleButton.setFocusable(false);
        unlockToggleButton.setMargin(new java.awt.Insets(0,0,0,0));
        unlockToggleButton.setFont(new Font("Arial", Font.BOLD, 12));
        updateUnlockButtonStyle();
        /// //////////////////////////////////////2.1
        unlockToggleButton.addActionListener(e -> {
            playClickSound(); // Added
            toggleMasterUnlock();
        });

        ///////////////////////////////2.1
        this.add(unlockToggleButton);

        // Sound Button
        soundToggleButton = new JButton(soundEnabled ? "S" : "M");
        soundToggleButton.setBounds(10, 55, 40, 40);
        soundToggleButton.setFocusable(false);
        soundToggleButton.setMargin(new java.awt.Insets(0,0,0,0));
        soundToggleButton.setFont(new Font("Arial", Font.BOLD, 12));
        updateSoundButtonStyle();
        /// ////////////////////////////////////////////2.2
        soundToggleButton.addActionListener(e -> {
            playClickSound(); // Added
            toggleSound();
        });
        /// ////////////////////////////////////////2.2
        this.add(soundToggleButton);

        level1Btn = new JButton("Level 1");
        level2Btn = new JButton("Level 2");
        level3Btn = new JButton("Level 3");
        level4Btn = new JButton("Level 4");
        level5Btn = new JButton("Level 5");
        level6Btn = new JButton("Level 6");
        justPlayBtn = new JButton("Just Play");


        JButton[] btns = {level1Btn, level2Btn, level3Btn, level4Btn, level5Btn, level6Btn, justPlayBtn};
        int yOffset = 180;
        for (JButton b : btns) {
            b.setFocusable(false);
            b.setBounds(WIDTH / 2 - 100, yOffset, 200, 30);
            b.setBackground(new Color(255, 255, 255, 255));
            b.setFont(new Font("SansSerif", Font.BOLD, 14));
            b.addActionListener(e -> playClickSound()); // Added universal sound ///2.3
            this.add(b);
            yOffset += 35;
        }

        level1Btn.addActionListener(e -> startLevel(1));
        level2Btn.addActionListener(e -> startLevel(2));
        level3Btn.addActionListener(e -> startLevel(3));
        level4Btn.addActionListener(e -> startLevel(4));
        level5Btn.addActionListener(e -> startLevel(5));
        level6Btn.addActionListener(e -> startLevel(6));
        justPlayBtn.addActionListener(e -> startLevel(0));

        updateLevelButtons();
        if(!timer.isRunning()) timer.start();
        repaint();
    }

    private void toggleMasterUnlock() {
        masterUnlocked = !masterUnlocked;
        unlockToggleButton.setText(masterUnlocked ? "U" : "L");
        updateUnlockButtonStyle();
        updateLevelButtons();
    }

    private void toggleSound() {
        soundEnabled = !soundEnabled;
        if (soundEnabled) {
            playMenuMusic();
        } else {
            stopMenuMusic();
        }
        updateSoundButtonStyle();
    }

    private void updateUnlockButtonStyle() {
        if (masterUnlocked) {
            unlockToggleButton.setBackground(Color.GREEN);
            unlockToggleButton.setForeground(Color.BLACK);
        } else {
            unlockToggleButton.setBackground(Color.RED);
            unlockToggleButton.setForeground(Color.WHITE);
        }
    }

    private void updateSoundButtonStyle() {
        if (soundEnabled) {
            soundToggleButton.setBackground(Color.BLUE);
            soundToggleButton.setForeground(Color.WHITE);
            soundToggleButton.setText("S");
        } else {
            soundToggleButton.setBackground(Color.RED);
            soundToggleButton.setForeground(Color.WHITE);
            soundToggleButton.setText("M");
        }
    }
    private void updateLevelButtons() {
        level2Btn.setEnabled(masterUnlocked || level2Unlocked);
        level3Btn.setEnabled(masterUnlocked || level3Unlocked);
        level4Btn.setEnabled(masterUnlocked || level4Unlocked);
        level5Btn.setEnabled(masterUnlocked || level5Unlocked);
        level6Btn.setEnabled(masterUnlocked || level6Unlocked);
    }
    private void startLevel(int lvl) {
        stopMenuMusic();
        showMenu = false;
        levelLost = false;
        levelWonThisSession = false;
        currentActiveLevel = lvl;
        bigAppleActive = false;
        spawnCooldown = 0;
        if (lvl == 2) createLevel2Walls();
        playGame();
    }

    public void playGame() {
        this.removeAll();
        resetButtonStates();
        this.requestFocusInWindow();
        length = 5;
        foodEaten = 0;
        direction = 'R';
        running = true;
        paused = false;
        initializeSnakePosition();
        addFood();
        if(!timer.isRunning()) timer.start();
        repaint();
    }

    public void initializeSnakePosition() {
        int startX = (WIDTH / 2 / UNIT_SIZE) * UNIT_SIZE;
        int startY = ((GAME_HEIGHT / 2) / UNIT_SIZE) * UNIT_SIZE;
        if (currentActiveLevel == 2 || currentActiveLevel == 4 || currentActiveLevel == 5 || currentActiveLevel == 6) {
            startY = HEIGHT - 100;
        }
        for (int i = 0; i < length; i++) {
            x[i] = startX - (i * UNIT_SIZE);
            y[i] = startY;
        }
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (isInitialLoading) {
            drawInitialLoading(g);
        } else if (showMenu) {
            drawAnimatedMenu(g);
        } else {
            drawGame(g);
        }
    }

    private void drawInitialLoading(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, WIDTH, HEIGHT);

        // Branding
        g.setColor(new Color(164, 200, 213));
        g.setFont(new Font("Arial", Font.BOLD, 50));
        FontMetrics fmMain = g.getFontMetrics();
        g.drawString("VIPPER", (WIDTH - fmMain.stringWidth("VIPPER")) / 2, 120);

/////////////////////////////////////////////////////////////
        // Set the color and font once for both lines
        g.setColor(new Color(0, 181, 250));
        g.setFont(new Font("Arial", Font.ITALIC, 14));
        FontMetrics fmSub = g.getFontMetrics();

// Define the two lines of text
        String line1 = "A handy snake Game         ";
        String line2 = "      Powered By Java";

// Draw Line 1 (Centered at Y = 150)
        int x1 = (WIDTH - fmSub.stringWidth(line1)) / 2;
        g.drawString(line1, x1, 150);

// Draw Line 2 (Centered at Y = 175 - moved down by 25 pixels)
        int x2 = (WIDTH - fmSub.stringWidth(line2)) / 2;
        g.drawString(line2, x2, 176);
        /// ///////////////////////////////////////////////////////
        long elapsed = System.currentTimeMillis() - loadingStartTime;
        double progress = Math.min(1.0, (double) elapsed / TOTAL_LOADING_TIME);
        int percentage = (int) (progress * 100);

        // Progress elements moved down
        int barWidth = 300;
        int barHeight = 15;
        int barX = (WIDTH - barWidth) / 2;
        int barY = 300;

        g.setColor(new Color(164, 200, 213));
        g.setFont(new Font("Arial", Font.PLAIN, 25));
        g.drawString("Loading...", barX, barY - 25);

        g.setColor(new Color(60, 60, 60));
        g.fillRect(barX, barY, barWidth, barHeight);
        g.setColor(new Color(164, 200, 213));
        g.fillRect(barX, barY, (int)(barWidth * progress), barHeight);

        g.setFont(new Font("Arial", Font.BOLD, 15));
        String percentText = percentage + "%";
        FontMetrics fmPerc = g.getFontMetrics();
        g.drawString(percentText, (WIDTH - fmPerc.stringWidth(percentText)) / 2, barY + 35);
    }

    private void drawAnimatedMenu(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        if (menuBackgroundGif != null) {
            g.drawImage(menuBackgroundGif, 0, 0, WIDTH, HEIGHT, this);
        } else {
            g.setColor(new Color(255, 244, 244));
            g.fillRect(0,0,WIDTH,HEIGHT);
        }
        float fontSize = (float) (45 * titleScale);
        g.setFont(new Font("Arial", Font.BOLD, (int)fontSize));
        g.setColor(Color.getHSBColor(hueValue / 360f, 0.8f, 0.8f));
        FontMetrics metrics = g.getFontMetrics(g.getFont());
        String title = "VIPPER";
        g.drawString(title, (WIDTH - metrics.stringWidth(title)) / 2, 80);
        g.setFont(new Font("Arial", Font.BOLD|Font.ITALIC, 15));
        g.setColor(new Color(246, 223, 223));
        g.drawString("Choose your challenge ", WIDTH/2 - 75, 130);
        g.setFont(new Font("Monospaced", Font.ITALIC |Font.BOLD, 15));
        g.setColor(new Color(0, 255, 101));
        g.drawString("Created by A.A.P", WIDTH - 150, HEIGHT - 20);
    }

    public void drawGame(Graphics g) {
        g.setColor(new Color(205, 169, 169));
        g.fillRect(0, 0, WIDTH, SCORE_HEIGHT);
        g.setFont(new Font("Arial", Font.BOLD, 18));
        g.setColor(Color.blue);
        g.drawString("Score: " + foodEaten, 20, 32);

        if (currentActiveLevel == 0) {
            g.setColor(new Color(0, 100, 0));
            g.drawString("High Score: " + highScore, WIDTH - 150, 32);
        }

        for (int r = 0; r < GAME_HEIGHT / UNIT_SIZE; r++) {
            for (int c = 0; c < WIDTH / UNIT_SIZE; c++) {
                g.setColor((r + c) % 2 == 0 ? new Color(224, 244, 238) : Color.WHITE);
                g.fillRect(c * UNIT_SIZE, r * UNIT_SIZE + SCORE_HEIGHT, UNIT_SIZE, UNIT_SIZE);
            }
        }

        if (running) {
            g.drawImage(appleImage, foodX, foodY + SCORE_HEIGHT, UNIT_SIZE, UNIT_SIZE, this);
            if (bigAppleActive) {
                g.drawImage(bigAppleImage, bigAppleX, bigAppleY + SCORE_HEIGHT, UNIT_SIZE * 2, UNIT_SIZE * 2, this);
                g.setColor(Color.GRAY);
                g.fillRect(150, HEIGHT - 15, 200, 10);
                g.setColor(Color.ORANGE);
                int barWidth = (int)((bigAppleTimer / 36.0) * 200);
                g.fillRect(150, HEIGHT - 15, barWidth, 10);
            }
            drawWalls(g);
            drawSnake(g);
        } else {
            handleGameOverUI(g);
        }
    }

    private void drawWalls(Graphics g) {
        if (currentActiveLevel >= 1) {
            g.setColor(Color.RED);
            for (Rectangle wall : wallBlocks) g.fillRect(wall.x, wall.y, wall.width, wall.height);
        }
        if (currentActiveLevel == 2 && level2ExtraWalls != null) {
            g.setColor(Color.MAGENTA);
            for (Rectangle wall : level2ExtraWalls) g.fillRect(wall.x, wall.y, wall.width, wall.height);
        }
        if (currentActiveLevel >= 3) {
            g.setColor(Color.RED);
            for (Rectangle wall : level3BorderWalls) g.fillRect(wall.x, wall.y, wall.width, wall.height);
        }
        if (currentActiveLevel == 4) {
            g.setColor(Color.GREEN);
            for (Rectangle wall : level4Walls) g.fillRect(wall.x, wall.y, wall.width, wall.height);
        }
        if (currentActiveLevel == 5) {
            g.setColor(Color.GREEN);
            g.fillRect(level5CenterBox.x, level5CenterBox.y, level5CenterBox.width, level5CenterBox.height);
        }
        if (currentActiveLevel == 6) {
            g.setColor(Color.RED);
            for (Rectangle wall : level6Walls) g.fillRect(wall.x, wall.y, wall.width, wall.height);
        }
    }

    private void drawSnake(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        for (int i = 0; i < length; i++) {
            if (i == 0) {
                g2d.setColor(new Color(0, 50, 200));
                g2d.fillRoundRect(x[i] + 1, y[i] + SCORE_HEIGHT + 1, UNIT_SIZE - 2, UNIT_SIZE - 2, 8, 8);
                g2d.setColor(new Color(248, 214, 214));
                g2d.fillOval(x[i] + 4, y[i] + SCORE_HEIGHT + 4, 4, 4);
                g2d.fillOval(x[i] + 12, y[i] + SCORE_HEIGHT + 4, 4, 4);
            } else {
                int pulseOffset = (int)(2 * snakePulse);
                g2d.setColor(new Color(50, 180, 50));
                g2d.fillRoundRect(x[i] + pulseOffset, y[i] + SCORE_HEIGHT + pulseOffset,
                        UNIT_SIZE - (pulseOffset * 2), UNIT_SIZE - (pulseOffset * 2), 5, 5);
                g2d.setColor(new Color(40, 150, 40));
                g2d.drawRoundRect(x[i] + pulseOffset, y[i] + SCORE_HEIGHT + pulseOffset,
                        UNIT_SIZE - (pulseOffset * 2), UNIT_SIZE - (pulseOffset * 2), 5, 5);
            }
        }
    }
    private void handleGameOverUI(Graphics g) {
        g.setFont(new Font("Arial", Font.BOLD, 45));
        FontMetrics fm = g.getFontMetrics();
        if (levelLost) {
            g.setColor(Color.RED);
            String msg = (currentActiveLevel == 0) ? "GAME OVER" : "YOU LOSE!";
            g.drawString(msg, (WIDTH - fm.stringWidth(msg)) / 2, 160);

            if (currentActiveLevel == 0) {
                g.setFont(new Font("Arial", Font.BOLD, 20));
                g.setColor(Color.BLACK);
                String scoreMsg = "Score: " + foodEaten;
                String hiScoreMsg = "Best: " + highScore;
                g.drawString(scoreMsg, (WIDTH - g.getFontMetrics().stringWidth(scoreMsg)) / 2, 200);
                g.drawString(hiScoreMsg, (WIDTH - g.getFontMetrics().stringWidth(hiScoreMsg)) / 2, 230);
            }
            createEndButtons(false);
        } else if (levelWonThisSession) {
            String msg = (currentActiveLevel == 6) ? "CHAMPION!" : "LEVEL CLEAR!";
            g.setColor(new Color(0, 150, 0));
            g.drawString(msg, (WIDTH - fm.stringWidth(msg)) / 2, 200);
            createEndButtons(true);
        }
    }
    public void move() {
        for (int i = length; i > 0; i--) {
            x[i] = x[i - 1];
            y[i] = y[i - 1];
        }
        switch (direction) {
            case 'U' -> y[0] -= UNIT_SIZE;
            case 'D' -> y[0] += UNIT_SIZE;
            case 'L' -> x[0] -= UNIT_SIZE;
            case 'R' -> x[0] += UNIT_SIZE;
        }
        if (currentActiveLevel < 3) {
            if (x[0] < 0) x[0] = WIDTH - UNIT_SIZE;
            if (x[0] >= WIDTH) x[0] = 0;
            if (y[0] < 0) y[0] = GAME_HEIGHT - UNIT_SIZE;
            if (y[0] >= GAME_HEIGHT) y[0] = 0;
        }
    }

    public void checkHit() {
        for (int i = length - 1; i > 0; i--) {
            if (x[0] == x[i] && y[0] == y[i]) { stopGameAsLoss(); return; }
        }
        Rectangle head = new Rectangle(x[0], y[0] + SCORE_HEIGHT, UNIT_SIZE, UNIT_SIZE);
        if (currentActiveLevel >= 1) {
            for (Rectangle w : wallBlocks) if (head.intersects(w)) { stopGameAsLoss(); return; }
        }
        if (currentActiveLevel == 2) {
            for (Rectangle w : level2ExtraWalls) if (head.intersects(w)) { stopGameAsLoss(); return; }
        }
        if (currentActiveLevel >= 3) {
            for (Rectangle w : level3BorderWalls) if (head.intersects(w)) { stopGameAsLoss(); return; }
            if (x[0] < 0 || x[0] >= WIDTH || y[0] < 0 || y[0] >= GAME_HEIGHT) { stopGameAsLoss(); return; }
        }
        if (currentActiveLevel == 4) {
            for (Rectangle w : level4Walls) if (head.intersects(w)) { stopGameAsLoss(); return; }
        }
        if (currentActiveLevel == 5) {
            if (head.intersects(level5CenterBox)) { stopGameAsLoss(); return; }
        }
        if (currentActiveLevel == 6) {
            for (Rectangle w : level6Walls) if (head.intersects(w)) { stopGameAsLoss(); return; }
        }
    }

    private void stopGameAsLoss() {
        running = false;
        levelLost = true;
        bigAppleActive = false;
        if (currentActiveLevel == 0 && foodEaten > highScore) {
            highScore = foodEaten;
            prefs.putInt("highScore", highScore);
        }
    }
    ///////////////////////////////////////////////////14

    @Override
    public void actionPerformed(ActionEvent e) {
        if (scalingUp) {
            titleScale += 0.005;
            if (titleScale >= 1.05) scalingUp = false;
        } else {
            titleScale -= 0.005;
            if (titleScale <= 0.95) scalingUp = true;
        }

        if (snakePulseUp) {
            snakePulse += 0.05f;
            if (snakePulse >= 1.5f) snakePulseUp = false;
        } else {
            snakePulse -= 0.05f;
            if (snakePulse <= 0.8f) snakePulseUp = true;
        }

        hueValue = (hueValue + 2) % 360;

        if (isInitialLoading) {
            long elapsed = System.currentTimeMillis() - loadingStartTime;
            if (elapsed >= TOTAL_LOADING_TIME) {
                isInitialLoading = false;
                firstTimeLaunch = false;
                showStartMenu();
            }
            repaint();
            return;
        }

        if (running && !paused) {
            move();
            if (x[0] == foodX && y[0] == foodY) {
                length++;
                foodEaten++;
                playSound("eat.wav");
                addFood();
            }
            if (bigAppleActive) {
                Rectangle head = new Rectangle(x[0], y[0], UNIT_SIZE, UNIT_SIZE);
                Rectangle bAppleRect = new Rectangle(bigAppleX, bigAppleY, UNIT_SIZE * 2, UNIT_SIZE * 2);
                if (head.intersects(bAppleRect)) {
                    foodEaten += 4;
                    bigAppleActive = false;
                    playSound("big_eat.wav");
                }
                bigAppleTimer--;
                if (bigAppleTimer <= 0) bigAppleActive = false;
            }
            if (currentActiveLevel == 0 && !bigAppleActive) {
                spawnCooldown++;
                if (spawnCooldown >= 90) {
                    spawnBigApple();
                    spawnCooldown = 0;
                }
            }
            checkHit();

            // Check for level completion targets
            if (running && currentActiveLevel > 0) {
                int target = 0;
                switch (currentActiveLevel) {
                    case 1 -> target = 10;     //level 1
                    case 2 -> target = 12;     //level 2
                    case 3 -> target = 14;     //level 3
                    case 4 -> target = 15;     //level 4
                    case 5 -> target = 16;     //level 5
                    case 6 -> target = 17;     //level 6
                }
                if (foodEaten >= target) {
                    running = false;
                    levelWonThisSession = true;
                    unlockNextLevel();
                }
            }
        }
        repaint();
    }
    private void unlockNextLevel() {
        if(currentActiveLevel == 1) level2Unlocked = true;
        else if(currentActiveLevel == 2) level3Unlocked = true;
        else if(currentActiveLevel == 3) level4Unlocked = true;
        else if(currentActiveLevel == 4) level5Unlocked = true;
        else if(currentActiveLevel == 5) level6Unlocked = true;
    }
    public void addFood() {
        foodX = random.nextInt(WIDTH / UNIT_SIZE) * UNIT_SIZE;
        foodY = random.nextInt(GAME_HEIGHT / UNIT_SIZE) * UNIT_SIZE;
        while (isInsideAnyWall(foodX, foodY) || isOnSnake(foodX, foodY)) {
            foodX = random.nextInt(WIDTH / UNIT_SIZE) * UNIT_SIZE;
            foodY = random.nextInt(GAME_HEIGHT / UNIT_SIZE) * UNIT_SIZE;
        }
    }
    private void spawnBigApple() {
        bigAppleX = random.nextInt((WIDTH - UNIT_SIZE*2) / UNIT_SIZE) * UNIT_SIZE;
        bigAppleY = random.nextInt((GAME_HEIGHT - UNIT_SIZE*2) / UNIT_SIZE) * UNIT_SIZE;
        bigAppleActive = true;
        bigAppleTimer = 36;
    }
    private boolean isInsideAnyWall(int fx, int fy) {
        Rectangle r = new Rectangle(fx, fy + SCORE_HEIGHT, UNIT_SIZE, UNIT_SIZE);
        if (currentActiveLevel >= 1) for (Rectangle w : wallBlocks) if (r.intersects(w)) return true;
        if (currentActiveLevel == 2) for (Rectangle w : level2ExtraWalls) if (r.intersects(w)) return true;
        if (currentActiveLevel >= 3) for (Rectangle w : level3BorderWalls) if (r.intersects(w)) return true;
        if (currentActiveLevel == 4) for (Rectangle w : level4Walls) if (r.intersects(w)) return true;
        if (currentActiveLevel == 5) if (r.intersects(level5CenterBox)) return true;
        if (currentActiveLevel == 6) for (Rectangle w : level6Walls) if (r.intersects(w)) return true;
        return false;
    }
    private boolean isOnSnake(int fx, int fy) {
        for (int i = 0; i < length; i++) if (x[i] == fx && y[i] == fy) return true;
        return false;
    }
    /// /////////////////////0000again01
   /* private void createEndButtons(boolean won) {
        if (menuButton == null) {
            menuButton = new JButton("Menu");
            menuButton.setFocusable(false);
            menuButton.setBounds(180, 260, 140, 40);
/// /////////////////////////////////////////////////////////////01
            menuButton.addActionListener(e -> {
                playClickSound(); // Added
                goToMenu();
            });
            /// //////////////////////////////////////////////01
            this.add(menuButton);
        }
        if (!won && restartButton == null) {
            restartButton = new JButton("Try Again");
            restartButton.setFocusable(false);
            restartButton.setBounds(180, 310, 140, 40);
            /// /////////////////////////////////////////////02
            restartButton.addActionListener(e -> {
                playClickSound(); // Added
                startLevel(currentActiveLevel);
            });
            /// ///////////////////////////////////////////02
            this.add(restartButton);
        }
        if (won && nextLevelButton == null && currentActiveLevel < 6 && currentActiveLevel > 0) {
            nextLevelButton = new JButton("Next Level");
            nextLevelButton.setFocusable(false);
            nextLevelButton.setBounds(180, 310, 140, 40);
           /// //////////////////////////////////////////////03
            nextLevelButton.addActionListener(e -> {
                playClickSound(); // Added
                startLevel(currentActiveLevel + 1);
            });
            ///  //////////////////////////////////////03
        }
    }
    */
/// //////////////////////////////////////////////////////////000again02
    private void createEndButtons(boolean won) {
        if (menuButton == null) {
            menuButton = new JButton("Menu");
            menuButton.setFocusable(false);
            menuButton.setBounds(180, 260, 140, 40);
            menuButton.addActionListener(e -> {
                playClickSound();
                goToMenu();
            });
            this.add(menuButton);
        }

        if (!won && restartButton == null) {
            restartButton = new JButton("Try Again");
            restartButton.setFocusable(false);
            restartButton.setBounds(180, 310, 140, 40);
            restartButton.addActionListener(e -> {
                playClickSound();
                startLevel(currentActiveLevel);
            });
            this.add(restartButton);
        }

        // The fix is the 'this.add' line inside this block
        if (won && nextLevelButton == null && currentActiveLevel < 6 && currentActiveLevel > 0) {
            nextLevelButton = new JButton("Next Level");
            nextLevelButton.setFocusable(false);
            nextLevelButton.setBounds(180, 310, 140, 40);
            nextLevelButton.addActionListener(e -> {
                playClickSound();
                startLevel(currentActiveLevel + 1);
            });
            this.add(nextLevelButton); // <--- FIXED: This line was missing!
        }
    }

    // Helper method to handle button sounds
    private void playClickSound() {
        if (soundEnabled) {
            playSound("click.wav");
        }
    }

/// ////////////////////////////00000again03

    private void resetButtonStates() {
        if (restartButton != null) this.remove(restartButton);
        if (menuButton != null) this.remove(menuButton);
        if (nextLevelButton != null) this.remove(nextLevelButton);
        if (unlockToggleButton != null) this.remove(unlockToggleButton);
        if (soundToggleButton != null) this.remove(soundToggleButton);
        restartButton = menuButton = nextLevelButton = unlockToggleButton = soundToggleButton = null;
    }
    public void goToMenu() {
        running = false;
        bigAppleActive = false;
        resetButtonStates();
        showStartMenu();
    }
    private class MyKeyAdapter extends KeyAdapter {
        public void keyPressed(KeyEvent e) {
            switch (e.getKeyCode()) {
                case KeyEvent.VK_LEFT -> { if (direction != 'R') direction = 'L'; }
                case KeyEvent.VK_RIGHT -> { if (direction != 'L') direction = 'R'; }
                case KeyEvent.VK_UP -> { if (direction != 'D') direction = 'U'; }
                case KeyEvent.VK_DOWN -> { if (direction != 'U') direction = 'D'; }
                case KeyEvent.VK_P -> paused = !paused;
            }
        }
    }
}