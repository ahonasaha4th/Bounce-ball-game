import javax.swing.*;
import java.awt.*;

public class BounceBallGameWeek4 extends JFrame {

    public BounceBallGameWeek4() {
        setTitle("Bounce Ball Game - Week 4 UI Layout & Window Development");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        GamePanel gamePanel = new GamePanel();
        add(gamePanel);
    }

    private static class GamePanel extends JPanel {
        private int score = 0;
        private String gameStatus = "PRESS SPACE TO START / PAUSE";

        public GamePanel() {
            setBackground(new Color(15, 23, 42));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;

            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g2d.setFont(new Font("Consolas", Font.BOLD, 22));
            g2d.setColor(Color.WHITE);
            g2d.drawString("SCORE: " + score, 20, 35);

            g2d.setFont(new Font("Arial", Font.BOLD, 14));
            g2d.setColor(new Color(250, 204, 21));
            int statusWidth = g2d.getFontMetrics().stringWidth(gameStatus);
            g2d.drawString(gameStatus, (getWidth() - statusWidth) / 2, 35);

            g2d.setColor(new Color(51, 65, 85));
            g2d.setStroke(new BasicStroke(2));
            g2d.drawLine(0, 50, getWidth(), 50);

            int brickRows = 3;
            int brickCols = 8;
            int brickWidth = 85;
            int brickHeight = 20;
            int startX = 40;
            int startY = 80;
            int padding = 8;

            Color[] brickColors = {
                new Color(239, 68, 68),
                new Color(249, 115, 22),
                new Color(34, 197, 94)
            };

            for (int row = 0; row < brickRows; row++) {
                g2d.setColor(brickColors[row]);
                for (int col = 0; col < brickCols; col++) {
                    int x = startX + col * (brickWidth + padding);
                    int y = startY + row * (brickHeight + padding);
                    g2d.fillRoundRect(x, y, brickWidth, brickHeight, 6, 6);
                }
            }

            g2d.setColor(new Color(56, 189, 248));
            g2d.fillOval(390, 470, 20, 20);

            g2d.setColor(new Color(129, 140, 248));
            g2d.fillRoundRect(340, 500, 120, 15, 8, 8);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            BounceBallGameWeek4 game = new BounceBallGameWeek4();
            game.setVisible(true);
        });
    }
}