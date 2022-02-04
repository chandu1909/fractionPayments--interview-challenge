package com.fraction.payments.coreService;

import com.fraction.payments.appConfig.ErrorsAndWarnings;
import com.fraction.payments.appConfig.FractionConstants;
import com.fraction.payments.appConfig.RabbitMqConfig;
import com.fraction.payments.entities.Account;
import com.fraction.payments.entities.Transaction;
import com.fraction.payments.entities.TransactionStatus;
import com.fraction.payments.entities.User;
import com.fraction.payments.responseBuilder.ResponseBuilder;
import com.fraction.payments.respository.AccountRespository;
import com.fraction.payments.respository.TransactionRepository;
import com.fraction.payments.respository.UserRepository;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * @Author : Chandra sekhar Polavarapu
 * @Description : This is the implementation for the service interface and holds the entire business logic. Depending on the traffic,
 * The functionality of this class can be divided into parts as separate verticals if required so that they can be scaled separately
 */

@Service
public class UserServiceImpl implements UserService {
    private static final Logger LOGGER = LoggerFactory.getLogger(UserServiceImpl.class);
    private static final Logger ELOGGER = LoggerFactory.getLogger(ErrorsAndWarnings.class);

    @Autowired
    UserRepository userRepository;
    @Autowired
    AccountRespository accountRespository;
    @Autowired
    TransactionRepository transactionRepository;
    @Autowired
    ResponseBuilder responseBuilder;
    @Autowired
    RabbitTemplate rabbitTemplate;

    @Override
    public User execute(User user) throws Exception {

        LOGGER.info(" user data has been successfully added to DB");
        return userRepository.save(user);
    }

