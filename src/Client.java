import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

/*
 * 
 * @author Liam Scott
 *
 */

public class Client {
	
	private Socket socket;
	private String username;
	private String host;
	private static final int PORT = 8080;
	private ObjectInputStream inStream;
	private ObjectOutputStream outStream;
	private ClientGUI gui;
	private boolean connectionStatus;

	private static Map<String, String> conversations;
	
	public Client(ClientGUI gui, String host) {
		this.gui = gui;
		this.host = host;
		if(openSocket()) {
			new Thread(new ReceiveMessage()).start();	
			new Thread(new ClientsListThread()).start();
			gui.getListModel().add(0, "All clients");
			conversations = new HashMap<String, String>();
			connectionStatus = true;
		} else {
			connectionStatus = false;
		}
	}
	
	public boolean getConnectionStatus() {
		return connectionStatus;
	}
	
	public boolean openSocket(){
	   try{
		   socket = new Socket(host, PORT);
		   outStream = new ObjectOutputStream(socket.getOutputStream());
		   return true;
	   } catch (UnknownHostException e) {
		   return false;
	   } catch (IOException e) {
		   return false;
	   }
	}
	
	public void closeSocket() {
		try {
			inStream.close();
			outStream.close();
			socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.exit(0);
	}
	
	public ObjectOutputStream getOutputStream() {
		return outStream;
	}

	public void send(Message message) {
		try {
			if(message.getType()==1 || (message.getType()==2 && message.getRecipient().equals("Server"))) {
				getOutputStream().writeObject(message);
				getOutputStream().flush();
			} else {
				String printWord = "";
				if(message.getType()==2)
					printWord = "Broadcasted: ";
				else
					printWord = "Sent: ";
				String recipient = message.getRecipient();
				if(!conversations.containsKey(recipient))
					conversations.put(recipient, "");
				String currentMessages = conversations.get(recipient);
				conversations.replace(recipient, currentMessages+printWord+
						message.getMessage()+System.lineSeparator());
				getOutputStream().writeObject(message);
				getOutputStream().flush();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void setUsername(String username) {
		this.username = username;
	}
	
	private class ClientsListThread implements Runnable {
		
		private DatagramSocket dataSocket;
		
		public ClientsListThread() {
			dataSocket = null;		
			try {
				dataSocket = new DatagramSocket();
			} catch (SocketException e) {
				e.printStackTrace();
			}
		}
		
		@SuppressWarnings("unchecked")
		public void run() {
			 while (true) {  
			 	byte[] buffer = new byte[256];
			 	InetAddress address;
				try {
					address = InetAddress.getByName(host);
					 DatagramPacket packet = new DatagramPacket(buffer, buffer.length, address, 4445);
					 dataSocket.send(packet);
					 packet = new DatagramPacket(buffer, buffer.length);
					 dataSocket.receive(packet);
					 ObjectInputStream input = new ObjectInputStream(new ByteArrayInputStream(buffer));
					 ArrayList<String> clients = (ArrayList<String>) input.readObject();
					 for(String current: clients) {
						 if(!gui.getListModel().contains(current) && !current.equals(username)) {
							 gui.getListModel().addElement(current);
						 }
					 }
					 Thread.sleep(1000);
				} catch (UnknownHostException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}				
			}
		}
	}
	
	public void refreshConversations() {
		int index = gui.getJList().getSelectedIndex();
		if(index>=0)
			gui.getTextArea().setText(conversations.get(gui.getListModel().getElementAt(index)));
		else {
			gui.getTextArea();
			gui.getMainLabel().setText("Hi "+username+", select a client to start chatting");
		}
	}
	
	private class ReceiveMessage implements Runnable {		
		public void run() {
			try {
				inStream = new ObjectInputStream(socket.getInputStream());
			} catch (IOException e) {
				e.printStackTrace();
			}
			while(true) {
				try{
					Message message = (Message) inStream.readObject();
					if(message.getType()==1) {
						JOptionPane.showMessageDialog(new JFrame("A client has disconnected"), message.getSender()+
								" has disconnected from the server");
						conversations.remove(message.getSender());
						gui.getListModel().removeElement(message.getSender());
						refreshConversations();
						continue;
					}
					String sender = message.getSender();
					String newLine = System.lineSeparator();
					if(!conversations.containsKey(sender))
						conversations.put(message.getSender(), "");
					String currentMessages = conversations.get(sender);
					if(message.getType()==2)
						conversations.replace(sender, currentMessages+sender+" broadcasted: "+message.getMessage()+newLine);
					else
						conversations.replace(sender, currentMessages+sender+": "+message.getMessage()+newLine);
					refreshConversations();
				} catch (IOException e){
					System.exit(0);
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
