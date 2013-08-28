package Client;

import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import Game.GameEvent.EventType;
import Game.Message;


@SuppressWarnings("serial")
public class GameWindow extends JFrame {
	private MapPanel mapPanel;
	private SidePanel sidePanel;
	
	private JLabel statusLabel;
	private JScrollPane statusPane;
	private JTextArea statusText;
	
	private JLabel chatLabel;
	private JScrollPane chatPane;
	private JTextArea chatText;
	private JTextField chatEntry;
	
	public GameWindow() {
		this.setLayout(null);
		
		mapPanel = new MapPanel();
		mapPanel.setLocation(5, 5);
		this.add(mapPanel);
		
		sidePanel = new SidePanel();
		sidePanel.setLocation(545, 5);
		this.add(sidePanel);
		
		statusLabel = new JLabel("Game Log");
		statusLabel.setFont(Assets.headerFont2);
		statusLabel.setSize(100, 25);
		statusLabel.setLocation(820, 500);
		this.add(statusLabel);
		
		statusText = new JTextArea();
		statusText.setEditable(false);
		statusText.setLineWrap(true);
		statusPane = new JScrollPane(statusText);
		statusPane.setSize(260, 170);
		statusPane.setLocation(815, 520);
		this.add(statusPane);
		
		chatLabel = new JLabel("Chat");
		chatLabel.setFont(Assets.headerFont2);
		chatLabel.setSize(100, 25);
		chatLabel.setLocation(545, 500);
		this.add(chatLabel);
		
		chatText = new JTextArea();
		chatText.setEditable(false);
		chatText.setLineWrap(true);
		chatPane = new JScrollPane(chatText);
		chatPane.setSize(260, 145);
		chatPane.setLocation(545, 520);
		this.add(chatPane);
		
		chatEntry = new JTextField();
		chatEntry.addKeyListener(new ChatLineListener());
		chatEntry.addFocusListener(new ChatFocusListener());
		chatEntry.setSize(260, 25);
		chatEntry.setLocation(545, 665);
		this.add(chatEntry);
		
		this.setSize(1087, 721);
		this.setResizable(false);
		this.setTitle("PvPExile");
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
		this.setVisible(false);
	}
	
	public MapPanel getMapPanel() { return mapPanel; }
	public SidePanel getSidePanel() { return sidePanel; }
	
	public void addStatus (String msg) { addStatus(msg, 0); }
	
	public void addStatus (String msg, int indent) {
		String line = "";
		for(int i=0; i<indent; i++) line += "  ";
		line += msg;
		
		statusText.append(line + "\n");
		JScrollBar vertical = statusPane.getVerticalScrollBar();
		vertical.setValue( vertical.getMaximum() );
	}
	
	public void addChat (String sender, String msg, boolean isPlayer) {
		String line = sender;
		if(!isPlayer) line += " (Observer)";
		line += ": " + msg;
		
		chatText.append(line + "\n");
		JScrollBar vertical = chatPane.getVerticalScrollBar();
		vertical.setValue( vertical.getMaximum() );
	}
	
	public class ChatLineListener implements KeyListener {
		@Override
		public void keyPressed(KeyEvent e) {
			if(e.getKeyCode() == KeyEvent.VK_ENTER) {
				String line = chatEntry.getText().trim();
				chatEntry.setText("");
				if(line.length() == 0) return;
				GameClient.inst().sendMessage(new Message(EventType.CHAT).add(line));
			}
		}

		public void keyReleased(KeyEvent e) {}
		public void keyTyped(KeyEvent e) {}
	}
	
	public class ChatFocusListener implements FocusListener {
		public void focusGained(FocusEvent e) {}

		@Override
		public void focusLost(FocusEvent e) {
			mapPanel.requestFocusInWindow();
		}
	}
}
