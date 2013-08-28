package Server;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;

import Game.BadFileFormatException;
import Game.FileData;
import Game.Game;
import Game.GameEvent.EventType;
import Game.GameMap;
import Game.Message;
import Game.Player;
import Game.Player.PlayerState;


@SuppressWarnings("serial")
public class ServerWindow extends JFrame {
	private JLabel statusLabel;
	private JLabel statusText;
	private JLabel messageLabel;
	
	private JLabel portLabel;
	private JTextField portField;
	private JButton startButton;
	
	private JLabel playersLabel;
	private JLabel playersText;
	
	private JButton mapButton;
	private JLabel mapLabel;
	
	private static ServerWindow singleton = null;
	
	public static ServerWindow inst() {
		if(singleton == null) singleton = new ServerWindow();
		return singleton;
	}
	
	public ServerWindow() {
		statusLabel = new JLabel();
		statusLabel.setText("Server status: ");
		statusText = new JLabel();
		statusText.setText("<html><font color=red><b>Not Started</b></font></html>");
		messageLabel = new JLabel();
		
		portLabel = new JLabel();
		portLabel.setText("Port (>1024): ");
		portField = new JTextField();
		portField.setColumns(5);
		if(Game.DEBUG)
			portField.setText("4808");
		portField.getDocument().addDocumentListener(new PortFieldListener());
		startButton = new JButton();
		startButton.addActionListener(new StartButtonListener());
		startButton.setText("Start");
		startButton.setEnabled(false);
		
		playersLabel = new JLabel();
		playersLabel.setText("Players:");
		playersText = new JLabel();
		playersText.setText("");
		
		mapButton = new JButton("Load Map");
		mapButton.setEnabled(false);
		mapButton.addActionListener(new LoadMapListener());
		mapLabel = new JLabel("No Map Loaded");
		
		this.setLayout(new BorderLayout());
		this.setSize(500, 225);
		this.add(new TopPanel(), BorderLayout.NORTH);
		this.add(new CenterPanel(), BorderLayout.CENTER);
		this.add(new BottomPanel(), BorderLayout.SOUTH);
		this.setTitle("Server Monitor");
		this.setResizable(false);
		this.setLocationRelativeTo(null);
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
		this.setVisible(true);
		
		updatePlayerList();
		
		if(Game.DEBUG) {
			statusText.setText("<html><font color=blue><b>Starting...</b></font></html>");
			int port = Integer.parseInt(portField.getText());

			GameServer.inst().start(port);
			messageLabel.setText("");
			statusText.setText("<html><font color=green>Server running</font></html>");
			portField.setEnabled(false);
			startButton.setEnabled(false);
			mapButton.setEnabled(true);
		
			String filePath = Game.DEBUG_MAP;
			try {
				FileData.inst().loadMap(filePath);
			}
			catch(IOException e1) {
				mapLabel.setText("<html><font color=red>The selected file count not be opened.</font></html>");
				return;
			}
			catch(BadFileFormatException e2) {
				mapLabel.setText("<html><font color=red>The selected file was not correctly formatted.</font></html>");
				return;
			}
    	
			GameMap map = Game.inst().getMap();
			mapButton.setEnabled(false);
			mapLabel.setText("Map Loaded: " + map.getName());
			GameServer.inst().sendAll(new Message(EventType.MAP).add(map));
			GameServer.inst().checkForGameStart();
		}
	}
	
	public class TopPanel extends JPanel {
		public TopPanel() {
			JPanel statusPanel = new JPanel();
			statusPanel.add(statusLabel);
			statusPanel.add(statusText);
			
			JPanel portPanel = new JPanel();
			portPanel.add(portLabel);
			portPanel.add(portField);
			portPanel.add(startButton);
			
			this.setLayout(new BorderLayout());
			this.add(statusPanel, BorderLayout.CENTER);
			this.add(portPanel, BorderLayout.EAST);
			this.add(messageLabel, BorderLayout.SOUTH);
		}
	}
	
