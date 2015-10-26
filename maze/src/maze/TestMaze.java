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


import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.prefs.Preferences;

import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;

import javafx.application.Application;

import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import javafx.scene.Group;
import javafx.scene.Node;

import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Slider;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextField;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.GridPane;

import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.media.AudioClip;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;

import javafx.scene.text.Font;
import javafx.scene.web.WebView;
import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;

public class TestMaze extends Application {

	protected BoardCanvas<MapCell> board;
	protected MapCell last;
	protected int lx,ly;
	
	//Layer the player gets Rendered on
	protected Canvas playerLayer;
	
	//Handle to generation task
	protected Task<Void> taskHandle = null;
	
	//Loaded Font
	protected Font lblFont;
	
	protected StackPane sp;
	protected Label lblStart;
	
	//Animated Label for 3,2,1 go
	protected Label lblAnim;
	
	protected Timeline pt;
	protected Timeline timeline;
	
	protected AudioClip tone1,tone2,err,win;
	
	protected ArrayList<Coord> updatedCells = new ArrayList<>();
	
	protected final SimpleBooleanProperty playProperty = new SimpleBooleanProperty(false);
	
	///Dialogs
	protected final GameOptionsDialog dlgGameOptions = new GameOptionsDialog();
	protected final Dialog<Void> dlgCredits = new Dialog<>();
	protected final Dialog<Void> dlgHelp = new Dialog<>();
	
	protected Stage mainWindow;
	
	protected Coord playerCoord  = new Coord();
	protected SimpleDoubleProperty 
		playerX = new SimpleDoubleProperty(0),
		playerY = new SimpleDoubleProperty(0);
	
	//Level Counter
	protected int Level = 1;

	protected ArrayList<MediaPlayer> playList;
	
	protected void loadMusic() {
		playList = new ArrayList<>();
		MediaPlayer mp;	
		mp = new MediaPlayer(new Media(this.getClass().getResource("resources/music/Eric_Skiff_06_Searching.mp3").toString()));
		mp.setCycleCount(MediaPlayer.INDEFINITE);
		mp.volumeProperty().bind(dlgGameOptions.musicVolumeProperty());
		playList.add(mp);
		mp = new MediaPlayer(new Media(this.getClass().getResource("resources/music/Eric_Skiff_05_Come_and_Find_Me.mp3").toString()));
		mp.setCycleCount(MediaPlayer.INDEFINITE);
		mp.volumeProperty().bind(dlgGameOptions.musicVolumeProperty());
		playList.add(mp);
		mp = new MediaPlayer(new Media(this.getClass().getResource("resources/music/Eric_Skiff_Prologue.mp3").toString()));
		mp.setCycleCount(MediaPlayer.INDEFINITE);
		mp.volumeProperty().bind(dlgGameOptions.musicVolumeProperty());
		playList.add(mp);
		
		err = new AudioClip(this.getClass().getResource("resources/blop.wav").toString());
		win = new AudioClip(this.getClass().getResource("resources/DualToneWin.wav").toString());
		tone1 = new AudioClip(this.getClass().getResource("resources/tone.wav").toString());
		tone2 = new AudioClip(this.getClass().getResource("resources/Tonehp.wav").toString());
	}
	
	protected void initStuff() {
		
		lblFont = Font.loadFont(this.getClass().getResourceAsStream("resources/OrbitronLight.ttf"), 22);
		
		initObjects();
		
		loadMusic();
		buildMenu();
		
		buildCredits();
		buildHelp();
		
		createBindings();
		
		createAnimations();

		loadProperties();
		
		createListeners();
	}
	
	

	private void createBindings() {
		playerLayer.widthProperty().bind(board.widthProperty());
		playerLayer.heightProperty().bind(board.heightProperty());
		board.getCellSizeProperty().bind(dlgGameOptions.cellSizeProperty());
		board.getMapWidthProperty().bind(dlgGameOptions.cellsWideProperty());
		board.getMapHeightProperty().bind(dlgGameOptions.cellsHighProperty());
	}

	protected void createListeners() {
		dlgGameOptions.playMusicProperty().addListener((ob,oldValue,newValue) -> {
			if(newValue && playProperty.get()) {
				//if we are playing, start the music, else just set the property
				playList.get(Level % playList.size()).play();
			} else if( !newValue && playProperty.get()) {
				playList.forEach( (media) -> media.stop() ); 
			}
		}) ;
		
		playProperty.addListener( (ob,oldValue,newValue) -> {
			if(newValue && dlgGameOptions.playMusicProperty().get()) {
				int levelSong = Level % playList.size();
				playList.get(levelSong).play();
			} else  {
				playList.forEach((media) -> media.stop() );  
				}
		} );
		
		
		
		createInputListeners();
		
	}
	
