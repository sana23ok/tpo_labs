package lab1.task1;

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
        this.setTitle("task1. Bounce program");
        this.canvas = new BallCanvas();
        System.out.println("In Frame Thread name = " + Thread.currentThread().getName());
        Container content = this.getContentPane();
        content.add(this.canvas, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(Color.lightGray);

        JButton buttonAdd1 = new JButton("Add 1");
        JButton buttonAdd10 = new JButton("Add 10");
        JButton buttonAdd100 = new JButton("Add 100");
        JButton buttonStop = new JButton("Stop");

        buttonAdd1.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addBalls(1);
            }
        });

        buttonAdd10.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addBalls(10);
            }
        });

        buttonAdd100.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addBalls(100);
            }
        });

        buttonStop.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });

        buttonPanel.add(buttonAdd1);
        buttonPanel.add(buttonAdd10);
        buttonPanel.add(buttonAdd100);
        buttonPanel.add(buttonStop);

        content.add(buttonPanel, BorderLayout.SOUTH);
    }

    private void addBalls(int count) {
        for (int i = 0; i < count; i++) {
            Ball b = new Ball(canvas);
            canvas.add(b);

            BallThread thread = new BallThread(b);
            thread.start();
            System.out.println("Thread name = " + thread.getName());
        }
    }
}
