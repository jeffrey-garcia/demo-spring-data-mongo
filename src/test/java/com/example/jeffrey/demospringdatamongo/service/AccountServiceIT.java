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

    @Before
    public void setUp() {
        accountRepository.deleteAll();
        accountRepository.save(new Account());
        accountRepository.save(new Account());
    }

    @Test
    public void loopTest() throws Exception {
        List<Account> accounts = accountRepository.findAll();
        String accountNumberToDebit = accounts.get(0).accountNumber;
        String accountNumberToCredit = accounts.get(1).accountNumber;

        final int MAX_THREAD = 4;
        final CountDownLatch lock = new CountDownLatch(MAX_THREAD);
        final Executor executor = Executors.newFixedThreadPool(MAX_THREAD);

        for (int i=0; i<MAX_THREAD; i++) {
            executor.execute(() -> {
                try {
//                accountService.debit(10L, accountNumberToDebit, accountNumberToCredit);
                    accountService.debitTx(10L, accountNumberToDebit, accountNumberToCredit);
                } catch (RuntimeException e) {
                    LOGGER.error("error during debit: {}", e.getMessage());
                }
                lock.countDown();
            });
        }

        lock.await();

        Assert.assertEquals(
                Long.valueOf(60L).longValue(),
                accountRepository.findByAccountNumber(accountNumberToDebit).accountBalance.longValue());
//        Assert.assertEquals(
//                Long.valueOf(140L).longValue(),
//                accountRepository.findByAccountNumber(accountNumberToCredit).accountBalance.longValue());
    }

    @Test
    public void loopTest2() throws Exception {
        List<Account> accounts = accountRepository.findAll();
        String accountNumberToDebit = accounts.get(0).accountNumber;
        String accountNumberToCredit = accounts.get(1).accountNumber;

        final int MAX = 4;

        for (int i=0; i<MAX; i++) {
            try {
//                accountService.debit(10L, accountNumberToDebit, accountNumberToCredit);
                accountService.debitTx(10L, accountNumberToDebit, accountNumberToCredit);
            } catch (RuntimeException e) {
                LOGGER.error("error during debit: {}", e.getMessage());
            } catch (Throwable t) {}
        }

        Assert.assertEquals(
                Long.valueOf(60L).longValue(),
                accountRepository.findByAccountNumber(accountNumberToDebit).accountBalance.longValue());
//        Assert.assertEquals(
//                Long.valueOf(140L).longValue(),
//                accountRepository.findByAccountNumber(accountNumberToCredit).accountBalance.longValue());
    }

}
