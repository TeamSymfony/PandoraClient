package nl.waldfelt.pandora.player;

import java.io.Serializable;


public class Player implements Serializable {
	
	private static final long serialVersionUID = 552264597706395448L;

	// ----- Player Variables
	private String mName;
	
	private int x, y;
	private float[][] mPath;
	private String mLocation = "";
	
	// ----- Player Experience
	//private int woodcuttingExperience;
	
	
	public Player() {
		
		// Initialize variables -> Later on retrieve from Database
		setName("Unknown");
		setPosition(10, 10);
		
	}
	
	// ----- Get & Setter
	public void setPosition(int pX, int pY) {
		x = pX;
		y = pY;
	}
	
	public int getX() {
		return x;
	}
	
	public int getY() {
		return y;
	}
	
	public void setName( String pName ) {
		mName = pName;
	}
	
	public String getName() {
		return mName;
	}
	
	public void setLocation( String pLocation ) {
		pLocation = mLocation;
	}
	
	public String getLocation() {
		return mLocation;
	}
	
	public void setPath(float[][] pPath) {
		mPath = pPath;
	}

	public float[] getPathX() {
		return mPath[0];
	}
	
	public float[] getPathY() {
		return mPath[1];
	}
	
	// ----- Overrides
	@Override
	public boolean equals(Object pOther) {
		
		if(pOther instanceof Player) {
			Player that = (Player) pOther;			
			return this.mName == that.mName;
		}
		
		return false;
	}
	
	@Override
	public String toString() {
		return this.mName + "(" + getX() + "," + getY() + ")";
	}
}
