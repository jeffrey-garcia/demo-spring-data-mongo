package com.example.jeffrey.demospringdatamongo.service;

import com.example.jeffrey.demospringdatamongo.config.DemoDbConfig;
import com.example.jeffrey.demospringdatamongo.model.Account;
import com.example.jeffrey.demospringdatamongo.repository.AccountRepository;
import com.mongodb.MongoException;
import com.mongodb.client.ClientSession;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.result.UpdateResult;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.atomic.AtomicInteger;

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Updates.inc;

public class AccountService {
    private static final Logger LOGGER = LoggerFactory.getLogger(AccountService.class);

    @Autowired
    DemoDbConfig dbConfig;

    @Autowired
    MongoTemplate mongoTemplate;

    @Autowired
    AccountRepository accountRepository;

    @Autowired
    MongoOperations operations; //MongoOperations will handle a mongo session

    AtomicInteger counter = new AtomicInteger();

    public void debit(long amount, String debitAccountNumber, String creditAccountNumber) {
//        Account debitAccount = accountRepository.findByAccountNumber(debitAccountNumber);
//        Account creditAccount = accountRepository.findByAccountNumber(creditAccountNumber);
//        debitAccount.accountBalance -= amount;
//        creditAccount.accountBalance += amount;
//        accountRepository.save(debitAccount);
//        accountRepository.save(creditAccount);

        mongoTemplate.findAndModify(
                Query.query(Criteria.where("accountNumber").is(debitAccountNumber)),
                new Update().inc("accountBalance", -amount),
                new FindAndModifyOptions().returnNew(true),
                Account.class
        );

        LOGGER.debug("account: {}, balance: {}", debitAccountNumber, accountRepository.findByAccountNumber(debitAccountNumber).accountBalance);
    }

    public void debitTx(long amount, String debitAccountNumber, String creditAccountNumber) {
//        debit0Tx(amount, debitAccountNumber, creditAccountNumber);
//        debit1Tx(amount, debitAccountNumber, creditAccountNumber);
//        debit2Tx(amount, debitAccountNumber, creditAccountNumber);
//        debit3Tx(amount, debitAccountNumber, creditAccountNumber);
        debit4Tx(amount, debitAccountNumber, creditAccountNumber);
//        debit5Tx(amount, debitAccountNumber, creditAccountNumber);
//        debit6Tx(amount, debitAccountNumber, creditAccountNumber);
    }

    /**
     * Query and Update is not atomic operation (transaction has no effect)
     * Concurrent update will encounter lost-update
     * Can't rollback if error/exception occurred
     * @param amount
     * @param debitAccountNumber
     * @param creditAccountNumber
     */
    @Transactional
    public void debit0Tx(long amount, String debitAccountNumber, String creditAccountNumber) {
        Account debitAccount = accountRepository.findByAccountNumber(debitAccountNumber);
        debitAccount.accountBalance -= 10;
        accountRepository.save(debitAccount);

        // simulate exception happen
//        int currentCount = counter.incrementAndGet();
//        if (currentCount == 2) {
//            throw new RuntimeException("interrupt!");
//        }

        LOGGER.debug("account: {}, balance: {}", debitAccountNumber, accountRepository.findByAccountNumber(debitAccountNumber).accountBalance);
    }

    /**
     * Query and Update is atomic operation (transaction is not required)
     * Can't rollback if error/exception occurred
     * @param amount
     * @param debitAccountNumber
     * @param creditAccountNumber
     */
    @Transactional
    public void debit1Tx(long amount, String debitAccountNumber, String creditAccountNumber) {
        // read and write is an atomic operation
        operations.findAndModify(
                Query.query(Criteria.where("accountNumber").is(debitAccountNumber)),
                new Update().inc("accountBalance", -amount),
                new FindAndModifyOptions().returnNew(true),
                Account.class
        );

        // simulate exception happen
        int currentCount = counter.incrementAndGet();
        if (currentCount == 2) {
            throw new RuntimeException("interrupt!");
        }

        LOGGER.debug("account: {}, balance: {}", debitAccountNumber, accountRepository.findByAccountNumber(debitAccountNumber).accountBalance);
    }

