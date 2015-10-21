package maze;

public class Coord {
	public int x;
	public int y;
	
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof Coord) {
			Coord other = (Coord)obj;
			if( other.x == x && other.y == y) { return true; }
			else { return false; }	
		} else {
			return false;
		}
	}
}