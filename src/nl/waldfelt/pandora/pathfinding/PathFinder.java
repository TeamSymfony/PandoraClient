package nl.waldfelt.pandora.pathfinding;

import java.util.ArrayList;

import nl.waldfelt.pandora.scenemanager.SceneManager;
import nl.waldfelt.tod6.Character.PlayerSprite;

import org.andengine.entity.modifier.PathModifier.Path;
import org.andengine.extension.tmx.TMXTile;
import org.andengine.extension.tmx.TMXTiledMap;
import org.andengine.util.Constants;


public class PathFinder extends Thread {
	
	// ----- Constants
	private static final int FRUSTRUM_BUFFER_BORDER = 7;
	
	// ----- TMX Variables
	private TMXTiledMap mTiledMap;
	private ArrayList<TMXTile> mObstacles;
	private TMXTile mCurrentTile;
	private TMXTile mTargetTile;
	
	// ----- Pathfinder Variables
	private Node[][] mPathMap;
	private Node mCurrentPoint;
	private Node mTargetPoint;
	private ArrayList<Node> path;
	private boolean isRunning;
	
	// ----- Frustrum
	private int mFrustrumX;
	private int mFrustrumY;
	private int mFrustrumOffsetX;
	private int mFrustrumOffsetY;
	
	public PathFinder(TMXTiledMap pTiledMap, ArrayList<TMXTile> pObstacles, TMXTile pCurrentTile, TMXTile pTargetTile) {
		mTiledMap = pTiledMap;
		mObstacles = pObstacles;
		mCurrentTile = pCurrentTile;
		mTargetTile = pTargetTile;
		path = new ArrayList<Node>();
		
		isRunning = true;

		// initialize the currentPoint/targetPoint on PathMap 
		mCurrentPoint = new Node(mCurrentTile.getTileColumn(), mCurrentTile.getTileRow());
		mTargetPoint = new Node(mTargetTile.getTileColumn(), mTargetTile.getTileRow());
	}
	
	@Override
	public void run() {
		// Generate the PathMap Matrix
		generatePathMap();
		
		//If Current & Target points differ, find a path
		if(!mCurrentPoint.equals(mTargetPoint)) {
			findPath();
		}
		
		isRunning = false;		
	}
	
	public boolean isRunning() {
		return isRunning;
	}
	
	// Populate PathMap: 0 = no collision, 1 = collision
	public void generatePathMap() {

		// Create localized pathMap between target & current point
		mFrustrumX = Math.abs(mTargetPoint.x - mCurrentPoint.x) + FRUSTRUM_BUFFER_BORDER * 2;
		mFrustrumY = Math.abs(mTargetPoint.y - mCurrentPoint.y) + FRUSTRUM_BUFFER_BORDER * 2;
		
		// Initialize offset Variables from the topleft corner of the complete map
		mFrustrumOffsetX = Math.min(mTargetPoint.x, mCurrentPoint.x);
		mFrustrumOffsetX = Math.max(mFrustrumOffsetX - FRUSTRUM_BUFFER_BORDER, 0);
		
		mFrustrumOffsetY = Math.min(mTargetPoint.y, mCurrentPoint.y);
		mFrustrumOffsetY = Math.max(mFrustrumOffsetY - FRUSTRUM_BUFFER_BORDER, 0);
		
		// Initialize PathMap
		mPathMap = new Node[mFrustrumX + 1][mFrustrumY + 1];
		
		for(int i = 0; i <= mFrustrumX; i++) {
			for(int j = 0; j <= mFrustrumY; j++ ) {
				mPathMap[i][j] = new Node(i, j);
				
				// If frustrum exceeds the map width & height, add obstacles inorder to improve validTargetPoint finder
				if(i + mFrustrumOffsetX > mTiledMap.getTileColumns() - 1 || j + mFrustrumOffsetY > mTiledMap.getTileRows() - 1) {
					mPathMap[i][j].setAsObstacle(true);
				}
			}
		}
		
		// Populate PathMap with Obstacles
		for(TMXTile pObstacle : mObstacles) {
			
			// Convert Scene offset to Local offset
			int pX = pObstacle.getTileColumn() - mFrustrumOffsetX;
			int pY = pObstacle.getTileRow() - mFrustrumOffsetY;
			
			if(pY < 0 || pY > mFrustrumY || pX < 0 || pX > mFrustrumX) {
				continue;
			} else {
				mPathMap[pX][pY].setAsObstacle(true);
			}
		}
		
		
		// If TargetPoint is on an obstacle, find nearest best non obstacle point
		mTargetPoint = getNonObstacleNode(mPathMap[mTargetPoint.x - mFrustrumOffsetX][mTargetPoint.y - mFrustrumOffsetY]);
		
		// Find offset of the current point
		mCurrentPoint.x = Math.max(mCurrentPoint.x - mFrustrumOffsetX, 0);
		mCurrentPoint.y = Math.max(mCurrentPoint.y - mFrustrumOffsetY, 0);
		
		//printPathMap();
		//System.out.print(path);
	}
	
