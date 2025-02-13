package task4;

public class BallThread extends Thread {
    private Ball ball;

    public BallThread(Ball ball) {
        this.ball = ball;
    }

    @Override
    public void run() {
        try {
            while (ball.isActive()) {
                ball.move();
                Thread.sleep(5);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
