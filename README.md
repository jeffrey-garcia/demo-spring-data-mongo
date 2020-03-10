# Accessing data with Spring Data Mongo
A `spring-boot-application` using Spring Data Mongo to access MongoDB database, 
and uses embedded database for local testing and debugging.

### MongoDB Transaction
In MongoDB, an operation on a single document is atomic. 

- In version 4.0, MongoDB supports multi-document transactions on `replica sets`.
- In version 4.2, MongoDB introduces distributed transactions, which adds support 
for multi-document transactions on sharded clusters and incorporates the existing 
support for multi-document transactions on replica sets.

Note that transactions are much more costly in terms of performance when it incurs the 
usage of lock to coordinate the parallel-execution of a set of transactions from 
distributed nodes.

```sh
In most cases, multi-document transaction incurs a greater performance cost 
over single document writes, and the availability of multi-document transaction 
should not be a replacement for effective schema design. For many scenarios, 
the denormalized data model (embedded documents and arrays) will continue to 
be optimal for your data and use cases. That is, for many scenarios, modeling 
your data appropriately will minimize the need for multi-document transactions.
```

<br/>

### System Requirement:
- MongoDB v4.0 or higher, which supports multi-document transactions on `replica sets`.
- MongoDB Java Driver v3.11 or higher, with improved transaction support:
    - The sessions API supports the ClientSession.withTransaction() method to conveniently run 
    a transaction with 
automatic retries and at-most-once semantics
- Spring-boot-starter-data-mongodb v2.2.0 or higher
    - Support for Hashed Indexes, facilitates hash based sharding within a sharded cluster. 
    Using hashed field values to shard collections results in a more random distribution of 
    data resided in the event store collection.

### Setup MongoDB

##### Install MongoDB (Mac OSX)
```sh
brew install mongodb
```

##### Startup MongoDB (Mac OSX)
```sh
sudo mongod
```

##### Setup local replica
Follow this link for the steps to setup local replica
- [Setup MongoDB local replica](https://gist.github.com/davisford/bb37079900888c44d2bbcb2c52a5d6e8)

##### Startup MongoDB with local replica
```sh
sudo mongod --replSet {replica_name}
```
`{replica_name}` is the name of the configured local replica

##### connection string in spring application.properties
```sh
spring.data.mongodb.uri=mongodb://127.0.0.1:27017/test?replicaSet={replica_name}
```
`{replica_name}` is the name of the configured local replica
<br/>

### Build, Run and Test
##### Running the unit-test
```sh
./mvnw test
```

##### Running the integration test with SpringRunner
```sh
./mvnw verify
```

##### Execute the integration test with an Embedded MongoDB based on mongod v4.0
- Read [HERE](https://github.com/flapdoodle-oss/de.flapdoodle.embed.mongo/issues/257)

<br/>

### References:
- MongoDB:
    - [Transaction](https://docs.mongodb.com/v4.0/core/transactions/#transactions)
    - [Atomicity and Transaction](https://docs.mongodb.com/manual/core/write-operations-atomicity/)
    - [Concurrency](https://docs.mongodb.com/manual/faq/concurrency/)
    - [Read, Isolation, Consistency and Recency](https://docs.mongodb.com/manual/core/read-isolation-consistency-recency/#read-isolation-consistency-and-recency)
    - [Perform 2 Phase Commit before v4](https://docs.mongodb.com/v3.6/tutorial/perform-two-phase-commits/)
    - [Java Driver Quick Start](https://mongodb.github.io/mongo-java-driver/3.4/driver/getting-started/quick-start/)
    - [Setup MongoDB local replica](https://gist.github.com/davisford/bb37079900888c44d2bbcb2c52a5d6e8)
    
- Spring:
    - [Hands on MongoDB 4.0 transactions with Spring Data](https://spring.io/blog/2018/06/28/hands-on-mongodb-4-0-transactions-with-spring-data)
    - [Transactions with MongoTransactionManager](https://docs.spring.io/spring-data/mongodb/docs/2.2.0.RELEASE/reference/html/#mongo.transactions.tx-manager)
    - [Transactions with Transaction Template](https://docs.spring.io/spring-data/mongodb/docs/2.2.0.RELEASE/reference/html/#mongo.transactions.transaction-template)
    - [Spring Data MongoDB - Transaction Sample](https://github.com/spring-projects/spring-data-examples/tree/master/mongodb/transactions)
    
- Stackoverflow:
    - [How MongoDB deals with transaction conflicts during concurrent update?](https://stackoverflow.com/a/53220202/12364493)
    - [MongoDB Concurrent Updates Behavior](https://stackoverflow.com/questions/56713196/spring-data-mongodb-concurrent-updates-behavior)
    - [Optimistic and Pessimistic Locking](https://stackoverflow.com/a/58952004/12364493)    