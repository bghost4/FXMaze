package maze;

@FunctionalInterface
public interface CellSupplier<T> {
	public T get(int x,int y);
}
