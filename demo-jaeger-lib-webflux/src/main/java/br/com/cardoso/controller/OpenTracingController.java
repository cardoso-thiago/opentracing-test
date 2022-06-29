package br.com.cardoso.controller;

import br.com.cardoso.service.OpenTracingService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
public class OpenTracingController {

    private final OpenTracingService openTracingService;

    public OpenTracingController(OpenTracingService openTracingService) {
        this.openTracingService = openTracingService;
    }

    @GetMapping("/hello")
    public Mono<String> hello() {
        return openTracingService.hello();
    }

    @GetMapping("/world")
    public Mono<String> world() {
        return Mono.just("World");
    }
}
