package maze;

import java.util.ArrayList;
import java.util.List;


public class MapCell {

	protected Coord myLoc;
	protected MapCell parent = null;
	protected boolean start = false;
	protected boolean end = false;
	protected boolean visited = false;
	
	protected ArrayList<Dir> TraveledDirections;

	public MapCell() {
		TraveledDirections = new ArrayList<>();
		visited = false;
		myLoc = new Coord();
	}
	
	public Coord getCoord() { return myLoc; }
	public List<Dir> getDirs() { return TraveledDirections; }
	public void addDirection(Dir d) {
		if(!TraveledDirections.contains(d) ) {
			TraveledDirections.add(d); 	
		}
		visited = true; 
		}
	public boolean getVisited() { return visited; }
	public boolean getEnd() { return end; }
	public void setEnd() { end = true; }
	public MapCell getParent() { return parent; }
	public void setParent(MapCell p) {
		parent = p;
		
		if(parent == this) {
			System.err.println("Family tree does not branch");
		}
		visited = true;
		if(p.myLoc.x > myLoc.x) { 
			//System.out.println("Parent is to the EAST");
			TraveledDirections.add(Dir.EAST);
		}
		if(p.myLoc.x < myLoc.x) {
			//System.out.println("Parent is to the WEST");
			TraveledDirections.add(Dir.WEST);
		}
		if(p.myLoc.y < myLoc.y) {
			//System.out.println("Parent is to the NORTH");
			TraveledDirections.add(Dir.NORTH);
		}
		if(p.myLoc.y > myLoc.y) {
			//System.out.println("Parent is to the SOUTH");
			TraveledDirections.add(Dir.SOUTH);
		}
	}
	
	
}
