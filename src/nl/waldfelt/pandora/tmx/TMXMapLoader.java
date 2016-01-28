package nl.waldfelt.pandora.tmx;

import java.util.ArrayList;
import java.util.HashMap;

import nl.waldfelt.pandora.WorldActivity;
import nl.waldfelt.tod6.Enum.EMap_ID;

import org.andengine.extension.tmx.TMXLayer;
import org.andengine.extension.tmx.TMXLoader;
import org.andengine.extension.tmx.TMXLoader.ITMXTilePropertiesListener;
import org.andengine.extension.tmx.TMXObject;
import org.andengine.extension.tmx.TMXObjectGroup;
import org.andengine.extension.tmx.TMXObjectGroupProperty;
import org.andengine.extension.tmx.TMXProperties;
import org.andengine.extension.tmx.TMXTile;
import org.andengine.extension.tmx.TMXTileProperty;
import org.andengine.extension.tmx.TMXTiledMap;
import org.andengine.extension.tmx.util.exception.TMXLoadException;
import org.andengine.opengl.texture.TextureOptions;
import org.andengine.util.debug.Debug;

public class TMXMapLoader {
	private WorldActivity mContext;
	
	private TMXLoader mTMXLoader;
	private TMXTiledMap mTiledMap;
	
	private ArrayList<TMXObject> mObjects;
	private ArrayList<TMXObjectGroup> mObjectGroups;
	
	private EMap_ID mMapName;
	
	private ArrayList<TMXTile> mObstacles;
	private HashMap<TMXTile, String> mResources;
	
	public TMXMapLoader(EMap_ID pMapName, WorldActivity pMain) {
		mContext = pMain;
		mMapName = pMapName;
		
		String fileName = getMapFileName();
		
		try {
		
			mTMXLoader = new TMXLoader(mContext.getAssets(), mContext.getTextureManager(), 
					TextureOptions.BILINEAR_PREMULTIPLYALPHA, mContext.getVertexBufferObjectManager(), new ITMXTilePropertiesListener() {
	
				@Override
				public void onTMXTileWithPropertiesCreated(
						TMXTiledMap pTMXTiledMap, TMXLayer pTMXLayer,
						TMXTile pTMXTile,
						TMXProperties<TMXTileProperty> pTMXTileProperties) {
				}
			
			});
			
			mTiledMap = mTMXLoader.loadFromAsset(fileName);
		
		} catch (final TMXLoadException e) {
			Debug.e(e);
		}
		
		
		mObjects = new ArrayList<TMXObject>();
		mObjectGroups = new ArrayList<TMXObjectGroup>();
		
		for(TMXObjectGroup pGroup : mTiledMap.getTMXObjectGroups()) {
			mObjects.addAll(pGroup.getTMXObjects());
			mObjectGroups.add(pGroup);
		}
		
		// Collect all collision tiles
		mObstacles = getTilesByProperty("collide", "true");
		mResources = getTilesByProperty("resource");

	}
	
	// ----- Map filename and location helper function
	private String getMapFileName() {
		switch(mMapName) {
		case OVERWORLD:
			return "tmx/terrain.tmx";
		case CAVE:
			return "tmx/cave.tmx";
		default:
			throw new Error("No such map file exists.");
		}
	}
	
	
	// ----- Setter & Getter methods
	public TMXTiledMap getTiledMap() {
		return mTiledMap;
	}
	
	public ArrayList<TMXTile> getObstacles() {
		return mObstacles;
	}
	
	public HashMap<TMXTile, String> getResources() {
		return mResources;
	}
	
	// ----- Supporting TMX methods
	public ArrayList<TMXTile> getTilesByProperty(String pName, String pValue) {
		ArrayList<TMXTile> ObjectTiles = new ArrayList<TMXTile>();
		
		// Search all ObjectGroups
		for( TMXObjectGroup pGroup : mObjectGroups) {
			
			// Search the ObjectGroup for its properties
			for( TMXObjectGroupProperty pGroupProperty : pGroup.getTMXObjectGroupProperties() ) {
				
				// Validate whether the property wanted is found
				if(pGroupProperty.getName().equals(pName) && pGroupProperty.getValue().equals(pValue)) {
					
					//Add Objects as tiles to temp arraylist
					for( TMXObject pObject : pGroup.getTMXObjects()) {
						
						int ObjectX = pObject.getX();
						int ObjectY = pObject.getY();
						// Gets the number of rows and columns in the object
						int ObjectRows = pObject.getHeight() / WorldActivity.TILE_HEIGHT;
						int ObjectColumns = pObject.getWidth() / WorldActivity.TILE_WIDTH;
						
						for (int TileRow = 0; TileRow < ObjectRows; TileRow++) {
							for (int TileColumn = 0; TileColumn < ObjectColumns; TileColumn++) {
								float lObjectTileX = ObjectX + TileColumn * WorldActivity.TILE_WIDTH;
								float lObjectTileY = ObjectY + TileRow * WorldActivity.TILE_HEIGHT;
								ObjectTiles.add(mTiledMap.getTMXLayers().get(0).getTMXTileAt(lObjectTileX, lObjectTileY));						
							}							 
						}
						
					}
				}
				
			}
		}
		
		return ObjectTiles;
	}
	

	public HashMap<TMXTile, String> getTilesByProperty(String pName) {
		HashMap<TMXTile, String> ObjectTiles = new HashMap<TMXTile, String>();
		
		// Search all ObjectGroups
		for( TMXObjectGroup pGroup : mObjectGroups) {
			
			// Search the ObjectGroup for its properties
			for( TMXObjectGroupProperty pGroupProperty : pGroup.getTMXObjectGroupProperties() ) {
				
				// Validate whether the property wanted is found
				if(pGroupProperty.getName().equals(pName)) {
					
					//Add Objects as tiles to temp arraylist
					for( TMXObject pObject : pGroup.getTMXObjects()) {
						
						int ObjectX = pObject.getX();
						int ObjectY = pObject.getY();
						// Gets the number of rows and columns in the object
						int ObjectRows = pObject.getHeight() / WorldActivity.TILE_HEIGHT;
						int ObjectColumns = pObject.getWidth() / WorldActivity.TILE_WIDTH;
						
						for (int TileRow = 0; TileRow < ObjectRows; TileRow++) {
							for (int TileColumn = 0; TileColumn < ObjectColumns; TileColumn++) {
								float lObjectTileX = ObjectX + TileColumn * WorldActivity.TILE_WIDTH;
								float lObjectTileY = ObjectY + TileRow * WorldActivity.TILE_HEIGHT;
								ObjectTiles.put(mTiledMap.getTMXLayers().get(0).getTMXTileAt(lObjectTileX, lObjectTileY), pObject.getType());						
							}							 
						}
						
					}
				}
				
			}
		}
		
		return ObjectTiles;
	}
}
