import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/*
 * 
 * @author Liam Scott
 *
 */

@SuppressWarnings("serial")
public class ClientGUI extends JPanel implements ActionListener {
	
	private Client client;
	private JPanel typePanel, sub;
	private JTextField textField, hostNameField;
	private JTextArea messages;
	private JButton send;
	private JButton disconnect;
	private JLabel label;
	private JScrollPane messagePane;
	private static JList<String> clientList;
	private static DefaultListModel<String> listModel;
	private boolean startup;
	private String username, host, currentClient;
	private Color backgroundColor;
	private ArrayList<Color> colours;
	
	public ClientGUI() {
		super();
		setLayout(new BorderLayout());
		setPreferredSize(new Dimension(625,300));
		setBackground(Color.LIGHT_GRAY);
		setVisible(true);	
		
		initComponents();
		JPanel leftPanel = new JPanel();
		leftPanel.setPreferredSize(new Dimension(150, 300));
		leftPanel.setBackground(Color.DARK_GRAY);
		leftPanel.setBorder(new EmptyBorder(7,10,10,10));
		JLabel clientsLabel = new JLabel("Clients");
		clientsLabel.setForeground(Color.WHITE);
		clientsLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
		clientsLabel.setAlignmentX(CENTER_ALIGNMENT);
		JPanel rightPanel = new JPanel();
		rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
		rightPanel.setPreferredSize(new Dimension(450, 300));
		JPanel fillY = new JPanel();
		fillY.setPreferredSize(new Dimension(25,300));
		fillY.setBackground(Color.LIGHT_GRAY);
		JPanel fillXTop = new JPanel();
		fillXTop.setPreferredSize(new Dimension(450, 25));
		fillXTop.setBackground(Color.LIGHT_GRAY);	
		JPanel fillXBottom = new JPanel();
		fillXBottom.setPreferredSize(new Dimension(450, 25));
		fillXBottom.setBackground(Color.LIGHT_GRAY);		
		
		JScrollPane scrollList = new JScrollPane(clientList);
		scrollList.setPreferredSize(new Dimension(130, 200));	
		leftPanel.add(clientsLabel);
		leftPanel.add(scrollList);
		leftPanel.add(disconnect);
		rightPanel.add(fillXTop);
		rightPanel.add(sub);
		rightPanel.add(fillXBottom);		
		add(fillY, BorderLayout.WEST);
		add(leftPanel, BorderLayout.CENTER);
		add(rightPanel, BorderLayout.EAST);		
		backgroundColor = Color.GRAY;
		startup = true;
		username = "";
		currentClient = "";
	}
	
