package com.qdx.qn.config;

import java.util.concurrent.Executor;

import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import lombok.Setter;

@Configuration
@EnableRabbit
@ConfigurationProperties(prefix = "rabbitmq")
@Setter
public class MessagingConfig {

    public static final String QUEUE_URL_SHORTENING = "url_shortening_queue";
    public static final String EXCHANGE = "qn_exchange";
    public static final String ROUTING_KEY_URL_SHORTENING = "url_shortening_routingKey";
    
    private String url;
    private String username;
    private String password;
    private int concurrentConsumers;

    @Bean
    public ConnectionFactory connectionFactory() {
        CachingConnectionFactory connectionFactory =
            new CachingConnectionFactory(url);
        connectionFactory.setUsername(username);
        connectionFactory.setPassword(password);
        return connectionFactory;
    }

    @Bean
    public Executor userExecutor() {
    	ThreadPoolTaskExecutor te = new ThreadPoolTaskExecutor();
    	te.setCorePoolSize(concurrentConsumers);
    	te.setThreadNamePrefix("userThreads-");
    	return te;
    }
    
    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory () {
    	SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
    	factory.setTaskExecutor(userExecutor());
        factory.setConnectionFactory(connectionFactory());
        factory.setConcurrentConsumers(concurrentConsumers);
        factory.setMessageConverter(converter());
        factory.setPrefetchCount(1);
        return factory;
    }


    @Bean
    public Queue queue() {
        return new Queue(QUEUE_URL_SHORTENING);
    }

    @Bean
    public TopicExchange exchange() {
        return new TopicExchange(EXCHANGE);
    }

    @Bean
    public Binding binding(Queue queue, TopicExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with(ROUTING_KEY_URL_SHORTENING);
    }

    @Bean
    public MessageConverter converter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public AmqpTemplate template(ConnectionFactory connectionFactory) {
        final RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
//        rabbitTemplate.setMessageConverter(converter());
        return rabbitTemplate;
    }}