	public void findPath() {
		
		if(lineOfSight(mCurrentPoint, mTargetPoint)) {
			path.add(mCurrentPoint);
			path.add(mTargetPoint); 
			return;
		}
		
		// Localized A* Pathfinding Algorithm
		ArrayList<Node> openList = new ArrayList<Node>();
		
		mTargetPoint.setDistanceFromStart(0); 
		openList.add( mTargetPoint );
		
		ArrayList<Node> closedList = new ArrayList<Node>();
		
		Node lFinger;

		while(openList.size() > 0) {
			
			lFinger = openList.remove(0);

			PlayerSprite lPlayerSprite = SceneManager.mWorldScene.getPlayerSprite();
			final float[] lPlayerCoordinates = SceneManager.mWorldScene.getPlayerSprite().convertLocalToSceneCoordinates(lPlayerSprite.getWidth() / 2, lPlayerSprite.getHeight());
			TMXTile lCurrentTile = SceneManager.mWorldScene.getTouchLayer().getTMXTileAt(lPlayerCoordinates[Constants.VERTEX_INDEX_X], lPlayerCoordinates[Constants.VERTEX_INDEX_Y]);
			Node lCurrentNode = new Node(lCurrentTile.getTileColumn(), lCurrentTile.getTileRow());
			lCurrentNode.x = Math.max(lCurrentNode.x - mFrustrumOffsetX, 0);
			lCurrentNode.y = Math.max(lCurrentNode.y - mFrustrumOffsetY, 0);
			
			if(lFinger.equals(lCurrentNode)) {
				reconstructPath(lFinger);
				return;
			}
			closedList.add(lFinger);

			for(Node pPoint : getNeighbours(lFinger)) {
				boolean neighbourIsBetter;
				
				if(closedList.contains(pPoint)) {
					continue;
				}
				
				if(!pPoint.isObstacle()) {
					float neighbourDistanceFromStart = lFinger.getDistanceFromStart() + getDistance(lFinger, pPoint);
					
					if(!openList.contains(pPoint)) {
						openList.add(pPoint);
						neighbourIsBetter = true;
					} else if(neighbourDistanceFromStart < lFinger.getDistanceFromStart()) {
						neighbourIsBetter = true;
					} else {
						neighbourIsBetter = false;
					}
					
					if(neighbourIsBetter) {
						pPoint.setParent(lFinger);
						pPoint.setDistanceFromStart( neighbourDistanceFromStart );

						float hX = (mTargetPoint.x - pPoint.x);
						float hY = (mTargetPoint.y - pPoint.y);
						
						pPoint.setHeuristicDistanceToEnd( hX * hX + hY * hY );
					}
				}
			}
		}
	}
	
	//Get path by searching parents of the nodes, starting at the final node
	private void reconstructPath(Node pNode) {
		path = new ArrayList<Node>();
		
		path.add(pNode);
		while(pNode.getParent() != null) {
			//path.add(0, pNode.getParent());
			path.add(pNode.getParent());
			pNode = pNode.getParent();
		}
		
		// Post Smoothpath Processing
		
		ArrayList<Node> smoothPath = new ArrayList<Node>();
		smoothPath.add(path.get(0));
		
		int k = 0;
		
		for(int i = 1; i < path.size() - 1; i++) {
			// Check whether a waypoint is necessary; if so, add it to the set
			if(!lineOfSight(smoothPath.get( k ), path.get(i + 1))) {
				smoothPath.add(path.get(i));
				k++;
			}
		}
		smoothPath.add(path.get( path.size() - 1));
		path = smoothPath;
		
		
	}
	
