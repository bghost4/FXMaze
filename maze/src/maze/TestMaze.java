package maze;

import java.util.ArrayList;
import java.util.List;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.application.Application;

import javafx.concurrent.Task;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.media.AudioClip;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.application.Platform;
import javafx.beans.property.SimpleDoubleProperty;

public class TestMaze extends Application {

	protected BoardCanvas<MapCell> myCanvas;
	protected MapCell last;
	protected int lx,ly;
	
	protected Coord playerCoord  = new Coord();
	protected SimpleDoubleProperty playerX = new SimpleDoubleProperty(0),playerY = new SimpleDoubleProperty(0);
	protected Canvas playerLayer;
	
	protected Task<Void> taskHandle = null;
	
	protected Timeline pt;
	
	protected ArrayList<Coord> updatedCells = new ArrayList<>();
	
	protected Spinner<Integer> spnWidth,spnHeight,spnCell; 
	protected TextField txtSeed;
	protected MediaPlayer mp;
	protected int Level = 1;
	
	@Override
	public void start(Stage primaryStage) throws Exception {
		
		mp = new MediaPlayer(new Media(this.getClass().getResource("resources/music/Eric_Skiff_06_Searching.mp3").toString()));
		//mp.play();
		playerCoord.x = 0;
		playerCoord.y = 0;
		
		playerLayer = new Canvas();
		
		pt = new Timeline();
		pt.setCycleCount(1);
		
		AudioClip err = new AudioClip(this.getClass().getResource("resources/blop.wav").toString());
		AudioClip win = new AudioClip(this.getClass().getResource("resources/tada.wav").toString());
		
		VBox vb = new VBox();
		playerLayer.addEventHandler(MouseEvent.ANY, (e) -> playerLayer.requestFocus() );
		playerLayer.setFocusTraversable(true);
		
		
		CellSupplier<MapCell> cs = (x,y) -> { 
			//System.out.println("New Map Cell: "+x+","+y);
			MapCell mc = new MapCell();
			mc.getCoord().x = x;
			mc.getCoord().y = y;
			return mc;
		};
		
		CellEvaluator<MapCell> ce = (b) -> {
			if( b == null) { return false; } else {
			return b.getVisited(); } };
		
		CellRenderer<MapCell> cr = (g,x,y,cellsize,d) -> {
			x = x*cellsize;
			y = y*cellsize;
			
			g.setLineWidth(2.0);
			
			if(d != null && d.getVisited()) {
				if(d.end == true) {
					g.setFill(Color.RED);
				} else if(d.start == true) {
					g.setFill(Color.GREEN);
				} else {
					g.setFill(Color.ORANGE);
				}
				g.fillRect(x, y, cellsize, cellsize);
				
				//Draw Walls
				List<Dir> dirs = d.getDirs();
				if( !dirs.contains(Dir.NORTH) ) {
					//Draw northern Wall
					g.strokeLine(x,y,x+cellsize,y);
				}
				if(!dirs.contains(Dir.SOUTH)) {
					//Draw Southern wall
					g.strokeLine(x, y+cellsize, x+cellsize, y+cellsize);
				}
				if(!dirs.contains(Dir.EAST)) {
					//Draw Eastern wall
					g.strokeLine(x+cellsize, y, x+cellsize, y+cellsize);
				}
				if(!dirs.contains(Dir.WEST)) {
					//Draw western wall
					g.strokeLine(x,y,x,y+cellsize);
				}
				
			} else {
				g.setFill(Color.CADETBLUE);
				g.fillRect(x, y, cellsize, cellsize);
				g.strokeRect(x, y, cellsize, cellsize);
			}
		};
		
		myCanvas = new BoardCanvas<>(cs,ce,cr);
		
		playerLayer.setOnKeyReleased( (keyEvent) -> { keyEvent.consume(); });
		playerLayer.setOnKeyPressed( (keyEvent) -> {
			if(keyEvent.getCode().isArrowKey()) {
				List<Dir> avail = myCanvas.getValue((int)playerCoord.x,(int)playerCoord.y).getDirs();
				
				switch(keyEvent.getCode()) {
				
				case UP:
					if(avail.contains(Dir.NORTH)) {
						playerCoord.y--;
					} else {
						err.play();
					}
					break;
				case DOWN:
					if(avail.contains(Dir.SOUTH)) {
						playerCoord.y++;
					} else {
						err.play();
					}
					
					break;
				case LEFT:
					if(avail.contains(Dir.WEST)) {
						playerCoord.x--;
					} else {
						err.play();
					}
					break;
				case RIGHT:
					if(avail.contains(Dir.EAST)) {
						playerCoord.x++;
					} else {
						err.play();
					}
					default:
						break;
				}
			}
			pt.getKeyFrames().clear();
			pt.getKeyFrames().add(
					new KeyFrame(Duration.millis(75),
					new KeyValue(playerX,playerCoord.x)));
			pt.getKeyFrames().add(
					new KeyFrame(Duration.millis(75),
					new KeyValue(playerY,playerCoord.y)));
			pt.play();
			
			MapCell mc = myCanvas.getValue(playerCoord.x, playerCoord.y);
			if(mc.end) {
				win.play();
				taskHandle = new genTask();
				
				taskHandle.setOnFailed( (ws) -> { System.err.println("Generation Failed"); });
				taskHandle.setOnCancelled( (ws) -> { System.err.println("Generation Canceled"); });
				taskHandle.setOnSucceeded( (ws) -> { System.err.println("Generation Sucess"); });
				
				Thread t = new Thread(taskHandle);
				t.setDaemon(true);
				t.start();
			}
			keyEvent.consume();
		});
		
		spnWidth = new Spinner<>(10,512,32,1);
		//myCanvas.getMapWidthProperty().bind(spnWidth.valueProperty());
		spnHeight = new Spinner<>(10,512,32,1);
		//myCanvas.getMapHeightProperty().bind(spnHeight.valueProperty());
		
		Button g = new Button("Generate");
		g.setOnAction((action) -> { 
			if(taskHandle == null || taskHandle.isDone()) {
				taskHandle = new genTask(txtSeed.getText());
				
				taskHandle.setOnFailed( (ws) -> { System.err.println("Generation Failed"); });
				taskHandle.setOnCancelled( (ws) -> { System.err.println("Generation Canceled"); });
				taskHandle.setOnSucceeded( (ws) -> { System.err.println("Generation Sucess"); });
				
				Thread t = new Thread(taskHandle);
				t.setDaemon(true);
				t.start();
			} else {
				//Still Running
			}
		} );
		
		//Update every 32 ms ~ 30FPS
		Timeline timeline = new Timeline( new KeyFrame( Duration.millis(32) , 
				ae ->  { 
					myCanvas.renderBoard(updatedCells);
					updatedCells.clear();
					renderPlayer();
					} ) );
		
		
		
		
		timeline.setCycleCount(Animation.INDEFINITE);
		timeline.play();
		
		playerLayer.widthProperty().bind(myCanvas.widthProperty());
		playerLayer.heightProperty().bind(myCanvas.heightProperty());
		
		spnCell = new Spinner<>(3,64,8,1);
			myCanvas.getCellSizeProperty().bind(spnCell.valueProperty());
			
		
		
		HBox hb = new HBox();
		hb.getChildren().add(new Label("Cell Size:"));
		hb.getChildren().add(spnCell);
		hb.getChildren().add(new Label("Grid Width:"));
		hb.getChildren().add(spnWidth);
		hb.getChildren().add(new Label("GridHeight:"));
		hb.getChildren().add(spnHeight);
		hb.getChildren().add(g);
		txtSeed = new TextField();
		txtSeed.setOnAction( (event) -> { myCanvas.setSeed(txtSeed.getText()); } );
		hb.getChildren().add(new Label("Level Seed:"));
		hb.getChildren().add(txtSeed);
		
		vb.getChildren().add(hb);
		
		Group gr = new Group();
		gr.getChildren().add(myCanvas);
		gr.getChildren().add(playerLayer);
		
		vb.getChildren().add(gr);
		VBox.setVgrow(myCanvas, Priority.ALWAYS);

		primaryStage.setScene(new Scene(vb));
		primaryStage.setTitle("Eval");
		primaryStage.show();
		//myCanvas.renderBoard();
		renderPlayer();
	} 
	
