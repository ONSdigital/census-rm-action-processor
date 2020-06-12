package uk.gov.ons.census.action.messaging;

import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;

@Component
@EnableRetry
public class RabbitQueueHelper {

  @Autowired private RabbitTemplate rabbitTemplate;

  @Autowired private AmqpAdmin amqpAdmin;

  public void sendMessage(String exchangeName, String routingKey, Object message) {
    rabbitTemplate.convertAndSend(exchangeName, routingKey, message);
  }

  @Retryable(
      value = {java.io.IOException.class},
      maxAttempts = 10,
      backoff = @Backoff(delay = 5000))
  public void purgeQueue(String queueName) {
    amqpAdmin.purgeQueue(queueName);
  }
}