	private void createInputListeners() {
		
		playerLayer.addEventHandler(MouseEvent.ANY, (e) -> playerLayer.requestFocus() );
		playerLayer.setFocusTraversable(true);
		
		playerLayer.setOnKeyPressed( (keyEvent) -> { keyEvent.consume(); });
		playerLayer.setOnKeyReleased( (keyEvent) -> {
			if(playProperty.get()) {
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
		
		
	}

	private void buildHelp() {
		// TODO Auto-generated method stub
		
	}

	private void initObjects() {
		playerCoord.x = 0;
		playerCoord.y = 0;
		
		playerLayer = new Canvas();

		createBoard();
		
		BackgroundFill bgFill = new BackgroundFill(Color.BLACK,new CornerRadii(10),new Insets(-5));
		Background lblBackground = new Background(bgFill);
		
		lblStart = new Label("Start");
		lblStart.setFont(lblFont);
		lblStart.setTextFill(Color.CADETBLUE);
		lblStart.setBackground(new Background(bgFill));
		
		
		
	}
	
	private void createAnimations() {
		pt = new Timeline();
		pt.setCycleCount(1);
		
		//Update every 32 ms ~ 30FPS
		timeline = new Timeline( new KeyFrame( Duration.millis(32) , 
				ae ->  { 
					board.renderBoard(updatedCells);
					updatedCells.clear();
					renderPlayer();
					} ) );

		timeline.setCycleCount(Animation.INDEFINITE);
		timeline.play();

		
	}
	
	
	private void createBoard() {
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
	}	
	
	@Override
	public void start(Stage primaryStage) throws Exception {
		
		mainWindow = primaryStage;
		mainWindow.show();
		
		initStuff();

		VBox vb = new VBox();
		sp = new StackPane();
		
		Group gr = new Group();
		gr.getChildren().add(board);
		gr.getChildren().add(playerLayer);
		
		sp.getChildren().add(gr);
		sp.getChildren().add(lblStart);
		
		vb.getChildren().add(buildMenu());
		vb.getChildren().add(sp);
		VBox.setVgrow(board, Priority.ALWAYS);
		
		primaryStage.setScene(new Scene(vb));
		primaryStage.setTitle("FXMaze");
		
		//primaryStage.show();
		board.renderBoard();
		renderPlayer();
		primaryStage.setOnCloseRequest( (handler) -> { saveProperties(); }  );
		
	} 
	
	private void loadProperties() {
		Preferences prefs = Preferences.userNodeForPackage(this.getClass());
		dlgGameOptions.cellsWideProperty().set(prefs.getInt("CellsWide", 10));
		dlgGameOptions.cellsHighProperty().set(prefs.getInt("CellsHigh", 10));
		dlgGameOptions.cellSizeProperty().set(prefs.getInt("CellSize", 25));
		dlgGameOptions.stretchLengthProperty().set(prefs.getInt("StretchLength", 10));
		dlgGameOptions.playMusicProperty().set(prefs.getBoolean("PlayMusic", true));
		dlgGameOptions.musicVolumeProperty().set(prefs.getDouble("MusicVolume", 0.5));
		dlgGameOptions.showGenerationProperty().set(prefs.getBoolean("SHOW_GENERATION", true));
		Level = prefs.getInt("LastLevel", 1);
		dlgGameOptions.gameSeedProperty().set(prefs.get("GameSeed", "MazeCraze"));
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
								playProperty.set(true);
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
	
	protected void buildCredits() {
		
			
			WebView wv = new WebView();
			dlgCredits.getDialogPane().setContent(wv);
			wv.getEngine().load(this.getClass().getResource("resources/Credits.html").toString());
			wv.setPrefSize(640, 480);
			dlgCredits.initStyle(StageStyle.UTILITY);
			//dlgCredits.initOwner(mainWindow);
			ButtonType btnSaveOptions = new ButtonType("OK!",ButtonData.OK_DONE);
			dlgCredits.getDialogPane().getButtonTypes().add(btnSaveOptions);
			
		
	}

	protected class genTask extends Task<Void> {

		//protected final String initSeed;
		protected final Boolean showGeneration;
		
		public void init() {
			playProperty.set(false);
			
			if(!showGeneration) {
				timeline.pause();
			}
			this.setOnFailed( (ws) -> { System.err.println("Generation Failed"); });
			this.setOnCancelled( (ws) -> { System.err.println("Generation Canceled"); });
			this.setOnSucceeded( (ws) -> { 
				sp.getChildren().remove(lblStart);
				if(!showGeneration) { timeline.play(); }
				play321go(sp);
			});
			
			board.setSeed(dlgGameOptions.gameSeedProperty().get(),Level);
			 
			board.reset();
			resetPlayer();
			board.renderBoard(); 
			renderPlayer();
		}
		
		public genTask() {
			this.showGeneration = dlgGameOptions.showGenerationProperty().getValue();
			mainWindow.setTitle("FX Maze - Level: "+Level);
			init();
		}
		
		@Override
		protected Void call() throws Exception {
			lx = 0; ly = 0; last = null;
			if(!showGeneration) {
				timeline.stop();
			}
			while(!board.completed() && !this.isCancelled() ) {
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
			miGameOptions.setOnAction( 
					(action) -> { 
						Optional<ButtonType> result = dlgGameOptions.showAndWait(); 
							if( result.isPresent() ) {
								if(taskHandle != null && taskHandle.isRunning()) {
									taskHandle.cancel();
								}
								if(result.get().getButtonData() == ButtonData.OK_DONE) {
									
									saveProperties();
									
									if(dlgGameOptions.mazeChangedProperty().get()) {
										playProperty.set(false);
										lblStart.setOpacity(1);
										Level = 1;
										dlgGameOptions.mazeChangedProperty().set(false);
									}
									
									board.renderBoard();
									if(!sp.getChildren().contains(lblStart)) {
										sp.getChildren().add(lblStart);
									}
								}
							}
						} );
			
			MenuItem miRestart = new MenuItem("Restart Level");
			miRestart.setOnAction( (action) -> {
				//taskHandle = new genTask();
				//Thread t = new Thread(taskHandle);
				//t.start();
				resetPlayer();
			});
			
			MenuItem miLevelReset = new MenuItem("Level Reset to 1");
			miLevelReset.setOnAction( (event) -> {
				Level = 1;
				taskHandle = new genTask();
				Thread t = new Thread(taskHandle);
				t.start();
			});	
			
			CheckMenuItem miPlaySound = new CheckMenuItem("Play Music");
			
			miPlaySound.selectedProperty().bindBidirectional(dlgGameOptions.playMusicProperty());
			mOptions.getItems().addAll(miGameOptions,miRestart,miLevelReset,miPlaySound);

		Menu mHelp = new Menu("Help");
			MenuItem miAbout = new MenuItem("About");
			miAbout.setOnAction( (ma) -> { dlgCredits.show();} );
			MenuItem miHelp = new MenuItem("Show Help");
		mHelp.getItems().addAll(miAbout,miHelp);	
		
		mb.getMenus().addAll(mOptions,mHelp);
		
		return mb;
	}
	
	private void saveProperties() {
		Preferences prefs = Preferences.userNodeForPackage(this.getClass());
		prefs.putInt("CellsWide", dlgGameOptions.cellsWideProperty().get());
		prefs.putInt("CellsHigh", dlgGameOptions.cellsHighProperty().get());
		prefs.putInt("CellSize", dlgGameOptions.cellSizeProperty().get());
		prefs.putBoolean("PlayMusic", dlgGameOptions.playMusicProperty().get());
		prefs.putDouble("MusicVolume", dlgGameOptions.musicVolumeProperty().get() );
		prefs.putBoolean("SHOW_GENERATION", dlgGameOptions.showGenerationProperty().get());
		prefs.putInt("StretchLength", dlgGameOptions.stretchLengthProperty().get());
		prefs.putInt("LastLevel", Level);
		prefs.put("GameSeed", dlgGameOptions.gameSeedProperty().get() );
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
		if(last == null) { //new Run
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
			if(a.size() == 0 || spancount > dlgGameOptions.stretchLengthProperty().get()) {
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
					Random r = new Random(dlgGameOptions.gameSeedProperty().get().hashCode());
					while(a.size() < 1) {
						int index = r.nextInt(cellsWithOptions.size());
						c = cellsWithOptions.get(index);
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

