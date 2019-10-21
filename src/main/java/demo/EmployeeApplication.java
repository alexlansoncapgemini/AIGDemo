package demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class EmployeeApplication {
	//calls MyRestController and establishes the mappings 
	public static void main(String[] args) {
		SpringApplication.run(EmployeeApplication.class, args);
	}

}
