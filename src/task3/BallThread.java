package task3;

import java.awt.*;

class BallThread extends Thread {
    private Ball ball;
    private BallCanvas canvas;

    public BallThread(Ball ball, BallCanvas canvas) {
        this.ball = ball;
        this.canvas = canvas;

        // Set priority
        if (ball.getColor() == Color.RED) {
            setPriority(Thread.MAX_PRIORITY);
        } else {
            setPriority(Thread.MIN_PRIORITY);
        }
    }

    @Override
    public void run() {
        System.out.println("Thread: " + Thread.currentThread().getName() + ", Priority: " + getPriority());
        while (true) {
            ball.move();
            try {
                Thread.sleep(5);
            } catch (InterruptedException ignored) {}
        }
    }
}
