import java.io.Serializable;

/*
 * 
 * @author Liam Scott
 *
 */

@SuppressWarnings("serial")
public abstract class Message implements Serializable{
	public abstract int getType();
	public abstract String getSender();
	public abstract String getRecipient();
	public abstract String getMessage();
}
