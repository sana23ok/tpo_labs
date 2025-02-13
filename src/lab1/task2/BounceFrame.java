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
        this.setTitle("lab1.task2. Billiard Simulation.");
        this.canvas = new BallCanvas();
        scoreLabel = new JLabel("Score: 0");

        System.out.println("In Frame Thread name = "
                + Thread.currentThread().getName());
        Container content = this.getContentPane();
        content.add(this.canvas, BorderLayout.CENTER);
        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(Color.lightGray);
        JButton buttonStart = new JButton("Start");
        JButton buttonStop = new JButton("Stop");

        buttonStart.addActionListener(e -> {
            Ball ball = new Ball(canvas);
            canvas.add(ball);
            BallThread thread = new BallThread(ball, canvas, scoreLabel);
            thread.start();
        });

        buttonStop.addActionListener(e -> System.exit(0));

        buttonPanel.add(buttonStart);
        buttonPanel.add(buttonStop);
        buttonPanel.add(scoreLabel);
        content.add(buttonPanel, BorderLayout.SOUTH);
    }
}