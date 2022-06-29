package br.com.cardoso.service;

import io.opentracing.contrib.jaxrs2.client.ClientTracingFeature;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

@Service
public class OpenTracingService {

    private final RestTemplate restTemplate;

    public OpenTracingService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public String hello() {
        String path = "http://localhost:8080/world";
        ResponseEntity<String> response = restTemplate.getForEntity(path, String.class);
        return response.getBody();
    }

    public String helloJersey() {
        String path = "http://localhost:8080/world";
        Client client = ClientBuilder.newBuilder().build();
        WebTarget webTarget = client
                //Configura o tracing para o client do jax-rs
                .register(ClientTracingFeature.class)
                .target(path);
        Response response = webTarget.request().get();
        return response.readEntity(String.class);
    }
}
