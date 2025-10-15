package be.esmay.atlas.velocity.utils;

import be.esmay.atlas.velocity.AtlasVelocityPlugin;
import lombok.experimental.UtilityClass;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Utility class for concurrency
 */
@UtilityClass
public final class Concurrency {

    private static final ExecutorService EXECUTOR_SERVICE = Executors.newFixedThreadPool(5);

    /**
     * Returns an executor that runs on the main thread.
     *
     * @return An executor that runs on the main thread.
     */
    public static Executor sync() {
        return runnable -> AtlasVelocityPlugin.getInstance().getProxyServer().getScheduler().buildTask(AtlasVelocityPlugin.getInstance(), runnable).schedule();
    }

    /**
     * Returns an executor that runs on the thread pool
     *
     * @return An executor that runs on the thread pool
     */
    public static Executor async() {
        return EXECUTOR_SERVICE;
    }

    /**
     * Returns an executor that runs on the current thread.
     * Useful if you're already on an async thread and don't want the overhead of waiting/creating another one.
     *
     * @return An executor that runs on the current thread.
     */
    public static Executor currentThread() {
        return Runnable::run;
    }
}
