package Client;

import java.awt.BorderLayout;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import Game.Game;


@SuppressWarnings("serial")
public class ConnectWindow extends JFrame {
	private JLabel messageLabel;
	
	private JLabel hostnameLabel;
	private JTextField hostnameField;
	private JLabel portLabel;
	private JTextField portField;
	
	private JLabel usernameLabel;
	private JTextField usernameField;
	
	private JButton connectButton;
	
	private String username;
	
	public ConnectWindow() {
		messageLabel = new JLabel(" ");
		
		hostnameLabel = new JLabel("Hostname / IP:");
		hostnameField = new JTextField();
		if(Game.DEBUG)
			hostnameField.setText("localhost");
		hostnameField.setColumns(20);
		portLabel = new JLabel("Port:");
		portField = new JTextField();
		if(Game.DEBUG)
			portField.setText("4808");
		portField.setColumns(5);
		
		usernameLabel = new JLabel("Username:");
		usernameField = new JTextField();
		if(Game.DEBUG)
			usernameField.setText("ben4808");
		usernameField.setColumns(15);
		
		connectButton = new JButton("Connect");
		connectButton.addActionListener(new ConnectButtonListener());
		
		username = null;
		
		JPanel topPanel = new JPanel();
		topPanel.add(messageLabel);
		
		JPanel centerPanel = new JPanel();
		centerPanel.add(hostnameLabel);
		centerPanel.add(hostnameField);
		centerPanel.add(portLabel);
		centerPanel.add(portField);
		centerPanel.add(usernameLabel);
		centerPanel.add(usernameField);
		
		JPanel bottomPanel = new JPanel();
		bottomPanel.add(connectButton);
		
		this.setLayout(new BorderLayout());
	    this.add(topPanel, BorderLayout.NORTH);
	    this.add(centerPanel, BorderLayout.CENTER);
	    this.add(bottomPanel, BorderLayout.SOUTH);
		
		this.setTitle("Connect to Server");
		this.setResizable(false);
		this.setSize(450, 150);
		this.setLocationRelativeTo(null);
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
		this.setVisible(false);
	}
	
	public class ConnectButtonListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			String hostname = hostnameField.getText().trim();
			if(hostname.length() == 0) {
				messageLabel.setText("<html><font color=red>Hostname field needs a value</font></html>");
				return;
			}
			
			String username = usernameField.getText().trim();
			if(username.length() == 0) {
				messageLabel.setText("<html><font color=red>Username field needs a value</font></html>");
				return;
			}
			ConnectWindow.this.username = username;
			
			int port;
			try {
				port = Integer.parseInt(portField.getText().trim());
			}
			catch (NumberFormatException e1) {
				messageLabel.setText("<html><font color=red>Port field needs to be a number 1025-65535</font></html>");
				return;
			}
			if(port < 1025 || port > 65535) {
				messageLabel.setText("<html><font color=red>Port field needs to be a number 1025-65535</font></html>");
				return;
			}
			
			try {
				GameClient.inst().connectToServer(hostname, port);
			}
			catch(IOException e2) {
				messageLabel.setText("<html><font color=red>Could not find server at the specified location.</font></html>");
				return;
			}
			
			messageLabel.setText("<html><font color=red>Connecting...</font></html>");
		}
	}
	
	public String getUsername() { return username; }
	
	public void usernameTaken() {
		messageLabel.setText("<html><font color=red>That username is already in use. Try a different one.</font></html>");
	}
}
