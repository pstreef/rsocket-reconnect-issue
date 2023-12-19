package io.moderne.rsocket;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import reactor.core.publisher.Hooks;

@Slf4j
@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        Hooks.onOperatorDebug();
        SpringApplication.run(Application.class, args);
    }
}
