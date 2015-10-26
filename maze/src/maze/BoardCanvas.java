/*
FX Maze - Maze generation program
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
Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
*/
package maze;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;

public class BoardCanvas<T> extends Canvas {
	//protected ArrayList<ArrayList<T>> map;
	protected ArrayList<ArrayList<T>> map;
	protected final SimpleIntegerProperty cellsize = new SimpleIntegerProperty(8);
	protected final SimpleIntegerProperty w = new SimpleIntegerProperty(32),h = new SimpleIntegerProperty(32);
	
	protected CellEvaluator<T> e;
	protected CellRandomizer cr;
	protected CellRenderer<T> ren;
	protected CellSupplier<T> cs;
	
	
	
	public T getValue(int x,int y) {
		return map.get(y).get(x);
	}
	
	public void setValue(int x, int y, T val) {
		map.get(y).set(x,val);
	}
	
	public int getMapWidth() { return w.get()-1; }
	public int getMapHeight() { return h.get()-1; }
	
	public void setCellEvaluator(CellEvaluator<T> c) {
		this.e = c;
	}
	
	public void setCellRandomizer( CellRandomizer r) { 
		cr = r;
	}
	
	public void setCellRenderer(CellRenderer<T> cellren) {
		ren = cellren;
	}
	
	protected Random myRandom;
	
	public BoardCanvas(CellSupplier<T> cs,CellEvaluator<T> ce,CellRenderer<T> cr) {
		
		this.setWidth(w.get()*cellsize.get());
		this.setHeight(h.get()*cellsize.get());
		
		this.cs = cs;
		this.e = ce;
		this.ren = cr;
		
		myRandom = new Random();
		
		initializeBoard();
		
		//Listeners
		w.addListener( (ob,oldValue,newValue) ->  {
			this.setWidth((int)newValue*cellsize.get());
			initializeBoard();
			//renderBoard();
		});
		h.addListener((ob,oldValue,newValue) ->  {
			this.setHeight((int)newValue*cellsize.get());
			initializeBoard();
			//renderBoard();
		});
		cellsize.addListener( (ob,oldValue,newValue) -> {
			this.setWidth(w.get()*(int)newValue);
			this.setHeight(h.get()*(int)newValue);
			//initializeBoard();
			//renderBoard();
		});
		
	}
	
	public IntegerProperty getCellSizeProperty() { return cellsize; }
	public IntegerProperty getMapWidthProperty() { return w; }
	public IntegerProperty getMapHeightProperty() { return h; }
	
	
	public ArrayList<Dir> available(int x,int y) {
		ArrayList<Dir> l = new ArrayList<>();
		l.add(Dir.NORTH);
		l.add(Dir.SOUTH);
		l.add(Dir.EAST);
		l.add(Dir.WEST);
		
		if( y == 0 || e.eval( getValue(x,y-1)) ) { //not up
			l.remove(Dir.NORTH);
		}
		if( x == 0 ||  e.eval(getValue(x-1, y)) ) {
			l.remove(Dir.WEST);
		}
		if(x == getMapWidth() || e.eval(getValue(x+1, y)) ) {
			l.remove(Dir.EAST);
		}
		if(y == getMapHeight() || e.eval(getValue(x, y+1)) ) {
			l.remove(Dir.SOUTH);
		}
		return l;
	}
	
	public Dir pickRandomDir(List<Dir> avail) {
		if(avail.size() == 0) {
			return null;
		} else if(avail.size() == 1) {
			return avail.get(0);
		} else {
			Dir[] values = avail.toArray(new Dir[] {});
			return values[myRandom.nextInt(values.length)];
		}
	}
	
	@Deprecated
	public Random getRandom() { return myRandom; }
	
	protected void initializeBoard() {
		
		map = new ArrayList<>();
		
		for(int y=0; y < h.get(); y++) {
			map.add(new ArrayList<>());
			for(int x=0; x < w.get(); x++) {
				T value = cs.get(x,y);
				if(value == null) {
					System.out.println("new map cell is null");
					System.exit(1);
				}
				map.get(y).add(value);
			}
		}
		//renderBoard();
	}
	
	public void setSeed(String strSeed,int Level) {
		int hc = strSeed.hashCode();
		myRandom.setSeed(hc+Level);
	}
	
	public void reset() {
		initializeBoard();
		for(int i=0; i < h.get(); i++) {
			for(int j=0; j < w.get(); j++) {
				setValue(j,i,cs.get(j, i));
			}
		}
	}
	
	public boolean completed() {
		boolean complete = true;
		for(int y=0; y < h.get(); y++) {
			for(int x=0; x < w.get(); x++) {
				try {
					if( !e.eval(getValue(x,y))) {
						return false;
					}
				}catch(Exception e) {
					e.printStackTrace();
				}
			}
		}
		System.out.println("Complete");
		return complete;
	}
	
	public void renderBoard(List<Coord> l) {
		if(this.isVisible()) {
			this.getScene().getWindow().sizeToScene();
		}
		//System.out.println(Thread.currentThread().getName());
		Coord[] a = l.toArray(new Coord[] {} );
		for(Coord cell : a ) {
			T value = getValue(cell.x,cell.y);
			if(value == null) {
				value = (cs.get(cell.x, cell.y));
				setValue(cell.x,cell.y,value);
			}
			ren.renderCell(getGraphicsContext2D(), cell.x, cell.y, cellsize.get(), value);
		}
	}
	
	public void renderBoard() {
		if(this.isVisible()) {
			this.getScene().getWindow().sizeToScene();
		}
		long begin = System.nanoTime();
		for(int y=0; y < h.get(); y++) {
			for(int x=0; x < w.get(); x++) {
				GraphicsContext g2d = this.getGraphicsContext2D();
				T value = getValue(x,y);
				ren.renderCell(g2d, x, y, cellsize.get(), value );
			}
		}
		long end = System.nanoTime();
		
		long delta = end - begin;
		//
		if(delta > 1000*1000) {
			System.out.println(Thread.currentThread().getName()+"::Render took "+delta/1000/1000+" millis");
			//From Thread
		}
		
	}
	
}
