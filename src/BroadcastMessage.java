/*
 * 
 * @author Liam Scott
 *
 */

@SuppressWarnings("serial")
public class BroadcastMessage extends Message {
	
	private int type;
	private String sender, recipient, message;
	
	public BroadcastMessage(String sender, String recipient, String message) {
		super();
		type = 2;
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
