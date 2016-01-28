package nl.waldfelt.pandora.scenemanager;

import java.util.HashMap;
import java.util.Map.Entry;

import nl.waldfelt.pandora.Discovery;
import nl.waldfelt.pandora.WorldActivity;
import nl.waldfelt.pandora.message.Client;
import nl.waldfelt.pandora.pathfinding.Pathing;
import nl.waldfelt.pandora.player.Player;
import nl.waldfelt.pandora.tmx.WorldLayer;
import nl.waldfelt.pandora.util.AssetLoaderUtil;
import nl.waldfelt.tod6.Character.ActionController;
import nl.waldfelt.tod6.Character.PlayerSprite;
import nl.waldfelt.tod6.Enum.EMap_ID;

import org.andengine.engine.handler.IUpdateHandler;
import org.andengine.entity.IEntity;
import org.andengine.entity.IEntityComparator;
import org.andengine.entity.shape.RectangularShape;
import org.andengine.entity.shape.Shape;
import org.andengine.entity.sprite.AnimatedSprite;
import org.andengine.extension.tmx.TMXLayer;
import org.andengine.extension.tmx.TMXTile;
import org.andengine.extension.tmx.TMXTiledMap;
import org.andengine.input.touch.TouchEvent;

public class WorldScene extends MyScene {
	
	// ----
	private WorldActivity mContext;
	private PlayerSprite mPlayerSprite;
	
	private Discovery mDiscovery;
	
	// ----- PlayerData
	private Player mPlayer;
	
	// ----- TMX Variables
	private TMXLayer mTouchLayer;
	private TMXTiledMap mTiledMap;
	
	// ----- Pathing
	private Pathing mPathing;
	
	// ----- Client
	private Client mClient;
	private HashMap<TMXTile, String> mResources;
	
	
	// ----- CONSTRUCTOR
	protected WorldScene(WorldActivity pMain) {
		super(WorldActivity.mWorldCamera, pMain);
		mContext = pMain;
	}
	
	@Override
	public MyScene create() {
		
		setClient( new Client() );
		
		// Block further action until player is loaded
		while(mPlayer == null) {};

		// Create World
		new WorldLayer(EMap_ID.OVERWORLD, mContext);
		
		// Create World Resources
		AnimatedSprite pSprite = null;
		// ------------------------------------------------------------------ PUT THIS IN ITS OWN CLASS PLS
		for(Entry<TMXTile, String> pResource : mResources.entrySet()) {
			final TMXTile pTile = (TMXTile) pResource.getKey();
			final String pType = (String) pResource.getValue();
			
			if(pType.equals("TreeNormal")) {
				pSprite = new AnimatedSprite(pTile.getTileX() - AssetLoaderUtil.mTreeNormalRegion.getWidth() / 2 + 8, pTile.getTileY() - AssetLoaderUtil.mTreeNormalRegion.getHeight() + 8, AssetLoaderUtil.mTreeNormalRegion, mContext.getVertexBufferObjectManager()) {
					
					@Override
					public boolean onAreaTouched(final TouchEvent pSceneTouchEvent, final float pTouchAreaLocalX, final float pTouchAreaLocalY) {
						if(pSceneTouchEvent.isActionDown()) {
							System.out.println(pType);
						}
						return false;
					}
				};
				
				pSprite.animate(new long[] {1200, 1200}, 0, 1, true); 
				pSprite.setZIndex((int) pSprite.getY());
				attachChild(pSprite); 
				registerTouchArea(pSprite);
			}
		}
		
		// Create Player
		mPlayerSprite = new PlayerSprite(mPlayer.getX(), mPlayer.getY(), AssetLoaderUtil.mPlayerTextureRegion, mContext);
		mPlayerSprite.loadAsPlayer();
		
		// Touch Listener
		ActionController listener = new ActionController( );
		setOnSceneTouchListener( listener );
		
		mDiscovery = new Discovery(mContext);
		
		System.out.println("Player: " + mPlayerSprite.getY() + "| Tree: " + pSprite.getY());
		
		
		
		this.registerUpdateHandler( new IUpdateHandler() {

			@Override
			public void onUpdate(float pSecondsElapsed) {
				mContext.runOnUpdateThread( new Runnable() {

					@Override
					public void run() {

						sortChildren( new IEntityComparator() {

							@Override
							public int compare(IEntity pEntity1, IEntity pEntity2) {
								if(pEntity1.getZIndex() > 1000) {
									return 1;
								}
								if(pEntity2.getZIndex() > 1000) {
									return -1;
								}
								
								if(pEntity1.getZIndex() == -1) {
									return -1;
								} else if(pEntity2.getZIndex() == -1) {
									return 1;
								} else {
									
									if(pEntity1 instanceof RectangularShape && pEntity2 instanceof RectangularShape) {
										RectangularShape pSprite1 = (RectangularShape) pEntity1;
										RectangularShape pSprite2 = (RectangularShape) pEntity2;
									
										return (int) ((pSprite1.getY() + ((RectangularShape) pSprite1).getHeight()) - (pSprite2.getY() + ((RectangularShape) pSprite2).getHeight()));
									} else {
										return 0;
									}
								}
							}
							
						});
					}
					
				});
			}

			@Override
			public void reset() {				
			}
			
		});
		return super.create();
	}
	
	
	
	// ----- GET & SETTERS
	
	public PlayerSprite getPlayerSprite() {
		return mPlayerSprite;
	}
	
	
	
	public void setTiledMap(TMXTiledMap pTiledMap) {
		mTiledMap = pTiledMap;
	}

	public TMXTiledMap getTiledMap() {
		return mTiledMap;
	}
	
	public TMXLayer getTouchLayer() {
		return mTouchLayer;
	}
	
	public void setTouchLayer(TMXLayer pTouchLayer) {
		mTouchLayer = pTouchLayer;
	}
	
	
	public Pathing getPathing() {
		return mPathing;
	}
	
	public void setPathing( Pathing pPathing ) {
		mPathing = pPathing;
	}
	
	
	public Client getClient() {
		return mClient;
	}
	
	public void setClient(Client pClient) {
		mClient = pClient;
	}
	
	
	public Player getPlayer() {
		return mPlayer;
	}
	
	public void setPlayer(Player pPlayer) {
		mPlayer = pPlayer;
	}
	
	public Discovery getDiscovery() {
		return mDiscovery;
	}

	public void setResources(HashMap<TMXTile, String> pResources) {
		mResources = pResources;		
	}
	
}
