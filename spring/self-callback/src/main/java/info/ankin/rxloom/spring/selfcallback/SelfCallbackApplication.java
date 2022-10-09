package info.ankin.rxloom.spring.selfcallback;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@SpringBootApplication
public class SelfCallbackApplication {
    public static void main(String[] args) {
        System.setProperty("server.port", "0");
        SpringApplication.run(SelfCallbackApplication.class, args);
    }

    @Slf4j
    @RestController
    public static class SelfCallbackController {
        private final WebClient.Builder builder;
        private final ObjectProvider<Integer> serverPort;
        WebClient webClient;

        /**
         * @param serverPort lazily inject {@code local.server.port}
         */
        public SelfCallbackController(WebClient.Builder builder,
                                      @Value("${local.server.port}") ObjectProvider<Integer> serverPort) {
            this.builder = builder;
            this.serverPort = serverPort;
        }

        @RequestMapping("/")
        Mono<String> home() {
            return Mono.just("home");
        }

        @RequestMapping("/sc")
        Mono<String> sc() {
            return getWebClient().get().uri("/").retrieve().bodyToMono(String.class);
        }

        private WebClient getWebClient() {
            if (webClient == null) {
                webClient = builder
                        .baseUrl("http://localhost:" + serverPort.getObject())
                        .build();
            }

            return webClient;
        }

        @Bean
        ApplicationRunner applicationRunner(ConfigurableApplicationContext ctx) {
            return args -> {
                log.info("started on {}", serverPort.getObject());
                getWebClient().get().uri("/sc").retrieve().toEntity(String.class).doOnNext(System.out::println).block();
                ctx.close();
            };
        }
    }
}
