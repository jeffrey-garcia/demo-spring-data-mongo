package com.example.jeffrey.demospringdatamongo.service;

import com.example.jeffrey.demospringdatamongo.config.DemoDbConfig;
import com.example.jeffrey.demospringdatamongo.model.Account;
import com.example.jeffrey.demospringdatamongo.repository.AccountRepository;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@RunWith(SpringRunner.class)
@DataMongoTest
@Import({DemoDbConfig.class, AccountService.class})
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class AccountServiceIT {
    private static final Logger LOGGER = LoggerFactory.getLogger(AccountServiceIT.class);

    @Autowired
    AccountRepository accountRepository;

    @Autowired
    AccountService accountService;

    String accountNumberToDebit;
    String accountNumberToCredit;
    long transferAmount = 10L;

    @Before
    public void setUp() {
        accountRepository.deleteAll();
        accountRepository.save(new Account());
        accountRepository.save(new Account());

        List<Account> accounts = accountRepository.findAll();
        accountNumberToDebit = accounts.get(0).accountNumber;
        accountNumberToCredit = accounts.get(1).accountNumber;
    }

// TEST 1

    @Test
    public void test_executeTransfer_withTransactionTemplate_runOneTransfer() {
        runOneTransfer(() -> {
            accountService.executeTransfer(transferAmount, accountNumberToDebit, accountNumberToCredit);
        });
    }

    @Test(expected = AssertionError.class)
    public void test_executeTransfer_withTransactionTemplate_runMultipleTransfer() {
        runMultipleTransfer(() -> {
            accountService.executeTransfer(transferAmount, accountNumberToDebit, accountNumberToCredit);
        });
    }

    @Test(expected = AssertionError.class)
    public void test_executeTransfer_withTransactionTemplate_runMultipleTransferConcurrently() {
        runMultipleTransferConcurrently(() -> {
            accountService.executeTransfer(transferAmount, accountNumberToDebit, accountNumberToCredit);
        });
    }

// TEST 2

    @Test
    public void test_executeTransferTx_withMongoRepository_runOneTransfer() {
        runOneTransfer(() -> {
            accountService.executeTransferTx_withMongoRepository(transferAmount, accountNumberToDebit, accountNumberToCredit);
        });
    }

    @Test
    public void test_executeTransferTx_withMongoRepository_runMultipleTransfer() {
        runMultipleTransfer(() -> {
            accountService.executeTransferTx_withMongoRepository(transferAmount, accountNumberToDebit, accountNumberToCredit);
        });
    }

    @Test(expected = AssertionError.class)
    public void test_executeTransferTx_withMongoRepository_runMultipleTransferConcurrently() {
        runMultipleTransferConcurrently(() -> {
            accountService.executeTransferTx_withMongoRepository(transferAmount, accountNumberToDebit, accountNumberToCredit);
        });
    }

// TEST 3

    @Test
    public void test_executeTransferTx_withMongoTemplate_runOneTransfer() {
        runOneTransfer(() -> {
            accountService.executeTransferTx_withMongoTemplate(transferAmount, accountNumberToDebit, accountNumberToCredit);
        });
    }

    @Test
    public void test_executeTransferTx_withMongoTemplate_runMultipleTransfer() {
        runMultipleTransfer(() -> {
            accountService.executeTransferTx_withMongoTemplate(transferAmount, accountNumberToDebit, accountNumberToCredit);
        });
    }

    @Test(expected = AssertionError.class)
    public void test_executeTransferTx_withMongoTemplate_runMultipleTransferConcurrently() {
        runMultipleTransferConcurrently(() -> {
            accountService.executeTransferTx_withMongoTemplate(transferAmount, accountNumberToDebit, accountNumberToCredit);
        });
    }

// TEST 4

    @Test
    public void test_executeTransferTx_withMongoTemplate_atomicReadWrite_runOneTransfer() {
        runOneTransfer(() -> {
            accountService.executeTransferTx_withMongoTemplate_atomicReadWrite(transferAmount, accountNumberToDebit, accountNumberToCredit);
        });
    }

    @Test
    public void test_executeTransferTx_withMongoTemplate_atomicReadWrite_runMultipleTransfer() {
        runMultipleTransfer(() -> {
            accountService.executeTransferTx_withMongoTemplate_atomicReadWrite(transferAmount, accountNumberToDebit, accountNumberToCredit);
        });
    }

    @Test(expected = AssertionError.class)
    public void test_executeTransferTx_withMongoTemplate_atomicReadWrite_runMultipleTransferConcurrently() {
        runMultipleTransferConcurrently(() -> {
            accountService.executeTransferTx_withMongoTemplate_atomicReadWrite(transferAmount, accountNumberToDebit, accountNumberToCredit);
        });
    }

// TEST 5

    @Test
    public void test_executeTransferTx_withTransactionTemplate_runOneTransfer() {
        runOneTransfer(() -> {
            accountService.executeTransferTx_withTransactionTemplate(transferAmount, accountNumberToDebit, accountNumberToCredit);
        });
    }

    @Test
    public void test_executeTransferTx_withTransactionTemplate_runMultipleTransfer() {
        runMultipleTransfer(() -> {
            accountService.executeTransferTx_withTransactionTemplate(transferAmount, accountNumberToDebit, accountNumberToCredit);
        });
    }

    @Test
    public void test_executeTransferTx_withTransactionTemplate_runMultipleTransferConcurrently() {
        runMultipleTransferConcurrently(() -> {
            accountService.executeTransferTx_withTransactionTemplate(transferAmount, accountNumberToDebit, accountNumberToCredit);
        });
    }

// TEST 6

    @Test
    public void test_executeTransferTx_withSession_AutoRetry_runOneTransfer() {
        runOneTransfer(() -> {
            accountService.executeTransferTx_withSession_AutoRetry(transferAmount, accountNumberToDebit, accountNumberToCredit);
        });
    }

    @Test
    public void test_executeTransferTx_withSession_AutoRetry_runMultipleTransfer() {
        runMultipleTransfer(() -> {
            accountService.executeTransferTx_withSession_AutoRetry(transferAmount, accountNumberToDebit, accountNumberToCredit);
        });
    }

    @Test
    public void test_executeTransferTx_withSession_AutoRetry_runMultipleTransferConcurrently() {
        runMultipleTransferConcurrently(() -> {
            accountService.executeTransferTx_withSession_AutoRetry(transferAmount, accountNumberToDebit, accountNumberToCredit);
        });
    }

// TEST 7

    @Test
    public void test_executeTransferTx_withSession_ManualRetry_runOneTransfer() {
        runOneTransfer(() -> {
            accountService.executeTransferTx_withSession_ManualRetry(transferAmount, accountNumberToDebit, accountNumberToCredit);
        });
    }

    @Test
    public void test_executeTransferTx_withSession_ManualRetry_runMultipleTransfer() {
        runMultipleTransfer(() -> {
            accountService.executeTransferTx_withSession_ManualRetry(transferAmount, accountNumberToDebit, accountNumberToCredit);
        });
    }

    @Test
    public void test_executeTransferTx_withSession_ManualRetry_runMultipleTransferConcurrently() {
        runMultipleTransferConcurrently(() -> {
            accountService.executeTransferTx_withSession_ManualRetry(transferAmount, accountNumberToDebit, accountNumberToCredit);
        });
    }

    protected void runOneTransfer(TransferCommand command) {
        List<Account> accounts = accountRepository.findAll();
        String accountNumberToDebit = accounts.get(0).accountNumber;
        String accountNumberToCredit = accounts.get(1).accountNumber;

        try {
            command.execute();
        } catch (RuntimeException e) {}

        Assert.assertEquals(
                Long.valueOf(90L).longValue(),
                accountRepository.findByAccountNumber(accountNumberToDebit).accountBalance.longValue());
        Assert.assertEquals(
                Long.valueOf(110L).longValue(),
                accountRepository.findByAccountNumber(accountNumberToCredit).accountBalance.longValue());
    }

    protected void runMultipleTransfer(TransferCommand command) throws AssertionError {
        List<Account> accounts = accountRepository.findAll();
        String accountNumberToDebit = accounts.get(0).accountNumber;
        String accountNumberToCredit = accounts.get(1).accountNumber;

        final int MAX = 4;

        for (int i=0; i<MAX; i++) {
            try {
                command.execute();
            } catch (RuntimeException e) {
                LOGGER.error("error during debit: {}", e.getMessage());
            } catch (Throwable t) {}
        }

        Assert.assertEquals(
                Long.valueOf(70L).longValue(),
                accountRepository.findByAccountNumber(accountNumberToDebit).accountBalance.longValue());
        Assert.assertEquals(
                Long.valueOf(130L).longValue(),
                accountRepository.findByAccountNumber(accountNumberToCredit).accountBalance.longValue());
    }

    protected void runMultipleTransferConcurrently(TransferCommand command) throws AssertionError {
        List<Account> accounts = accountRepository.findAll();
        String accountNumberToDebit = accounts.get(0).accountNumber;
        String accountNumberToCredit = accounts.get(1).accountNumber;

        final int MAX_THREAD = 4;
        final CountDownLatch lock = new CountDownLatch(MAX_THREAD);
        final Executor executor = Executors.newFixedThreadPool(MAX_THREAD);

        for (int i=0; i<MAX_THREAD; i++) {
            executor.execute(() -> {
                try {
                    command.execute();
                } catch (RuntimeException e) {
                    LOGGER.error("error during debit: {}", e.getMessage());
                }
                lock.countDown();
            });
        }

        try {
            lock.await();
        } catch (InterruptedException e) { }

        Assert.assertEquals(
                Long.valueOf(70L).longValue(),
                accountRepository.findByAccountNumber(accountNumberToDebit).accountBalance.longValue());
        Assert.assertEquals(
                Long.valueOf(130L).longValue(),
                accountRepository.findByAccountNumber(accountNumberToCredit).accountBalance.longValue());
    }
}

@FunctionalInterface
interface TransferCommand {
    void execute() throws RuntimeException;
}
