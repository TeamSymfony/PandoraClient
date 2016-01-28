package nl.waldfelt.pandora.pathfinding;

public class Node {
	
	public int x;
	public int y;
	
	private boolean isObstacle;
	
	private float mDistanceFromStart;
	private float mHeuristicDistanceToEnd;
	
	private Node mParent;
	
	public Node(int pX, int pY) {
		x = pX;
		y = pY;
		isObstacle = false;
		mDistanceFromStart = Integer.MAX_VALUE;
		mHeuristicDistanceToEnd = Integer.MAX_VALUE;
	}
	
	@Override
	public boolean equals(Object o) {
		if(o instanceof Node) {
			Node that = (Node) o;
			
			return this.x == that.x && this.y == that.y;
		}
		return false;
	}
	
	public float getDistanceFromStart() {
		return mDistanceFromStart;
	}
	
	public void setDistanceFromStart(float pValue) {
		mDistanceFromStart = pValue;
	}
	
	public float getHeuristicDistanceToEnd() {
		return mHeuristicDistanceToEnd;
	}
	
	public void setHeuristicDistanceToEnd(float pValue) {
		mHeuristicDistanceToEnd = pValue;
	}
	
	public float getTotalDistance() {
		return mDistanceFromStart + mHeuristicDistanceToEnd;
	}
	
	
	public void setAsObstacle(boolean pIsObstacle) {
		isObstacle = pIsObstacle;
	}
	
	public boolean isObstacle() {
		return isObstacle;
	}
	
	
	
	public void setParent(Node pParent) {
		mParent = pParent;
	}
	
	public Node getParent() {
		return mParent;
	}
	
	@Override
	public String toString() {
		return "<" + this.x + ", " + this.y + ">";
	}
}
