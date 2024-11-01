package com.qdx.qn.consumer;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import com.qdx.qn.config.MessagingConfig;
import com.qdx.qn.dto.OrderStatus;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class User {

    @RabbitListener(queues = MessagingConfig.QUEUE)
    public void consumeMessageFromQueue(OrderStatus orderStatus) {
        log.info("Message recieved from queue : " + orderStatus);
    }
}
