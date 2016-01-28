package nl.waldfelt.pandora.scenemanager;

import nl.waldfelt.pandora.WorldActivity;

import org.andengine.engine.Engine;
import org.andengine.ui.activity.BaseGameActivity;

import android.util.DisplayMetrics;

public class SceneManager {
	private static SceneManager instance = new SceneManager();
	
	// ---- SCENE MANAGER
	private BaseGameActivity mActivity;
	private DisplayMetrics mDisplayMetrics;
	private Engine mEngine;
	
	// --- SCENES
	public static WorldScene mWorldScene;
	
	public static void initialize(WorldActivity pMain) {
		instance.mDisplayMetrics = new DisplayMetrics();
		instance.mEngine = pMain.getEngine();
		instance.mActivity = pMain;
		instance.mActivity.getWindowManager().getDefaultDisplay().getMetrics(instance.mDisplayMetrics);
		
		//Create scenes that will not be destroyed for the lifetime of the application
		mWorldScene = new WorldScene( pMain );
	}
	
	public static SceneManager getInstance() {
		return instance;
	}
	
	public MyScene getCurrentScene() {
		return (MyScene) mEngine.getScene();
	}
}
