package misc;

import java.util.*;

import org.jgrapht.*;
import org.jgrapht.generate.GraphGenerator;
import org.jgrapht.generate.LinearGraphGenerator;


/**
 * Generates a linear graph of any size. The code based on 
 * {@link LinearGraphGenerator} and differs by that for a 
 * directed graph, the edges are oriented also from 
 * END_VERTEX toSTART_VERTEX.
 *
 * @author Assaf Mizrachi
 * @since Sep 16, 2003
 */
public class BidirectionalLinearGraphGenerator<V, E>
    implements GraphGenerator<V, E, V>
{
    //~ Static fields/initializers ---------------------------------------------

    /**
     * Role for the first vertex generated.
     */
    public static final String START_VERTEX = "Start Vertex";

    /**
     * Role for the last vertex generated.
     */
    public static final String END_VERTEX = "End Vertex";

    //~ Instance fields --------------------------------------------------------

    private int size;

    //~ Constructors -----------------------------------------------------------

    /**
     * Construct a new LinearGraphGenerator.
     *
     * @param size number of vertices to be generated
     *
     * @throws IllegalArgumentException if the specified size is negative.
     */
    public BidirectionalLinearGraphGenerator(int size)
    {
        if (size < 0) {
            throw new IllegalArgumentException("must be non-negative");
        }

        this.size = size;
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * {@inheritDoc}
     */
    public void generateGraph(
        Graph<V, E> target,
        VertexFactory<V> vertexFactory,
        Map<String, V> resultMap)
    {
        V lastVertex = null;

        for (int i = 0; i < size; ++i) {
            V newVertex = vertexFactory.createVertex();
            target.addVertex(newVertex);

            if (lastVertex == null) {
                if (resultMap != null) {
                    resultMap.put(START_VERTEX, newVertex);
                }
            } else {            	
                target.addEdge(lastVertex, newVertex);
                if (target instanceof DirectedGraph<?, ?>) {
                	target.addEdge(newVertex, lastVertex);
                }
            }

            lastVertex = newVertex;
        }

        if ((resultMap != null) && (lastVertex != null)) {
            resultMap.put(END_VERTEX, lastVertex);
        }
    }
}

// End LinearGraphGenerator.java
