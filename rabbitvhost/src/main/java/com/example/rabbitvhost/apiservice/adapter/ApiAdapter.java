package com.example.rabbitvhost.apiservice.adapter;

import java.util.UUID;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessagePropertiesBuilder;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.example.rabbitvhost.util.MyRoutingConnectionFactory;

@Component
public class ApiAdapter {
  private static Logger log = LogManager.getLogger();

  @Autowired
  private RabbitTemplate rabbit;
  @Autowired
  private String vHostBackend;

  @RabbitListener(admin = "apiAdmin", containerFactory = "apiContainerFactory",
      queuesToDeclare = @Queue(admins = "apiAdmin", durable = "false", value = "q.api.query"))
  public String handleApiQuery(final Message query) throws Exception {
    Message reply = new Message("empty reply".getBytes(), null);
    final String queryText = new String(query.getBody());
    log.debug("======> {}", query);

    final Message backendQuery = createBackendQuery(queryText);

    try {
      MyRoutingConnectionFactory.select(vHostBackend);
      reply = rabbit.sendAndReceive("q.backend.query", backendQuery);
    } finally {
      MyRoutingConnectionFactory.unselect();
    }

    log.debug("<====== reply: {}", reply);
    return reply.toString();
  }

  private Message createBackendQuery(final String queryText) {
    return new Message(queryText.getBytes(),
        MessagePropertiesBuilder.newInstance().setContentType("text/plain")
            .setCorrelationId(UUID.randomUUID().toString()).setReplyTo("q.backend.query.reply")
            .build());
  }

}
