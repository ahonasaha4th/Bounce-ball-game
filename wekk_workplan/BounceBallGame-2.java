import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;

// Main Game Class - Week 3: entry point / main game window
public class BounceBallGame extends JFrame {
    public BounceBallGame() {
        setTitle("Bounce Ball - developed by Ahona Saha Aishi (Week 6 Build)");
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
        SwingUtilities.invokeLater(() -> new BounceBallGame());
    }
}

// Game state - Week 6: overall game state management
enum GameState {
    RUNNING, PAUSED, GAME_OVER
}

// Game Panel Class - Week 4-6: window/HUD (Week 4), ball+paddle+wall bounce (Week 5),
// obstacle collision + ball-paddle interaction + state management (Week 6)
class GamePanel extends JPanel implements ActionListener, KeyListener {
    private static final int PANEL_WIDTH = 700;
    private static final int PANEL_HEIGHT = 600;
    private static final int FPS = 60;

    private Timer gameTimer;
    private Ball ball;
    private Paddle paddle;
    private ArrayList<Obstacle> obstacles;

    private int score;
    private GameState state;

    public GamePanel() {
        setPreferredSize(new Dimension(PANEL_WIDTH, PANEL_HEIGHT));
        setBackground(Color.BLACK);
        setFocusable(true);
        addKeyListener(this);

        obstacles = new ArrayList<>();
        initGame();
    }

    private void initGame() {
        ball = new Ball(PANEL_WIDTH / 2 - 8, PANEL_HEIGHT / 2 - 8);
        paddle = new Paddle(PANEL_WIDTH / 2 - 45, PANEL_HEIGHT - 40);
        score = 0;
        state = GameState.RUNNING;

        obstacles.clear();
        int rows = 4;
        int cols = 8;
        int obstacleWidth = 70;
        int obstacleHeight = 25;
        int gap = 8;
        int startX = (PANEL_WIDTH - (cols * (obstacleWidth + gap) - gap)) / 2;
        int startY = 50;

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                int x = startX + c * (obstacleWidth + gap);
                int y = startY + r * (obstacleHeight + gap);
                obstacles.add(new Obstacle(x, y, obstacleWidth, obstacleHeight));
            }
        }
    }

    public void startGame() {
        gameTimer = new Timer(1000 / FPS, this);
        gameTimer.start();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (state == GameState.RUNNING) {
            update();
        }
        repaint();
    }

    private void update() {
        paddle.update(PANEL_WIDTH);
        ball.update();

        // --- Wall collision (Week 5) ---
        if (ball.getX() <= 0 || ball.getX() + ball.getSize() >= PANEL_WIDTH) {
            ball.reverseX();
        }
        if (ball.getY() <= 0) {
            ball.reverseY();
        }

        // --- Ball-Paddle interaction (Week 6) ---
        if (ball.intersects(paddle)) {
            ball.reverseY();
            // Deflect the ball based on where it hit the paddle,
            // so hitting near the edge gives a sharper angle
            double paddleCenter = paddle.getX() + paddle.getWidth() / 2.0;
            double ballCenter = ball.getX() + ball.getSize() / 2.0;
            double offset = (ballCenter - paddleCenter) / (paddle.getWidth() / 2.0);
            ball.setDx(offset * 4.0);
            // Nudge the ball just above the paddle so it doesn't get stuck inside it
            ball.setY(paddle.getY() - ball.getSize());
        }

        // --- Ball-Obstacle collision (Week 6) ---
        for (Obstacle obstacle : obstacles) {
            if (obstacle.isActive() && ball.intersects(obstacle)) {
                obstacle.setActive(false);
                ball.reverseY();
                score += 10;
                break; // only resolve one obstacle hit per frame
            }
        }

        // --- Game state: ball missed the paddle (Week 6) ---
        if (ball.getY() > PANEL_HEIGHT) {
            state = GameState.GAME_OVER;
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        for (Obstacle obstacle : obstacles) {
            if (obstacle.isActive()) {
                obstacle.draw(g2d);
            }
        }

        paddle.draw(g2d);
        ball.draw(g2d);

        drawUI(g2d);

        if (state == GameState.PAUSED) {
            drawPaused(g2d);
        } else if (state == GameState.GAME_OVER) {
            drawGameOver(g2d);
        }
    }

    private void drawUI(Graphics2D g2d) {
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 20));
        g2d.drawString("Score: " + score, 15, 25);
    }

    private void drawPaused(Graphics2D g2d) {
        g2d.setColor(new Color(0, 0, 0, 160));
        g2d.fillRect(0, 0, PANEL_WIDTH, PANEL_HEIGHT);

        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 45));
        g2d.drawString("PAUSED", PANEL_WIDTH / 2 - 110, PANEL_HEIGHT / 2);

        g2d.setFont(new Font("Arial", Font.BOLD, 20));
        g2d.drawString("Press P to Resume", PANEL_WIDTH / 2 - 110, PANEL_HEIGHT / 2 + 40);
    }

    private void drawGameOver(Graphics2D g2d) {
        g2d.setColor(new Color(0, 0, 0, 200));
        g2d.fillRect(0, 0, PANEL_WIDTH, PANEL_HEIGHT);

        g2d.setColor(Color.RED);
        g2d.setFont(new Font("Arial", Font.BOLD, 50));
        g2d.drawString("GAME OVER", PANEL_WIDTH / 2 - 160, PANEL_HEIGHT / 2 - 20);

        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 25));
        g2d.drawString("Final Score: " + score, PANEL_WIDTH / 2 - 90, PANEL_HEIGHT / 2 + 25);
        g2d.drawString("Press R to Restart", PANEL_WIDTH / 2 - 100, PANEL_HEIGHT / 2 + 60);
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (state == GameState.GAME_OVER) {
            if (e.getKeyCode() == KeyEvent.VK_R) {
                initGame();
            }
            return;
        }

        if (e.getKeyCode() == KeyEvent.VK_P) {
            // Toggle pause / resume (Week 6: game state management)
            state = (state == GameState.RUNNING) ? GameState.PAUSED : GameState.RUNNING;
            return;
        }

        if (state == GameState.RUNNING) {
            if (e.getKeyCode() == KeyEvent.VK_LEFT) {
                paddle.setMovingLeft(true);
            } else if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
                paddle.setMovingRight(true);
            }
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_LEFT) {
            paddle.setMovingLeft(false);
        } else if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
            paddle.setMovingRight(false);
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {}
}

