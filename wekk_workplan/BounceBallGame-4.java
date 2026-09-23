import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;

public class BounceBallGame extends JFrame {
    public BounceBallGame() {
        setTitle("Bounce Ball - Final Release (Weeks 8-10)");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        GamePanel gamePanel = new GamePanel();
        add(gamePanel);

        pack();
        setLocationRelativeTo(null);
        setVisible(true);

        gamePanel.startGame();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                new BounceBallGame();
            } catch (Exception e) {
                System.err.println("Fatal Error initializing application: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }
}

enum GameState {
    RUNNING, PAUSED, LEVEL_TRANSITION, GAME_OVER, WIN
}

class GamePanel extends JPanel implements ActionListener, KeyListener {
    private static final int PANEL_WIDTH = 700;
    private static final int PANEL_HEIGHT = 600;
    private static final int FPS = 60;

    private Timer gameTimer;
    private Ball ball;
    private Paddle paddle;
    private ArrayList<Obstacle> obstacles;

    private int currentLevel;
    private GameState state;

    // Scoring Engine Data: Peak Multi-Hit Single Shot Tracking
    private int highestSingleShotScore;
    private int currentShotHits;
    private int totalClearedObstacles;
    private int screenFlashFrames = 0;

    public GamePanel() {
        setPreferredSize(new Dimension(PANEL_WIDTH, PANEL_HEIGHT));
        setBackground(Color.BLACK);
        setFocusable(true);
        addKeyListener(this);

        obstacles = new ArrayList<>();
        currentLevel = 1;
        highestSingleShotScore = 0;
        currentShotHits = 0;
        totalClearedObstacles = 0;

        initLevel(currentLevel);
    }

    private void initLevel(int level) {
        try {
            state = GameState.RUNNING;
            currentShotHits = 0;

            int startXPos = PANEL_WIDTH / 2 - 8;
            int startYPos = PANEL_HEIGHT / 2 - 8;
            int startPaddleX = PANEL_WIDTH / 2 - 45;
            int startPaddleY = PANEL_HEIGHT - 40;

            if (ball == null) {
                ball = new Ball(startXPos, startYPos);
            } else {
                ball.reset(startXPos, startYPos);
            }

            if (paddle == null) {
                paddle = new Paddle(startPaddleX, startPaddleY);
            } else {
                paddle.reset(startPaddleX, startPaddleY);
            }

            double speedMultiplier = 1.0 + (level - 1) * 0.25;
            ball.setBaseSpeed(3.5 * speedMultiplier);

            // Memory management & array cleanup before building layout
            obstacles.clear();
            int rows = 3 + level;
            int cols = 8;
            int obstacleWidth = 70;
            int obstacleHeight = 22;
            int gap = 8;
            int startX = (PANEL_WIDTH - (cols * (obstacleWidth + gap) - gap)) / 2;
            int startY = 50;

            for (int r = 0; r < rows; r++) {
                for (int c = 0; c < cols; c++) {
                    if (level == 2 && (r + c) % 2 == 0) continue;
                    if (level >= 3 && c % 2 != 0 && r % 2 != 0) continue;
                    int x = startX + c * (obstacleWidth + gap);
                    int y = startY + r * (obstacleHeight + gap);
                    obstacles.add(new Obstacle(x, y, obstacleWidth, obstacleHeight, level));
                }
            }
        } catch (Exception e) {
            System.err.println("Error initializing level " + level + ": " + e.getMessage());
        }
    }

    public void startGame() {
        if (gameTimer == null) {
            gameTimer = new Timer(1000 / FPS, this);
        }
        if (!gameTimer.isRunning()) {
            gameTimer.start();
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        try {
            if (state == GameState.RUNNING) {
                update();
            }
            if (screenFlashFrames > 0) {
                screenFlashFrames--;
            }
            repaint();
        } catch (Exception ex) {
            System.err.println("Runtime exception during game update: " + ex.getMessage());
        }
    }

    private void update() {
        paddle.update(PANEL_WIDTH);
        ball.update();

        // Screen Side Wall Collisions
        if (ball.getX() <= 0) {
            ball.setX(0);
            ball.reverseX();
        } else if (ball.getX() + ball.getSize() >= PANEL_WIDTH) {
            ball.setX(PANEL_WIDTH - ball.getSize());
            ball.reverseX();
        }

        // Top Wall Collision
        if (ball.getY() <= 0) {
            ball.setY(0);
            ball.reverseY();
        }

        // Paddle Collision -> Resets single shot hit counter for new launch
        if (ball.intersects(paddle)) {
            ball.reverseY();
            double paddleCenter = paddle.getX() + paddle.getWidth() / 2.0;
            double ballCenter = ball.getX() + ball.getSize() / 2.0;
            double offset = (ballCenter - paddleCenter) / (paddle.getWidth() / 2.0);
            ball.setDx(offset * 5.0);
            ball.setY(paddle.getY() - ball.getSize());
            
            // New shot sequence starts
            currentShotHits = 0;
        }

        // Obstacle Collision & Multi-Hit Logic
        boolean allCleared = true;
        for (Obstacle obstacle : obstacles) {
            if (obstacle.isActive()) {
                allCleared = false;
                if (ball.intersects(obstacle)) {
                    obstacle.setActive(false);
                    ball.reverseY();
                    
                    // Increment continuous shot hit count
                    currentShotHits++;
                    totalClearedObstacles++;

                    // Update Score ONLY IF current shot breaks more bricks than peak record
                    if (currentShotHits > highestSingleShotScore) {
                        highestSingleShotScore = currentShotHits;
                    }
                    break;
                }
            }
        }

        if (allCleared) {
            if (currentLevel < 3) {
                state = GameState.LEVEL_TRANSITION;
            } else {
                state = GameState.WIN;
            }
            return;
        }

        // Ball out of bounds -> Game Over State Trigger
        if (ball.getY() > PANEL_HEIGHT) {
            screenFlashFrames = 5; // Dynamic visual Red Flash
            state = GameState.GAME_OVER;
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Render Active Obstacles
        for (Obstacle obstacle : obstacles) {
            if (obstacle.isActive()) {
                obstacle.draw(g2d);
            }
        }

        paddle.draw(g2d);
        ball.draw(g2d);

        // Visual Feedback: Red Impact Flash on Ball Loss
        if (screenFlashFrames > 0) {
            g2d.setColor(new Color(255, 0, 0, 100));
            g2d.fillRect(0, 0, PANEL_WIDTH, PANEL_HEIGHT);
        }

        drawUI(g2d);

        // Render Context Overlays
        if (state == GameState.PAUSED) {
            drawPaused(g2d);
        } else if (state == GameState.LEVEL_TRANSITION) {
            drawLevelTransition(g2d);
        } else if (state == GameState.WIN) {
            drawWin(g2d);
        } else if (state == GameState.GAME_OVER) {
            drawGameOver(g2d);
        }
    }

    private void drawUI(Graphics2D g2d) {
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 18));
        g2d.drawString("Level: " + currentLevel, 15, 25);
        g2d.drawString("Score (Max Single Shot): " + highestSingleShotScore, 140, 25);
        g2d.drawString("Current Shot: " + currentShotHits, 430, 25);
        g2d.drawString("Cleared: " + totalClearedObstacles, 580, 25);
    }

    private void drawPaused(Graphics2D g2d) {
        g2d.setColor(new Color(0, 0, 0, 160));
        g2d.fillRect(0, 0, PANEL_WIDTH, PANEL_HEIGHT);

        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 45));
        g2d.drawString("PAUSED", PANEL_WIDTH / 2 - 95, PANEL_HEIGHT / 2 - 20);

        g2d.setFont(new Font("Arial", Font.BOLD, 20));
        g2d.drawString("Press P to Resume", PANEL_WIDTH / 2 - 95, PANEL_HEIGHT / 2 + 30);
    }

