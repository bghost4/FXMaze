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

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextField;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.layout.GridPane;
import javafx.stage.StageStyle;

public class GameOptionsDialog extends Dialog<ButtonType> {
	
	protected final Slider slMusicVolume;
	protected final Spinner<Integer> spnWidth,spnHeight,spnCell,spnStretch; 
	protected final TextField txtSeed;
	protected final CheckBox cbMusic,cbPlaySfx,cbShowGen;
	
	protected final SimpleBooleanProperty mazePropertyChange = new SimpleBooleanProperty(false);
	
	public ObjectProperty<Integer> cellsWideProperty() { return spnWidth.getValueFactory().valueProperty(); }
	public ObjectProperty<Integer> cellsHighProperty() { return spnHeight.getValueFactory().valueProperty(); }
	public ObjectProperty<Integer> cellSizeProperty() { return spnCell.getValueFactory().valueProperty(); }
	public ObjectProperty<Integer> stretchLengthProperty() { return spnStretch.getValueFactory().valueProperty(); }
	
	protected DoubleProperty musicVolumeProperty() { return slMusicVolume.valueProperty(); }
	
	public StringProperty gameSeedProperty() { return txtSeed.textProperty(); }
	
	public BooleanProperty mazeChangedProperty() { return mazePropertyChange; }
	public BooleanProperty showGenerationProperty() { return cbShowGen.selectedProperty(); }
	public BooleanProperty playMusicProperty() { return cbMusic.selectedProperty(); }
	
	
	
	
	
	public GameOptionsDialog() {
		super();
		
		Dialog<ButtonType> dlgGameOptions = this;
		
		dlgGameOptions.initStyle(StageStyle.UTILITY);
		//dlgGameOptions.initOwner(mainWindow);
		
		ChangeListener<Object> chrebuild = (Ob,oldValue,newValue) -> {
			mazePropertyChange.set(true);
		};
		
		txtSeed = new TextField();
		slMusicVolume = new Slider(0,1,0.01);
		cbMusic = new CheckBox("Play Game Music");
		
		cbShowGen = new CheckBox("Show Maze Generation");
		cbPlaySfx = new CheckBox("Play SFX");
		
		spnWidth = new Spinner<>(10,512, 10 ,1);
		spnHeight = new Spinner<>(10,512, 10 ,1);
		spnCell = new Spinner<>(5,64,25 ,1);
		spnStretch  = new Spinner<>(2,512,10,1);
		
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
		
		
		grid.add(spnStretch, 1, 4);
		
		grid.add(slMusicVolume, 1, 5);
		grid.add(cbMusic, 0, 5);
			
		grid.add(cbShowGen, 0, 6);	
			
		ButtonType btnSaveOptions = new ButtonType("Save & Close",ButtonData.OK_DONE);
		dlgGameOptions.getDialogPane().getButtonTypes().add(btnSaveOptions);
	
		txtSeed.textProperty().addListener(chrebuild);
		spnWidth.valueProperty().addListener(chrebuild);
		spnHeight.valueProperty().addListener(chrebuild);
		
		dlgGameOptions.getDialogPane().setContent(grid);
		dlgGameOptions.setTitle("Game Options");
		
	}
	

}
