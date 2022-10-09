package info.ankin.rxloom.rx;

import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

@SuppressWarnings({
        "preview",
        "RedundantSuppression",
        "ReactiveStreamsPublisherImplementation",
        "ReactiveStreamsSubscriberImplementation",
})
public class RxExperiments {
    public static void main(String[] args) {
    }

    @SuppressWarnings("unused")
    public static void publisherExperiment() {
        Publisher<Integer> integerPublisher = new Publisher<>() {
            private final int wait = 500;

            private Thread handleSubscriberReady(Subscriber<? super Integer> subscriber) {
                return Thread.ofVirtual().start(() -> {
                    try {
                        for (int i = 0; i < 5; i++) {
                            subscriber.onNext(i);
                            sleep(wait);
                        }

                        subscriber.onComplete();
                    } catch (Exception e) {
                        boolean interrupted = Thread.interrupted();
                        System.out.println("we were interrupted: " + interrupted);
                        subscriber.onError(e);
                    }
                });
            }

            @Override
            public void subscribe(Subscriber<? super Integer> s) {
                s.onSubscribe(new Subscription() {
                    Thread thread;

                    @Override
                    public void request(long n) {
                        thread = handleSubscriberReady(s);
                    }

                    @Override
                    public void cancel() {
                        if (thread != null) {
                            thread.interrupt();
                        }
                    }
                });
            }
        };

        // this keeps the jvm alive, virtual threads do not
        CountDownLatch countDownLatch = new CountDownLatch(1);

        final AtomicReference<Subscription> sub = new AtomicReference<>();
        integerPublisher.subscribe(new Subscriber<>() {
            @Override
            public void onSubscribe(Subscription s) {
                sub.set(s);
                s.request(10);
            }

            @Override
            public void onNext(Integer integer) {
                System.out.println("got new integer: " + integer);
            }

            @Override
            public void onError(Throwable t) {
                System.out.println("got throwable: " + t);
                countDownLatch.countDown();
            }

            @Override
            public void onComplete() {
                System.out.println("we are done");
                countDownLatch.countDown();
            }
        });

        sleep(1250);

        sub.get().cancel();

        try {
            countDownLatch.await();
            System.out.println("count down latch is done");
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public static void sleep(long time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unused")
    public static void simpleSingleWaitTime() {
        long t0 = System.nanoTime();
        try (ExecutorService executorService = Executors.newVirtualThreadPerTaskExecutor()) {
            for (int i = 0; i < 3; i++) {
                Runnable runnable = () -> sleep(5_000L);
                executorService.submit(runnable);
            }
        }

        long t1 = System.nanoTime();

        // will print about 5000
        System.out.println((t1 - t0) / 1000_000);
    }
}
