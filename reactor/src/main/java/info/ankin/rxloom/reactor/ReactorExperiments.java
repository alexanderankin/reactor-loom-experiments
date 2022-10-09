package info.ankin.rxloom.reactor;

import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

public class ReactorExperiments {
    public static void main(String[] args) {
        Mono<String> abcMono = Mono.create(stringMonoSink -> {
            stringMonoSink.onRequest(requested -> {
                sleep(500);
                stringMonoSink.success("abc");
            });
        });

        Scheduler scheduler = Schedulers.boundedElastic();

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
}
