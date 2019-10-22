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
import org.springframework.stereotype.Component;
import com.example.rabbitvhost.util.ArgonRoutingConnectionFactory;

@Component
public class BackendAdapter {
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
    final Message nestedReply = nestedSendAndReceive();
    log.debug("received nested reply: {}", new String(nestedReply.getBody()));
    return "reply no. " + count++ + " to " + new String(query.getBody());
  }



  private Message nestedSendAndReceive() {
    final Message nestedQuery = createNestedReply("nested query");
    try {
      ArgonRoutingConnectionFactory.select(vHostBackend);
      log.debug("sending nested query {}", nestedQuery);
      return rabbit.sendAndReceive("", "q.backend.nested", nestedQuery);
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



  @RabbitListener(admin = "backendAdmin", containerFactory = "backendContainerFactory",
      queuesToDeclare = @Queue(admins = "backendAdmin", durable = "false",
          value = "q.backend.nested"))
  public String handleNestedSendAndReceive(final Message nested) {
    log.debug("received nested query: {}", nested);
    return "nested reply no. " + countNested++ + " to " + new String(nested.getBody());
  }


  // @RabbitListener(admin = "backendAdmin", containerFactory = "backendContainerFactory",
  // queuesToDeclare = @Queue(admins = "backendAdmin", durable = "false",
  // value = "q.backend.query.reply"))
  // public void handleBackendQueryReply(final Message reply) {
  // log.debug("received reply: {}", reply);
  // final String replyText = new String(reply.getBody());
  // final Message apiQueryReply = createApiReply(replyText);
  // try {
  // ArgonRoutingConnectionFactory.select(vHostApi);
  // rabbit.send("", "q.api.query.reply", apiQueryReply);
  // } finally {∖
  // ArgonRoutingConnectionFactory.unselect();
  // }
  // }
  //
  // private Message createApiReply(final String replyText) {
  // final MessageProperties props =
  // MessagePropertiesBuilder.newInstance().setContentType("text/plain").build();
  // final Message msg = new Message(replyText.getBytes(), props);
  // return msg;
  // }


}
