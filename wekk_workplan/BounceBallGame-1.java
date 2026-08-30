import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class BounceBallGame extends JFrame {

    public BounceBallGame() {
        setTitle("Bounce Ball Game - Week 5: Ball Movement & Paddle");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        GamePanel gamePanel = new GamePanel();
        add(gamePanel);

        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new BounceBallGame());
    }
}

class GamePanel extends JPanel implements ActionListener, KeyListener {
    private static final int WIDTH = 800;
    private static final int HEIGHT = 600;

    private Timer timer;

    // Ball Movement Variables
    private int ballX = 390;
    private int ballY = 200;
    private int ballSize = 20;
    private int ballDX = 3;
    private int ballDY = 3;

    // Paddle Movement Variables
    private int paddleWidth = 120;
    private int paddleHeight = 15;
    private int paddleX = (WIDTH - paddleWidth) / 2;
    private int paddleY = HEIGHT - 60;
    private int paddleSpeed = 7;
    private boolean moveLeft = false;
    private boolean moveRight = false;

    public GamePanel() {
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setBackground(new Color(15, 23, 42));
        setFocusable(true);
        addKeyListener(this);

        // Game Loop Timer (~60 FPS)
        timer = new Timer(16, this);
        timer.start();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Status Header
        g2d.setColor(new Color(30, 41, 59));
        g2d.fillRect(0, 0, WIDTH, 45);
        g2d.setColor(new Color(56, 189, 248));
        g2d.setFont(new Font("SansSerif", Font.BOLD, 16));
        g2d.drawString("Week 5 Demo: Ball Movement & Paddle Control (Use Left/Right Arrow Keys)", 120, 28);

        // Draw Ball
        g2d.setColor(new Color(56, 189, 248));
        g2d.fillOval(ballX, ballY, ballSize, ballSize);

        // Draw Paddle
        g2d.setColor(new Color(244, 63, 94));
        g2d.fillRoundRect(paddleX, paddleY, paddleWidth, paddleHeight, 10, 10);
        g2d.setColor(Color.WHITE);
        g2d.drawRoundRect(paddleX, paddleY, paddleWidth, paddleHeight, 10, 10);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // Continuous Ball Movement
        ballX += ballDX;
        ballY += ballDY;

        // Smooth Paddle Movement with Boundary Checks
        if (moveLeft && paddleX > 0) {
            paddleX -= paddleSpeed;
        }
        if (moveRight && paddleX < WIDTH - paddleWidth) {
            paddleX += paddleSpeed;
        }

        repaint();
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();
        if (key == KeyEvent.VK_LEFT || key == KeyEvent.VK_A) {
            moveLeft = true;
        }
        if (key == KeyEvent.VK_RIGHT || key == KeyEvent.VK_D) {
            moveRight = true;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int key = e.getKeyCode();
        if (key == KeyEvent.VK_LEFT || key == KeyEvent.VK_A) {
            moveLeft = false;
        }
        if (key == KeyEvent.VK_RIGHT || key == KeyEvent.VK_D) {
            moveRight = false;
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {}
}