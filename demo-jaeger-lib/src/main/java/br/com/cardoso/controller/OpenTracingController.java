package br.com.cardoso.controller;

import br.com.cardoso.service.OpenTracingService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class OpenTracingController {

    private final OpenTracingService openTracingService;

    public OpenTracingController(OpenTracingService openTracingService) {
        this.openTracingService = openTracingService;
    }

    @GetMapping("/hello")
    public String hello() {
        return "Hello " + openTracingService.hello();
    }

    @GetMapping("/helloJersey")
    public String helloJersey() {
        return "Hello " + openTracingService.helloJersey();
    }

    @GetMapping("/world")
    public String world() {
        return "World";
    }
}
