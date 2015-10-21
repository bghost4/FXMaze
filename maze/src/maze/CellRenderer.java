package maze;

import javafx.scene.canvas.GraphicsContext;

@FunctionalInterface
public interface CellRenderer<T> {
	void renderCell(GraphicsContext g,int x, int y, int size,T value);
}
