package Client;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.filechooser.FileNameExtensionFilter;

import Game.BadFileFormatException;
import Game.FileData;
import Game.Game;
import Game.GameEvent.EventType;
import Game.GameMap;
import Game.Message;
import Game.Monster;
import Game.Player;
import Game.Player.PlayerState;


@SuppressWarnings("serial")
public class PartyWindow extends JFrame {
	private JButton loadPartyButton;
	private JButton readyButton;
	
	private boolean ready;
	
	private JLabel messageLabel;
	
	private PartyPanel partyPanel;
	
	private CountdownTimer countdownTimer;
	
	public PartyWindow() {
		loadPartyButton = new JButton("Load Party...");
		loadPartyButton.addActionListener(new LoadPartyListener());
		loadPartyButton.setEnabled(false);
		
		readyButton = new JButton("Ready");
		readyButton.addActionListener(new ReadyListener());
		readyButton.setEnabled(false);
		
		ready = false;
		
		messageLabel = new JLabel(" ");
		
		partyPanel = new PartyPanel();
		
		countdownTimer = null;
		
		JPanel buttonsPanel = new JPanel();
		buttonsPanel.add(loadPartyButton);
		buttonsPanel.add(readyButton);
		
		JPanel bottomPanel = new JPanel(new GridLayout(0, 1));
		bottomPanel.add(buttonsPanel);
		bottomPanel.add(messageLabel);
		
		this.setLayout(new BorderLayout());
		this.add(partyPanel, BorderLayout.CENTER);
		this.add(bottomPanel, BorderLayout.SOUTH);
		
		this.setSize(500, 500);
		this.setResizable(false);
		this.setLocationRelativeTo(null);
		this.setTitle("Load Party");
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
		this.setVisible(false);
	}
	
	public class PartyPanel extends JPanel {
		@Override
		public void paintComponent(Graphics g) {
			g.setColor(Color.white);
			g.fillRect(0, 0, getWidth(), getHeight());
			
			Game game = Game.inst();
			
			// player list
			g.setColor(Color.black);
			for(int i=0; i < Game.MAX_PLAYERS; i++) {
				int x = 100, y = 30 + 25 * i;
				Player party = game.nthPlayer(i);
				g.drawString(Integer.toString(i+1) + ". ", x, y);
				String username = party != null ? party.getUsername() : "OPEN";
				g.drawString(username, x + 20, y);
				if(party != null) {
					String readyStr = party.getState() == PlayerState.READY ? "Ready" : "Not Ready";
					g.drawString(readyStr, x + 220, y);
				}
			}
			
			// map
			GameMap map = Game.inst().getMap();
			String mapName = "Map: " + (map == null ? "Not Loaded" : map.getName());
			g.drawString(mapName, 100, 125);
			
			if (game.isRunning() || GameClient.inst().isObserver()) return;
			
			// party
			Player party = GameClient.inst().yourParty();
			for(int i=0; i<party.partySize(); i++) {
				Monster monster = party.nthMonster(i);
				int x = 150*(i%3) + 50;
				int y = 100*(i/3) + 160;
				int graphic = monster.getGraphic();
				Point dst = new Point(x, y + 10);
				Point src = Assets.monsterPoint(graphic, false);
				g.drawImage(Assets.monsterImg, dst.x, dst.y, dst.x + MapPanel.SQ_WIDTH, dst.y + MapPanel.SQ_HEIGHT, 
						src.x, src.y, src.x + MapPanel.SQ_WIDTH, src.y + MapPanel.SQ_HEIGHT, null);
				
				g.drawString(monster.getName(), x, y);
				g.drawString("HP:", x + 35, y+20);
				g.drawString(Integer.toString(monster.getMaxHealth()), x + 60, y+20);
				g.drawString("SP:", x + 35, y+40);
				g.drawString(Integer.toString(monster.getBaseSpellPoints()), x + 60, y+40);
			}
		}
	}
	
	public class LoadPartyListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			JFileChooser chooser = new JFileChooser();
		    FileNameExtensionFilter filter = new FileNameExtensionFilter("Party Files", "txt");
		    chooser.setFileFilter(filter);
		    chooser.setCurrentDirectory(new File(FileData.DATA_DIRECTORY));
		    int returnVal = chooser.showOpenDialog(null);
		    if(returnVal == JFileChooser.APPROVE_OPTION) {
		    	String filePath = chooser.getSelectedFile().getName();
		    	try {
		    		FileData.inst().loadParty(GameClient.inst().getPlayerId(), filePath);
		    	}
		    	catch(IOException e1) {
		    		messageLabel.setText("<html><font color=red>The selected file count not be opened.</font></html>");
		    		return;
		    	}
		    	catch(BadFileFormatException e2) {
		    		messageLabel.setText("<html><font color=red>The selected file was not correctly formatted.</font></html>");
		    		return;
		    	}
		    	
		    	readyButton.setEnabled(true);
		    	repaint();
		    }
		}
	}
	
	public class ReadyListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			GameClient client = GameClient.inst();
			ready = !ready;
			if(ready)
				client.sendMessage(new Message(EventType.READY).add(client.yourParty().getMonsters()));
			else
				client.sendMessage(new Message(EventType.NOT_READY));
			readyButton.setText(ready ? "Not Ready" : "Ready");
		}
	}
	
	public void waitingForMap() {
		messageLabel.setText("Waiting for a map before play can start...");
	}
	
	public void startGameCountdown() {
		countdownTimer = new CountdownTimer();
	}
	
	public void stopGameCountdown() {
		if(countdownTimer != null) {
			countdownTimer.cancel();
			countdownTimer = null;
			messageLabel.setText("Start Cancelled... Player not ready.");
		}
	}
	
	public class CountdownTimer {
		private Timer timer;
		private int secondsLeft;
		
		public CountdownTimer() {
			secondsLeft = 5;
			
			timer = new Timer();
			timer.schedule(new TimerTask() {
				public void run() {
					messageLabel.setText("Game will start in " + Integer.toString(secondsLeft) + " seconds...");
					if(secondsLeft <= 0) this.cancel();
					else secondsLeft--;
				}
			}, 0, 1000);
		}
		
		public void cancel() { timer.cancel(); }
	}
	
	public void initialize() {
		if(!GameClient.inst().isObserver()) loadPartyButton.setEnabled(true);
		this.setVisible(true);
	}
}
