package nl.waldfelt.tod6.Character;

import nl.waldfelt.pandora.WorldActivity;
import nl.waldfelt.pandora.scenemanager.SceneManager;

import org.andengine.entity.primitive.Rectangle;
import org.andengine.entity.scene.IOnSceneTouchListener;
import org.andengine.entity.scene.Scene;
import org.andengine.extension.tmx.TMXTile;
import org.andengine.input.touch.TouchEvent;

public class ActionController implements IOnSceneTouchListener {

	public Rectangle target;
	
	public ActionController() {
		target = new Rectangle(0, 0, 16, 16, WorldActivity.mWorldEngine.getVertexBufferObjectManager());
		target.setColor(1, 0, 0, 0.5f);
	}
	
	@Override 
	public boolean onSceneTouchEvent(Scene pScene, TouchEvent pSceneTouchEvent) {
		
		if(pSceneTouchEvent.isActionDown()) {
			target.detachSelf();
			
			// Capture touch area
			float destX = pSceneTouchEvent.getX();
			float destY = pSceneTouchEvent.getY();
			SceneManager.mWorldScene.getPlayerSprite().moveTo(destX, destY);
			
			TMXTile lTile = SceneManager.mWorldScene.getTouchLayer().getTMXTileAt(destX, destY);
			target.setPosition(lTile.getTileX(), lTile.getTileY());
			
			SceneManager.mWorldScene.attachChild(target);
		}
		
		return false;
	}

}
