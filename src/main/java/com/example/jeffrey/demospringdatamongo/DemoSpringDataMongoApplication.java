package com.example.jeffrey.demospringdatamongo;

import com.example.jeffrey.demospringdatamongo.repository.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@SpringBootApplication
@EnableMongoRepositories
public class DemoSpringDataMongoApplication implements CommandLineRunner {

	public static void main(String[] args) {
		SpringApplication.run(DemoSpringDataMongoApplication.class, args);
	}

	@Autowired
    CustomerRepository customerRepository;

	@Override
	public void run(String... args) throws Exception {
		customerRepository.deleteAll();
	}
}
