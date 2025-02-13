package task4;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.util.Random;

class Ball {
    private Component canvas;
    private static final int SIZE = 20;
    private int x = 0, y = 0, dx = 2, dy = 2;
    private boolean active = true;
    private Color color;

    public Ball(Component c, Color color) {
        this.canvas = c;
        this.color = color;
        this.x = new Random().nextInt(canvas.getWidth() - SIZE);
        this.y = new Random().nextInt(canvas.getHeight() - SIZE);
    }

    public void draw(Graphics2D g2) {
        if (active) {
            g2.setColor(color);
            g2.fill(new Ellipse2D.Double(x, y, SIZE, SIZE));
        }
    }

    public void move() {
        if (!active) return;

        x += dx;
        y += dy;

        if (x < 0 || x + SIZE >= canvas.getWidth()) dx = -dx;
        if (y < 0 || y + SIZE >= canvas.getHeight()) dy = -dy;

        if (isInPocket()) {
            active = false;
        }

        canvas.repaint();
    }

    private boolean isInPocket() {
        int pocketSize = 30;
        int width = canvas.getWidth();
        int height = canvas.getHeight();

        return (x < pocketSize && y < pocketSize) ||
                (x > width - pocketSize && y < pocketSize) ||
                (x < pocketSize && y > height - pocketSize) ||
                (x > width - pocketSize && y > height - pocketSize) ||
                (x > width / 2 - pocketSize / 2 && x < width / 2 + pocketSize / 2 && y < pocketSize) ||
                (x > width / 2 - pocketSize / 2 && x < width / 2 + pocketSize / 2 && y > height - pocketSize);
    }

    public boolean isActive() {
        return active;
    }
}