    /**
     * @param userID
     * @return
     * @Description: This method checks if the account balance is already available for the user accounts and returns it. If
     * Not available / 0, it calculates the balance and updates the database Asynchronously.
     */
    @Override
    public ResponseEntity<JSONObject> getBalance(String userID) {
        UUID userAccount = UUID.fromString(userID);
        JSONObject responseJson = new JSONObject();
        try {

            Optional<User> userResponse = userRepository.findById(userAccount);

            //check if user exists in the database first
            if (userResponse.isEmpty()) {
                LOGGER.error("Invalid User ID : {}", userID);
                responseJson = responseBuilder.blanketTransactionFailed();
                responseJson.put(FractionConstants.REASON_FOR_FAILURE, "Invalid User : " + userID);
                return new ResponseEntity<>(responseJson, HttpStatus.EXPECTATION_FAILED);

            }
            Set<Account> taggedAccounts = userResponse.get().getAccounts();

            int accountCount = 1;
            for (Account account : taggedAccounts) {

                double accountBalance = account.getAccountBalance();

                if (accountBalance != 0) { //re-evaluate for the balance if the value is 0 just to make sure right value is being given
                    responseJson.put("Account #" + accountCount, createBalanceResponse(account.getPrimaryAccountNumber(), accountBalance, account.getCreditLimit() - accountBalance, account.getCreditLimit()));
                } else {
                    Set<Transaction> transactions = account.getTransactions();
                    responseJson.put("Account #" + accountCount, auditAccount(transactions, account));
                    //AuditAccount method also updates database Asynchronously
                }
                accountCount++;
            }

        } catch (Exception e) {
            LOGGER.error("Exception occurred while while processing the get-balance request: {}", e.getMessage());
            responseJson = responseBuilder.blanketTransactionFailed();
            responseJson.put(FractionConstants.REASON_FOR_FAILURE, "INTERNAL SYSTEM ERROR");
            return new ResponseEntity<>(responseJson, HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<>(responseJson, HttpStatus.OK);
    }

    /**
     * @param transaction
     * @return
     * @Description: This method accepts incoming transaction if the account is tagged to incoming user ID
     * Throws error if not found ! Withdrawn will be performed if funds available and transaction is not duplicate (idempotency-key)
     * and new balance will be updated to DB.
     * @Notes: Processing partial amounts is possible but usually not recommended and DENIAL is necessary. Typically, retry is required with correction from customer (eg: ATM)
     */

    @Override
    public ResponseEntity<JSONObject> drawFundsFromAccount(JSONObject transaction) {

        JSONObject responseJson;
        try {
            UUID userAccount = UUID.fromString(transaction.get(FractionConstants.USERID).toString());
            //check if the user exists in DB
            Optional<User> userObject = userRepository.findById(userAccount);

            //Deny transaction if user is Invalid- Do not proceed further
            if (userObject.isEmpty()) {
                LOGGER.error("Invalid User ID : {}", userAccount);
                responseJson = responseBuilder.blanketTransactionFailed();
                responseJson.put(FractionConstants.REASON_FOR_FAILURE, "Invalid User : " + userAccount);
                responseJson.put(FractionConstants.STATUS, "DENY");
                return new ResponseEntity<>(responseJson, HttpStatus.EXPECTATION_FAILED);
            }

            Set<Account> userAccounts = userObject.get().getAccounts();

            for (Account account : userAccounts) {
                if (account.getPrimaryAccountNumber().toString().equals(transaction.get(FractionConstants.PAN))) {
                    LOGGER.debug("Account found !!");
                    double debitAmount = (Double) transaction.get(FractionConstants.DEBIT);
                    double fundAvaialable = account.getCreditLimit() - account.getAccountBalance();

                    //check if available funds available is more than requested amount
                    if (debitAmount <= fundAvaialable) {

                        //update transaction table with new transaction
                        Transaction transactionObj = new Transaction();
                        transactionObj.setAccount(account);
                        transactionObj.setDebit(debitAmount);
                        Transaction transactionResult = transactionRepository.save(transactionObj);

                        double balance = account.getAccountBalance() + debitAmount;
                        TransactionStatus notificationPayload = new TransactionStatus(transactionResult, "SUCCESS", "Debit transaction successful");


                        //update new balance to accounts table asynchronusly and publish to RabbitMQ
                        CompletableFuture.runAsync(() -> {
                            try {
                                updateBalance(account, balance);
                                publishToRabbit(notificationPayload);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        });

                        return new ResponseEntity<>(responseBuilder.transactionSuccess(), HttpStatus.OK);
                    } else {
                        LOGGER.info("Insufficient Funds to withdraw !");
                        responseJson = responseBuilder.blanketTransactionFailed();
                        responseJson.put(FractionConstants.REASON_FOR_FAILURE, "Insufficient Funds- Try Less than: " + fundAvaialable);
                        return new ResponseEntity<>(responseJson, HttpStatus.PRECONDITION_FAILED);
                    }
                }
            }

        } catch (Exception e) {
            LOGGER.error("Exception occurred while processing Debit transaction : {}", e.getMessage());
            responseJson = responseBuilder.blanketTransactionFailed();
            responseJson.put(FractionConstants.REASON_FOR_FAILURE, "INTERNAL SYSTEM ERROR");
            return new ResponseEntity<>(responseJson, HttpStatus.INTERNAL_SERVER_ERROR);
        }
        responseJson = responseBuilder.blanketTransactionFailed();
        responseJson.put(FractionConstants.REASON_FOR_FAILURE, "Incorrect Account Number / Account Not owner by the user");

        return new ResponseEntity<>(responseJson, HttpStatus.EXPECTATION_FAILED);

    }

    /**
     * @param transaction
     * @return
     * @Descrition: This method processes incoming transaction if the account is tagged to incoming user ID
     * Throws error if not found ! Credit operation will be performed if the balance is more or equal to incoming amount. if not DENY and
     * publish notification to RabbitMQ
     * @Notes: Processing partial amounts involves partial reversals (GCAG 1420s) and most likely not acceptable for an ACH payment.
     * Typically, retry is required with correction from the user
     */
    @Override
    public ResponseEntity<JSONObject> payDownAccountBalance(JSONObject transaction) {

        JSONObject responseObject;

        try {
            UUID userAccount = UUID.fromString(transaction.get(FractionConstants.USERID).toString());
            //check if the user exists in DB
            Optional<User> userObject = userRepository.findById(userAccount);

            //Deny transaction if user is Invalid- Do not proceed further
            if (userObject.isEmpty()) {
                LOGGER.error("Invalid User ID : {}", userAccount);
                responseObject = responseBuilder.blanketTransactionFailed();
                responseObject.put(FractionConstants.REASON_FOR_FAILURE, "Invalid User : " + userAccount);
                responseObject.put(FractionConstants.STATUS, "DENY");
                return new ResponseEntity<>(responseObject, HttpStatus.EXPECTATION_FAILED);
            }

            Set<Account> userAccounts = userObject.get().getAccounts();

            for (Account account : userAccounts) {

                if (account.getPrimaryAccountNumber().toString().equals(transaction.get(FractionConstants.PAN))) {
                    LOGGER.debug("Account found !!");
                    double creditAmount = (Double) transaction.get(FractionConstants.CREDIT_TRANSACTION);
                    double balance = account.getAccountBalance() - creditAmount;

                    //Only proceed if the transaction amount is less or equal to balance.
                    //in some conditions, issuers can process partial amounts and create reversal for left over amount. But As of my knowledge ACH payments don't work that way
                    if (creditAmount <= account.getAccountBalance()) {


                        //update transaction table with new transaction
                        Transaction transactionObj = new Transaction();
                        transactionObj.setAccount(account);
                        transactionObj.setCredit(creditAmount);
                        Transaction transactionResult = transactionRepository.save(transactionObj); // This can also be done async for performance so that it won't block other transactions on main thread
                        TransactionStatus notificationPayload = new TransactionStatus(transactionResult, "SUCCESS", "Credit transaction successful");
                        //update the new balance to account table - Async op and publish to rabbitMQ so that notification and logging can consume

                        CompletableFuture.runAsync(() -> {
                            try {
                                updateBalance(account, balance);
                                publishToRabbit(notificationPayload);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        });


                        return new ResponseEntity<>(responseBuilder.transactionSuccess(), HttpStatus.OK);


                    } else {
                        LOGGER.error("Amount is more than customer owed ! ");
                        responseObject = responseBuilder.blanketTransactionFailed();
                        responseObject.put(FractionConstants.REASON_FOR_FAILURE, "Invalid Amount - try lesser than: " + account.getAccountBalance());
                        return new ResponseEntity<>(responseObject, HttpStatus.PRECONDITION_FAILED);
                    }
                }

            }


        } catch (Exception e) {
            LOGGER.error("Exception Occurred while processing Transaction {}", e.getMessage());
            responseObject = responseBuilder.blanketTransactionFailed();
            responseObject.put(FractionConstants.REASON_FOR_FAILURE, "INTERNAL SYSTEM ERROR");
            return new ResponseEntity<>(responseObject, HttpStatus.INTERNAL_SERVER_ERROR);
        }
        responseObject = responseBuilder.blanketTransactionFailed();
        responseObject.put(FractionConstants.REASON_FOR_FAILURE, "Incorrect Account Number / Account Not owner by the user");

        return new ResponseEntity<>(responseObject, HttpStatus.EXPECTATION_FAILED);

    }

    @Override
    public ResponseEntity<List<User>> getAllAccounts() {
        List<User> allUsers =
                userRepository.findAll();
        return new ResponseEntity<>(allUsers, HttpStatus.OK);
    }

    // Utility methods -- for above methods --can be separated out of singleton in future

    /**
     * This method calculates the balance of account and updates the balance to database Asynchronously using CompletedFutures
     *
     * @param transactions
     * @param account
     * @return
     */
    private JSONObject auditAccount(Set<Transaction> transactions, Account account) {
        JSONObject accountAuditResponse = new JSONObject();
        double creditTotal = 0;
        double debitTotal = 0;
        double creditLimit = account.getCreditLimit();

        for (Transaction transaction : transactions) {

            creditTotal += transaction.getCredit();
            debitTotal += transaction.getDebit();
        }
        //Removing the initial credit transaction from the total credits.
        creditTotal -= creditLimit;

        double balance = creditLimit - ((creditLimit - debitTotal) + creditTotal);
        accountAuditResponse = createBalanceResponse(
                account.getPrimaryAccountNumber(), balance, account.getCreditLimit() - balance, creditLimit);

        //update the database with new balances asynchronously so that request doesn't have to wait
        CompletableFuture.runAsync(() -> {
            try {
                updateBalance(account, balance);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        return accountAuditResponse;
    }


    /**
     * This method executes synchronously on a worker thread
     *
     * @param account
     * @param balance
     */
    @Async
    public void updateBalance(Account account, double balance) throws InterruptedException {
        Thread.sleep(1000L);
        int rowsUpdated = accountRespository.updateBalance(balance, account.getPrimaryAccountNumber());

        if (rowsUpdated < 1) {
            LOGGER.error("Failed to update the database with balance for the account: {}", account.getPrimaryAccountNumber());
        } else {
            LOGGER.info("Successfully updated the database with new balance for the account: {} and executed on thread: {}", account.getPrimaryAccountNumber(), Thread.currentThread());
        }
        //return CompletableFuture.completedFuture("Update Success");
    }


    /**
     * @param pan
     * @param balance
     * @param creditAvailable
     * @param originalCreditLimit
     * @return
     * @Description This method creates the response object for transactions. It's a better way to respond back to frontend rather than giving up original object
     */
    private JSONObject createBalanceResponse(UUID pan, double balance, double creditAvailable, double originalCreditLimit) {
        JSONObject jsonObject = new JSONObject();

        jsonObject.put(FractionConstants.PAN, pan);
        jsonObject.put(FractionConstants.ACCOUNT_BALANCE, balance);
        jsonObject.put(FractionConstants.AVAILABLE_CREDIT, creditAvailable);
        jsonObject.put(FractionConstants.CREDIT_LIMIT, originalCreditLimit);
        return jsonObject;
    }

    /**
     * This method publishes the transaction object to two different queues. one for notification and another one for logging
     *
     * @param notificationPayload
     */
    private void publishToRabbit(TransactionStatus notificationPayload) {
        rabbitTemplate.convertAndSend(RabbitMqConfig.FRACTION_MESSAGING_EXCHANGE, RabbitMqConfig.FRACTION_CHANDRA_ROUTING_KEY, notificationPayload);
        rabbitTemplate.convertAndSend(RabbitMqConfig.FRACTION_MESSAGING_EXCHANGE, RabbitMqConfig.FRACTION_LOGGING_ROUTING_KEY, notificationPayload);
    }


}
