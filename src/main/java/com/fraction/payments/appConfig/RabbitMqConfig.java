package com.fraction.payments.appConfig;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @Author: Chandra sekhar Polavarapu
 * @Description : RabbitMQ configuration
 * @Notes: Ideally configurations CAN'T be initialized here, rather should consume from properties or env variables.
 * For ease of use , I have added here.
 */
@Configuration
public class RabbitMqConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger(RabbitMqConfig.class);

    //Notification Queue config
    public static final String NOTIFICATIONS_QUEUE = "notificationQueue";
    public static final String FRACTION_CHANDRA_ROUTING_KEY = "fraction_chandra_routing_key";

    //Logging framework Queue config
    public static final String LOGGING_QUEUE = "loggingQueue";
    public static final String FRACTION_LOGGING_ROUTING_KEY = "fraction_logging_routing_key";

    public static final String FRACTION_MESSAGING_EXCHANGE = "fraction_messaging_exchange";

    @Bean
    Queue notificationQueue() {
        LOGGER.info("Creating queue: {}", NOTIFICATIONS_QUEUE);
        return new Queue(NOTIFICATIONS_QUEUE);

    }

    @Bean
    Queue loggingQueue() {
        LOGGER.info("Creating queue: {}", LOGGING_QUEUE);
        return new Queue(LOGGING_QUEUE);
    }

    @Bean
    public TopicExchange exchange() {
        return new TopicExchange(FRACTION_MESSAGING_EXCHANGE);

    }

    @Bean
    public Binding notificationQueueBinding(Queue notificationQueue, TopicExchange topicExchange) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.info("Binding the Notification Queue with Exchange");
        }
        return BindingBuilder.bind(notificationQueue).to(topicExchange).with(FRACTION_CHANDRA_ROUTING_KEY);
    }

    @Bean
    public Binding logginQueueBinding(Queue loggingQueue, TopicExchange topicExchange) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.info("Binding the Logging Queue with Exchange");
        }
        return BindingBuilder.bind(loggingQueue).to(topicExchange).with(FRACTION_LOGGING_ROUTING_KEY);
    }

    @Bean
    public MessageConverter converter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public AmqpTemplate template(ConnectionFactory connectionFactory) {

        final RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(converter());

        return rabbitTemplate;
    }
}
