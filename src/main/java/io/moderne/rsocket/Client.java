package io.moderne.rsocket;

import io.rsocket.RSocket;
import io.rsocket.transport.netty.client.TcpClientTransport;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.rsocket.RSocketRequester;
import reactor.util.retry.Retry;
import reactor.util.retry.RetryBackoffSpec;

import java.time.Duration;
import java.util.function.Function;

@Slf4j
@Configuration
public class Client {

    @Value("${rsocket.port:9000}")
    int rSocketPort;

    @Bean
    RSocketRequester requester(RSocketRequester.Builder rsocketRequesterBuilder) {

        Function<String, RetryBackoffSpec> reconnectSpec = reason -> Retry.backoff(Long.MAX_VALUE, Duration.ofSeconds(10L))
                .doBeforeRetry(retrySignal -> log.info("Reconnecting. Reason: {}", reason));

        RSocketRequester requester = rsocketRequesterBuilder
                .rsocketConnector(c -> c.fragment(16384)
                        .reconnect(reconnectSpec.apply("connector-close"))
                        .keepAlive(Duration.ofMillis(100L), Duration.ofMillis(900L)))
                .transport(TcpClientTransport.create(rSocketPort));

        // https://github.com/rsocket/rsocket-java/issues/987#issuecomment-796176003
        requester.rsocketClient()
                .source()
                .flatMap(RSocket::onClose)
                .doOnError(err -> log.error("Error during onClose.", err))
                .retryWhen(reconnectSpec.apply("client-close"))
                .doFirst(() -> log.info("Connected on client side."))
                .doOnTerminate(() -> log.info("Connection closed on client side."))
                .repeat()
                .subscribe();

        return requester;
    }

}