	//Create the actual path to modifiy the entity
	public Path getPath() {

		
		boolean isCurrentPath = true;
		
		PlayerSprite lPlayerSprite = SceneManager.mWorldScene.getPlayerSprite();
		final float[] lPlayerCoordinates = SceneManager.mWorldScene.getPlayerSprite().convertLocalToSceneCoordinates(lPlayerSprite.getWidth() / 2, lPlayerSprite.getHeight());

		int lCurrentTileOffsetX = (int) (lPlayerCoordinates[Constants.VERTEX_INDEX_X] % 16); 
		int lCurrentTileOffsetY = (int) (lPlayerCoordinates[Constants.VERTEX_INDEX_Y] % 16);
		


		TMXTile lCurrentTile = SceneManager.mWorldScene.getTouchLayer().getTMXTileAt(lPlayerCoordinates[Constants.VERTEX_INDEX_X], lPlayerCoordinates[Constants.VERTEX_INDEX_Y]);
		Node lCurrentNode = new Node(lCurrentTile.getTileColumn(), lCurrentTile.getTileRow());
		lCurrentNode.x = Math.max(lCurrentNode.x - mFrustrumOffsetX, 0);
		lCurrentNode.y = Math.max(lCurrentNode.y - mFrustrumOffsetY, 0);
		
		/*if(!lCurrentNode.equals(mCurrentPoint)) {
			path.remove(0);
			path.add(0, lCurrentNode);
		}*/
		
		Path lPath = new Path(path.size());
		
		
		for(Node pNode : path) {

			int lPathOffsetX = mTiledMap.getTileWidth() / 2;
			int lPathOffsetY = mTiledMap.getTileHeight() / 2;
			
			// If it is the first tile in the path, adopt current offset on the tile
			if(isCurrentPath) {
				lPathOffsetX = lCurrentTileOffsetX;
				lPathOffsetY = lCurrentTileOffsetY;
				isCurrentPath = false;
			}
			lPath.to((pNode.x + mFrustrumOffsetX)* 16 - lPlayerSprite.getWidth() / 2 + lPathOffsetX, (pNode.y  + mFrustrumOffsetY) * 16 - lPlayerSprite.getHeight() + lPathOffsetY);
		}

		return lPath; 
	}
	
	// Determine whether two nodes are in line of sight of eachother
	private boolean lineOfSight(Node pNode1, Node pNode2) {
		
		
		int pX0 = pNode1.x;
		int pY0 = pNode1.y;
		
		int pX1 = pNode2.x;
		int pY1 = pNode2.y;
		
		int dX = Math.abs(pX1 - pX0);
		int dY = Math.abs(pY1 - pY0);
		
		int x = pX0;
		int y = pY0;
		
		int n = 1 + dX + dY;

		int x_inc = (pX1 > pX0) ? 1 : -1;
		int y_inc = (pY1 > pY0) ? 1 : -1;
		
		int error = dX - dY;
		
		dX = dX * 2;
		dY = dY * 2;
		
		for(; n > 0; n--) {
			if(x < 0 || y < 0 || x > mFrustrumX || y > mFrustrumY) {
				return false;
			}
			
			if(mPathMap[x][y].isObstacle()) {
				return false;
			}
			
			if(error > 0) {
				x = x + x_inc;
				error = error - dY;
			} else if(error < 0) {
				y = y + y_inc;
				error = error + dX;
			} else {
				x = x + x_inc;
				y = y + y_inc;
				error = error - dY;
				error = error + dX;
			}
		}
		
		return true; 
	}

