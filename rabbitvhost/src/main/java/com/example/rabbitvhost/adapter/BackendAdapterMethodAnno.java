package com.example.rabbitvhost.adapter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.core.MessagePropertiesBuilder;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import com.example.rabbitvhost.util.ArgonRoutingConnectionFactory;

@Profile("LISTENERMETHODS")
@Component
public class BackendAdapterMethodAnno {
  private static Logger log = LogManager.getLogger();

  private static int count = 0;
  private static int countNested = 0;

  @Autowired
  private RabbitTemplate rabbit;
  @Autowired
  private String vHostBackend;

  @RabbitListener(admin = "backendAdmin", containerFactory = "backendContainerFactory",
      queuesToDeclare = @Queue(admins = "backendAdmin", durable = "false",
          value = "q.backend.query"))
  public String handleBackendQuery(final Message query) {
    log.debug("received {}", query);

    final String nestedReply = nestedSendAndReceive();

    log.debug("received nested reply: {}", nestedReply);

    return "reply no. " + count++ + " to " + new String(query.getBody());
  }


  @RabbitListener(admin = "backendAdmin", containerFactory = "backendContainerFactory",
      queuesToDeclare = @Queue(admins = "backendAdmin", durable = "false",
          value = "q.backend.nested"))
  public String handleNestedSendAndReceive(final Message nested) {
    log.debug("received nested query: {}", nested);
    return "nested reply no. " + countNested++ + " to " + new String(nested.getBody());
  }



  private String nestedSendAndReceive() {
    final Message nestedQuery = createNestedReply("nested query");

    log.debug("sending nested query {}", nestedQuery);

    try {
      ArgonRoutingConnectionFactory.select(vHostBackend);

      return (String) rabbit.convertSendAndReceive("", "q.backend.nested", "nested query");

    } finally {
      ArgonRoutingConnectionFactory.unselect();
    }
  }



  private Message createNestedReply(final String replyText) {
    final MessageProperties props =
        MessagePropertiesBuilder.newInstance().setContentType("text/plain").build();
    final Message msg = new Message(replyText.getBytes(), props);
    return msg;
  }



}
