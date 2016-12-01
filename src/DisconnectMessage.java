/*
 * 
 * @author Liam Scott
 *
 */

@SuppressWarnings("serial")
public class DisconnectMessage extends Message {
	
	private int type;
	private String sender, recipient, message;
	
	public DisconnectMessage(String sender) {
		type = 1;
		this.sender = sender;
		this.recipient = "Server";
		message = "Disconnect";
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
