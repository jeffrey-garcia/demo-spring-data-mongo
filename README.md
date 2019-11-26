# Accessing data with Spring Data Mongo
A `spring-boot-application` using Spring Data Mongo to access MongoDB database, 
and uses embedded database for local testing and debugging.

### MongoDB Transaction
In MongoDB, an operation on a single document is atomic. 

In version 4.0, MongoDB supports multi-document transactions on `replica sets`.
In version 4.2, MongoDB introduces distributed transactions, which adds support 
for multi-document transactions on sharded clusters and incorporates the existing 
support for multi-document transactions on replica sets.

Transactions are much more costly in terms of performance when it incurs the 
usage of lock to for parallel-execution of a set of transactions to run in 
serial manner.

### Running the unit-test
```sh
./mvnw test
```

### Running the integration test with SpringRunner
```sh
./mvnw verify
```

### Startup Mongo
```sh
sudo mongod
```

### References: