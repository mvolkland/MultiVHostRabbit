package de.mvolkland.rabbitwebclient.rabbit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import de.mvolkland.rabbitwebclient.RabbitwebclientConfig;

@Component
public class SenderService {
	Logger log = LogManager.getLogger();
	
	private RabbitTemplate template;

	public SenderService(RabbitTemplate template) {
		this.template = template;
	}
	
	public void sendQuery(String msg) {
		log.debug("==> sending {}", msg);
		var reply = (String)template.convertSendAndReceive(RabbitwebclientConfig.Q_API_QUERY, msg);
		log.debug("<== received reply {}", reply);
	}
}
