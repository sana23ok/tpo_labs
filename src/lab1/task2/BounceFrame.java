package lab1.task2;

import javax.swing.*;
import java.awt.*;

public class BounceFrame extends JFrame {
    private BallCanvas canvas;
    private JLabel scoreLabel;
    public static final int WIDTH = 450;
    public static final int HEIGHT = 350;

    public BounceFrame() {
        this.setSize(WIDTH, HEIGHT);
        this.setTitle("task2. Billiard Simulation.");
        this.canvas = new BallCanvas();
        scoreLabel = new JLabel("Score: 0");

        System.out.println("In Frame Thread name = " + Thread.currentThread().getName());
        Container content = this.getContentPane();
        content.add(this.canvas, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(Color.lightGray);

        JButton buttonAdd1 = new JButton("Add 1");
        JButton buttonAdd10 = new JButton("Add 10");
        JButton buttonAdd100 = new JButton("Add 100");
        JButton buttonStop = new JButton("Stop");

        buttonAdd1.addActionListener(e -> addBalls(1));
        buttonAdd10.addActionListener(e -> addBalls(10));
        buttonAdd100.addActionListener(e -> addBalls(100));

        buttonStop.addActionListener(e -> System.exit(0));

        buttonPanel.add(buttonAdd1);
        buttonPanel.add(buttonAdd10);
        buttonPanel.add(buttonAdd100);
        buttonPanel.add(buttonStop);
        buttonPanel.add(scoreLabel);

        content.add(buttonPanel, BorderLayout.SOUTH);
    }

    private void addBalls(int count) {
        for (int i = 0; i < count; i++) {
            Ball ball = new Ball(canvas);
            canvas.add(ball);

            BallThread thread = new BallThread(ball, canvas, scoreLabel);
            thread.start();

            System.out.println("Thread name = " + thread.getName());
        }
    }
}
