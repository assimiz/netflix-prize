package graphframework;

import misc.HardCacheMap;
import strategy.MessageKey;
import dataframework.User;

/**
 * {@link TrustCalculator} that caches last calculations. This is useful
 * when:
 * 1. The calculator is used to build a graph in combine with {@link EdgeAdditionPolicer} that uses
 * this calculator for its decision (w/o caching calculator will be called at least twice, once
 * for the decision of adding the edge or not and second for the weight calculation.
 * 2. When BestOfBreedTrustGraphGenerator is called, the edges are first sorted by their weight,
 * (per user), hence, coming to add them to the graph the calculation is already done.
 * 
 * @author Assaf Mizrachi
 *
 */
public abstract class CacheTrustCalculator implements TrustCalculator {

	private HardCacheMap<MessageKey, Double> cacheMap;
	
	/**
	 * Constructor for a cache size of 1 (only last calculation is saved).
	 */
	public CacheTrustCalculator() {
		cacheMap = new HardCacheMap<MessageKey, Double>(1);
	}
	
	/**
	 * Constructor for a specified cache size).
	 * @param cachSize the cache size
	 */
	public CacheTrustCalculator(int cachSize) {
		cacheMap = new HardCacheMap<MessageKey, Double>(cachSize);
	}
	
	@Override
	public double getTrust(User from, User to) {
		MessageKey key = new MessageKey(from.getId(), to.getId());
		if (cacheMap.containsKey(key)) {
			return (Double) cacheMap.get(key);
		} else {
			double trust = calcTrust(from, to);
			cacheMap.put(key, trust);			
			return trust;
		}
	}

	protected abstract double calcTrust(User from, User to);
}
