package net.nhs.sgh.emailadapter.schedulingtasks;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SghEmailApplication {

	public static void main(String[] args) {
		SpringApplication.run(SghEmailApplication.class);
	}
}
