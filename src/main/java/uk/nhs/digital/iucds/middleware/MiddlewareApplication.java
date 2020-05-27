package uk.nhs.digital.iucds.middleware;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class MiddlewareApplication {

  public static void main(String[] args) {
    SpringApplication.run(MiddlewareApplication.class);
  }
}