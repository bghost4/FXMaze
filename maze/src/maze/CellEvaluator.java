package maze;

@FunctionalInterface
public interface CellEvaluator<T> {
	boolean eval(T v);
}
