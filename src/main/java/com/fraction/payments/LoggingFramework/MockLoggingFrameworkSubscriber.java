package com.fraction.payments.LoggingFramework;

import com.fraction.payments.appConfig.RabbitMqConfig;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * @Author: Chandra sekhar Polavarapu
 * @Description: This is a consumer class that consumes data from the rabbitMQ and prints them onto Console. This can be
 * directed to splunk / any other collection frameworks.
 */
@Component
public class MockLoggingFrameworkSubscriber {
    private static final Logger LOGGER = LoggerFactory.getLogger(MockLoggingFrameworkSubscriber.class);

    @RabbitListener(queues = RabbitMqConfig.LOGGING_QUEUE)
    public void consumeLogs(JSONObject logData) {
        LOGGER.info("Log from Payments service, : {}", logData);
    }
}
