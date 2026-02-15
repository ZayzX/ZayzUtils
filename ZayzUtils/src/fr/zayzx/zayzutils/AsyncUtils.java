package fr.zayzx.zayzutils;

import java.util.List;
import java.util.concurrent.*;
import java.util.function.Consumer;

public final class AsyncUtils {

    private static final ExecutorService EXECUTOR =
            Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

    private static final ScheduledExecutorService SCHEDULER =
            Executors.newScheduledThreadPool(2);

    private AsyncUtils() {}

    /* =========================
       ASYNC TASKS
       ========================= */

    public static CompletableFuture<Void> runAsync(Runnable task) {
        return CompletableFuture.runAsync(task, EXECUTOR);
    }

    public static <T> CompletableFuture<T> supplyAsync(Callable<T> task) {
        return CompletableFuture.supplyAsync(() -> {
            try { return task.call(); }
            catch (Exception e) { throw new CompletionException(e); }
        }, EXECUTOR);
    }

    public static <T> void supplyAsync(Callable<T> task, Consumer<T> onSuccess, Consumer<Throwable> onError) {
        supplyAsync(task).whenComplete((result, throwable) -> {
            if (throwable != null) onError.accept(throwable);
            else onSuccess.accept(result);
        });
    }

    public static void runAsync(Runnable task, Consumer<Throwable> onError) {
        runAsync(task).whenComplete((r, throwable) -> {
            if (throwable != null) onError.accept(throwable);
        });
    }

    /* =========================
       DELAY / SCHEDULE
       ========================= */

    public static ScheduledFuture<?> delay(Runnable task, long millis) {
        return SCHEDULER.schedule(task, millis, TimeUnit.MILLISECONDS);
    }

    public static ScheduledFuture<?> scheduleAtFixedRate(Runnable task, long delay, long period) {
        return SCHEDULER.scheduleAtFixedRate(task, delay, period, TimeUnit.MILLISECONDS);
    }

    public static ScheduledFuture<?> scheduleAtFixedRate(Runnable task, long delay, long period, Consumer<Throwable> onError) {
        return SCHEDULER.scheduleAtFixedRate(() -> {
            try { task.run(); }
            catch (Throwable t) { onError.accept(t); }
        }, delay, period, TimeUnit.MILLISECONDS);
    }

    /* =========================
       COMPOSITE / WAIT
       ========================= */

    public static <T> List<T> awaitAll(List<CompletableFuture<T>> tasks) {
        CompletableFuture<Void> all = CompletableFuture.allOf(tasks.toArray(new CompletableFuture[0]));
        all.join();
        return tasks.stream().map(CompletableFuture::join).toList();
    }

    @SuppressWarnings("unchecked")
	public static <T> CompletableFuture<T> awaitAny(List<CompletableFuture<T>> tasks) {
        return CompletableFuture.anyOf(tasks.toArray(new CompletableFuture[0]))
                .thenApply(obj -> (T) obj);
    }

    /* =========================
       SHUTDOWN
       ========================= */

    public static void shutdown() {
        EXECUTOR.shutdown();
        SCHEDULER.shutdown();
    }

    public static void shutdownNow() {
        EXECUTOR.shutdownNow();
        SCHEDULER.shutdownNow();
    }
}
