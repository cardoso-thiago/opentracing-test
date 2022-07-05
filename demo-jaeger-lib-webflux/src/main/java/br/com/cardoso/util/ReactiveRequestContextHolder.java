package br.com.cardoso.util;

import org.springframework.http.server.reactive.ServerHttpRequest;
import reactor.core.publisher.Mono;

import java.util.Optional;

public class ReactiveRequestContextHolder {
    public static final Class<ServerHttpRequest> CONTEXT_KEY = ServerHttpRequest.class;

    public static Mono<Optional<Object>> getRequest() {
        return Mono.deferContextual(ctx -> Mono.just(ctx.getOrEmpty(CONTEXT_KEY)));
    }
}
