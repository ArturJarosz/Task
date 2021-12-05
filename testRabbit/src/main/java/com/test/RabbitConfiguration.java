package com.test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.Connection;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.listener.MessageListenerContainer;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfiguration {
    private static final Logger LOG = LoggerFactory.getLogger(RabbitConfiguration.class);
    private static final String TEST_EXCHANGE = "test.exchange";
    private static final String TEST_EXCHANGE_2 = "test.exchange2";
    private static final String TEST_QUEUE = "test.queue";
    private static final String TEST_QUEUE_2 = "test.queue";
    private static final String LOGIN = "guest";
    private static final String PASSWORD = "guest";
    private static final String HOST = "localhost";
    private static final String VIRTUAL_HOST = "/";
    private static final String ROUTING_KEY = "#";
    private static final String ADDRESSES = "127.0.0.1:30000,127.0.0.1:30002,127.0.0.1:30004";

    private final CachingConnectionFactory connectionFactory;
    private final Connection connection;
    private RabbitAdmin admin;

    @Autowired
    public RabbitConfiguration() {
        LOG.info("Creating rabbit configuration.");
        com.rabbitmq.client.ConnectionFactory rabbitFactory = new com.rabbitmq.client.ConnectionFactory();
        rabbitFactory.setUsername(LOGIN);
        rabbitFactory.setPassword(PASSWORD);
        //rabbitFactory.setHost(HOST);
        //rabbitFactory.setVirtualHost(VIRTUAL_HOST);
        rabbitFactory.setAutomaticRecoveryEnabled(true);

        this.connectionFactory = new CachingConnectionFactory(rabbitFactory);
        this.connectionFactory.setAddresses(ADDRESSES);
        this.connection = this.connectionFactory.createConnection();
    }

    @Bean
    public AmqpAdmin admin() {
        this.admin = new RabbitAdmin(this.connectionFactory);
        return this.admin;
    }

    @Bean
    MessageListenerContainer messageListenerContainer() {
        SimpleMessageListenerContainer simpleMessageListenerContainer = new SimpleMessageListenerContainer();
        simpleMessageListenerContainer.setConnectionFactory(this.connectionFactory);
        simpleMessageListenerContainer.setQueueNames(TEST_QUEUE_2);
        simpleMessageListenerContainer.setMessageListener(new Receiver());
        return simpleMessageListenerContainer;
    }


}
