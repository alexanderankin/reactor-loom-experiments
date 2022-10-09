package info.ankin.rxloom.reactor;

import reactor.core.Disposable;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;
import reactor.util.annotation.NonNull;

import java.time.Duration;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ReactorExperiments {
    public static void main(String[] args) {
        Mono<String> abcMono = Mono.create(stringMonoSink -> {
            stringMonoSink.onRequest(requested -> {
                sleep(500);
                stringMonoSink.success("abc");
            });
        });

        Scheduler scheduler = new LoomScheduler();

        String abc = abcMono
                .subscribeOn(scheduler)
                .delayElement(Duration.ofMillis(10))
                .block();

        System.out.println(abc);
    }

    @SuppressWarnings("SameParameterValue")
    private static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public static class LoomScheduler implements Scheduler {
        Thread.Builder.OfVirtual ofVirtual;
        ThreadFactory factory;
        final Scheduler scheduler;

        public LoomScheduler() {
            ofVirtual = Thread.ofVirtual();
            factory = ofVirtual.factory();

            scheduler = Schedulers.fromExecutor(new ThreadPoolExecutor(1,
                    5,
                    10,
                    TimeUnit.SECONDS,
                    new LinkedBlockingQueue<>(),
                    factory,
                    new ThreadPoolExecutor.AbortPolicy()));
        }

        @NonNull
        @Override
        public Disposable schedule(@NonNull Runnable task) {
            return scheduler.schedule(task);
        }

        @NonNull
        @Override
        public Disposable schedule(@NonNull Runnable task,
                                   long delay,
                                   @NonNull TimeUnit unit) {
            return schedule(new Delays.DelayedTask(this, task, delay, unit));
        }

        @NonNull
        @Override
        public Disposable schedulePeriodically(@NonNull Runnable task,
                                               long initialDelay,
                                               long period,
                                               @NonNull TimeUnit unit) {
            return schedule(new Delays.PeriodTask(this, task, initialDelay, period, unit));
        }

        @NonNull
        @Override
        public Worker createWorker() {
            return scheduler.createWorker();
        }
    }
}
