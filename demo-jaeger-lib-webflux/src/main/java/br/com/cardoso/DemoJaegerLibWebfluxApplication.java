package br.com.cardoso;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import reactor.blockhound.BlockHound;

@SpringBootApplication
public class DemoJaegerLibWebfluxApplication {

	public static void main(String[] args) {
		BlockHound.install();
		SpringApplication.run(DemoJaegerLibWebfluxApplication.class, args);
	}

}
