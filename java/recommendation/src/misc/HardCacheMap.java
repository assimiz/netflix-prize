package misc;

import java.io.Serializable;
import java.lang.ref.SoftReference;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

import com.sun.jmx.remote.util.CacheMap;

/**
 * A map that caches last added values. Unlike {@link CacheMap} which uses {@link SoftReference}
 * to maintain cache, this map uses a {@link Queue} so the cache size is strictly forced.
 * Map does not support * {@link #remove(Object)} and {@link #putAll(Map)} methods.
 * The last will throw an exception, using the first has unpredicted outcome.
 * 
 * @author Assaf Mizrachi
 *
 * @param <K> key
 * @param <V> value
 */
public class HardCacheMap<K, V> extends LinkedHashMap<K, V>
	implements Map<K,V>, Cloneable, Serializable {

	private static final long serialVersionUID = 3056919620031625584L;
	
	private LinkedBlockingQueue<K> queue;
	
	
	/**
	 * @param initialCapacity
	 * @param maxCapacity
	 * @param loadFactor
	 */
	public HardCacheMap(int initialCapacity, int maxCapacity, float loadFactor) {		
		super(initialCapacity, loadFactor);
		if (initialCapacity > maxCapacity) {
			throw new IllegalArgumentException("initial capacity cannot exceed max capacity");
		}
		queue = new LinkedBlockingQueue<K>(maxCapacity);
		
	}

	/**
	 * @param initialCapacity
	 * @param maxCapacity
	 */
	public HardCacheMap(int initialCapacity, int maxCapacity) {
		super(initialCapacity);
		if (initialCapacity > maxCapacity) {
			throw new IllegalArgumentException("initial capacity cannot exceed max capacity");
		}
		queue = new LinkedBlockingQueue<K>(maxCapacity);
	}
	
	/**
	 * @param maxCapacity
	 */
	public HardCacheMap(int maxCapacity) {
		super();
		queue = new LinkedBlockingQueue<K>(maxCapacity);
	}

	/**
	 * @param m
	 * @param initialCapacity
	 */
	public HardCacheMap(int maxCapacity, Map<? extends K, ? extends V> m) {
		super(m);
		if (m.size() > maxCapacity) {
			throw new IllegalArgumentException("map size cannot exceed max capacity");
		}
		queue = new LinkedBlockingQueue<K>(maxCapacity);
	}	
	
	/* (non-Javadoc)
	 * @see java.util.HashMap#put(java.lang.Object, java.lang.Object)
	 */
	@Override
	public V put(K key, V value) {
		if (!queue.offer(key)) {
			remove(queue.remove());
			queue.add(key);
		}
		return super.put(key, value);
	}

	/**
	 * Always throws UnsupportedOperationException
	 * @throws UnsupportedOperationException
	 */
	@Override
	public void putAll(Map<? extends K, ? extends V> m) {
		throw new UnsupportedOperationException();
	}

	/* (non-Javadoc)
	 * @see java.util.HashMap#clear()
	 */
	@Override
	public void clear() {
		super.clear();
		queue.clear();
	}

	/* (non-Javadoc)
	 * @see java.util.HashMap#clone()
	 */
	@Override
	public Object clone() {
		HardCacheMap<K,V> result = (HardCacheMap<K,V>) super.clone();
		result.queue = new LinkedBlockingQueue<K>(queue.size() + queue.remainingCapacity());
		return result;
	}

	
}
