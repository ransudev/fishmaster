package rohan.fishmaster.util;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class TickScheduler {
    private static final Queue<Runnable> tasks = new ConcurrentLinkedQueue<>();

    public static void schedule(Runnable task) {
        tasks.add(task);
    }

    public static void tick() {
        Runnable task;
        while ((task = tasks.poll()) != null) {
            try {
                task.run();
            } catch (Exception e) {
                System.err.println("[FishMaster] Error executing scheduled task: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
}

