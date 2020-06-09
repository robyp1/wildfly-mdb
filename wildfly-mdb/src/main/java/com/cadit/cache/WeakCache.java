package com.cadit.cache;/*
 * This class is an implementation of AbstractMap which internally uses a synchronized WeakHashMap.
 * A constant size hard cache keeps strong references to last-used keys to prevent them from being GC'ed. 
 * This class is recommended to be used for large caches and is currently used for SkinResources cache.
 * @author Khushboo Bhatia
 */

import java.util.AbstractMap;
import java.util.Collections;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.locks.ReentrantLock;

public class WeakCache<K,V> extends AbstractMap<K,V> {

	/** The internal HashMap that will hold the weakly-referenced data */
	private final Map<K,V> hash = Collections.synchronizedMap(new WeakHashMap<K,V>());
	/** The size of hard cache. */
	private final int HARD_SIZE;
	/** This LinkedList is used to keep strong references to a constant number of last used keys. */
	private final LinkedList<K> hardCache = new LinkedList<K>();  
	/** The lock used to synchronize access to hardCache*/
	private final ReentrantLock hardCacheLock = new ReentrantLock();

	public WeakCache() {
		this(100); 
	}

	public WeakCache(int hardSize) {
		HARD_SIZE = hardSize;
	}

	public WeakCache(Map<? extends K, ? extends V> t) {
		this();
		hash.putAll(t);
	}	
	public WeakCache(Map<? extends K, ? extends V> t, int hardSize) {
		this(hardSize);
		hash.putAll(t);
	}	

	public V get(Object key) {
		V val = hash.get(key);
		if (val != null) {			
			hardCacheLock.lock();
			try{
				hardCache.add((K)key);
				if (hardCache.size() > HARD_SIZE) 
					hardCache.removeFirst();
			}finally{
				hardCacheLock.unlock();
			}
		}
		return val;
	}

	public V put(K key, V value) {
		if (value != null) {				
			hardCacheLock.lock();
			try{
				hardCache.add((K)key);
				if (hardCache.size() > HARD_SIZE) 
					hardCache.removeFirst();
			}finally{
				hardCacheLock.unlock();
			}

		}
		return hash.put(key, value);

	}

	public V remove(Object key) {
		V oldVal =  hash.remove(key);	
		if(key!=null){
			hardCacheLock.lock();
			try{
				ListIterator<K> li= hardCache.listIterator(0);
				while(li.hasNext()){
					if(li.next().equals(key))
						li.remove();
				}
			}finally{
				hardCacheLock.unlock();
			}

		}
		return oldVal;

	}

	public void clear() {
		hardCacheLock.lock();
		try{
			hardCache.clear();
		}finally{
			hardCacheLock.unlock();
		}
		hash.clear();
	}

	public int size() {
		return hash.size();
	}

	public Set<Entry<K,V>> entrySet() {
		return hash.entrySet();
	}
}