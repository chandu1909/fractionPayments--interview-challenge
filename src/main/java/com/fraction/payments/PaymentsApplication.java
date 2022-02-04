package com.fraction.payments;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class PaymentsApplication {

    private static final Logger LOGGER = LoggerFactory.getLogger(PaymentsApplication.class);

    public static void main(String[] args) {
        ConfigurableApplicationContext configurableApplicationContext = SpringApplication.run(PaymentsApplication.class, args);
        LOGGER.info("Payments Server Started successfully");

        LOGGER.info("Attempting to load the data to database from the utility class!!");
        LoadDataUtility loadDataUtility = new LoadDataUtility();
        loadDataUtility.loadRandomData(configurableApplicationContext);

    }

}
