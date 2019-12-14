package com.example.jeffrey.demospringdatamongo.service;

import com.example.jeffrey.demospringdatamongo.config.DemoMongoDbConfig;
import com.example.jeffrey.demospringdatamongo.model.Account;
import com.example.jeffrey.demospringdatamongo.repository.AccountRepository;
import com.mongodb.MongoException;
import com.mongodb.client.ClientSession;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.MongoTransactionManager;
import org.springframework.data.mongodb.SessionSynchronization;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.concurrent.atomic.AtomicInteger;

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Updates.inc;

public class AccountService {
    private static final Logger LOGGER = LoggerFactory.getLogger(AccountService.class);

    @Autowired
    DemoMongoDbConfig dbConfig;

    @Autowired
    MongoTemplate mongoTemplate;

    @Autowired
    AccountRepository accountRepository;

    @Autowired
    MongoTransactionManager mongoTransactionManager;

    AtomicInteger counter = new AtomicInteger();

    /**
     * Does not support concurrent update triggered from distributed nodes
     * since it involve 2 separate operation, even though each operation
     * performs the read and write atomically, they are not bounded in
     * a session or coordinated in any ways.
     *
     * Without using any transaction manager or session to coordinate the operations
     *
     * - No automatic rollback if crash happen between the operations
     *
     * @param amount
     * @param debitAccountNumber
     * @param creditAccountNumber
     */
    public void executeTransfer(long amount, String debitAccountNumber, String creditAccountNumber) {
        mongoTemplate.findAndModify(
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

        mongoTemplate.findAndModify(
                Query.query(Criteria.where("accountNumber").is(creditAccountNumber)),
                new Update().inc("accountBalance", +amount),
                new FindAndModifyOptions().returnNew(true),
                Account.class
        );

        LOGGER.debug("account: {}, balance: {}", debitAccountNumber, accountRepository.findByAccountNumber(debitAccountNumber).accountBalance);
    }

    /**
     * Support concurrent update triggered from distributed nodes
     *
     * Use Spring managed transaction features. The MongoTransactionManager binds a ClientSession to the thread.
     * MongoTemplate detects the session and operates on these resources which are associated with the transaction
     * accordingly.
     *
     * - No loss-update, but will encounter write-conflict modifying same document
     * - Automatic rollback if crash before commit
     * - Automatic abort if exception
     * - Manual retry to handle error (can only be made on the caller due to AOP)
     *
     * @param amount
     * @param debitAccountNumber
     * @param creditAccountNumber
     */
    @Transactional
    public void executeTransferTx_withMongoRepository(long amount, String debitAccountNumber, String creditAccountNumber) {
        Account debitAccount = accountRepository.findByAccountNumber(debitAccountNumber);
        debitAccount.accountBalance -= amount;
        accountRepository.save(debitAccount);

        Account creditAccount = accountRepository.findByAccountNumber(creditAccountNumber);
        creditAccount.accountBalance += amount;
        accountRepository.save(creditAccount);

        // simulate exception happen
        int currentCount = counter.incrementAndGet();
        if (currentCount == 2) {
            throw new RuntimeException("interrupt!");
        }

        LOGGER.debug("debit account: {}, balance: {}", debitAccountNumber, accountRepository.findByAccountNumber(debitAccountNumber).accountBalance);
        LOGGER.debug("credit account: {}, balance: {}", creditAccountNumber, accountRepository.findByAccountNumber(creditAccountNumber).accountBalance);
    }

    /**
     * Support concurrent update triggered from distributed nodes
     *
     * Use Spring managed transaction features. The MongoTransactionManager binds a ClientSession to the thread.
     * MongoTemplate detects the session and operates on these resources which are associated with the transaction
     * accordingly.
     *
     * - No loss-update, but will encounter write-conflict modifying same document
     * - Automatic rollback if crash before commit
     * - Automatic abort if exception
     * - Manual retry to handle error (can only be made on the caller due to AOP)
     *
     * @param amount
     * @param debitAccountNumber
     * @param creditAccountNumber
     */
    @Transactional
    public void executeTransferTx_withMongoTemplate(long amount, String debitAccountNumber, String creditAccountNumber) {
        Account debitAccount = mongoTemplate.find(Query.query(Criteria.where("accountNumber").is(debitAccountNumber)), Account.class).get(0);
        debitAccount.accountBalance -= amount;
        mongoTemplate.save(debitAccount);

        Account creditAccount = mongoTemplate.find(Query.query(Criteria.where("accountNumber").is(creditAccountNumber)), Account.class).get(0);
        creditAccount.accountBalance += amount;
        mongoTemplate.save(creditAccount);

        // simulate exception happen, both the debit and credit should be able to rollback
        int currentCount = counter.incrementAndGet();
        if (currentCount == 2) {
            throw new RuntimeException("interrupt!");
        }

        LOGGER.debug("debit account: {}, balance: {}", debitAccountNumber, accountRepository.findByAccountNumber(debitAccountNumber).accountBalance);
        LOGGER.debug("credit account: {}, balance: {}", creditAccountNumber, accountRepository.findByAccountNumber(creditAccountNumber).accountBalance);
    }

    /**
     * Support concurrent update triggered from distributed nodes
     *
     * Use Spring managed transaction features. The MongoTransactionManager binds a ClientSession to the thread.
     * MongoTemplate detects the session and operates on these resources which are associated with the transaction
     * accordingly.
     *
     * - No loss-update, but will encounter write-conflict modifying same document
     * - Automatic rollback if crash before commit
     * - Automatic abort if exception
     * - Manual retry to handle error (can only be made on the caller due to AOP)
     *
     * @param amount
     * @param debitAccountNumber
     * @param creditAccountNumber
     */
    @Transactional
    public void executeTransferTx_withMongoTemplate_atomicReadWrite(long amount, String debitAccountNumber, String creditAccountNumber) {
        mongoTemplate
                .update(Account.class)
                .matching(Query.query(Criteria.where("accountNumber").is(debitAccountNumber)))
                .apply(new Update().inc("accountBalance", -amount))
                .first();

        mongoTemplate
                .update(Account.class)
                .matching(Query.query(Criteria.where("accountNumber").is(creditAccountNumber)))
                .apply(new Update().inc("accountBalance", +amount))
                .first();

        // simulate exception happen, both the debit and credit should be able to rollback
        int currentCount = counter.incrementAndGet();
        if (currentCount == 2) {
            throw new RuntimeException("interrupt!");
        }

        LOGGER.debug("debit account: {}, balance: {}", debitAccountNumber, accountRepository.findByAccountNumber(debitAccountNumber).accountBalance);
        LOGGER.debug("credit account: {}, balance: {}", creditAccountNumber, accountRepository.findByAccountNumber(creditAccountNumber).accountBalance);
    }

    /**
     * Support concurrent update triggered from distributed nodes
     *
     * Use MongoTransactionManager and setSessionSynchronization(ALWAYS) to
     * participating MongoTemplate in managed transactions
     *
     * - No loss-update, but will encounter write-conflict modifying same document
     * - Automatic rollback if crash before commit
     * - Automatic abort if exception
     * - Manual retry to handle error
     *
     * @param amount
     * @param debitAccountNumber
     * @param creditAccountNumber
     */
    public void executeTransferTx_withTransactionTemplate(long amount, String debitAccountNumber, String creditAccountNumber) {
        final int maxAttempt = 10;
        int attempt = 1;

        while (true) {
            if (attempt >= maxAttempt) {
                break;
            } else {
                LOGGER.debug("attempt-{}: {}", attempt, Thread.currentThread().getName());
                attempt++;
            }

            mongoTemplate.setSessionSynchronization(SessionSynchronization.ALWAYS);

            try {
                LOGGER.debug("start tx: {}", Thread.currentThread().getName());

                TransactionTemplate txTemplate = new TransactionTemplate(mongoTransactionManager);
                txTemplate.execute(new TransactionCallbackWithoutResult() {
                    @Override
                    protected void doInTransactionWithoutResult(TransactionStatus status) {
                        Account debitAccount = mongoTemplate.find(Query.query(Criteria.where("accountNumber").is(debitAccountNumber)), Account.class).get(0);
                        debitAccount.accountBalance -= amount;
                        mongoTemplate.save(debitAccount);

                        Account creditAccount = mongoTemplate.find(Query.query(Criteria.where("accountNumber").is(creditAccountNumber)), Account.class).get(0);
                        creditAccount.accountBalance += amount;
                        mongoTemplate.save(creditAccount);

                        // simulate exception happen, both the debit and credit should be able to rollback
                        int currentCount = counter.incrementAndGet();
                        if (currentCount == 2) {
                            throw new RuntimeException("interrupt!");
                        }

                        LOGGER.debug("debit account: {}, balance: {}", debitAccountNumber, accountRepository.findByAccountNumber(debitAccountNumber).accountBalance);
                        LOGGER.debug("credit account: {}, balance: {}", creditAccountNumber, accountRepository.findByAccountNumber(creditAccountNumber).accountBalance);
                    }
                });

                LOGGER.debug("commit tx: {}", Thread.currentThread().getName());
                break;

            } catch (RuntimeException e) {
                LOGGER.debug("abort tx: {}", Thread.currentThread().getName());

                if (e.getCause() instanceof MongoException) {
                    MongoException mongoException = (MongoException) e.getCause();
                    // error is a transient commit error, can retry commit
                    if (mongoException.hasErrorLabel(MongoException.TRANSIENT_TRANSACTION_ERROR_LABEL)) {
                        LOGGER.error("retrying commit operation: {} ... {}", Thread.currentThread().getName(), e.getMessage());
                        continue;

                    } else {
                        LOGGER.error("error not retryable: {}", e.getMessage());
                        throw e;
                    }

                } else {
                    throw e;
                }
            }
        }
    }

    /**
     * Support concurrent update triggered from distributed nodes
     *
     * Use client session to coordinate operations
     *
     * - No loss-update, but will encounter write-conflict modifying same document
     * - Automatic rollback if crash before commit
     * - Automatic abort if exception
     * - Automatic retry if error is transient
     *
     * @param amount
     * @param debitAccountNumber
     * @param creditAccountNumber
     */
    public void executeTransferTx_withSession_AutoRetry(long amount, String debitAccountNumber, String creditAccountNumber) {
        MongoClient client = dbConfig.mongoClient();
        String dbName = dbConfig.mongoDbFactory().getDb().getName();

        // client session should be short-lived and released once no longer needed
        try (ClientSession session = client.startSession()) {
            LOGGER.debug("start tx: {}", Thread.currentThread().getName());

            /**
             * Runs a provided lambda within a transaction, retrying either the commit operation
             * or entire transaction as needed (and when the error permits) to better ensure that
             * the transaction can complete successfully.
             */
            session.withTransaction(() -> {
                MongoCollection<Document> collection = client.getDatabase(dbName).getCollection("AccountCollection");
                collection.updateOne(session, eq("accountNumber", debitAccountNumber), inc("accountBalance", -amount));
                collection.updateOne(session, eq("accountNumber", creditAccountNumber), inc("accountBalance", +amount));

                // simulate exception happen, both the debit and credit should be able to rollback
                int currentCount = counter.incrementAndGet();
                if (currentCount == 2) {
                    throw new RuntimeException("interrupt!");
                }

                LOGGER.debug("commit tx: {}", Thread.currentThread().getName());

                Document debitAccount = collection.find(session, eq("accountNumber", debitAccountNumber)).first();
                Document creditAccount = collection.find(session, eq("accountNumber", creditAccountNumber)).first();
                LOGGER.debug("debit account: {} balance: {}", debitAccount.get("accountNumber"), debitAccount.get("accountBalance"));
                LOGGER.debug("credit account: {} balance: {}", creditAccount.get("accountNumber"), creditAccount.get("accountBalance"));
                return debitAccount;
            });

        } catch (RuntimeException e) {
            LOGGER.debug("abort tx: {}", Thread.currentThread().getName());
            throw e;
        }
    }

    /**
     * Support concurrent update triggered from distributed nodes
     *
     * Use client session to coordinate operations
     *
     * - No loss-update, but concurrent write will encounter write-conflict modifying same document
     * - Automatic rollback if crash before commit
     * - Manual abort to handle exception
     * - Manual retry to handle error
     *
     * @param amount
     * @param debitAccountNumber
     * @param creditAccountNumber
     */
    public void executeTransferTx_withSession_ManualRetry(long amount, String debitAccountNumber, String creditAccountNumber) {
        MongoClient client = dbConfig.mongoClient();
        String dbName = dbConfig.mongoDbFactory().getDb().getName();

//        TransactionOptions txnOptions = TransactionOptions.builder()
//                .readPreference(ReadPreference.primary())
//                .readConcern(ReadConcern.MAJORITY)
//                .writeConcern(WriteConcern.MAJORITY)
//                .build();

        // client session should be short-lived and released once no longer needed
        try (ClientSession session = client.startSession()) {
            final int maxAttempt = 10;
            int attempt = 1;

            while (true) {
                if (attempt >= maxAttempt) {
                    break;
                } else {
                    LOGGER.debug("attempt-{}: {}", attempt, Thread.currentThread().getName());
                    attempt++;
                }

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
                    MongoCollection<Document> collection = client.getDatabase(dbName).getCollection("AccountCollection");
                    collection.updateOne(session, eq("accountNumber", debitAccountNumber), inc("accountBalance", -amount));
                    collection.updateOne(session, eq("accountNumber", creditAccountNumber), inc("accountBalance", +amount));

                    // simulate exception happen, both the debit and credit should be able to rollback
                    int currentCount = counter.incrementAndGet();
                    if (currentCount == 2) {
                        throw new RuntimeException("interrupt!");
                    }

                    Document debitAccount = collection.find(session, eq("accountNumber", debitAccountNumber)).first();
                    Document creditAccount = collection.find(session, eq("accountNumber", creditAccountNumber)).first();
                    LOGGER.debug("debit account: {} balance: {}", debitAccount.get("accountNumber"), debitAccount.get("accountBalance"));
                    LOGGER.debug("credit account: {} balance: {}", creditAccount.get("accountNumber"), creditAccount.get("accountBalance"));

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

}
