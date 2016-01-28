package nl.waldfelt.tod6.Character;

import nl.waldfelt.pandora.WorldActivity;
import nl.waldfelt.pandora.scenemanager.SceneManager;
import nl.waldfelt.pandora.util.AssetLoaderUtil;

import org.andengine.engine.handler.IUpdateHandler;
import org.andengine.entity.primitive.Rectangle;
import org.andengine.entity.sprite.AnimatedSprite;
import org.andengine.entity.text.Text;
import org.andengine.extension.tmx.TMXTile;
import org.andengine.opengl.texture.region.ITiledTextureRegion;
import org.andengine.util.Constants;

public class PlayerSprite extends AnimatedSprite {
	
	// ----- CONSTANTS
	private WorldActivity mContext;
	
	// -----
	private String nName;
	
	// ----- Entities

	private Text mPlayerName;
	private Rectangle mPlayerNamePlate;

	// ----- CONSTRUCTOR
	public PlayerSprite(float pX, float pY, ITiledTextureRegion pTiledTextureRegion, final WorldActivity pMain) {
		super(pX * 16, pY * 16, pTiledTextureRegion, pMain.getVertexBufferObjectManager());
		setPosition(getAnchorX() + 8, getAnchorY() + 8);
		
		mContext = pMain;
		nName = SceneManager.mWorldScene.getPlayer().getName();
	}
	
	// ----- Position
	public float getAnchorX() { 
		return this.getX() - this.getWidth() / 2;
	}
	
	public float getAnchorY() {
		return this.getY() - this.getHeight();
	}
	
	// ----- ANIMATION
	public void animateWalkLeft() {
		animate(new long[]{200, 200, 200}, 9, 11, true);
	}
	
	public void animateWalkRight() {
		animate(new long[]{200, 200, 200}, 3, 5, true);
	}
	
	public void loadName() {
		
		// Create Text Entity
		mPlayerName = new Text(0, -2, AssetLoaderUtil.mFontEntityName, nName, 16, mContext.getVertexBufferObjectManager());
		
		// Create Background for the Text Entity
		mPlayerNamePlate = new Rectangle(0, 0, mPlayerName.getWidth() + 4, mPlayerName.getHeight() - 1, mContext.getVertexBufferObjectManager());
		mPlayerNamePlate.setColor(0, 0, 0, 0.35f);
		mPlayerNamePlate.setPosition(-2, 0);
		mPlayerName.attachChild(mPlayerNamePlate);
		
		// Calculate offset for horizontal placement at the right height
		int offsetWidth =  (int) (this.getWidth() - mPlayerName.getWidth()) / 2;
		mPlayerName.setPosition(offsetWidth, -2);
	}
	
	public void loadAsPlayer() {
		
		// Attach NamePlate to PlayerSprite
		load();
		
		WorldActivity.mWorldCamera.setChaseEntity(this);
		WorldActivity.mWorldCamera.setBounds(0, 0, SceneManager.mWorldScene.getTouchLayer().getWidth(), SceneManager.mWorldScene.getTouchLayer().getHeight());
		WorldActivity.mWorldCamera.setBoundsEnabled(true);
		
		final Rectangle tileHighlighter = new Rectangle(0, 0, 16, 16, mContext.getVertexBufferObjectManager());
		tileHighlighter.setColor(1, 0, 0, 0.25f);
		SceneManager.mWorldScene.attachChild(tileHighlighter);
		SceneManager.mWorldScene.registerUpdateHandler( new IUpdateHandler() {
			@Override
			public void onUpdate(float pSecondsElapsed) {
				final float[] lPlayerCoordinates = convertLocalToSceneCoordinates(getWidth() / 2, getHeight());
				final TMXTile lCurrentTile = SceneManager.mWorldScene.getTouchLayer().getTMXTileAt(lPlayerCoordinates[Constants.VERTEX_INDEX_X], lPlayerCoordinates[Constants.VERTEX_INDEX_Y]);
				
				if(lCurrentTile != null) {
					tileHighlighter.setPosition(lCurrentTile.getTileX(), lCurrentTile.getTileY());
				}
			}

			@Override
			public void reset() {};
		});
	}
	
	public void load() {
		SceneManager.mWorldScene.attachChild(this);
		// Attach NamePlate to PlayerSprite
		loadName();
		this.attachChild(mPlayerName);
		mPlayerName.setZIndex(5000);
		mPlayerNamePlate.setZIndex(-1);
		this.sortChildren();
	}

	public void setName(String pName) {
		nName = pName;
	}
	
	// Movement handler
	public void moveTo(float pX, float pY) {
		SceneManager.mWorldScene.getPathing().movePlayerInBackground(pX, pY);
	}

	public String getName() {
		return nName;
	}
	
	public void detach() {
		
		mContext.runOnUpdateThread(new Runnable() {

			public void run() {
				detachSelf();
			}
			
		});
		
		dispose();
	}
	
}
