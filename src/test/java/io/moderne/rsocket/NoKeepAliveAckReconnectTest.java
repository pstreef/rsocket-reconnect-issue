package io.moderne.rsocket;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.messaging.rsocket.RSocketRequester;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@SpringBootTest
class NoKeepAliveAckReconnectTest {

    @Autowired
    RSocketRequester requester;

    @Test
    void noKeepAliveAcksRestart() {
        int expectedCount = 4;
        AtomicBoolean sleepOnce = new AtomicBoolean(true);
        StepVerifier.create(
                        Flux.range(0, expectedCount)
                                .delayElements(Duration.ofMillis(1000))
                                .flatMapSequential(i -> requester.route("ping")
                                        .retrieveMono(String.class)
                                        .doOnNext(message -> {
                                            if (sleepOnce.getAndSet(false)) {
                                                try {
                                                    log.info("Sleeping...");
                                                    Thread.sleep(1_000);
                                                    log.info("Waking up.");
                                                } catch (InterruptedException e) {
                                                    throw new RuntimeException(e);
                                                }
                                            }
                                        })
                                ))
                .expectSubscription()
                .expectNextCount(expectedCount)
                .verifyComplete();
    }

}
