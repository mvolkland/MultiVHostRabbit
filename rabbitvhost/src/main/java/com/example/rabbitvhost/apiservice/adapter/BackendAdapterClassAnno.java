package com.example.rabbitvhost.apiservice.adapter;

import java.util.Random;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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
        @Queue(admins = "backendAdmin", durable = "false", value = "q.backend.query")})
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

    final String nestedReply = sendAndReceive();

    log.debug("received nested reply in backend: {}", nestedReply);
    return "reply no. " + count++ + " to " + query + " with nested reply <" + nestedReply + ">";
  }


  private String sendAndReceive() {
    final Integer rand = getRandomNumberInRange(1, 100);
    log.debug("L1: sending nested query (Integer) {}", rand);

    try {
      ArgonRoutingConnectionFactory.select(vHostBackend);

      return (String) rabbit.convertSendAndReceive("", "q.backend.nested", rand);

    } finally {
      ArgonRoutingConnectionFactory.unselect();
    }
  }


  private static final Random random = new Random();

  private static Integer getRandomNumberInRange(final int min, final int max) {

    if (min >= max) {
      throw new IllegalArgumentException("max must be greater than min");
    }

    return Integer.valueOf(random.nextInt(max - min + 1) + min);
  }
}
