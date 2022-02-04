package com.fraction.payments;

import com.fraction.payments.entities.Account;
import com.fraction.payments.entities.Transaction;
import com.fraction.payments.entities.User;
import com.fraction.payments.respository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ConfigurableApplicationContext;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.*;

public class LoadDataUtility {

    private static final Logger LOGGER = LoggerFactory.getLogger(LoadDataUtility.class);
    DecimalFormat df = new DecimalFormat("0.00");

    List<String> firstNames = Arrays.asList("Chandra", "Mike", "Sandy", "Ram", "Brandon", "Anup", "Juan", "Ben", "Tristan", "Sunil", "Bipin");
    List<String> lastNames = Arrays.asList("Polavarapu", "Tyson", "Gibson", "Potluri", "kenny", "Saha", "Carlos", "Lorenzo", "Patel", "Bail");

    Random rand = new Random();

    public void loadRandomData(ConfigurableApplicationContext configurableApplicationContext) {

        int i = 0;
        while (i < 10) {

            //creating user
            String fName = firstNames.get(rand.nextInt(firstNames.size()));
            String lname = lastNames.get(rand.nextInt(lastNames.size()));
            User user = new User(fName, lname);

            double limit1 = getrandomLimit();
            double limit2 = getrandomLimit();

            //Creating accounts for the above user
            Account account = new Account(limit1, 0.00, user);
            Account account1 = new Account(limit2, 0.00, user);

            Set<Account> accounts = new HashSet<>();

            accounts.add(account);
            accounts.add(account1);

            user.setAccounts(accounts);

            //Creating transactions for the accounts -- with balance zero
            //adding first transaction as credit of total limit
            Transaction transaction1 = new Transaction(limit1, 0, account);
            Transaction transaction2 = new Transaction(limit2, 0, account1);
            Transaction transaction3 = new Transaction(0, getRandomTransaction(), account);
            Transaction transaction4 = new Transaction(0, getRandomTransaction(), account1);


            Set<Transaction> trasactions = new HashSet<>();
            trasactions.add(transaction1);
            trasactions.add(transaction2);
            trasactions.add(transaction3);
            trasactions.add(transaction4);

            account.setTransactions(trasactions);

            UserRepository userRepository = configurableApplicationContext.getBean(UserRepository.class);
            User userResponse = userRepository.save(user);

            LOGGER.info("User : {} added with accounts: {}", userResponse.getUserID());
            i++;

        }
        LOGGER.info("Data loaded successfully ! ");

    }

    public double getrandomLimit() {
        df.setRoundingMode(RoundingMode.UP);
        return new Double(df.format(100000 + (5000000 - 100000) * rand.nextDouble()));
    }

    public double getRandomTransaction() {
        df.setRoundingMode(RoundingMode.UP);
        return new Double(df.format(1000 + (50000 - 1000) * rand.nextDouble()));
    }


}
