package nl.waldfelt.pandora;

import nl.waldfelt.pandora.scenemanager.SceneManager;
import nl.waldfelt.pandora.util.AssetLoaderUtil;

import org.andengine.engine.Engine;
import org.andengine.engine.LimitedFPSEngine;
import org.andengine.engine.camera.BoundCamera;
import org.andengine.engine.options.EngineOptions;
import org.andengine.engine.options.ScreenOrientation;
import org.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.util.FPSLogger;
import org.andengine.ui.activity.SimpleBaseGameActivity;


public class WorldActivity extends SimpleBaseGameActivity {
	
	// ----- CONSTANTS
	public static final int CAMERA_WIDTH = 400;
	public static final int CAMERA_HEIGHT = 240;
	private static final int FRAMES_PER_SECOND = 60;
	public static final int TILE_WIDTH = 16;
	public static final int TILE_HEIGHT = 16;
	
	// ----- ACTIVITY
	
	public static BoundCamera mWorldCamera;
	public static Engine mWorldEngine;

	@Override
	public EngineOptions onCreateEngineOptions() {
			
		mWorldCamera = new BoundCamera(0, 0, CAMERA_WIDTH, CAMERA_HEIGHT);
		
		EngineOptions engineOptions = new EngineOptions(true, ScreenOrientation.LANDSCAPE_SENSOR, new RatioResolutionPolicy(CAMERA_WIDTH, CAMERA_HEIGHT), mWorldCamera);
		engineOptions.getRenderOptions().setMultiSampling(false);
		
		return engineOptions;
		
	}
	
	@Override
	public Engine onCreateEngine(EngineOptions pEngineOptions) {
		Engine engine = new LimitedFPSEngine(pEngineOptions, FRAMES_PER_SECOND);
		return engine;
	}

	@Override
	protected void onCreateResources() {

		AssetLoaderUtil.loadAllTextures(this);
		AssetLoaderUtil.loadAllFonts(this);
		
	}

	@Override
	protected Scene onCreateScene() {
		this.getEngine().registerUpdateHandler( new FPSLogger() );
		mWorldEngine = this.getEngine();
		SceneManager.initialize(this);
		SceneManager.mWorldScene.create();
		return SceneManager.mWorldScene;
	}
	

}
