package nl.waldfelt.pandora.message;

import java.util.ArrayList;

import nl.waldfelt.pandora.player.Player;
import nl.waldfelt.pandora.scenemanager.SceneManager;
import android.os.Build;

public class MessageHandler extends Thread {
	
	public static final long HANDLER_THROTTLE = 5;
	
	private Client mClient;
	private ArrayList<ClientMessage> mTasks;
	
	public MessageHandler(Client pClient) {
		mClient = pClient;
		
		mTasks = new ArrayList<ClientMessage>(); 
		
	}
	
	private ServerMessage getReply(ClientMessage pClientMessage) {
		String pCommand = pClientMessage.getCommand();
		Object pData = pClientMessage.getData();
		
		// If server asks for identification, send phone model
		if(pCommand.equals("identify")) {
			return new ServerMessage(pCommand, Build.MODEL);
		}
		
		// Receive player data from Server's permanent storage
		if(pCommand.equals("loadPlayerData")) {
			if(pData instanceof Player)	 {
				
				Player player = new Player();
				
				if(pData != null) {
					player = (Player) pData;
				}
				
				SceneManager.mWorldScene.setPlayer(  player );
			}
			return null;
		}
		
		if(pCommand.equals("move")) {
			return null;
		}
		
		// If client receives ping message from the server, echo pong! locally
		if(pCommand.equals("ping")) {
			System.out.println("pong!");
			return null;
		}
		
		
		
		if(pCommand.equals("discovery.add")) {
			if(pData instanceof Player) { 
				Player lPlayer = (Player) pData;
				SceneManager.mWorldScene.getDiscovery().add(lPlayer);
			}
			
			return null;
		}
		
		if(pCommand.equals("discovery.remove")) {
			if(pData instanceof Player) {
				Player lPlayer = (Player) pData;
				SceneManager.mWorldScene.getDiscovery().remove(lPlayer);
			}
			
			return null;
		}
		
		if(pCommand.equals("discovery.update")) {
			if(pData instanceof Player) {
				Player lPlayer = (Player) pData;
				SceneManager.mWorldScene.getDiscovery().update(lPlayer);
			}
			
			return null;
		}
		
		return null;
	}

	public void addTask(ClientMessage mClientMessage) {
		mTasks.add(mClientMessage);
	}
	
	@Override
	public void run() {
		
		while(true) {
			
			if(!mTasks.isEmpty()) {
				ServerMessage response = getReply(mTasks.remove(0));
				
				if(response != null)
					mClient.addRequest(response);
			}
			
			try {
				Thread.sleep(HANDLER_THROTTLE);
			} catch(InterruptedException e) {
				e.printStackTrace();
			}
			
		}
	}
}