	public void initComponents() {
		colours = new ArrayList<Color>();
		colours.add(new Color(100,149,237));	
		colours.add(new Color(176,196,222));
		colours.add(new Color(202,225,255));
		colours.add(new Color(162,181,205));
		colours.add(new Color(61,89,171));			
		textField = new JTextField(28);
		textField.addActionListener(this);
		hostNameField = new JTextField(28);
		hostNameField.addActionListener(this);
		hostNameField.setText("localhost");
		messages = new JTextArea();
		messages.setEditable(false);
		messages.setVisible(false);
		send = new JButton("Send");
		send.addActionListener(this);
		disconnect = new JButton("Disconnect");
		disconnect.addActionListener(this);
		label = new JLabel("Enter a username & host below");
		label.setAlignmentX(JLabel.CENTER_ALIGNMENT);	
		label.setForeground(Color.black);
		label.setFont(new Font("SansSerif", Font.PLAIN, 16));
		listModel = new DefaultListModel<String>();
		clientList = new JList<String>(listModel);
		clientList.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
		clientList.setLayoutOrientation(JList.VERTICAL);	
		clientList.setEnabled(false);
		clientList.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent event) {
				listChanged();
			}
		}); 
		typePanel = new JPanel();
		typePanel.add(textField);
		typePanel.add(hostNameField);
		typePanel.add(send);
		typePanel.setBackground(backgroundColor);
		sub = new JPanel();
		sub.setLayout(new BoxLayout(sub, BoxLayout.Y_AXIS));
		sub.setPreferredSize(new Dimension(450,250));
		sub.setBackground(backgroundColor);
		sub.setBorder(new EmptyBorder(5,10,0,10));	
		messagePane = new JScrollPane(messages, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
	            JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		messagePane.setPreferredSize(new Dimension(430, 120));
		messagePane.setVisible(false);
		sub.add(label);
		sub.add(messagePane);
		sub.add(typePanel);
	}
	
	public void listChanged() {
		int index = clientList.getSelectedIndex();
		if(index!=-1) {
			currentClient = listModel.getElementAt(index);
			if(currentClient.equals("All clients"))
				send.setText("Broadcast");
			else
				send.setText("Send");
			backgroundColor = colours.get(index);			
			label.setText("Hi "+username+", you're chatting with - "+currentClient);
			setColours();
			client.refreshConversations();
		}
	}
	
	public DefaultListModel<String> getListModel() {
		return listModel;
	}
	
	public JList<String> getJList() {
		return clientList;
	}
	
	public JTextArea getTextArea() {
		return messages;
	}
	
	public JLabel getMainLabel() {
		return label;
	}
	
	public String getHost() {
		return host;
	}
	
	public void setColours() {
		sub.setBackground(backgroundColor);
		typePanel.setBackground(backgroundColor);
	}
	
	public void startup() {
		username = textField.getText();
		host = hostNameField.getText();
		client = new Client(this, host);		
		if(client.getConnectionStatus()) {
			typePanel.remove(hostNameField);
			if(listModel.contains(username)) {
				showErrorMessage("Please enter a unique username");
				return;
			}			
			Message message = new MessageTo(username, "Server", "");
			client.send(message);
			client.setUsername(username);
			messages.setVisible(true);
			messagePane.setVisible(true);
			clientList.setEnabled(true);
			label.setText("Hi "+username+", select a client to start chatting");
			startup = false;
			textField.setText("");
		} else {
			if(host.equals("localhost")) {
				int dialogType = JOptionPane.YES_NO_OPTION;
				int dialogResult = JOptionPane.showConfirmDialog (null, 
						"The server does not exist, would you like to create one?" , "Error", dialogType);
				if(dialogResult == JOptionPane.YES_OPTION)
					new Server();
			}
			else
				JOptionPane.showMessageDialog(new JFrame("Error"), "The server does not exist");
		}
	}
	
	public void disconnect() {
		Message message = new DisconnectMessage(username);
		client.send(message);
		client.closeSocket();
		System.exit(0);
	}
	
	public void showErrorMessage(String messageContents) {
		JOptionPane.showMessageDialog(new JFrame(), messageContents, "Error", JOptionPane.ERROR_MESSAGE);
	}

	public void actionPerformed(ActionEvent event) {
		Object source = event.getSource();
		if(source==disconnect) {
			disconnect();
		}
		if(textField.getText().isEmpty()) {
			String content;
			if(startup)
				content = "Username";
			else
				content = "Message contents ";
			showErrorMessage(content+" can't be empty");
			return;
		}
		if(source == send){			
			if(startup) {
				startup();
			}
			else {
				if(clientList.getSelectedIndex()<0 && !startup) {
					showErrorMessage("Please select a client to send a message");
					return;
				}
				Message message;
				if(currentClient.equals("All clients"))
					message = new BroadcastMessage(username, "All clients", textField.getText());
				else
					message = new MessageTo(username, currentClient, textField.getText());
				client.send(message);
				client.refreshConversations();
				textField.setText(new String(""));
			}			
		}
	}

	public static void main(String [] args) {
		ClientGUI panel = new ClientGUI();
		JFrame frame = new JFrame("DMS Assignment 1");
		frame.setVisible(true);
		frame.setResizable(false);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().add(panel);
		frame.pack();
		frame.setLocationRelativeTo(null);
	}
}