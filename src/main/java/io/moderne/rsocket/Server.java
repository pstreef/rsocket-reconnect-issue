package io.moderne.rsocket;

import io.rsocket.core.RSocketServer;
import io.rsocket.frame.decoder.PayloadDecoder;
import io.rsocket.transport.netty.server.TcpServerTransport;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.rsocket.RSocketMessageHandlerCustomizer;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.messaging.rsocket.RSocketStrategies;
import org.springframework.messaging.rsocket.annotation.ConnectMapping;
import org.springframework.messaging.rsocket.annotation.support.RSocketMessageHandler;

import java.util.List;

@Configuration
@Slf4j
public class Server {

    public Server(RSocketStrategies rSocketStrategies,
                  ObjectProvider<RSocketMessageHandlerCustomizer> customizers,
                  @Value("${rsocket.port:9000}") int rSocketPort) {
        RSocketMessageHandler messageHandler = new RSocketMessageHandler();
        messageHandler.setRSocketStrategies(rSocketStrategies);
        messageHandler.setHandlers(List.of(this));
        customizers.orderedStream().forEach((customizer) -> customizer.customize(messageHandler));
        messageHandler.afterPropertiesSet();

        RSocketServer.create()
                .payloadDecoder(PayloadDecoder.ZERO_COPY)
                .acceptor(messageHandler.responder())
                .bind(TcpServerTransport.create(rSocketPort))
                .doOnNext(s -> log.info("RSocket Server started on {}.", s.address()))
                .subscribe();
    }

    @MessageMapping("ping")
    public String echo() {
        log.info("ping");
        return "pong";
    }

    @ConnectMapping
    void connect(RSocketRequester requester) {
        requester.rsocket()
                .onClose()
                .doFirst(() -> log.info("Connected on server side."))
                .doOnTerminate(() -> log.info("Connection closed on server side.")).subscribe();
    }
}
