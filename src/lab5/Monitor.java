package lab5;


public class Monitor extends Thread {
    private final Manager manager;

    public Monitor(Manager manager) {
        this.manager = manager;
    }

    @Override
    public void run() {
        while (true) {
            synchronized (manager) {
                if (manager.isSimulationDone() && manager.getQueueSize() == 0) {
                    break;
                }

                System.out.println("[Monitor] Queue Size: " + manager.getQueueSize()
                        + " | Processed: " + manager.processedTasks
                        + " | Rejected: " + manager.rejectedTasks);
            }

            try {
                Thread.sleep(500); // оновлення кожні пів секунди
            } catch (InterruptedException ignored) {}
        }

        // Фінальний стан після завершення
        synchronized (manager) {
            System.out.println("[Monitor] Simulation Finished");
            System.out.println("Final Stats -> Queue Size: " + manager.getQueueSize()
                    + " | Processed: " + manager.processedTasks
                    + " | Rejected: " + manager.rejectedTasks);
        }
    }
}

