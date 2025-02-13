package lab1.task4;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class BounceFrame extends JFrame {
    private BallCanvas canvas;
    public static final int WIDTH = 450;
    public static final int HEIGHT = 350;

    public BounceFrame() {
        this.setSize(WIDTH, HEIGHT);
        this.setTitle("Bounce Program");
        this.canvas = new BallCanvas();
        Container content = this.getContentPane();
        content.add(this.canvas, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(Color.lightGray);
        JButton buttonStart = new JButton("Start");
        JButton buttonStop = new JButton("Stop");

        buttonStart.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Ball blueBall = new Ball(canvas, Color.BLUE);
                Ball redBall = new Ball(canvas, Color.RED);

                canvas.add(blueBall);
                canvas.add(redBall);

                BallThread blueThread = new BallThread(blueBall);
                BallThread redThread = new BallThread(redBall);

                blueThread.start();

                // Use join() to synchronize
                new Thread(() -> {
                    try {
                        blueThread.join();
                        redThread.start();
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    }
                }).start();
            }
        });

        buttonStop.addActionListener(e -> System.exit(0));

        buttonPanel.add(buttonStart);
        buttonPanel.add(buttonStop);
        content.add(buttonPanel, BorderLayout.SOUTH);
    }
}

