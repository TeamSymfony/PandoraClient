package nl.waldfelt.pandora;

import java.util.ArrayList;
import java.util.HashMap;

import nl.waldfelt.pandora.message.ServerMessage;
import nl.waldfelt.pandora.player.Player;
import nl.waldfelt.pandora.scenemanager.SceneManager;
import nl.waldfelt.pandora.util.AssetLoaderUtil;
import nl.waldfelt.tod6.Character.PlayerSprite;

import org.andengine.entity.IEntity;
import org.andengine.entity.modifier.PathModifier;
import org.andengine.entity.modifier.PathModifier.Path;

public class Discovery {

	private HashMap<String, PlayerSprite> mPlayerSprites;
	private HashMap<String, Player> mPlayers;
	private HashMap<String, ArrayList<Player>> mPlayerUpdateQueue;
	private HashMap<String, DiscoveryUpdateThread> mPlayerUpdateThread;
	
	WorldActivity mContext;
	
	public Discovery(WorldActivity pMain) {
		mContext = pMain;
		
		mPlayerSprites = new HashMap<String, PlayerSprite>();
		mPlayers = new HashMap<String, Player>();
		mPlayerUpdateQueue = new HashMap<String, ArrayList<Player>>();
		mPlayerUpdateThread = new HashMap<String, DiscoveryUpdateThread>();
		
		// Request server to sync
		SceneManager.mWorldScene.getClient().addRequest( new ServerMessage("discovery.sync", null) );
	}
	
	public void add(Player pPlayer) {
		
		if(!pPlayer.equals(SceneManager.mWorldScene.getPlayer())) {
			
			PlayerSprite pSprite = new PlayerSprite(pPlayer.getX(), pPlayer.getY(), AssetLoaderUtil.mPlayerTextureRegion, mContext);
			pSprite.setName(pPlayer.getName()); 
			pSprite.setCullingEnabled(true);
			pSprite.load();
			
			mPlayerSprites.put(pPlayer.getName(), pSprite);
			mPlayerUpdateQueue.put(pPlayer.getName(), new ArrayList<Player>());
			DiscoveryUpdateThread lPlayerUpdateThread = new DiscoveryUpdateThread(pPlayer);
			
			lPlayerUpdateThread.start();
			mPlayerUpdateThread.put(pPlayer.getName(), lPlayerUpdateThread);
			
			mPlayers.put(pPlayer.getName(), pPlayer); 
		}
		
	}
	
	public void remove(Player pPlayer) {

		
		DiscoveryUpdateThread lThread = mPlayerUpdateThread.get(pPlayer.getName());
		mPlayerUpdateThread.remove(pPlayer.getName());
		
		lThread.dispatch();
		if(lThread != null) {
			while(lThread.isAlive()) {}
		}
		
		PlayerSprite lSprite = mPlayerSprites.get(pPlayer.getName()); 
		
		if(lSprite != null) {
			lSprite.detach();
		}
		
		mPlayers.remove(pPlayer.getName());
		mPlayerSprites.remove(pPlayer.getName());
		
		mPlayerUpdateQueue.remove(pPlayer.getName());
		
	}
	
	public void update(Player pPlayer) {
		
		mPlayerUpdateQueue.get(pPlayer.getName()).add(pPlayer);

		
		System.out.println(pPlayer.getPathX().length);
		
		DiscoveryUpdateThread lPlayerUpdateThread = mPlayerUpdateThread.get(pPlayer.getName());
		
		lPlayerUpdateThread.restart();
	}
	
	// ----- Discovery Update Thread
	// Processes movement in another thread per other player
	private class DiscoveryUpdateThread extends Thread {
		private Player mPlayer;
		private PathModifier lMoveModifier;
		private Object mMonitor;
		private boolean isBlocked;
		private boolean isRunning;
		
		public DiscoveryUpdateThread(Player pPlayer) { 
			mPlayer = pPlayer;
			mMonitor = new Object();
			isBlocked = false;
			isRunning = true;
		}
		
		public void restart() {
			if(isBlocked) {
				synchronized(mMonitor) {
					isBlocked = true;
					mMonitor.notifyAll();
				}
			}
		}
		
		public void dispatch() {
			isRunning = false;
			restart();
		}
		
		public void run() {
			while(isRunning) {
				
				while(mPlayerUpdateQueue.get(mPlayer.getName()).size() > 0) {
					
					final Player lPlayer = mPlayerUpdateQueue.get(mPlayer.getName()).remove(0);
					final PlayerSprite lSprite = mPlayerSprites.get(lPlayer.getName());
					
					if(lMoveModifier != null) {
						lMoveModifier.reset();
						lSprite.unregisterEntityModifier(lMoveModifier);
					}
					
					Path lPath = new Path(lPlayer.getPathX(), lPlayer.getPathY());
					
					lMoveModifier = new PathModifier(lPath.getLength() / 75, lPath, new PathModifier.IPathModifierListener() {
		
					@Override
						public void onPathStarted(PathModifier pPathModifier,
								IEntity pEntity) {
							
						}
		
						@Override
						public void onPathWaypointStarted(PathModifier pPathModifier,
								IEntity pEntity, int pWaypointIndex) {
							
								float[] x = pPathModifier.getPath().getCoordinatesX();
								
								if(x[0 + pWaypointIndex] < x[1 + pWaypointIndex]) {
									lSprite.animateWalkRight();
								} else {
									lSprite.animateWalkLeft();
								}
							
						}
						
						@Override
						public void onPathWaypointFinished(PathModifier pPathModifier,
								IEntity pEntity, int pWaypointIndex) {
							
							
							
						}
						
						@Override
						public void onPathFinished(PathModifier pPathModifier,
								IEntity pEntity) {
								// Stop player animation
								lSprite.stopAnimation();
							
								mPlayers.put(lPlayer.getName(), lPlayer);		
							}
							
					});
						
					lMoveModifier.setAutoUnregisterWhenFinished(true);
					lSprite.registerEntityModifier(lMoveModifier);
				}
			
				try {
					synchronized(mMonitor) {
						isBlocked = true;
						mMonitor.wait();
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				
				if(!isRunning) {
					break;
				}
			}
		}
		
	}
}
