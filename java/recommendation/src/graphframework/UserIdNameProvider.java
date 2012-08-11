package graphframework;

import org.jgrapht.ext.VertexNameProvider;

import dataframework.User;

public class UserIdNameProvider implements VertexNameProvider<User> {

	@Override
	public String getVertexName(User vertex) {
		return vertex.toString();
	}

}
