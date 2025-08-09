package rohan.fishmaster.core;

import net.minecraft.client.MinecraftClient;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Supplier;

/**
 * Responsive scheduler that manages task execution with priorities and adaptive timing
 */
public class ResponsiveScheduler {
    private static ResponsiveScheduler instance;

    // Priority levels for different tasks
    public enum Priority {
        CRITICAL(0),    // Key inputs, emergency stops
        HIGH(1),        // Fishing logic, bite detection
        MEDIUM(2),      // Sea creature killer, failsafes
        LOW(3),         // Webhooks, statistics
        BACKGROUND(4);  // Cleanup, maintenance

        private final int level;
        Priority(int level) { this.level = level; }
        public int getLevel() { return level; }
    }

    // Task wrapper
    public static class ScheduledTask {
        private final Runnable task;
        private final Priority priority;
        private final long scheduledTime;
        private final int intervalTicks;
        private final String name;
        private long lastExecution;
        private boolean isRepeating;

        public ScheduledTask(String name, Runnable task, Priority priority, int intervalTicks) {
            this.name = name;
            this.task = task;
            this.priority = priority;
            this.scheduledTime = System.currentTimeMillis();
            this.intervalTicks = intervalTicks;
            this.lastExecution = 0;
            this.isRepeating = intervalTicks > 0;
        }

        public boolean shouldExecute(long currentTime, int currentTick) {
            if (isRepeating) {
                return (currentTick - lastExecution) >= intervalTicks;
            }
            return currentTime >= scheduledTime && lastExecution == 0;
        }

        public void execute(int currentTick) {
            try {
                task.run();
                lastExecution = currentTick;
            } catch (Exception e) {
                System.err.println("[ResponsiveScheduler] Error executing task " + name + ": " + e.getMessage());
            }
        }

        public Priority getPriority() { return priority; }
        public String getName() { return name; }
        public boolean isRepeating() { return isRepeating; }
    }

    private final Map<Priority, Queue<ScheduledTask>> taskQueues;
    private final Queue<ScheduledTask> oneTimeTasks;
    private int currentTick = 0;
    private long lastFrameTime = System.nanoTime();
    private double avgFrameTime = 16.67; // Target 60 FPS
    private int maxTasksPerTick = 5;

    private ResponsiveScheduler() {
        taskQueues = new EnumMap<>(Priority.class);
        for (Priority priority : Priority.values()) {
            taskQueues.put(priority, new ConcurrentLinkedQueue<>());
        }
        oneTimeTasks = new ConcurrentLinkedQueue<>();
    }

    public static ResponsiveScheduler getInstance() {
        if (instance == null) {
            instance = new ResponsiveScheduler();
        }
        return instance;
    }

    /**
     * Schedule a repeating task
     */
    public void scheduleRepeating(String name, Runnable task, Priority priority, int intervalTicks) {
        ScheduledTask scheduledTask = new ScheduledTask(name, task, priority, intervalTicks);
        taskQueues.get(priority).offer(scheduledTask);
    }

    /**
     * Schedule a one-time task
     */
    public void scheduleOnce(String name, Runnable task, Priority priority) {
        ScheduledTask scheduledTask = new ScheduledTask(name, task, priority, 0);
        oneTimeTasks.offer(scheduledTask);
    }

    /**
     * Schedule a task asynchronously (for heavy operations)
     */
    public CompletableFuture<Void> scheduleAsync(String name, Runnable task, Priority priority) {
        return CompletableFuture.runAsync(() -> {
            try {
                task.run();
            } catch (Exception e) {
                System.err.println("[ResponsiveScheduler] Error in async task " + name + ": " + e.getMessage());
            }
        });
    }

    /**
     * Main tick method - called from client tick handler
     */
    public void tick() {
        currentTick++;
        updateFrameTimeTracking();

        // Update performance monitoring
        PerformanceMonitor.getInstance().updateFps();

        adaptiveExecute();
    }

    private void updateFrameTimeTracking() {
        long currentTime = System.nanoTime();
        double frameTime = (currentTime - lastFrameTime) / 1_000_000.0; // Convert to milliseconds
        avgFrameTime = (avgFrameTime * 0.9) + (frameTime * 0.1); // Exponential moving average
        lastFrameTime = currentTime;

        // Adaptive task limit based on frame time
        if (avgFrameTime > 25) { // If frame time > 25ms (less than 40 FPS)
            maxTasksPerTick = Math.max(1, maxTasksPerTick - 1);
        } else if (avgFrameTime < 12) { // If frame time < 12ms (more than 80 FPS)
            maxTasksPerTick = Math.min(10, maxTasksPerTick + 1);
        }
    }

    private void adaptiveExecute() {
        int executedTasks = 0;
        long startTime = System.nanoTime();
        final long maxExecutionTime = 2_000_000; // 2ms max per tick

        // Execute one-time tasks first
        while (!oneTimeTasks.isEmpty() && executedTasks < maxTasksPerTick) {
            ScheduledTask task = oneTimeTasks.poll();
            if (task != null) {
                task.execute(currentTick);
                executedTasks++;

                if (System.nanoTime() - startTime > maxExecutionTime) {
                    break;
                }
            }
        }

        // Execute scheduled tasks by priority
        for (Priority priority : Priority.values()) {
            if (executedTasks >= maxTasksPerTick ||
                System.nanoTime() - startTime > maxExecutionTime) {
                break;
            }

            Queue<ScheduledTask> queue = taskQueues.get(priority);
            List<ScheduledTask> toRequeue = new ArrayList<>();

            while (!queue.isEmpty() && executedTasks < maxTasksPerTick) {
                ScheduledTask task = queue.poll();
                if (task == null) break;

                if (task.shouldExecute(System.currentTimeMillis(), currentTick)) {
                    task.execute(currentTick);
                    executedTasks++;

                    if (System.nanoTime() - startTime > maxExecutionTime) {
                        if (task.isRepeating()) {
                            toRequeue.add(task);
                        }
                        break;
                    }
                }

                if (task.isRepeating()) {
                    toRequeue.add(task);
                }
            }

            // Re-queue repeating tasks
            toRequeue.forEach(queue::offer);
        }
    }

    /**
     * Get performance statistics
     */
    public Map<String, Object> getStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("currentTick", currentTick);
        stats.put("avgFrameTime", String.format("%.2f ms", avgFrameTime));
        stats.put("maxTasksPerTick", maxTasksPerTick);
        stats.put("queueSizes", getQueueSizes());
        return stats;
    }

    private Map<Priority, Integer> getQueueSizes() {
        Map<Priority, Integer> sizes = new EnumMap<>(Priority.class);
        for (Priority priority : Priority.values()) {
            sizes.put(priority, taskQueues.get(priority).size());
        }
        return sizes;
    }

    /**
     * Clear all tasks (for cleanup)
     */
    public void clear() {
        taskQueues.values().forEach(Queue::clear);
        oneTimeTasks.clear();
    }
}