	protected class genTask extends Task<Void> {

		protected final String initSeed;
		
		public void init() {
			myCanvas.getMapWidthProperty().set(spnWidth.getValue());
			myCanvas.getMapHeightProperty().set(spnHeight.getValue());
			if(initSeed != null) {
				myCanvas.setSeed(txtSeed.getText());
			} 
			myCanvas.reset();
			resetPlayer();
			myCanvas.renderBoard(); 
			renderPlayer();
		}
		
		public genTask(String seed) {
			initSeed = seed;
			init();
		}
		
		public genTask() {
			initSeed = null;
			init();
		}
		
		@Override
		protected Void call() throws Exception {
			lx = 0; ly = 0; last = null;
			while(!myCanvas.completed()) {
				updatedCells.add(generate().getCoord());
				Thread.sleep(5);
			}
			Platform.runLater( () -> { myCanvas.renderBoard(); playerLayer.requestFocus();} );
			
			return null;
		}
		
	}
	
	public void resetPlayer() {
		playerCoord.x = 0;
		playerCoord.y = 0;
		playerX.set(0);
		playerY.set(0);
		pt.getKeyFrames().clear();
		pt.getKeyFrames().add(
				new KeyFrame(Duration.millis(75),
				new KeyValue(playerX,playerCoord.x)));
		pt.getKeyFrames().add(
				new KeyFrame(Duration.millis(75),
				new KeyValue(playerY,playerCoord.y)));
		pt.play();
		
	}
	
