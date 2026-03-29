package com.example.video_platform.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    // 交换机名称
    public static final String VIDEO_EXCHANGE = "video.exchange";
    // 转码队列
    public static final String TRANSCODE_QUEUE = "video.transcode.queue";
    // 审核队列
    public static final String AUDIT_QUEUE = "video.audit.queue";
    // 死信队列
    public static final String DEAD_LETTER_QUEUE = "video.dead.letter.queue";

    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public Exchange videoExchange() {
        return ExchangeBuilder.topicExchange(VIDEO_EXCHANGE).durable(true).build();
    }

    @Bean
    public Queue transcodeQueue() {
        return QueueBuilder.durable(TRANSCODE_QUEUE)
                .withArgument("x-dead-letter-exchange", VIDEO_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", "video.dead.letter")
                .build();
    }

    @Bean
    public Queue auditQueue() {
        return QueueBuilder.durable(AUDIT_QUEUE).build();
    }

    @Bean
    public Queue deadLetterQueue() {
        return QueueBuilder.durable(DEAD_LETTER_QUEUE).build();
    }

    @Bean
    public Binding transcodeBinding(Queue transcodeQueue, Exchange videoExchange) {
        return BindingBuilder.bind(transcodeQueue)
                .to(videoExchange)
                .with("video.transcode")
                .noargs();
    }

    @Bean
    public Binding auditBinding(Queue auditQueue, Exchange videoExchange) {
        return BindingBuilder.bind(auditQueue)
                .to(videoExchange)
                .with("video.audit")
                .noargs();
    }

    @Bean
    public Binding deadLetterBinding(Queue deadLetterQueue, Exchange videoExchange) {
        return BindingBuilder.bind(deadLetterQueue)
                .to(videoExchange)
                .with("video.dead.letter")
                .noargs();
    }
}