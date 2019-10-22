package com.example.rabbitvhost.util;

import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

public class FixedReplyRabbitTemplate extends RabbitTemplate {

  public FixedReplyRabbitTemplate(final ConnectionFactory connectionFactory) {
    super(connectionFactory);
    setUseDirectReplyToContainer(false);
    setReplyAddress("q.backend.query.reply");
  }

  @Override
  protected boolean useDirectReplyTo() {
    return false;
  }



}