	public void renderPlayer() {
		int cellsize = myCanvas.getCellSizeProperty().get();
		playerLayer.getGraphicsContext2D().setFill(Color.AQUA);
		playerLayer.getGraphicsContext2D().clearRect(0, 0, playerLayer.getWidth(), playerLayer.getHeight());
		playerLayer.getGraphicsContext2D().fillRect(playerX.get()*cellsize+2,playerY.get()*cellsize+2,cellsize-4,cellsize-4);
	}
	
	public MapCell generate() {
		//Start at 0,0;
		if(last == null) {
			lx = 0;
			ly = 0;
		} 

			MapCell c = myCanvas.getValue(lx,ly);
			if(last == null) {
				c.start = true;
			} else {
				c.setParent(last);
			}

			List<Dir> a = myCanvas.available(lx,ly);
			if(myCanvas.getMapHeight() == ly && myCanvas.getMapWidth() == lx) {
				a.clear();
				c.setEnd();
			}
			if(a.size() == 0) {
				if(myCanvas.completed()) {
					//c.setEnd();
					myCanvas.setValue(lx,ly,c);
				} else {
					//Backtrack until we find a cell with available Directions
					updatedCells.add(c.getCoord());
					while(a.size() < 1) {
						c = c.getParent();
						lx = c.getCoord().x;
						ly = c.getCoord().y;
						a = myCanvas.available(lx,ly);
					}
				}
			} 
			
			
			
			Dir rd = myCanvas.pickRandomDir(a);
			if(rd == null) {
				//No where else to go
				System.out.println("No Directions to go");
			} else {
				c.addDirection(rd);
				//myCanvas.setValue(lx,ly,c);
				
				switch(rd) {
					case NORTH:
						ly--;
						break;
					case SOUTH:
						ly++;
						break;
					case EAST:
						lx++;
						break;
					case WEST:
						lx--;
						break;
					default:
						System.out.println("Entering the Abyss");
						break;
				}
				last = c;
			}
			return c;
	}

	
	
	
	
	public static void main(String[] args) {
		launch(args);
	}
	
}
