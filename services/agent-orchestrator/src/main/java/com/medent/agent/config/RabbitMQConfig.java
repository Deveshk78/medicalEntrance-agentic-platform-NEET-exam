package com.medent.agent.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE = "medent.agent.events";
    public static final String EXAM_QUEUE = "medent.exam.events";
    public static final String ANALYTICS_QUEUE = "medent.analytics.events";
    public static final String ALERT_QUEUE = "medent.alert.events";

    @Bean
    public TopicExchange agentExchange() {
        return new TopicExchange(EXCHANGE, true, false);
    }

    @Bean
    public Queue examQueue() {
        return QueueBuilder.durable(EXAM_QUEUE).build();
    }

    @Bean
    public Queue analyticsQueue() {
        return QueueBuilder.durable(ANALYTICS_QUEUE).build();
    }

    @Bean
    public Queue alertQueue() {
        return QueueBuilder.durable(ALERT_QUEUE).build();
    }

    @Bean
    public Binding examBinding(Queue examQueue, TopicExchange agentExchange) {
        return BindingBuilder.bind(examQueue).to(agentExchange).with("exam.#");
    }

    @Bean
    public Binding analyticsBinding(Queue analyticsQueue, TopicExchange agentExchange) {
        return BindingBuilder.bind(analyticsQueue).to(agentExchange).with("analytics.#");
    }

    @Bean
    public Binding alertBinding(Queue alertQueue, TopicExchange agentExchange) {
        return BindingBuilder.bind(alertQueue).to(agentExchange).with("agent.action");
    }
}
