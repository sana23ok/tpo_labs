package task3;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

class BallCanvas extends JPanel {
    private ArrayList<Ball> balls = new ArrayList<>();

    public void add(Ball b) {
        balls.add(b);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        for (Ball b : balls) {
            b.draw(g2);
        }
    }
}


