package br.com.cardoso.filter;

import br.com.cardoso.util.ReactiveRequestContextHolder;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import reactor.core.publisher.Mono;

import java.util.Objects;

public class FilterFunctions {

    public static ExchangeFilterFunction addTestHeader() {
        return ExchangeFilterFunction.ofRequestProcessor(clientRequest -> Mono.just(ClientRequest.from(clientRequest).header("filter_test", "test").build()));
    }

    public static ExchangeFilterFunction addHeaderFromServerRequest() {
        Mono<ServerHttpRequest> serverHttpRequestMono = ReactiveRequestContextHolder.getRequest();
        return ExchangeFilterFunction.ofRequestProcessor(clientRequest -> serverHttpRequestMono
                .map(serverHttpRequest -> ClientRequest.from(clientRequest)
                        .header("server_header_test", Objects.requireNonNull(serverHttpRequest.getHeaders()
                                .get("server_header_test")).get(0)).build()));
    }
}
