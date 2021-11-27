package com.arturjarosz.task.configuration;

import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.Connection;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

@Configuration
public class RabbitConfiguration {
    private static final String TEST_EXCHANGE = "test.exchange";
    private static final String TEST_EXCHANGE_2 = "test.exchange2";
    private static final String TEST_QUEUE = "test.queue";
    private static final String TEST_QUEUE_2 = "test.queue";
    private static final String LOGIN = "guest";
    private static final String PASSWORD = "guest";
    private static final String HOST = "localhost";
    private static final String VIRTUAL_HOST = "/";

    private final ConnectionFactory connectionFactory;
    private RabbitAdmin admin;
    private final Connection connection;

    @Autowired
    public RabbitConfiguration() {
        com.rabbitmq.client.ConnectionFactory rabbitFactory = new com.rabbitmq.client.ConnectionFactory();
        rabbitFactory.setUsername(LOGIN);
        rabbitFactory.setPassword(PASSWORD);
        rabbitFactory.setHost(HOST);
        rabbitFactory.setVirtualHost(VIRTUAL_HOST);
        rabbitFactory.setAutomaticRecoveryEnabled(true);

        this.connectionFactory = new CachingConnectionFactory(rabbitFactory);
        this.connection = this.connectionFactory.createConnection();
    }

    @Bean
    public AmqpAdmin admin() {
        this.admin = new RabbitAdmin(this.connectionFactory);
        return this.admin;
    }

    @Bean
    public Queue queue() {
        System.out.println("tworze sie 1!");
        Queue queue = new Queue(TEST_QUEUE + "2", true);
        //queue.setAdminsThatShouldDeclare(this.admin);
        return queue;
    }

    @Bean
    public Queue queue2() {
        System.out.println("tworze sie 2!");
        return new Queue(TEST_QUEUE_2, true);
    }

    @Bean
    public TopicExchange topicExchange() {
        System.out.println("tworze sie 3!");
        return new TopicExchange(TEST_EXCHANGE_2);
    }

    @Bean
    public Binding binding() {
        System.out.println("tworze sie 4!");
        return BindingBuilder.bind(this.queue2())
                .to(this.topicExchange())
                .with("#");
    }

    @PostConstruct
    public void init() {
/*        this.admin.declareQueue(this.queue());
        Binding binding = new Binding("test.queue", Binding.DestinationType.QUEUE, "test.exchange", "#", null);
        this.admin.declareBinding(binding);*/
    }
}
