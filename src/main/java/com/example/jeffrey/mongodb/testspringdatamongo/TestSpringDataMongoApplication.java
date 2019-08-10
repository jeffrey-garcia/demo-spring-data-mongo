package com.example.jeffrey.mongodb.testspringdatamongo;

import com.example.jeffrey.mongodb.testspringdatamongo.model.Sequence;
import com.example.jeffrey.mongodb.testspringdatamongo.repository.CustomerRepository;
import com.example.jeffrey.mongodb.testspringdatamongo.repository.SequenceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class TestSpringDataMongoApplication implements CommandLineRunner {

	public static void main(String[] args) {
		SpringApplication.run(TestSpringDataMongoApplication.class, args);
	}

	@Autowired
	CustomerRepository customerRepository;

	@Autowired
	SequenceRepository sequenceRepository;

	@Override
	public void run(String... args) throws Exception {
		customerRepository.deleteAll();
		sequenceRepository.deleteAll();
		sequenceRepository.save(new Sequence());
	}
}
