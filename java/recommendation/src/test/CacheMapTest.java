package test;

import com.sun.jmx.remote.util.CacheMap;

import strategy.MessageKey;
import misc.HardCacheMap;
import dataframework.User;
import graphframework.RandomTrustCalculator;

/**
 * Test for {@link HardCacheMap} and {@link CacheMap}
 * 
 * @author Assaf Mizrachi
 *
 */
public class CacheMapTest {

	public static void main(String[] args) {
		int capacity = 5;
		int num = 10;		
		User[] users = new User[num];
		for (int i = 0; i < num; i++) {
			users[i] = new User(i);
		}
		RandomTrustCalculator calc = new RandomTrustCalculator(capacity);
		
		CacheMap softMap = new CacheMap(capacity);
		for (User u : users) {
			for (User v : users) {
				softMap.put(new MessageKey(u.getId(), v.getId()), calc.getTrust(u, v));
				System.out.println("Soft Map size is " + softMap.size() + ". Map = " + softMap.toString());
			}
		}
		
		HardCacheMap<MessageKey, Double> hardCahceMap = new HardCacheMap<MessageKey, Double>(capacity);
		for (User u : users) {
			for (User v : users) {
				hardCahceMap.put(new MessageKey(u.getId(), v.getId()), calc.getTrust(u, v));
				System.out.println("Hard Map size is " + hardCahceMap.size() + ". Map = " + hardCahceMap.toString());
			}
		}
		
	}
}
