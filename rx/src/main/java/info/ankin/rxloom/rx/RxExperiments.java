package info.ankin.rxloom.rx;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RxExperiments {
    public static void main(String[] args) {
        long t0 = System.nanoTime();
        try (ExecutorService executorService = Executors.newVirtualThreadPerTaskExecutor()) {
            for (int i = 0; i < 3; i++) {
                interface ThrowingRunnable {
                    default void run() {
                        try {
                            runUnsafe();
                        } catch (Throwable e) {
                            throw new RuntimeException(e);
                        }
                    }
                    void runUnsafe() throws Throwable;
                }

                ThrowingRunnable runnable = () -> Thread.sleep(5_000L);
                executorService.submit(runnable::run);
            }
        }

        long t1 = System.nanoTime();

        System.out.println((t1-t0)/1000_000);
    }
}
