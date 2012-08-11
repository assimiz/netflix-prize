package algframework;

import java.util.*;

import org.jgrapht.*;

/**
 * A new path is created from a path concatenated to an edge. It's like a linked
 * list.<br>
 * The empty path is composed only of one vertex.<br>
 * In this case the path has no previous path element.<br>
 * .
 *
 * @author mizrachi copied from org.jgrapht.alg.AbstractPathElement by Guillaume Boulmier
 * @since July 5, 2007
 */
public class PathElement<V, E> {
	//~ Instance fields --------------------------------------------------------

	/**
	 * Number of hops of the path.
	 */
	protected int nHops;

	/**
	 * Edge reaching the target vertex of the path.
	 */
	protected E prevEdge;

	/**
	 * Previous path element.
	 */
	protected PathElement<V, E> prevPathElement;

	/**
	 * Target vertex.
	 */
	private V vertex;

	//~ Constructors -----------------------------------------------------------

	/**
	 * Creates a path element by concatenation of an edge to a path element.
	 *
	 * @param pathElement
	 * @param edge edge reaching the end vertex of the path element created.
	 */
	public PathElement(Graph<V, E> graph,
			PathElement<V, E> pathElement, E edge) {
		this.vertex = Graphs.getOppositeVertex(graph, edge, pathElement
				.getVertex());
		this.prevEdge = edge;
		this.prevPathElement = pathElement;

		this.nHops = pathElement.getHopCount() + 1;
	}

	/**
	 * Copy constructor.
	 *
	 * @param original source to copy from
	 */
	public PathElement(PathElement<V, E> original) {
		this.nHops = original.nHops;
		this.prevEdge = original.prevEdge;
		this.prevPathElement = original.prevPathElement;
		this.vertex = original.vertex;
	}

	/**
	 * Creates an empty path element.
	 *
	 * @param vertex end vertex of the path element.
	 */
	public PathElement(V vertex) {
		this.vertex = vertex;
		this.prevEdge = null;
		this.prevPathElement = null;

		this.nHops = 0;
	}

	//~ Methods ----------------------------------------------------------------

	/**
	 * Returns the path as a list of edges.
	 *
	 * @return list of <code>Edge</code>.
	 */
	public List<E> createEdgeListPath() {
		List<E> path = new ArrayList<E>();
		PathElement<V, E> pathElement = this;

		// while start vertex is not reached.
		while (pathElement.getPrevEdge() != null) {
			path.add(pathElement.getPrevEdge());

			pathElement = pathElement.getPrevPathElement();
		}

		Collections.reverse(path);

		return path;
	}

	/**
	 * Returns the number of hops (or number of edges) of the path.
	 *
	 * @return .
	 */
	public int getHopCount() {
		return this.nHops;
	}

	/**
	 * Returns the edge reaching the target vertex of the path.
	 *
	 * @return <code>null</code> if the path is empty.
	 */
	public E getPrevEdge() {
		return this.prevEdge;
	}

	/**
	 * Returns the previous path element.
	 *
	 * @return <code>null</code> is the path is empty.
	 */
	public PathElement<V, E> getPrevPathElement() {
		return this.prevPathElement;
	}

	/**
	 * Returns the target vertex of the path.
	 *
	 * @return .
	 */
	public V getVertex() {
		return this.vertex;
	}
}

// End AbstractPathElement.java

