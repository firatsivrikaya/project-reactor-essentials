package com.firatsivrikaya.reactive.test;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.util.List;

@Slf4j
public class FluxText {

    @Test
    public void fluxSubscriber() {
        Flux<String> fluxString = Flux.just("Firat", "Sivrikaya", "Reactive").log();
        StepVerifier.create(fluxString).expectNext("Firat", "Sivrikaya", "Reactive").verifyComplete();
    }

    @Test
    public void fluxSubscriberNumbers() {
        Flux<Integer> fluxInteger = Flux.range(1, 5).log();
        fluxInteger.subscribe(integer -> log.info("value is {}", integer));
        StepVerifier.create(fluxInteger).expectNext(1, 2, 3, 4, 5).verifyComplete();
    }

    @Test
    public void fluxSubscriberFromList() {
        Flux<Integer> fluxInteger = Flux.fromIterable(List.of(1, 2, 3, 4, 5)).log();
        fluxInteger.subscribe(integer -> log.info("value is {}", integer));
        StepVerifier.create(fluxInteger).expectNext(1, 2, 3, 4, 5).verifyComplete();
    }

    @Test
    public void fluxSubscriberNumbersError() {
        Flux<Integer> fluxInteger = Flux.range(1, 5).log()
                .map(integer -> {
                    if (integer == 4) {
                        throw new IndexOutOfBoundsException();
                    }
                    return integer;
                });
        fluxInteger.subscribe(integer -> log.info("value is {}", integer),
                Throwable::printStackTrace,
                () -> log.info("Completed"), subscription -> {
                    subscription.request(3);
                }
        );
        StepVerifier.create(fluxInteger).expectNext(1, 2, 3).expectError(IndexOutOfBoundsException.class).verify();
    }

    @Test
    public void fluxSubscriberNumbersUglyBackPressure() {
        Flux<Integer> fluxInteger = Flux.range(1, 10).log();
        fluxInteger.subscribe(new Subscriber<Integer>() {
            private int count;
            private Subscription subscription;
            private int requestCount = 2;

            @Override
            public void onSubscribe(Subscription subscription) {
                this.subscription = subscription;
                subscription.request(requestCount);
            }

            @Override
            public void onNext(Integer integer) {
                count++;
                if (count >= requestCount) {
                    count = 0;
                    subscription.request(requestCount);
                }

            }

            @Override
            public void onError(Throwable throwable) {

            }

            @Override
            public void onComplete() {

            }
        });
        StepVerifier.create(fluxInteger).expectNext(1, 2, 3, 4, 5, 6, 7, 8, 9, 10).verifyComplete();
    }
}
