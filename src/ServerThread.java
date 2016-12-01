import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Map;
import java.util.Map.Entry;

/*
 * 
 * @author Liam Scott
 *
 */

public class ServerThread implements Runnable {
	
	private Socket client;
	private boolean startup;
	private Map<String, ObjectOutputStream> sockets;
	private ObjectInputStream inStream;
	private ObjectOutputStream outStream;

	public ServerThread(Socket client, Map<String, ObjectOutputStream> sockets) {
		this.client = client;
		this.sockets = sockets;
		startup = true;
	}

	public void run(){
		try {
			inStream = new ObjectInputStream(client.getInputStream());
			outStream = new ObjectOutputStream(client.getOutputStream());
		} catch (IOException e) {
			e.printStackTrace();
		}
		while(true){
			try{
				Message message = (Message) inStream.readObject();
				if(message.getType()==1) {
					if(sockets.containsKey(message.getSender())) {
						sockets.remove(message.getSender());
						for(Entry<String, ObjectOutputStream> current: sockets.entrySet()) {
							current.getValue().writeObject(message);
						}
					}
					System.out.println(sockets.size());
					inStream.close();
					outStream.close();
					client.close();
					break;
				}
				if(startup) {
					sockets.put(message.getSender(), outStream);
					startup = false;
				} else {
					if(message.getType()==2) {
						for(Entry<String, ObjectOutputStream> current: sockets.entrySet()) {
							if(!current.getKey().equals(message.getSender()))
								current.getValue().writeObject(message);
						}
					}
					if(sockets.containsKey(message.getRecipient())) {
							sockets.get(message.getRecipient()).writeObject(message);
					}
				}
			}catch (IOException e) {
				break;
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		}
	}
}