package de.mvolkland.rabbitwebclient.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import de.mvolkland.rabbitwebclient.model.Message;
import de.mvolkland.rabbitwebclient.rabbit.SenderService;

@Service
public class MessageService {
	
	private SenderService sender;

	public MessageService(SenderService sender) {
		this.sender = sender;
	}

	public List<Message> getMessageList() {
		var msgList = new ArrayList<Message>();
		
		for (int i = 0; i < 10; i++) {
			msgList.add(new Message("Message-" + (i+1)));
		}
		return msgList;
	}
	
	public void sendQuery(String msg) {
		sender.sendQuery(msg);
	}
}
