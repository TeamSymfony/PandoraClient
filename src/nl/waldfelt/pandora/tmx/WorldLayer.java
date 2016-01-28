package nl.waldfelt.pandora.tmx;

import nl.waldfelt.pandora.WorldActivity;
import nl.waldfelt.pandora.pathfinding.Pathing;
import nl.waldfelt.pandora.scenemanager.SceneManager;
import nl.waldfelt.tod6.Enum.EMap_ID;

import org.andengine.extension.tmx.TMXTiledMap;

public class WorldLayer {
	
	private WorldActivity mContext;
	
	
	public WorldLayer(final EMap_ID pMapName, final WorldActivity pMain) {
		mContext = pMain;

		// load TMXMap for further processing
		TMXMapLoader mMapLoader = new TMXMapLoader(pMapName, pMain);
		TMXTiledMap lTiledMap = mMapLoader.getTiledMap();
		
		// add Map & TouchLayout to world scene
		SceneManager.mWorldScene.setTiledMap(lTiledMap);
		SceneManager.mWorldScene.setTouchLayer(lTiledMap.getTMXLayers().get(0));
		SceneManager.mWorldScene.attachChild(lTiledMap.getTMXLayers().get(0));
		lTiledMap.getTMXLayers().get(0).setZIndex(-1);
		// set up camera for the scene
		
		// set up pathing for the scene
		Pathing pathing = new Pathing( mContext );
		pathing.setObstacles( mMapLoader.getObstacles() );
		
		SceneManager.mWorldScene.setResources( mMapLoader.getResources() );
		
		SceneManager.mWorldScene.setPathing( pathing );
	}
}
