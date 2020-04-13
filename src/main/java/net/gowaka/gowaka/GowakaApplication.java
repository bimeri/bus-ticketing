package net.gowaka.gowaka;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

import javax.annotation.PostConstruct;
import java.util.Date;
import java.util.TimeZone;

@SpringBootApplication
@EnableDiscoveryClient
public class GowakaApplication {

	@PostConstruct
	public void init(){
		TimeZone.setDefault(TimeZone.getTimeZone("Africa/Douala"));   // It will set Africa/Douala
		System.out.println("Spring boot application running in UTC timezone :"+new Date());   // It will print UTC timezone
	}
	public static void main(String[] args) {
		SpringApplication.run(GowakaApplication.class, args);
	}

}