    /**
     * Query and Update is atomic operation (transaction is not required)
     * Can't rollback if error/exception occurred
     * @param amount
     * @param debitAccountNumber
     * @param creditAccountNumber
     */
    public void debit2Tx(long amount, String debitAccountNumber, String creditAccountNumber) {
        operations
            .update(Account.class)
            .matching(Query.query(Criteria.where("accountNumber").is(debitAccountNumber)))
            .apply(new Update().inc("accountBalance", -amount))
            .first();

        // simulate exception happen
        int currentCount = counter.incrementAndGet();
        if (currentCount == 2) {
            throw new RuntimeException("interrupt!");
        }

        LOGGER.debug("account: {}, balance: {}", debitAccountNumber, accountRepository.findByAccountNumber(debitAccountNumber).accountBalance);
    }

    /**
     * Query and Update is atomic operation (transaction is not required)
     * Can't rollback if error/exception occurred
     * @param amount
     * @param debitAccountNumber
     * @param creditAccountNumber
     */
    public void debit3Tx(long amount, String debitAccountNumber, String creditAccountNumber) {
        MongoCollection<Document> collection = dbConfig.mongoClient().getDatabase("test").getCollection("AccountCollection");
        UpdateResult result = collection.updateOne(eq("accountNumber", debitAccountNumber), inc("accountBalance", -10));
        LOGGER.debug("updated document count: {}", result.getModifiedCount());

        // simulate exception happen
        int currentCount = counter.incrementAndGet();
        if (currentCount == 2) {
            throw new RuntimeException("interrupt!");
        }

        LOGGER.debug("account: {}, balance: {}", debitAccountNumber, accountRepository.findByAccountNumber(debitAccountNumber).accountBalance);
    }

    /**
     * Concurrent update is executed sequentially without write-conflict and lost-update
     * Automatic retry/rollback if error/exception occurred
     * Does not require @Transaction and @EnableTransactionManagement annotation
     * Works only when MongoDB started with replica set
     * @param amount
     * @param debitAccountNumber
     * @param creditAccountNumber
     */
    public void debit4Tx(long amount, String debitAccountNumber, String creditAccountNumber) {
        MongoClient client = dbConfig.mongoClient();

        try (ClientSession session = client.startSession())
        {
            LOGGER.debug("start tx: {}", Thread.currentThread().getName());

            /**
             * Runs a provided lambda within a transaction, retrying either the commit operation
             * or entire transaction as needed (and when the error permits) to better ensure that
             * the transaction can complete successfully.
             */
            session.withTransaction(() -> {
                MongoCollection<Document> collection = client.getDatabase("test").getCollection("AccountCollection");
                UpdateResult result = collection.updateOne(session, eq("accountNumber", debitAccountNumber), inc("accountBalance", -10));
                LOGGER.debug("updated document count: {}", result.getModifiedCount());

                // simulate exception happen
                int currentCount = counter.incrementAndGet();
                if (currentCount == 2) {
                    throw new RuntimeException("interrupt!");
                }

                LOGGER.debug("commit tx: {}", Thread.currentThread().getName());
                Document document = collection.find(session, eq("accountNumber", debitAccountNumber)).first();
                LOGGER.debug("account: {} balance: {}", document.get("accountNumber"), document.get("accountBalance"));

                return document;
            });

        } catch (RuntimeException e) {
            LOGGER.debug("abort tx: {}", Thread.currentThread().getName());
            throw e;
        }
    }

