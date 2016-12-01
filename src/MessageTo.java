/*
 * 
 * @author Liam Scott
 *
 */

@SuppressWarnings("serial")
public class MessageTo extends Message {
	
	private int type;
	private String sender, recipient, message;
	
	public MessageTo(String sender, String recipient, String message) {
		super();
		type = 3;
		this.sender = sender;
		this.recipient = recipient;
		this.message = message;
	}

	public int getType() {
		return type;
	}

	public String getSender() {
		return sender;
	}

	public String getRecipient() {
		return recipient;
	}
	
	public String getMessage() {
		return message;
	}
}
