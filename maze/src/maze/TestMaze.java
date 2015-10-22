package maze;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.application.Application;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextField;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.media.AudioClip;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.scene.text.Font;
import javafx.application.Platform;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;

public class TestMaze extends Application {

	protected BoardCanvas<MapCell> board;
	protected MapCell last;
	protected int lx,ly;
	
	protected Coord playerCoord  = new Coord();
	protected SimpleDoubleProperty playerX = new SimpleDoubleProperty(0),playerY = new SimpleDoubleProperty(0);
	protected Canvas playerLayer;
	
	protected Task<Void> taskHandle = null;
	protected Font lblFont;
	protected Label lblAnim;
	protected Timeline pt;
	
	Timeline timeline;
	protected boolean play = false;
	
	protected AudioClip tone1,tone2;
	
	protected ArrayList<Coord> updatedCells = new ArrayList<>();
	
	protected Spinner<Integer> spnWidth,spnHeight,spnCell; 
	protected TextField txtSeed;
	
	//protected MediaPlayer mp;
	
	//Level Counter
	protected int Level = 1;
	
	protected Dialog<Void> dlgGameOptions = null;
	protected Dialog<Void> dlgCredits = null;
	protected Dialog<Void> dlgHelp = null;
	
	protected Stage mainWindow;
	
	protected final SimpleIntegerProperty StretchLength = new SimpleIntegerProperty(10);
	
	protected StackPane sp;
	
