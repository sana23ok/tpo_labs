package task2;

import javax.swing.*;

public class BallThread extends Thread {
    private Ball ball;
    private BallCanvas canvas;
    private JLabel scoreLabel;
    private static int score = 0; // removed balls counter

    public BallThread(Ball ball, BallCanvas canvas, JLabel scoreLabel) {
        this.ball = ball;
        this.canvas = canvas;
        this.scoreLabel = scoreLabel;
    }

    @Override
    public void run() {
        try {
            while (ball.isActive()) {
                ball.move();
                System.out.println("Thread name = "
                        + Thread.currentThread().getName());
                Thread.sleep(5);
            }
            canvas.removeInactiveBalls();
            updateScore();
        } catch (InterruptedException ignored) {}
    }

    private synchronized void updateScore() {
        score++;
        SwingUtilities.invokeLater(() -> scoreLabel.setText("Score: " + score));
    }
}
