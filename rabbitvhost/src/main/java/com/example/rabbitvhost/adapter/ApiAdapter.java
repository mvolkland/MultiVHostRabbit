package com.example.rabbitvhost.adapter;

import java.util.UUID;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessagePropertiesBuilder;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.example.rabbitvhost.util.ArgonRoutingConnectionFactory;
import com.example.rabbitvhost.util.FixedReplyRabbitTemplate;

@Component
public class ApiAdapter {
  private static Logger log = LogManager.getLogger();

  @Autowired
  private final RabbitTemplate rabbit;
  @Autowired
  private String vHostBackend;

  public ApiAdapter(final ConnectionFactory connectionFactory) {
    rabbit = new FixedReplyRabbitTemplate(connectionFactory);
  }

  @RabbitListener(admin = "apiAdmin", containerFactory = "apiContainerFactory",
      queuesToDeclare = @Queue(admins = "apiAdmin", durable = "false", value = "q.api.query"))
  public String handleApiQuery(final Message query) throws Exception {
    Message answer = new Message("empty answer".getBytes(), null);
    final String queryText = new String(query.getBody());
    log.debug("======> {}", query);

    final Message backendQuery = createBackendQuery(queryText);

    try {
      ArgonRoutingConnectionFactory.select(vHostBackend);
      answer = rabbit.sendAndReceive("q.backend.query", backendQuery);
    } finally {
      ArgonRoutingConnectionFactory.unselect();
    }

    log.debug("<====== answer: {}", answer);
    return answer.toString();
  }

  private Message createBackendQuery(final String queryText) {
    return new Message(queryText.getBytes(),
        MessagePropertiesBuilder.newInstance().setContentType("text/plain")
            .setCorrelationId(UUID.randomUUID().toString()).setReplyTo("q.backend.query.reply")
            .build());
  }

}
