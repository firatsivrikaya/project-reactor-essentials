package com.firatsivrikaya.reactive.test;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.reactivestreams.Subscription;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

/**
 * Reactive Streams
 * Async
 * Non-blocking
 * Back Pressure
 * Publisher <- subscribe(Subscriber<? super T> var1);
 * when subscriber subscribes to publisher, a context named Subscription will be created by Publisher
 * Subscriber.onSubscribe with Subscription will be called by Publisher
 * Backpressure is handled by Subscriber via Subscription, with request(long var1) method
 * Publisher will call Subscriber's onNext method
 * until:
 * 1. Publisher sends all the objects requested
 * 2. Publisher sends all the object it has. Subscriber.onComplete() subscriber and subscription will be canceled
 * 3. There is and error, Subscriber.onError() will be called, subscriber and subscription will be canceled
 **/
@Slf4j
public class MonoTest {

    @Test
    public void monoSubscriber() {
        String name = "Firat";
        Mono<String> mono = Mono.just(name).log();
        mono.subscribe();
        log.info("--------------------");
        StepVerifier.create(mono).expectNext(name).verifyComplete();
    }

    @Test
    public void monoSubscriberConsumer() {
        String name = "Firat";
        Mono<String> mono = Mono.just(name).log();
        mono.subscribe(s -> log.info("Value is {}", s));
        log.info("--------------------");
        StepVerifier.create(mono).expectNext(name).verifyComplete();
    }

    @Test
    public void monoSubscriberConsumerError() {
        String name = "Firat";
        Mono<String> mono = Mono.just(name).map(s -> {
            throw new RuntimeException("Testing mono with error");
        });

        mono.subscribe(s -> log.info("Value is {}", s), Throwable::printStackTrace);
        log.info("--------------------");
        StepVerifier.create(mono).expectError(RuntimeException.class).verify();
    }

    @Test
    public void monoSubscriberConsumerComplete() {
        String name = "Firat";
        Mono<String> mono = Mono.just(name).log().map(String::toUpperCase);
        mono.subscribe(s -> log.info("Value is {}", s),
                Throwable::printStackTrace,
                () -> log.info("Finished"));
        log.info("--------------------");
        StepVerifier.create(mono).expectNext(name.toUpperCase()).verifyComplete();
    }

    @Test
    public void monoSubscriberConsumerSubscriptionCancel() {
        String name = "Firat";
        Mono<String> mono = Mono.just(name).log().map(String::toUpperCase);
        mono.subscribe(s -> log.info("Value is {}", s),
                Throwable::printStackTrace,
                () -> log.info("Finished"), Subscription::cancel);
        log.info("--------------------");
    }

    @Test
    public void monoSubscriberConsumerSubscription() {
        String name = "Firat";
        Mono<String> mono = Mono.just(name).log().map(String::toUpperCase);
        mono.subscribe(s -> log.info("Value is {}", s),
                Throwable::printStackTrace,
                () -> log.info("Finished"), subscription -> {
                    subscription.request(5);
                });
        log.info("--------------------");
        //StepVerifier.create(mono).expectNext(name.toUpperCase()).verifyComplete();
    }

    @Test
    public void monoDoOnMethods() {
        String name = "Firat";
        Mono<String> mono = Mono.just(name).log().map(String::toUpperCase)
                .doOnSubscribe(subscription -> {
                    log.info("doOnSubscribe");
                })
                .doOnRequest(value -> log.info("doOnRequest with value {}", value))
                .doOnNext(value -> log.info("doOnNext, value is {}", value))
                .doOnSuccess(s -> log.info("doOnSuccess executed"));
        mono.subscribe(s -> log.info("Value is {}", s),
                Throwable::printStackTrace,
                () -> log.info("Finished"));
        log.info("--------------------");
        //StepVerifier.create(mono).expectNext(name.toUpperCase()).verifyComplete();
    }

    @Test
    public void monoDoOnError() {
        Mono<Object> error = Mono.error(new IllegalArgumentException("Illegal Argument Exception"))
                .doOnError(throwable -> log.error("Error message {} ", throwable.getMessage()))
                .doOnNext(o -> log.info("inside doOnNext"))//this line will not be excuted
                .log();

        StepVerifier.create(error).expectError(IllegalArgumentException.class).verify();
    }

    @Test
    public void monoDoOnErrorResume() {
        String name = "Firat";
        Mono<Object> error = Mono.error(new IllegalArgumentException("Illegal Argument Exception"))
                .doOnError(throwable -> log.error("Error message {} ", throwable.getMessage()))
                .onErrorResume(throwable -> {
                    log.info("inside of onErrorResume");
                    return Mono.just(name);
                })
                .log();
        //if you change the above as .onErrorResume.doOnError, doOnError will not be executed bec onErrorResume will return Mono.just
        //StepVerifier.create(error).expectError(IllegalArgumentException.class).verify(); //this will fail
        StepVerifier.create(error).expectNext(name).verifyComplete();
    }

    @Test
    public void monoDoOnErrorReturn() {
        String name = "Firat";
        Mono<Object> error = Mono.error(new IllegalArgumentException("Illegal Argument Exception"))
                .onErrorReturn("EMPTY")
                .onErrorResume(throwable -> {
                    log.info("inside of onErrorResume");
                    return Mono.just(name);
                })
                .log();
        StepVerifier.create(error).expectNext("EMPTY").verifyComplete();
    }
}