    /**
     * Concurrent update is executed sequentially without write-conflict and lost-update
     * Manual retry/rollback if error/exception occurred
     * Does not require @Transaction and @EnableTransactionManagement annotation
     * Works only when MongoDB started with replica set
     * @param amount
     * @param debitAccountNumber
     * @param creditAccountNumber
     */
    public void debit5Tx(long amount, String debitAccountNumber, String creditAccountNumber) {
        MongoClient client = dbConfig.mongoClient();

//        TransactionOptions txnOptions = TransactionOptions.builder()
//                .readPreference(ReadPreference.primary())
//                .readConcern(ReadConcern.MAJORITY)
//                .writeConcern(WriteConcern.MAJORITY)
//                .build();

        try (ClientSession session = client.startSession()) {
            while (true) {
                LOGGER.debug("start tx: {}", Thread.currentThread().getName());

                // Start a transaction
                /**
                 * The individual write operations inside the transaction are not retryable,
                 * regardless of whether retryWrites is set to true.
                 *
                 * If an operation encounters an error, the returned error may have an errorLabels
                 * array field. If the error is a transient error, the errorLabels array field
                 * contains "TransientTransactionError" as an element and the transaction as
                 * a whole can be retried.
                 */
                session.startTransaction();

                try {
                    // Operations inside the transaction
                    MongoCollection<Document> collection = client.getDatabase("test").getCollection("AccountCollection");
                    UpdateResult result = collection.updateOne(session, eq("accountNumber", debitAccountNumber), inc("accountBalance", -10));
                    LOGGER.debug("updated document count: {}", result.getModifiedCount());

                    // simulate exception happen
                    int currentCount = counter.incrementAndGet();
                    if (currentCount == 2) {
                        throw new RuntimeException("interrupt!");
                    }

                    Document document = collection.find(session, eq("accountNumber", debitAccountNumber)).first();
                    LOGGER.debug("account balance: {}", document.get("accountBalance"));

                    // Commit the transaction using write concern set at transaction start
                    LOGGER.debug("commit tx: {}", Thread.currentThread().getName());
                    session.commitTransaction();

                    break;

                } catch (MongoException e) {
                    LOGGER.debug("abort tx: {}", Thread.currentThread().getName());
                    session.abortTransaction();

                    // error is a transient commit error, can retry commit
                    if (e.hasErrorLabel(MongoException.TRANSIENT_TRANSACTION_ERROR_LABEL)) {
                        LOGGER.error("retrying commit operation: {} ... {}", Thread.currentThread().getName(), e.getMessage());
                        continue;

                    } else {
                        LOGGER.error("error not retryable: {}", e.getMessage());
                        throw e;
                    }
                }
            }
        }
    }

    /**
     * Query and Update is atomic operation (transaction is not required)
     * Manual retry/rollback if error/exception occurred
     * Does not require @Transaction and @EnableTransactionManagement annotation
     * Works only when MongoDB started with replica set
     * @param amount
     * @param debitAccountNumber
     * @param creditAccountNumber
     */
    public void debit6Tx(long amount, String debitAccountNumber, String creditAccountNumber) {
        MongoClient client = dbConfig.mongoClient();

        try (ClientSession session = client.startSession())
        {
            mongoTemplate.withSession(session).execute(action -> {
                LOGGER.debug("start tx: {}", Thread.currentThread().getName());
                session.startTransaction();
                try {
                    // query and update is atomic
                    UpdateResult result = mongoTemplate
                            .update(Account.class)
                            .matching(Query.query(Criteria.where("accountNumber").is(debitAccountNumber)))
                            .apply(new Update().inc("accountBalance", -amount))
                            .first();
                    LOGGER.debug("updated document count: {}", result.getModifiedCount());

                    // simulate exception happen
                    int currentCount = counter.incrementAndGet();
                    if (currentCount == 2) {
                        throw new RuntimeException("interrupt!");
                    }

                    LOGGER.debug("commit tx: {}", Thread.currentThread().getName());
                    session.commitTransaction();

                    return result;

                } catch (RuntimeException e) {
                    LOGGER.debug("abort tx: {}", Thread.currentThread().getName());
                    session.abortTransaction();
                    throw e;
                }
            });
        }
    }

}
