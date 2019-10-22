package de.mvolkland.rabbitwebclient.model;

public class Message {
	private String content;

	public Message(String content) {
		super();
		this.content = content;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	@Override
	public String toString() {
		return "Message [content=" + content + "]";
	}
	
	
}
