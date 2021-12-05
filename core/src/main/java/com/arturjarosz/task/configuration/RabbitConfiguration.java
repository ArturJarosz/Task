package com.arturjarosz.task.configuration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.Connection;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

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
    private AmqpTemplate amqpTemplate;

    private int counter = 0;

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
        this.amqpTemplate = new RabbitTemplate(this.connectionFactory);
        this.publishMessages(this.amqpTemplate);
    }

    @Bean
    public AmqpAdmin admin() {
        this.admin = new RabbitAdmin(this.connectionFactory);
        return this.admin;
    }

    @Bean
    public Queue queue() {
        Queue queue = new Queue(TEST_QUEUE + "2", true);
        //queue.setAdminsThatShouldDeclare(this.admin);
        return queue;
    }

    @Bean
    public Queue queue2() {
        return new Queue(TEST_QUEUE_2, true);
    }

    @Bean
    public TopicExchange topicExchange2() {
        return new TopicExchange(TEST_EXCHANGE_2);
    }

    @Bean
    public Binding binding() {
        return BindingBuilder.bind(this.queue2()).to(this.topicExchange2()).with(ROUTING_KEY);
    }

    public void publishMessages(AmqpTemplate amqpTemplate) {
        LOG.info("** Publish message **");
        Runnable newRunnable = new Runnable() {

            public void publishMessage() {
                LOG.info("*** Publishing next message:  " + RabbitConfiguration.this.counter + " ***");
                amqpTemplate.convertAndSend(TEST_EXCHANGE_2, ROUTING_KEY,
                        "Event number: " + RabbitConfiguration.this.counter);
                RabbitConfiguration.this.counter++;
            }

            @Override
            public void run() {
                this.publishMessage();
            }
        };
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        executor.scheduleAtFixedRate(newRunnable, 0, 10, TimeUnit.SECONDS);
    }

}
