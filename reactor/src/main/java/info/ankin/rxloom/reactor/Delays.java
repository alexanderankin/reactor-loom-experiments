package info.ankin.rxloom.reactor;

import java.util.concurrent.TimeUnit;

public class Delays {
    public static class DelayedTask implements Runnable {
        private final ReactorExperiments.LoomScheduler loomScheduler;
        private final Runnable task;
        private final long delayMillis;

        public DelayedTask(ReactorExperiments.LoomScheduler loomScheduler,
                           Runnable task,
                           long delay,
                           TimeUnit unit) {
            this.loomScheduler = loomScheduler;
            this.task = task;
            delayMillis = unit.toMillis(delay);
        }

        @Override
        public void run() {
            loomScheduler.ofVirtual.start(() -> {
                try {
                    Thread.sleep(delayMillis);
                    task.run();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }
    }

    public static class PeriodTask implements Runnable {
        final ReactorExperiments.LoomScheduler loomScheduler;
        private final Runnable task;
        final long initialDelay;
        final long period;
        final TimeUnit unit;

        public PeriodTask(ReactorExperiments.LoomScheduler loomScheduler,
                          Runnable task,
                          long initialDelay,
                          long period,
                          TimeUnit unit) {
            this.loomScheduler = loomScheduler;
            this.task = task;
            this.initialDelay = initialDelay;
            this.period = period;
            this.unit = unit;
        }

        @Override
        public void run() {
            loomScheduler.ofVirtual.start(() -> {
                try {
                    Thread.sleep(initialDelay);
                    // this is what we actually want, i think
                    //noinspection InfiniteLoopStatement
                    while (true) {
                        task.run();
                        // maybe this is okay with loom?
                        //noinspection BusyWait
                        Thread.sleep(unit.toMillis(period));
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }
    }

}
