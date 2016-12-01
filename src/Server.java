import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.border.EmptyBorder;

/*
 * 
 * @author Liam Scott
 *
 */

@SuppressWarnings("serial")
public class Server extends JPanel{
	
	private ServerSocket server;
	private JTextArea textArea;
	private JLabel label;
	private final static int PORT = 8080;
	private static Map<String, ObjectOutputStream> sockets;
	
	public Server() {
		super();
		setLayout(new GridLayout(2,1));
		setPreferredSize(new Dimension(300,50));
		setVisible(true);		
		setBorder(new EmptyBorder(0,10,10,10));
		sockets = new HashMap<String, ObjectOutputStream>();
		textArea = new JTextArea();
		textArea.setWrapStyleWord(true);
		textArea.setBackground(Color.lightGray);
		label = new JLabel("Chat Server Active");
		label.setHorizontalAlignment(JLabel.CENTER);
		add(label);
		add(textArea);		
		
		JFrame frame = new JFrame("Server");
		frame.setVisible(true);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().add(this);
		frame.pack();
		frame.setLocationRelativeTo(null);
		
		new Thread(new SocketThread()).start();
		new Thread(new SendUsers()).start();
	}
	
	public Map<String, ObjectOutputStream> getMap() {
		return sockets;
	}
	
	private class SendUsers implements Runnable {
		
		private DatagramSocket datagramSocket;
		
		public SendUsers() {
		    try {
				datagramSocket = new DatagramSocket(4445);
			} catch (SocketException e) {
				e.printStackTrace();
			}
		}
	
		public void run() {
			while(true) {
				textArea.setText(" - Number of clients: "+Server.sockets.size()+"\n");
				if(!sockets.isEmpty()) {
					byte[] buffer = new byte[256];
					// Create DatagramPacket and receive request for client side list update
					DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
					try {
						datagramSocket.receive(packet);
					} catch (IOException e) {
						e.printStackTrace();
					}
					buffer = convertUsernamesToBuffer();				    
				    InetAddress address = packet.getAddress();
				    int port = packet.getPort();
				    packet = new DatagramPacket(buffer, buffer.length, address, port);
				    try {
						datagramSocket.send(packet);
						Thread.sleep(500);
					} catch (IOException e) {
						e.printStackTrace();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}		    
				}
			}
		}
	}
	
	public byte[] convertUsernamesToBuffer() {
		// Package usernames currently in sockets map to a byte array
		ArrayList<String> usernames = new ArrayList<String>();
		for(String current: sockets.keySet()) {
			usernames.add(current);
		}
	    ByteArrayOutputStream out = new ByteArrayOutputStream();
	    ObjectOutputStream outputStream;
		try {
			outputStream = new ObjectOutputStream(out);
			outputStream.writeObject(usernames);
		    outputStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	   return out.toByteArray();
	}
	
	private class SocketThread implements Runnable {
		
		public void run(){
			try{
				server = new ServerSocket(PORT);
			} catch (IOException e) {
				System.out.println("Can't listen on port "+PORT);
				System.exit(0);
			}
			while(true){
				// Listens for incoming connections and creates ServerThread for each one
				ServerThread s;
				try{
					Socket sock = server.accept();
					s = new ServerThread(sock, sockets);
					Thread thread = new Thread(s);
					thread.start();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	public static void main(String [] args) {
		new Server();
	}
}