    private void drawLevelTransition(Graphics2D g2d) {
        g2d.setColor(new Color(0, 0, 0, 200));
        g2d.fillRect(0, 0, PANEL_WIDTH, PANEL_HEIGHT);

        g2d.setColor(new Color(80, 255, 120));
        g2d.setFont(new Font("Arial", Font.BOLD, 40));
        g2d.drawString("LEVEL " + currentLevel + " CLEARED!", PANEL_WIDTH / 2 - 160, PANEL_HEIGHT / 2 - 40);

        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 20));
        g2d.drawString("Peak Single-Shot Record: " + highestSingleShotScore, PANEL_WIDTH / 2 - 140, PANEL_HEIGHT / 2 + 10);
        g2d.drawString("Press ENTER to Start Level " + (currentLevel + 1), PANEL_WIDTH / 2 - 150, PANEL_HEIGHT / 2 + 50);
    }

    private void drawWin(Graphics2D g2d) {
        g2d.setColor(new Color(0, 0, 0, 220));
        g2d.fillRect(0, 0, PANEL_WIDTH, PANEL_HEIGHT);

        g2d.setColor(Color.YELLOW);
        g2d.setFont(new Font("Arial", Font.BOLD, 45));
        g2d.drawString("YOU WIN!", PANEL_WIDTH / 2 - 110, PANEL_HEIGHT / 2 - 60);

        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 20));
        g2d.drawString("Highest Single-Shot Break Score: " + highestSingleShotScore, PANEL_WIDTH / 2 - 165, PANEL_HEIGHT / 2 - 10);
        g2d.drawString("Total Bricks Cleared: " + totalClearedObstacles, PANEL_WIDTH / 2 - 110, PANEL_HEIGHT / 2 + 30);

        g2d.setFont(new Font("Arial", Font.BOLD, 18));
        g2d.drawString("Press R to Restart Level | Press M for Main Menu", PANEL_WIDTH / 2 - 200, PANEL_HEIGHT / 2 + 90);
    }

    private void drawGameOver(Graphics2D g2d) {
        g2d.setColor(new Color(0, 0, 0, 220));
        g2d.fillRect(0, 0, PANEL_WIDTH, PANEL_HEIGHT);

        g2d.setColor(new Color(255, 70, 70));
        g2d.setFont(new Font("Arial", Font.BOLD, 45));
        g2d.drawString("GAME OVER", PANEL_WIDTH / 2 - 145, PANEL_HEIGHT / 2 - 60);

        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 20));
        g2d.drawString("Highest Single-Shot Score: " + highestSingleShotScore, PANEL_WIDTH / 2 - 140, PANEL_HEIGHT / 2 - 10);
        g2d.drawString("Total Cleared Bricks: " + totalClearedObstacles, PANEL_WIDTH / 2 - 110, PANEL_HEIGHT / 2 + 25);
        g2d.drawString("Level Reached: " + currentLevel, PANEL_WIDTH / 2 - 85, PANEL_HEIGHT / 2 + 60);

        g2d.setFont(new Font("Arial", Font.BOLD, 18));
        g2d.drawString("Press R to Retry Level | Press M to Main Menu", PANEL_WIDTH / 2 - 190, PANEL_HEIGHT / 2 + 105);
    }

    private void resetGameToFirstLevel() {
        currentLevel = 1;
        highestSingleShotScore = 0;
        totalClearedObstacles = 0;
        initLevel(currentLevel);
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();

        if (state == GameState.GAME_OVER || state == GameState.WIN) {
            if (key == KeyEvent.VK_R) {
                initLevel(currentLevel);
            } else if (key == KeyEvent.VK_M) {
                resetGameToFirstLevel();
            }
            return;
        }

        if (state == GameState.LEVEL_TRANSITION) {
            if (key == KeyEvent.VK_ENTER) {
                currentLevel++;
                initLevel(currentLevel);
            }
            return;
        }

        if (key == KeyEvent.VK_P) {
            if (state == GameState.RUNNING) {
                state = GameState.PAUSED;
            } else if (state == GameState.PAUSED) {
                state = GameState.RUNNING;
            }
            return;
        }

        if (state == GameState.RUNNING) {
            if (key == KeyEvent.VK_LEFT) {
                paddle.setMovingLeft(true);
            } else if (key == KeyEvent.VK_RIGHT) {
                paddle.setMovingRight(true);
            }
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int key = e.getKeyCode();
        if (key == KeyEvent.VK_LEFT) {
            paddle.setMovingLeft(false);
        } else if (key == KeyEvent.VK_RIGHT) {
            paddle.setMovingRight(false);
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {}
}

class Ball {
    private double x, y;
    private double dx = 3.5, dy = -3.5;
    private double size = 16;
    private double baseSpeed = 3.5;

    public Ball(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public void reset(double x, double y) {
        this.x = x;
        this.y = y;
        this.dx = baseSpeed;
        this.dy = -baseSpeed;
    }

    public void setBaseSpeed(double speed) {
        this.baseSpeed = speed;
        this.dx = (dx < 0) ? -speed : speed;
        this.dy = -speed;
    }

    public void update() {
        x += dx;
        y += dy;
    }

    public void reverseX() { dx = -dx; }
    public void reverseY() { dy = -dy; }

    public void draw(Graphics2D g2d) {
        g2d.setColor(Color.WHITE);
        g2d.fillOval((int) x, (int) y, (int) size, (int) size);
    }

    public boolean intersects(Paddle paddle) {
        return x < paddle.getX() + paddle.getWidth() &&
               x + size > paddle.getX() &&
               y < paddle.getY() + paddle.getHeight() &&
               y + size > paddle.getY();
    }

    public boolean intersects(Obstacle obstacle) {
        return x < obstacle.getX() + obstacle.getWidth() &&
               x + size > obstacle.getX() &&
               y < obstacle.getY() + obstacle.getHeight() &&
               y + size > obstacle.getY();
    }

    public double getX() { return x; }
    public double getY() { return y; }
    public double getSize() { return size; }
    public void setX(double x) { this.x = x; }
    public void setY(double y) { this.y = y; }
    public void setDx(double dx) { this.dx = dx; }
}

class Paddle {
    private double x, y;
    private double width = 90, height = 15;
    private double speed = 7;
    private boolean movingLeft = false;
    private boolean movingRight = false;

    public Paddle(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public void reset(double x, double y) {
        this.x = x;
        this.y = y;
        movingLeft = false;
        movingRight = false;
    }

    public void update(int panelWidth) {
        if (movingLeft) x -= speed;
        if (movingRight) x += speed;

        if (x < 0) x = 0;
        if (x + width > panelWidth) x = panelWidth - width;
    }

    public void draw(Graphics2D g2d) {
        g2d.setColor(new Color(80, 160, 255));
        g2d.fillRoundRect((int) x, (int) y, (int) width, (int) height, 8, 8);
    }

    public void setMovingLeft(boolean movingLeft) { this.movingLeft = movingLeft; }
    public void setMovingRight(boolean movingRight) { this.movingRight = movingRight; }

    public double getX() { return x; }
    public double getY() { return y; }
    public double getWidth() { return width; }
    public double getHeight() { return height; }
}

class Obstacle {
    private double x, y;
    private double width, height;
    private boolean active = true;
    private Color color;

    public Obstacle(double x, double y, double width, double height, int level) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;

        if (level == 1) {
            this.color = new Color(200, 60, 60);
        } else if (level == 2) {
            this.color = new Color(220, 140, 40);
        } else {
            this.color = new Color(180, 50, 220);
        }
    }

    public void draw(Graphics2D g2d) {
        g2d.setColor(color);
        g2d.fillRoundRect((int) x, (int) y, (int) width, (int) height, 6, 6);
        g2d.setColor(color.darker());
        g2d.setStroke(new BasicStroke(2));
        g2d.drawRoundRect((int) x, (int) y, (int) width, (int) height, 6, 6);
    }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public double getX() { return x; }
    public double getY() { return y; }
    public double getWidth() { return width; }
    public double getHeight() { return height; }
}