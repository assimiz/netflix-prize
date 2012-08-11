package graphframework;

import java.text.DecimalFormat;

import org.jgrapht.ext.EdgeNameProvider;

import dataframework.Trust;

public class TrustEdgeNameProvider implements EdgeNameProvider<Trust>{

	private DecimalFormat format;
	
	public TrustEdgeNameProvider() {
		format = new DecimalFormat();
		format.setMaximumFractionDigits(2);
		format.setMinimumFractionDigits(2);
	}
	
	@Override
	public String getEdgeName(Trust edge) {
		return format.format(edge.getLevel());
	}

}
