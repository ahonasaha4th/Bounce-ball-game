import javax.swing.*;
import java.awt.*;

public class BounceBallGame extends JFrame {

    public BounceBallGame() {
        setTitle("Bounce Ball Game - Week 3 Environment Test");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        GamePanel gamePanel = new GamePanel();
        add(gamePanel);
    }

    private static class GamePanel extends JPanel {
        public GamePanel() {
            setBackground(Color.BLACK);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            g.setColor(Color.GREEN);
            g.setFont(new Font("Consolas", Font.BOLD, 22));
            g.drawString("Bounce Ball Game Environment Setup", 180, 200);

            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.PLAIN, 16));
            g.drawString("Status: Environment Test Successfully Passed!", 220, 260);
            g.drawString("JDK, Swing, AWT & Window Initialization Ready.", 210, 300);

            g.setColor(Color.ORANGE);
            g.fillOval(385, 350, 30, 30);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            BounceBallGame game = new BounceBallGame();
            game.setVisible(true);
        });
    }
}