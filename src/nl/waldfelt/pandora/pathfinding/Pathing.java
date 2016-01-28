package nl.waldfelt.pandora.pathfinding;

import java.util.ArrayList;

import nl.waldfelt.pandora.WorldActivity;
import nl.waldfelt.pandora.message.ServerMessage;
import nl.waldfelt.pandora.scenemanager.SceneManager;
import nl.waldfelt.tod6.Character.PlayerSprite;

import org.andengine.entity.IEntity;
import org.andengine.entity.modifier.PathModifier;
import org.andengine.entity.modifier.PathModifier.Path;
import org.andengine.extension.tmx.TMXLayer;
import org.andengine.extension.tmx.TMXTile;
import org.andengine.util.Constants;

public class Pathing {
	private TMXLayer mTouchLayer;
	private PlayerSprite mPlayerSprite;
	
	private PathModifier lMoveModifier;
	private PathFinder mPathFinder;
	
	private ArrayList<TMXTile> mObstacles;
	
	public Pathing( WorldActivity pMain ) {
		mTouchLayer = SceneManager.mWorldScene.getTouchLayer();
	}
	
	public void movePlayerInBackground(final float pX, final float pY) {
			if(mPlayerSprite == null) {
				mPlayerSprite = SceneManager.mWorldScene.getPlayerSprite();
			}
			
			new Thread( new Runnable() {

				@Override
				public void run() {
					movePlayer(pX, pY);
				}
				
			}).start();
			

	}
	
	public void movePlayer(float pX, float pY) {
		
		if(validCoordinates(pX, pY)) {
			
			// Capture necessary position variables
			float targetX = pX;
			float targetY = pY;
			

			// Update position of target rectangle
			TMXTile lSelectedTile = SceneManager.mWorldScene.getTouchLayer().getTMXTileAt(targetX, targetY);			
			

			float[] lPlayerCoordinates = mPlayerSprite.convertLocalToSceneCoordinates(mPlayerSprite.getWidth() / 2, mPlayerSprite.getHeight());
			
			TMXTile lCurrentTile = SceneManager.mWorldScene.getTouchLayer().getTMXTileAt(lPlayerCoordinates[Constants.VERTEX_INDEX_X], lPlayerCoordinates[Constants.VERTEX_INDEX_Y]);
			
			
			// Calculate Path by Localized AStar
			mPathFinder = new PathFinder(SceneManager.mWorldScene.getTiledMap(), mObstacles, lCurrentTile, lSelectedTile);
			// Start thread to calculate path
			mPathFinder.start();
			
			//long startTime = System.nanoTime();
			while(mPathFinder.isRunning()) {};
			//System.out.println((System.nanoTime() - startTime) / 1000000);
			
			// Interrupt current routine if necessary
			if(lMoveModifier != null) {
				lMoveModifier.reset();
				mPlayerSprite.clearEntityModifiers();
				mPlayerSprite.stopAnimation();
			}
			
			Path lPath = mPathFinder.getPath();
			
			
			if(lPath.getSize() > 1) {
			
				// Create simple path from current to target position
				//Path path = new Path(2).to(currentX, currentY).to(targetX - mPlayerSprite.getWidth() / 2, targetY - mPlayerSprite.getHeight());
				// MoveModifier handler
				
				lMoveModifier = new PathModifier(lPath.getLength() / 75, lPath, new PathModifier.IPathModifierListener() {
					
					@Override
					public void onPathWaypointStarted(PathModifier pPathModifier,
							IEntity pEntity, int pWaypointIndex) {

						float[] x = pPathModifier.getPath().getCoordinatesX();
						
						if(x[0 + pWaypointIndex] < x[1 + pWaypointIndex]) {
							mPlayerSprite.animateWalkRight();
						} else {
							mPlayerSprite.animateWalkLeft();
						}
						
					}
					
					@Override
					public void onPathWaypointFinished(PathModifier pPathModifier,
							IEntity pEntity, int pWaypointIndex) {
						

						float[] x = pPathModifier.getPath().getCoordinatesX(); 
						float[] y = pPathModifier.getPath().getCoordinatesY();
						TMXTile lCurrentTile = SceneManager.mWorldScene.getTouchLayer().getTMXTileAt(x[pWaypointIndex + 1] + mPlayerSprite.getWidth() / 2, y[pWaypointIndex + 1] + mPlayerSprite.getHeight());
					
						updatePlayerPosition(lCurrentTile.getTileColumn(), lCurrentTile.getTileRow());
						
						
					}
					
					@Override
					public void onPathStarted(PathModifier pPathModifier, IEntity pEntity) {
						float[][] lPathVector = {pPathModifier.getPath().getCoordinatesX(), pPathModifier.getPath().getCoordinatesY()};
						
						
						SceneManager.mWorldScene.getClient().addRequest( new ServerMessage("discovery.update", lPathVector) );
					}
					
					@Override
					public void onPathFinished(PathModifier pPathModifier, IEntity pEntity) {
						// Stop player animation
						mPlayerSprite.stopAnimation();
					}
				});
				
				lMoveModifier.setAutoUnregisterWhenFinished(true);
				mPlayerSprite.registerEntityModifier(lMoveModifier);
			}
		}		
	}
	
	private void updatePlayerPosition(int pX, int pY) {
		SceneManager.mWorldScene.getPlayer().setPosition(pX, pY);
		
		int[] lPosition = {pX, pY};
		SceneManager.mWorldScene.getClient().addRequest( new ServerMessage("move", lPosition) );
	}
	
	// ----- Check if coordinates are valid
	private boolean validCoordinates(float pX, float pY) {
		return pX > 0 && pX < mTouchLayer.getWidth() && pY > 0 && pY < mTouchLayer.getHeight();
	}
	
	// ----- Get & Setters
	public void setObstacles(ArrayList<TMXTile> pObstacles) {
		mObstacles = pObstacles;
	}
	
	public void setPathFinder(PathFinder pPathFinder) {
		mPathFinder = pPathFinder;
	}
	
	public PathFinder getPathFinder() {
		while(mPathFinder == null) {}
		
		return mPathFinder;
	}
}
