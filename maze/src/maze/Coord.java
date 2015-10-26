/* FX Maze - Maze generation program
Copyright (C) 2015  Delbert Martin <bghost4@gmail.com>

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 2
of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA. */
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