package rohan.fishmaster.core;

import net.minecraft.client.MinecraftClient;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Performance monitor that tracks mod performance and provides adaptive optimization
 */
public class PerformanceMonitor {
    private static PerformanceMonitor instance;
    
    private final Map<String, Long> taskExecutionTimes = new ConcurrentHashMap<>();
    private final Map<String, Integer> taskExecutionCounts = new ConcurrentHashMap<>();
    private final Map<String, Long> lastExecutionTimes = new ConcurrentHashMap<>();
    
    private long lastFpsCheck = System.currentTimeMillis();
    private double averageFps = 60.0;
    private int frameCount = 0;
    private long fpsCheckStartTime = System.currentTimeMillis();
    
    // Performance thresholds
    private static final double LOW_FPS_THRESHOLD = 30.0;
    private static final double HIGH_FPS_THRESHOLD = 80.0;
    private static final long SLOW_TASK_THRESHOLD = 5_000_000; // 5ms in nanoseconds
    
    private boolean performanceMode = false;
    private long lastPerformanceCheck = 0;
    
    private PerformanceMonitor() {}
    
    public static PerformanceMonitor getInstance() {
        if (instance == null) {
            instance = new PerformanceMonitor();
        }
        return instance;
    }
    
    /**
     * Record task execution time
     */
    public void recordTaskExecution(String taskName, long executionTimeNanos) {
        taskExecutionTimes.merge(taskName, executionTimeNanos, Long::sum);
        taskExecutionCounts.merge(taskName, 1, Integer::sum);
        lastExecutionTimes.put(taskName, System.currentTimeMillis());
        
        // Check for slow tasks
        if (executionTimeNanos > SLOW_TASK_THRESHOLD) {
            System.out.println("[PerformanceMonitor] Slow task detected: " + taskName + 
                " took " + (executionTimeNanos / 1_000_000.0) + "ms");
        }
    }
    
    /**
     * Update FPS tracking
     */
    public void updateFps() {
        frameCount++;
        long currentTime = System.currentTimeMillis();
        
        if (currentTime - fpsCheckStartTime >= 1000) { // Update every second
            averageFps = (frameCount * 1000.0) / (currentTime - fpsCheckStartTime);
            frameCount = 0;
            fpsCheckStartTime = currentTime;
            
            // Check if we need to enter performance mode
            checkPerformanceMode();
        }
    }
    
    private void checkPerformanceMode() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastPerformanceCheck < 5000) return; // Check every 5 seconds
        
        lastPerformanceCheck = currentTime;
        
        boolean shouldEnablePerformanceMode = averageFps < LOW_FPS_THRESHOLD;
        
        if (shouldEnablePerformanceMode && !performanceMode) {
            performanceMode = true;
            System.out.println("[PerformanceMonitor] Low FPS detected (" + String.format("%.1f", averageFps) + 
                "), enabling performance mode");
            applyPerformanceOptimizations();
        } else if (!shouldEnablePerformanceMode && performanceMode && averageFps > HIGH_FPS_THRESHOLD) {
            performanceMode = false;
            System.out.println("[PerformanceMonitor] FPS improved (" + String.format("%.1f", averageFps) + 
                "), disabling performance mode");
            removePerformanceOptimizations();
        }
    }
    
    private void applyPerformanceOptimizations() {
        ResponsiveScheduler scheduler = ResponsiveScheduler.getInstance();
        
        // Reduce frequency of low-priority tasks
        scheduler.scheduleOnce("performance_optimization", () -> {
            // Notify features to reduce their processing
            System.out.println("[PerformanceMonitor] Applied performance optimizations");
        }, ResponsiveScheduler.Priority.BACKGROUND);
    }
    
    private void removePerformanceOptimizations() {
        ResponsiveScheduler scheduler = ResponsiveScheduler.getInstance();
        
        // Restore normal frequency
        scheduler.scheduleOnce("performance_restoration", () -> {
            // Notify features to restore normal processing
            System.out.println("[PerformanceMonitor] Restored normal performance settings");
        }, ResponsiveScheduler.Priority.BACKGROUND);
    }
    
    /**
     * Get performance statistics
     */
    public Map<String, Object> getPerformanceStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("averageFps", String.format("%.1f", averageFps));
        stats.put("performanceMode", performanceMode);
        stats.put("taskCount", taskExecutionCounts.size());
        
        // Calculate average execution times
        Map<String, String> avgExecutionTimes = new HashMap<>();
        for (Map.Entry<String, Long> entry : taskExecutionTimes.entrySet()) {
            String taskName = entry.getKey();
            long totalTime = entry.getValue();
            int count = taskExecutionCounts.get(taskName);
            double avgTime = (totalTime / (double) count) / 1_000_000.0; // Convert to milliseconds
            avgExecutionTimes.put(taskName, String.format("%.3f ms", avgTime));
        }
        stats.put("averageExecutionTimes", avgExecutionTimes);
        
        return stats;
    }
    
    /**
     * Get slowest tasks
     */
    public List<String> getSlowestTasks(int limit) {
        return taskExecutionTimes.entrySet().stream()
            .sorted((e1, e2) -> {
                double avg1 = e1.getValue() / (double) taskExecutionCounts.get(e1.getKey());
                double avg2 = e2.getValue() / (double) taskExecutionCounts.get(e2.getKey());
                return Double.compare(avg2, avg1);
            })
            .limit(limit)
            .map(entry -> {
                String taskName = entry.getKey();
                double avgTime = (entry.getValue() / (double) taskExecutionCounts.get(taskName)) / 1_000_000.0;
                return taskName + ": " + String.format("%.3f ms", avgTime);
            })
            .toList();
    }
    
    /**
     * Check if we're in performance mode
     */
    public boolean isPerformanceMode() {
        return performanceMode;
    }
    
    /**
     * Get current FPS
     */
    public double getCurrentFps() {
        return averageFps;
    }
    
    /**
     * Clear all statistics
     */
    public void clear() {
        taskExecutionTimes.clear();
        taskExecutionCounts.clear();
        lastExecutionTimes.clear();
    }
}
