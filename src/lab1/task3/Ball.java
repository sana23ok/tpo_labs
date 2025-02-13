package lab1.task3;

import java.awt.*;
import java.awt.geom.Ellipse2D;

class Ball {
    private Component canvas;
    private static final int SIZE = 20;
    private int x = 0, y= 0, dx = 2, dy = 2;
    private Color color;


    public Ball(Component c, Color color) {
        this.canvas = c;
        this.color = color;
        this.x = canvas.getWidth() / 2;
        this.y = canvas.getHeight() / 2;
    }

    public void draw (Graphics2D g2){
        g2.setColor(color);
        g2.fill(new Ellipse2D.Double(x,y,SIZE,SIZE));
    }

    public void move() {
        x += dx;
        y += dy;
        if (x < 0 || x + SIZE >= canvas.getWidth()) dx = -dx;
        if (y < 0 || y + SIZE >= canvas.getHeight()) dy = -dy;
        canvas.repaint();
    }

    public Color getColor() {
        return color;
    }
}


