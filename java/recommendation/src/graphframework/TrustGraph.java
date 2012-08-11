package graphframework;

import java.io.Serializable;

import org.jgrapht.DirectedGraph;
import org.jgrapht.graph.AbstractBaseGraph;

import dataframework.Trust;
import dataframework.User;

/**
 * A trsut graph. A trust network graph is an directed graph for which  
 * at most one edge connects any two vertices in each direction,
 * and loops are not permitted. Each node represents an agent and if
 * exists an edge from agent A to B it means that agent A trust B. The
 * level of trust is represented by the edge's weight.
 * 
 * @author mizrachi
 *
 * @param <V> Vertex
 * @param <E> Edge
 */
public class TrustGraph extends AbstractBaseGraph<User, Trust>
	implements DirectedGraph<User, Trust>, Serializable {

	private static final long serialVersionUID = -642225678463301798L;
	
	/**
     * Creates a new trust graph.
     *
     * @param edgeClass class on which to base factory for edges
     */
	public TrustGraph() {
		super(new TrustBasedEdgeFactory(), false, false);
	}

	@Override
	public TrustBasedEdgeFactory getEdgeFactory() {
		return (TrustBasedEdgeFactory) super.getEdgeFactory();
	}

	
}

