package br.com.cardoso.service;

import br.com.cardoso.filter.FilterFunctions;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class OpenTracingService {
    private final WebClient webClient;

    public OpenTracingService(WebClient webClient) {
        this.webClient = webClient;
    }

    public Mono<String> hello() {

        return webClient.mutate().baseUrl("http://localhost:8080")
                .filter(FilterFunctions.addTestHeader())
                .filter(FilterFunctions.addHeaderFromServerRequest())
                .build().get().uri("world")
                .exchangeToMono(response -> response.bodyToMono(String.class).map(str -> "Hello " + str));
    }
}
