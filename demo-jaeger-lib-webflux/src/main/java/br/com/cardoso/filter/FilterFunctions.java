package br.com.cardoso.filter;

import br.com.cardoso.util.ReactiveRequestContextHolder;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import reactor.core.publisher.Mono;

import java.util.Objects;
import java.util.Optional;

public class FilterFunctions {

    public static ExchangeFilterFunction addTestHeader() {
        return ExchangeFilterFunction.ofRequestProcessor(clientRequest -> Mono.just(ClientRequest.from(clientRequest).header("filter_test", "test").build()));
    }

    public static ExchangeFilterFunction addHeaderFromServerRequest() {
        Mono<Optional<Object>> serverHttpRequestMono = ReactiveRequestContextHolder.getRequest();
        return ExchangeFilterFunction.ofRequestProcessor(clientRequest -> serverHttpRequestMono.map(serverHttpRequest -> {
            if (serverHttpRequest.isPresent()) {
                return ClientRequest.from(clientRequest).header("server_header_test",
                        Objects.requireNonNull(((ServerHttpRequest) serverHttpRequest.get()).getHeaders().get("server_header_test")).get(0)).build();
            } else {
                return clientRequest;
            }
        }));


    }
}
