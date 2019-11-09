package com.example.rabbitvhost.backend.adapter;

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

@Profile({"LISTENERMETHODS & withContainerFactory"})
@Component
public class ServiceOneAdapterMethodAnno {
  private static Logger log = LogManager.getLogger();

  @Autowired
  private RabbitTemplate rabbit;
  @Autowired
  private String vHostBackend;

  private static int countNested = 0;

  @RabbitListener(admin = "backendAdmin", containerFactory = "backendContainerFactory",
      queuesToDeclare = @Queue(admins = "backendAdmin", durable = "false",
          value = "q.backend.nested"))
  public String handleNestedSendAndReceive(final Message nested) {
    log.debug("received nested query: {}", nested);

    final String nestedReply2 = nestedSendAndReceive();

    log.debug("received nested-2 reply: {}", nestedReply2);

    return "nested reply no. " + countNested++ + " to " + new String(nested.getBody());
  }

  @RabbitListener(admin = "backendAdmin", containerFactory = "backendContainerFactory",
      queuesToDeclare = @Queue(admins = "backendAdmin", durable = "false",
          value = "q.backend.nested2"))
  public String handleNestedSendAndReceive2(final Message nested) {
    log.debug("received nested-2 query: {}", nested);
    return "nested-2 reply no. " + countNested++ + " to " + new String(nested.getBody());
  }


  private String nestedSendAndReceive() {
    final Message nestedQuery = createNestedReply("nested query");

    log.debug("sending nested query {}", nestedQuery);

    try {
      ArgonRoutingConnectionFactory.select(vHostBackend);

      return (String) rabbit.convertSendAndReceive("", "q.backend.nested2", "nested-2 query");

    } finally {
      ArgonRoutingConnectionFactory.unselect();
    }
  }



  private Message createNestedReply(final String replyText) {
    final MessageProperties props =
        MessagePropertiesBuilder.newInstance().setContentType("text/plain").build();
    return new Message(replyText.getBytes(), props);
  }



}