// Ball Class - Week 5: movement + wall bounce; Week 6: collision checks vs Paddle/Obstacle
class Ball {
    private double x, y;
    private double dx = 3.5, dy = -3.5;
    private double size = 16;

    public Ball(double x, double y) {
        this.x = x;
        this.y = y;
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

    // AABB collision check against the paddle
    public boolean intersects(Paddle paddle) {
        return x < paddle.getX() + paddle.getWidth() &&
               x + size > paddle.getX() &&
               y < paddle.getY() + paddle.getHeight() &&
               y + size > paddle.getY();
    }

    // AABB collision check against an obstacle
    public boolean intersects(Obstacle obstacle) {
        return x < obstacle.getX() + obstacle.getWidth() &&
               x + size > obstacle.getX() &&
               y < obstacle.getY() + obstacle.getHeight() &&
               y + size > obstacle.getY();
    }

    public double getX() { return x; }
    public double getY() { return y; }
    public double getSize() { return size; }
    public void setY(double y) { this.y = y; }
    public void setDx(double dx) { this.dx = dx; }
}

// Paddle Class - Week 5: player-controlled paddle
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

// Obstacle Class - Week 6: destructible obstacle the ball can collide with
class Obstacle {
    private double x, y;
    private double width, height;
    private boolean active = true;
    private Color color;

    public Obstacle(double x, double y, double width, double height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.color = new Color(200 + (int) (Math.random() * 55), 60, 60);
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