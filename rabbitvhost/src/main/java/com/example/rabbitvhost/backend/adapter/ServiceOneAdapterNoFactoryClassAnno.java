package com.example.rabbitvhost.backend.adapter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import com.example.rabbitvhost.util.MyRoutingConnectionFactory;

@Profile({"!withContainerFactory"})
@Component
@RabbitListener(queuesToDeclare = {@Queue(durable = "false", value = "q.backend.nested"),
    @Queue(durable = "false", value = "q.backend.nested2")})
public class ServiceOneAdapterNoFactoryClassAnno {
  private static Logger log = LogManager.getLogger();

  private static int countNested = 0;

  @Autowired
  private RabbitTemplate rabbit;
  @Autowired
  private String vHostBackend;

  @RabbitHandler
  public String handleServicOneQuery(final Integer nested) {
    log.debug("L1: received nested query: {}", nested);
    final String nestedReply = sendAndReceive();
    return "L1: nested reply no. " + countNested++ + " to (Integer) " + nested
        + " with nested reply " + nestedReply;
  }

  @RabbitHandler
  public String handleServiceOneQueryNested(final Double nested) {
    log.debug("L2: received nested query: {}", nested);
    return "L2: nested reply no. " + countNested++ + " to (Double) " + nested;
  }



  private String sendAndReceive() {
    final Double rand = Double.valueOf(Math.random());
    log.debug("L2: sending nested query (Double) {}", rand);
    try {
      MyRoutingConnectionFactory.select(vHostBackend);

      return (String) rabbit.convertSendAndReceive("", "q.backend.nested2", rand);

    } finally {
      MyRoutingConnectionFactory.unselect();
    }
  }

}
