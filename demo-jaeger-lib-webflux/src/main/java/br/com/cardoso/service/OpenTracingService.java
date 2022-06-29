package br.com.cardoso.service;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class OpenTracingService {

    private final WebClient webClientHello;

    public OpenTracingService(WebClient webClientHello) {
        this.webClientHello = webClientHello;
    }

    public Mono<String> hello() {
        return webClientHello.get().exchangeToMono(response -> response.bodyToMono(String.class).map(str -> "Hello " + str));
    }
}
