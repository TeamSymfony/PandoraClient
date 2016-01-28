package nl.waldfelt.pandora.util;

import nl.waldfelt.pandora.WorldActivity;

import org.andengine.opengl.font.Font;
import org.andengine.opengl.font.FontFactory;
import org.andengine.opengl.texture.TextureOptions;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
import org.andengine.opengl.texture.region.TiledTextureRegion;

import android.graphics.Color;

public class AssetLoaderUtil {
	// ----- TEXTURES
		
	// Player Textures
	public static TiledTextureRegion 	mPlayerTextureRegion;
	public static Font					mFontEntityName;
	
	// World Textures
	public static TiledTextureRegion	mTreeNormalRegion;
	
	public static void loadAllTextures(WorldActivity pMain) {
		
		BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/");
		
		BitmapTextureAtlas pPlayerTextureAtlas = new BitmapTextureAtlas(pMain.getTextureManager(), 72, 128, TextureOptions.DEFAULT);
		mPlayerTextureRegion = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(pPlayerTextureAtlas, pMain, "player.png", 0, 0, 3, 4);
		pPlayerTextureAtlas.load();
		
		BitmapTextureAtlas pTreeNormalAtlas = new BitmapTextureAtlas(pMain.getTextureManager(), 96, 48, TextureOptions.DEFAULT);
		mTreeNormalRegion = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(pTreeNormalAtlas, pMain, "normal_tree.png", 0, 0, 2, 1);
		pTreeNormalAtlas.load();
		
	}
	
	public static void loadAllFonts(WorldActivity pMain) {
		FontFactory.setAssetBasePath("font/");
		
		BitmapTextureAtlas mFontEntityNameTexture = new BitmapTextureAtlas(pMain.getTextureManager(), 128, 128, TextureOptions.NEAREST);
		mFontEntityName = FontFactory.createFromAsset(pMain.getFontManager(), mFontEntityNameTexture, pMain.getAssets(), "bitmap.ttf", 8, true, Color.WHITE);
		mFontEntityName.load();
		mFontEntityNameTexture.load();
	}
}
