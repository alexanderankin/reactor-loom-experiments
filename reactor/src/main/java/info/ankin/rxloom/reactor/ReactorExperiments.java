package info.ankin.rxloom.reactor;

import reactor.core.Disposable;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;
import reactor.util.annotation.NonNull;

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

    private static class LoomScheduler implements Scheduler {
        final Scheduler scheduler;

        public LoomScheduler() {
            Thread.Builder.OfVirtual ofVirtual = Thread.ofVirtual();
            ThreadFactory factory = ofVirtual.factory();

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
        public Worker createWorker() {
            return scheduler.createWorker();
        }

        @NonNull
        public Worker createWorkerIdea() {
            return new Worker() {
                @NonNull
                @Override
                public Disposable schedule(@NonNull Runnable task) {
                    return new Disposable() {
                        @Override
                        public void dispose() {

                        }
                    };
                }

                @Override
                public void dispose() {

                }
            };
        }
    }
}
