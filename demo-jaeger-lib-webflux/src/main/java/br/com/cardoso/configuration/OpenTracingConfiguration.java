package br.com.cardoso.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class OpenTracingConfiguration {

    //Criando o bean do WebClient para que possa ser considerado no Tracing
    @Bean
    public WebClient webClient() {
        return WebClient.builder().build();
    }
}
