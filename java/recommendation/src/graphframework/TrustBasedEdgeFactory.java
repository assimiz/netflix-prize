package graphframework;

import java.io.Serializable;

import org.jgrapht.EdgeFactory;

import dataframework.Trust;
import dataframework.User;

/**
 * 
 * @author mizrachi
 *
 */
public class TrustBasedEdgeFactory implements EdgeFactory<User, Trust>, Serializable {

	private static final long serialVersionUID = -2424471836547317953L;

		@Override
	public Trust createEdge(User sourceVertex, User targetVertex) {
		
		return new Trust();
	}	
}
