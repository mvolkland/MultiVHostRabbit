package de.mvolkland.rabbitwebclient.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import de.mvolkland.rabbitwebclient.service.MessageService;

@Controller
public class AppController {
	
	private MessageService service;

	public AppController(MessageService service) {
		this.service = service;
	}
	
	@RequestMapping("/messagelist")
	public String listMessages(@RequestParam(name="content", required=false, defaultValue="World") String content, Model model) {
		var list = service.getMessageList();
		model.addAttribute("messages", list);
		
		if (content != null)
			service.sendQuery(content);
		
		return "messages";
	}
}
