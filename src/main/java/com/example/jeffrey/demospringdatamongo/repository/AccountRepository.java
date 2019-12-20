package com.example.jeffrey.demospringdatamongo.repository;

import com.example.jeffrey.demospringdatamongo.model.Account;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AccountRepository extends MongoRepository<Account, String> {

    public Account findByAccountNumber(String accountNumber);

}