	public class CenterPanel extends JPanel {
		public CenterPanel() {
			this.setLayout(new GridLayout(0, 1));
			this.setAlignmentY(CENTER_ALIGNMENT);
			this.add(playersLabel);
			this.add(playersText);
		}
	}
	
	public class BottomPanel extends JPanel {
		public BottomPanel() {
			this.setLayout(new GridLayout(0, 1));
			JPanel buttonPanel = new JPanel();
			buttonPanel.add(mapButton);
			this.add(buttonPanel);
			JPanel labelPanel = new JPanel();
			labelPanel.add(mapLabel);
			this.add(labelPanel);
		}
	}
	
	public class PortFieldListener implements DocumentListener {
		@Override public void insertUpdate(DocumentEvent e) { processChange(); }
		@Override public void removeUpdate(DocumentEvent e) { processChange(); }
		@Override public void changedUpdate(DocumentEvent e) { processChange(); }
		
		void processChange() {
			try {
				int portNo = Integer.parseInt(portField.getText());
				startButton.setEnabled(portNo >= 1024 && portNo < 65536);
			}
			catch (NumberFormatException ex) {
				startButton.setEnabled(false);
			}
		}
	}
	
	public class StartButtonListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			statusText.setText("<html><font color=blue><b>Starting...</b></font></html>");
			int port = Integer.parseInt(portField.getText());

			GameServer.inst().start(port);
			messageLabel.setText("");
			statusText.setText("<html><font color=green>Server running</font></html>");
			portField.setEnabled(false);
			startButton.setEnabled(false);
			mapButton.setEnabled(true);
		}
	}
	
	public class LoadMapListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			JFileChooser chooser = new JFileChooser();
		    FileNameExtensionFilter filter = new FileNameExtensionFilter("Map Files", "txt");
		    chooser.setFileFilter(filter);
		    chooser.setCurrentDirectory(new File(FileData.DATA_DIRECTORY));
		    int returnVal = chooser.showOpenDialog(null);
		    if(returnVal == JFileChooser.APPROVE_OPTION) {
		    	String filePath = chooser.getSelectedFile().getName();
		    	try {
		    		FileData.inst().loadMap(filePath);
		    	}
		    	catch(IOException e1) {
		    		mapLabel.setText("<html><font color=red>The selected file count not be opened.</font></html>");
		    		return;
		    	}
		    	catch(BadFileFormatException e2) {
		    		mapLabel.setText("<html><font color=red>The selected file was not correctly formatted.</font></html>");
		    		return;
		    	}
		    	
		    	GameMap map = Game.inst().getMap();
		    	mapButton.setEnabled(false);
		    	mapLabel.setText("Map Loaded: " + map.getName());
		    	GameServer.inst().sendAll(new Message(EventType.MAP).add(map));
		    	GameServer.inst().checkForGameStart();
		    }
		}
	}
	
	public void updatePlayerList() {
		String str = "<html>";
		for(int i=0; i < Game.MAX_PLAYERS; i++) {
			Player player = Game.inst().nthPlayer(i);
			str += Integer.toString(i+1) + ": ";

			PlayerState state = player.getState();
			switch(state) {
			case EMPTY: 
				str += "OPEN"; 
				break;
			case NOT_READY:
				str += player.getUsername() + " (Not Ready)"; 
				break;
			case READY:
				str += player.getUsername() + " (Ready)"; 
				break;
			case PLAYING:
				str += player.getUsername() + " (Playing)"; 
				break;
			case DISCONNECTED:
				str += player.getUsername() + " (DISCONNECTED)";
				break;
			}
			str += "<br>";
		}
		str += "</html>";
		playersText.setText(str);
	}
	
	public void serverErrorOccurred() {
		messageLabel.setText("<html><font color=red>ERROR: The server encountered an error and has been stopped.</font></html>");
		statusText.setText("<html><font color=red>Not Started</font></html>");
		portField.setEnabled(false);
		startButton.setEnabled(false);
	}
}
