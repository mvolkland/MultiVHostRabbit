package com.example.rabbitvhost.adapter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Random;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.core.MessagePropertiesBuilder;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import com.example.rabbitvhost.util.ArgonRoutingConnectionFactory;

@Profile("LISTENERCLASS")
@Component
@RabbitListener(admin = "backendAdmin", containerFactory = "backendContainerFactory",
    queuesToDeclare = {
        @Queue(admins = "backendAdmin", durable = "false", value = "q.backend.query"),
        @Queue(admins = "backendAdmin", durable = "false", value = "q.backend.nested"),
        @Queue(admins = "backendAdmin", durable = "false", value = "q.backend.nested2")})
public class BackendAdapterClassAnno {
  private static Logger log = LogManager.getLogger();

  private static int count = 0;
  private static int countNested = 0;

  @Autowired
  private RabbitTemplate rabbit;
  @Autowired
  private String vHostBackend;

  @RabbitHandler
  public String handleBackendQuery(final String query) {
    log.debug("received {}", query);

    final String nestedReply = nestedSendAndReceiveL1();

    log.debug("received nested reply in backend: {}", nestedReply);
    return "reply no. " + count++ + " to " + query + " with nested reply <" + nestedReply + ">";
  }


  @RabbitHandler
  public String handleNestedSendAndReceiveL1(final Integer nested) {
    log.debug("L1: received nested query: {}", nested);
    final String nestedReply = nestedSendAndReceiveL2();
    return "L1: nested reply no. " + countNested++ + " to (Integer) " + nested
        + " with nested reply " + nestedReply;
  }

  @RabbitHandler
  public String handleNestedSendAndReceiveL2(final Double nested) {
    log.debug("L2: received nested query: {}", nested);
    return "L2: nested reply no. " + countNested++ + " to (Double) " + nested;
  }



  private String nestedSendAndReceiveL1() {
    final Message nestedQuery = createNestedQuery();

    final Integer rand = getRandomNumberInRange(1, 100);
    log.debug("L1: sending nested query (Integer) {}", rand);

    try {
      ArgonRoutingConnectionFactory.select(vHostBackend);

      return (String) rabbit.convertSendAndReceive("", "q.backend.nested", rand);

    } finally {
      ArgonRoutingConnectionFactory.unselect();
    }
  }

  private String nestedSendAndReceiveL2() {
    final Message nestedQuery = createNestedQuery();

    final Double rand = Double.valueOf(Math.random());
    log.debug("L2: sending nested query (Double) {}", rand);
    try {
      ArgonRoutingConnectionFactory.select(vHostBackend);

      return (String) rabbit.convertSendAndReceive("", "q.backend.nested2", rand);

    } finally {
      ArgonRoutingConnectionFactory.unselect();
    }
  }



  private Message createNestedQuery() {
    final Integer rand = getRandomNumberInRange(1, 100);

    final MessageProperties props = MessagePropertiesBuilder.newInstance()
        .setContentType("application/x-java-serialized-object").build();
    final ByteArrayOutputStream bout = new ByteArrayOutputStream(2048);
    ObjectOutputStream oout;
    try {
      oout = new ObjectOutputStream(bout);
      oout.writeObject(rand);
    } catch (final IOException e) {
      e.printStackTrace();
    }
    final Message msg = new Message(bout.toByteArray(), props);
    return msg;
  }


  private static Integer getRandomNumberInRange(final int min, final int max) {

    if (min >= max) {
      throw new IllegalArgumentException("max must be greater than min");
    }

    final Random r = new Random();
    return Integer.valueOf(r.nextInt(max - min + 1) + min);
  }
}