	// ----- AStar helper functions
	private ArrayList<Node> getNeighbours(Node pPoint) {
		ArrayList<Node> neighbours = new ArrayList<Node>();
		
		int pX = pPoint.x;
		int pY = pPoint.y;
		
		// check top, right, bottom, left
		if(isValidCoordinate(pX - 1, pY)) {
			neighbours.add( mPathMap[pX - 1][pY] );
		}

		if(isValidCoordinate(pX, pY + 1)) {
			neighbours.add( mPathMap[pX][pY + 1] );
		}

		if(isValidCoordinate(pX + 1, pY)) {
			neighbours.add( mPathMap[pX + 1][pY] );
		}

		if(isValidCoordinate(pX, pY - 1)) {
			neighbours.add(mPathMap[pX][pY - 1] );
		}
		
		// check topleft, topright, bottomright, bottomleft
		if(isValidCoordinate(pX - 1, pY - 1)) {
			neighbours.add( mPathMap[pX - 1][pY - 1] );
		}

		if(isValidCoordinate(pX - 1, pY + 1)) {
			neighbours.add( mPathMap[pX - 1][pY + 1] );
		}

		if(isValidCoordinate(pX + 1, pY + 1)) {
			neighbours.add( mPathMap[pX + 1][pY + 1] );
		}

		if(isValidCoordinate(pX + 1, pY - 1)) {
			neighbours.add( mPathMap[pX + 1][pY - 1] );
		}
		
		return neighbours;
		
	}
	
	//Check whether coordinates do exist on the localized pathmap
	private boolean isValidCoordinate(int pX, int pY) {
		
		// Breach map width
		if(pX < 0 || pX > mPathMap.length - 1) {
			return false;
		}
		
		// Breach map height
		if(pY < 0 || pY > mPathMap[0].length - 1) {
			return false;
		}
		
		return true;
	}
	
	// Find closest nonObstacleNode
	private Node getNonObstacleNode(Node pNode) {
		
		// Initialize smallest distance
		float validDistance = Float.MAX_VALUE;
		Node validNode = null;
		
		ArrayList<Node> needles = new ArrayList<Node>();
		ArrayList<Node> haystack = new ArrayList<Node>();
		ArrayList<Node> visited = new ArrayList<Node>();
		
		haystack.add(pNode);
		Node lFinger;
		
		// Continue while the searchArea is populated
		while(haystack.size() > 0) {
			
			lFinger = haystack.remove(0);
			
			// If we already visited the node, skip routines
			if(visited.contains(lFinger)) {
				continue;
			}
			
			// Add to visited nodes
			visited.add(lFinger);
			
			
			if(!lFinger.isObstacle()) {
				// If Node is not an obstacle, it may be a candidate for closest valid nodes
				if(!needles.contains(lFinger))
					needles.add(lFinger);
				
			} else {
				// If the this node also is an obstacle, find it's neighbours and add them to the searcharea
				for(Node neighbour : getNeighbours(lFinger)) {
					if(!visited.contains(neighbour)) {
						haystack.add(neighbour); 
					}
				}
			}
		}
		
		
		// We got all the possible candidates
		for(Node lNode : needles) {
			float dist = getDistance(pNode, lNode);
			
			// If this node is a closer fit, replace current closest fit
			if(dist < validDistance) {
				validDistance = dist;
				validNode = lNode;
			}
			
		}		
		
		return validNode;
	}
	
	
	// Return single line distance
	private float getDistance(Node pStart, Node pEnd) {
		return new Path(2).to(pStart.x, pStart.y).to(pEnd.x, pEnd.y).getLength();
	}
	
	
	// Print PathMap: . = no collision, # = collision, O = lCurrentTile, X = lTargetTile
	public void printPathMap() {
		System.out.println("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
		
		// reverse j & i. Because you first a line, before you start a second line.
		for(int j = 0; j <= mFrustrumY; j++ ) {
			for(int i = 0; i <= mFrustrumX; i++) {
				String ch = ".";
				
				if(mPathMap[i][j].isObstacle()) {
					ch = "#";
				}
				
				if(i == mCurrentPoint.x && j == mCurrentPoint.y) {
					ch = "O";
				}
				
				if(i == mTargetPoint.x && j == mTargetPoint.y) {
					ch = "X";
				}
				
				System.out.print(ch + " ");
			}
			System.out.println();
		}
	}
}
