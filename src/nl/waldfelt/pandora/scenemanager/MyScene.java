package nl.waldfelt.pandora.scenemanager;

import nl.waldfelt.pandora.WorldActivity;

import org.andengine.engine.camera.Camera;
import org.andengine.entity.IEntity;
import org.andengine.entity.IEntityComparator;
import org.andengine.entity.scene.Scene;
import org.andengine.util.Constants;

public class MyScene extends Scene {
	
	public MyScene(final Camera pCamera, final WorldActivity pMain) {
		super();
	}
	
	public MyScene create() {
		this.setTouchAreaBindingOnActionDownEnabled(true);
		this.setTouchAreaBindingOnActionMoveEnabled(true);
		this.setOnAreaTouchTraversalFrontToBack();
		return this;
	}
	
	public MyScene delete() {
		this.clearChildScene();
		this.detachChildren();
		this.clearEntityModifiers();
		this.clearTouchAreas();
		this.clearUpdateHandlers();
		return this;
	}
	
	public MyScene update() {
		this.clearChildScene();
		this.detachChildren();
		this.clearEntityModifiers();
		this.clearTouchAreas();
		this.clearUpdateHandlers();
		return this;
	}
	
	
}
