package task2;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.util.ArrayList;

public class BallCanvas extends JPanel{
    private ArrayList<Ball> balls = new ArrayList<>();

    public void add(Ball b){
        this.balls.add(b);
    }

    // deletion of inactive   balls
    public void removeInactiveBalls() {
        balls.removeIf(ball -> !ball.isActive());
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        drawPockets(g2);
        for (Ball b : balls) {
            b.draw(g2);
        }
    }

    private void drawPockets(Graphics2D g2) {
        int pocketSize = 30;
        int width = getWidth();
        int height = getHeight();
        g2.setColor(Color.LIGHT_GRAY);

        g2.fill(new Ellipse2D.Double(0, 0, pocketSize, pocketSize));
        g2.fill(new Ellipse2D.Double(width - pocketSize, 0, pocketSize, pocketSize));
        g2.fill(new Ellipse2D.Double(0, height - pocketSize, pocketSize, pocketSize));
        g2.fill(new Ellipse2D.Double(width - pocketSize, height - pocketSize, pocketSize, pocketSize));
        g2.fill(new Ellipse2D.Double(width / 2 - pocketSize / 2, 0, pocketSize, pocketSize));
        g2.fill(new Ellipse2D.Double(width / 2 - pocketSize / 2, height - pocketSize, pocketSize, pocketSize));
    }
}

