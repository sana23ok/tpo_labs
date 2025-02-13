package lab1.task3;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class BounceFrame extends JFrame {
    private BallCanvas canvas;

    public BounceFrame() {
        setSize(600, 400);
        setTitle("lab1.task3. Billiard Simulation.");
        canvas = new BallCanvas();

        Container content = getContentPane();
        content.add(canvas, BorderLayout.CENTER);
        JPanel buttonPanel = new JPanel();

        JButton buttonAdd10 = new JButton("Add 10");
        JButton buttonAdd100 = new JButton("Add 100");
        JButton buttonAdd1000 = new JButton("Add 1000");
        JButton buttonStop = new JButton("Stop");

        // action for 10
        buttonAdd10.addActionListener((ActionEvent e) -> addBalls(10));

        // action for 100
        buttonAdd100.addActionListener((ActionEvent e) -> addBalls(100));

        // action for 1000
        buttonAdd1000.addActionListener((ActionEvent e) -> addBalls(1000));

        // action for stop
        buttonStop.addActionListener(e -> System.exit(0));

        buttonPanel.add(buttonAdd10);
        buttonPanel.add(buttonAdd100);
        buttonPanel.add(buttonAdd1000);
        buttonPanel.add(buttonStop);
        content.add(buttonPanel, BorderLayout.SOUTH);
    }

    private void addBalls(int numBlueBalls) {
        Ball redBall = new Ball(canvas, Color.RED);
        canvas.add(redBall);
        BallThread redThread = new BallThread(redBall, canvas);
        redThread.start();

        for (int i = 0; i < numBlueBalls; i++) {
            Ball blueBall = new Ball(canvas, Color.BLUE);
            canvas.add(blueBall);
            BallThread blueThread = new BallThread(blueBall, canvas);
            blueThread.start();
        }
    }
}