	@Override
	public void start(Stage primaryStage) throws Exception {
		
		mainWindow = primaryStage;
		
		//mp = new MediaPlayer(new Media(this.getClass().getResource("resources/music/Eric_Skiff_06_Searching.mp3").toString()));
		//mp.play();
		playerCoord.x = 0;
		playerCoord.y = 0;
		
		playerLayer = new Canvas();
		
		pt = new Timeline();
		pt.setCycleCount(1);
		
		AudioClip err = new AudioClip(this.getClass().getResource("resources/blop.wav").toString());
		AudioClip win = new AudioClip(this.getClass().getResource("resources/tada.wav").toString());
		tone1 = new AudioClip(this.getClass().getResource("resources/tone.wav").toString());
		tone2 = new AudioClip(this.getClass().getResource("resources/Tonehp.wav").toString());
		lblFont = Font.loadFont(this.getClass().getResourceAsStream("resources/OrbitronLight.ttf"), 22);
		
		
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
		
		board = new BoardCanvas<>(cs,ce,cr);
		
		
		
		playerLayer.setOnKeyPressed( (keyEvent) -> { keyEvent.consume(); });
		playerLayer.setOnKeyReleased( (keyEvent) -> {
			if(play) {
			if(keyEvent.getCode().isArrowKey()) {
				List<Dir> avail = board.getValue((int)playerCoord.x,(int)playerCoord.y).getDirs();
				
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
			
			MapCell mc = board.getValue(playerCoord.x, playerCoord.y);
			if(mc.end) {
				win.play();
				Level++;
				taskHandle = new genTask();
				Thread t = new Thread(taskHandle);
				t.setDaemon(true);
				t.start();
			}
			}
			keyEvent.consume();
		});
		
		spnWidth = new Spinner<>(10,512,32,1);
		spnHeight = new Spinner<>(10,512,32,1);
		spnCell = new Spinner<>(3,64,8,1);
			board.getCellSizeProperty().bind(spnCell.valueProperty());
		
		Button g = new Button("Generate");
		g.setOnAction((action) -> { 
			if(taskHandle == null || taskHandle.isDone()) {
				taskHandle = new genTask();
				Thread t = new Thread(taskHandle);
				t.setDaemon(true);
				t.start();
			} else {
				//Still Running
			}
		} );
		
		//Update every 32 ms ~ 30FPS
		timeline = new Timeline( new KeyFrame( Duration.millis(32) , 
				ae ->  { 
					board.renderBoard(updatedCells);
					updatedCells.clear();
					renderPlayer();
					} ) );

		timeline.setCycleCount(Animation.INDEFINITE);
		timeline.play();
		
		playerLayer.widthProperty().bind(board.widthProperty());
		playerLayer.heightProperty().bind(board.heightProperty());

		txtSeed = new TextField();
		
		//vb.getChildren().add(hb);
		
		VBox vb = new VBox();
		
		sp = new StackPane();
		
		Group gr = new Group();
		gr.getChildren().add(board);
		gr.getChildren().add(playerLayer);
		
		BackgroundFill bgFill = new BackgroundFill(Color.BLACK,new CornerRadii(10),new Insets(-5));
		Background lblBackground = new Background(bgFill);
		
		Label lblStart = new Label("Start");
		lblStart.setFont(lblFont);
		lblStart.setTextFill(Color.CADETBLUE);
		lblStart.setBackground(new Background(bgFill));

		lblStart.setOnMouseClicked( (mouseEvent) -> {
			FadeTransition ft = new FadeTransition(Duration.millis(500),lblStart);
			ft.setFromValue(1.0f);
			ft.setToValue(0f);
			ft.setCycleCount(1);
			ft.setAutoReverse(false);
			ft.setOnFinished( (fin) -> { 
				if(taskHandle == null || taskHandle.isDone()) {
				taskHandle = new genTask();
				Thread t = new Thread(taskHandle);
				t.setDaemon(true);
				t.start();
			} else {
				//Still Running
			} });
			ft.play();
		});
		
		sp.getChildren().add(gr);
		sp.getChildren().add(lblStart);
		
		vb.getChildren().add(buildMenu());
		vb.getChildren().add(sp);
		VBox.setVgrow(board, Priority.ALWAYS);
		
		primaryStage.setScene(new Scene(vb));
		primaryStage.setTitle("FXMaze");
		buildGameOptions();
		primaryStage.show();
		
		renderPlayer();
	} 
	
	protected Animation fadeOut(Node n) {
		FadeTransition ft = new FadeTransition();
			ft.setNode(n);
			ft.setDuration(Duration.millis(1000));
			ft.setFromValue(1.0f);
			ft.setToValue(0.0f);
		return ft;
	}
	
	protected void play321go(StackPane content) {
		
		System.out.println("Starting Animation");
		BackgroundFill bgFill = new BackgroundFill(Color.BLACK,new CornerRadii(10),new Insets(-5));
		Background lblBackground = new Background(bgFill);
		final Label lblText = new Label();
		
		lblText.setFont(lblFont);
		lblText.setTextFill(Color.CADETBLUE);
		lblText.setBackground(new Background(bgFill));
		lblText.setOpacity(1);
		lblText.setText("Level "+Level);
		content.getChildren().add(lblText);
		
		Animation a = fadeOut(lblText);
		a.setOnFinished( (event) -> {
			tone1.play();
			lblText.setText("3");
			Animation b = fadeOut(lblText);
				b.setOnFinished( (e2) -> {
					tone1.play();
					lblText.setText("2");
					Animation c = fadeOut(lblText);
					c.setOnFinished( (ce) -> {
						tone1.play();
						lblText.setText("1");
						Animation d = fadeOut(lblText);
						d.setOnFinished((de) -> { 
							lblText.setText("GO!");
							tone2.play();
							Animation e = fadeOut(lblText);
							e.setOnFinished((ee) -> {
								content.getChildren().remove(lblText);
								play = true;
							});
							e.play();
						});
						d.play();	
						
					});	
					c.play();
					
				});
				b.play();
			} );
			a.play();
			
	}
	
	protected void showCredits() {
		
	}
	
	protected void buildGameOptions() {
		if(dlgGameOptions == null) {
			
			dlgGameOptions = new Dialog<Void>();
			dlgGameOptions.initStyle(StageStyle.UTILITY);
			dlgGameOptions.initOwner(mainWindow);
			
			GridPane grid = new GridPane();
			grid.setHgap(10);
			grid.setVgap(10);
			
			grid.add(new Label("Game Seed:"),0,0);
			grid.add(txtSeed,1,0);
			grid.add(new Label("Cell Size:"),0,1);
			grid.add(spnCell,1,1);
			grid.add(new Label("Grid Width:"),0,2);
			grid.add(spnWidth,1,2);
			grid.add(new Label("GridHeight:"),0,3);
			grid.add(spnHeight,1,3);
			grid.add(new Label("Path Stretch Length"),0,4);
			Spinner<Integer> spnStretch = new Spinner<>(2,512,StretchLength.get(),1);
			StretchLength.bind(spnStretch.valueProperty());
			grid.add(spnStretch, 1, 4);
			
			ButtonType btnSaveOptions = new ButtonType("Save & Close",ButtonData.OK_DONE);
			dlgGameOptions.getDialogPane().getButtonTypes().add(btnSaveOptions);
				
			dlgGameOptions.getDialogPane().setContent(grid);
			dlgGameOptions.setTitle("Game Options");
		}
	}
	
	
	
	protected class genTask extends Task<Void> {

		//protected final String initSeed;
		protected final boolean showGeneration = true;
		
		public void init() {
			if(!showGeneration) {
				timeline.pause();
			}
			this.setOnFailed( (ws) -> { System.err.println("Generation Failed"); });
			this.setOnCancelled( (ws) -> { System.err.println("Generation Canceled"); });
			//taskHandle.setOnSucceeded( (ws) -> { System.err.println("Generation Sucess"); });
			this.setOnSucceeded( (ws) -> { 
				if(!showGeneration) { timeline.play(); }
				play321go(sp);
			});
			
			board.getMapWidthProperty().set(spnWidth.getValue());
			board.getMapHeightProperty().set(spnHeight.getValue());
			
			board.setSeed(txtSeed.getText(),Level);
			 
			board.reset();
			resetPlayer();
			board.renderBoard(); 
			renderPlayer();
		}
		
		public genTask() {
			init();
		}
		
		@Override
		protected Void call() throws Exception {
			lx = 0; ly = 0; last = null;
			if(!showGeneration) {
				
			}
			while(!board.completed()) {
				updatedCells.add(generate().getCoord());
				if(showGeneration) {
					Thread.sleep(10);
				}
				
			}
			Platform.runLater( () -> { board.renderBoard(); playerLayer.requestFocus();} );
			
			return null;
		}
		
	}
	
	protected MenuBar buildMenu() {
		MenuBar mb = new MenuBar();
		
		Menu mOptions = new Menu("Options");
			MenuItem miGameOptions = new MenuItem("Game Options");
			miGameOptions.setOnAction( (action) -> dlgGameOptions.show() );
			MenuItem miRestart = new MenuItem("Restart Level");
			miRestart.setOnAction( (action) -> {
				taskHandle = new genTask();
				Thread t = new Thread(taskHandle);
				t.start();
			});
			mOptions.getItems().addAll(miGameOptions,miRestart);
			
			
			
		
		Menu mHelp = new Menu("Help");
			MenuItem miAbout = new MenuItem("About");
			MenuItem miHelp = new MenuItem("Show Help");
		mHelp.getItems().addAll(miAbout,miHelp);	
		
		mb.getMenus().addAll(mOptions,mHelp);
		
		return mb;
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
		int cellsize = board.getCellSizeProperty().get();
		playerLayer.getGraphicsContext2D().setFill(Color.AQUA);
		playerLayer.getGraphicsContext2D().clearRect(0, 0, playerLayer.getWidth(), playerLayer.getHeight());
		playerLayer.getGraphicsContext2D().fillRect(playerX.get()*cellsize+2,playerY.get()*cellsize+2,cellsize-4,cellsize-4);
	}
	
	protected int spancount = 0;
	protected ArrayList<MapCell> cellsWithOptions;
	
	public MapCell generate() {
		//Start at 0,0;
		
		if(last == null) {
			lx = 0;
			ly = 0;
			cellsWithOptions = new ArrayList<>();
			spancount = 0;
		} 

			MapCell c = board.getValue(lx,ly);
			if(last == null) {
				c.start = true;
			} else {
				c.setParent(last);
			}

			List<Dir> a = board.available(lx,ly);
			if(a.size() > 1) {
				cellsWithOptions.add(c);
			}
			if(board.getMapHeight() == ly && board.getMapWidth() == lx) {
				a.clear();
				c.setEnd();
			}
			if(a.size() == 0 || spancount > StretchLength.get()) {
				if(spancount > 10) {
					//System.out.println("Spancount reached");
					spancount = 0; 
				}
				if(board.completed()) {
					//c.setEnd();
					board.setValue(lx,ly,c);
				} else {
					//Backtrack until we find a cell with available Directions
					updatedCells.add(c.getCoord());
					Random r = new Random(txtSeed.getText().hashCode());
					while(a.size() < 1) {
						int index = r.nextInt(cellsWithOptions.size());
						c = cellsWithOptions.get(index);
						//c = c.getParent();
						lx = c.getCoord().x;
						ly = c.getCoord().y;
						a = board.available(lx,ly);
					}
				}
			} 
			spancount++;
			
			Dir rd = board.pickRandomDir(a);
			if(rd == null) {
				//System.out.println("No Directions to go");
			} else {
				c.addDirection(rd);
				
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

