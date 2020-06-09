package com.cadit.cache;/*
 * This class is an implementation of an AbstractMap with values stored as SoftReferences.
 * A constant size hard cache keeps strong references to last-used values to prevent them from being GC'ed.
 * This class is recommended to be used for large caches and is currently used for SkinResources cache.
 * @author Khushboo Bhatia
 *
 * http://java2go.blogspot.com/2010/04/how-to-write-simple-yet-bullet-proof.html
 *
 * The SoftCache
This one was the trickiest. We also replaced the internal HashMap with a WeakHashMap, however the mapped values are
SoftReference objects. But instead of simply pointing them directly to the target
 object being added to the cache, they point to a wrapper class called ValueHolder. The trick here was
  to not only hold the map entries’ value objects but also their corresponding key objects. And what goes into the
   internal “hard cache” in this case are hard references to these ValueHolder instances. Therefore, because the internal
   “hard cache” is the only place where hard references to the softly referenced ValueHolder objects mapped in the
   internal WeakHashMap are being held and those ValueHolder objects hold hard references to their corresponding map keys,
   ultimately, we can assume that, provided no external hard references to a key K inside the map exist, the map entry
   corresponding to that key K will be kept in the map as long as there’s at least 1 instance of ValueHolder
   containing key K exists in the internal “hard cache”. Otherwise, the weakly referenced key K will be available
   for disposal and so will be its corresponding map entry.
 */

import java.lang.ref.SoftReference;
import java.util.AbstractMap;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.locks.ReentrantLock;

public class SoftCache<K,V> extends AbstractMap<K,V>  {

	/** The internal HashMap that will hold the softly-referenced data */
	private final Map<K,SoftValue<K,V>> hash = Collections.synchronizedMap(new WeakHashMap<K,SoftValue<K,V>>());
	/** The size of hard cache. */
	private final int HARD_SIZE;
	/** This LinkedList is used to keep strong references to a constant number of last used values. */
	//quando rimuovo il ValueHolder dalla lista (perchè la lista è piena)
	// rimuovo la SoftReference (Softvalue in mappa)  ma anche la chiave in mappa
	// perchè la wekMap non trovandosi più la chiave nella lista, la cancella anche dalla mappa
	// se per caso la softreference viene cancellata per mancanza memoria la get on trova più il valore
	// e rimuove lei da codice la chiave (è una softValue quindi dura finchè la memoria non si riempe)
	private final LinkedList<ValueHolder<K,V>> hardCache= new LinkedList<ValueHolder<K,V>>();
	/** The lock used to synchronize access to hardCache*/
	private final ReentrantLock hardCacheLock = new ReentrantLock();

	public SoftCache() {
		this(100); 
	}

	public SoftCache(int hardSize) {
		HARD_SIZE = hardSize;
	}

	public SoftCache(Map<? extends K, ? extends V> t) {
		this();
		putAll(t);
	}	
	public SoftCache(Map<? extends K, ? extends V> t, int hardSize) {
		this(hardSize);
		putAll(t);
	}	

	public void putAll(Map<? extends K, ? extends V> t) {
		Iterator i = t.entrySet().iterator();
		while (i.hasNext()) {
			Map.Entry<K, V> e = (Map.Entry<K, V>) i.next();
			put(e.getKey(), e.getValue());
		}
	}

	public V get(Object key) {
		ValueHolder<K,V> result = null;
		SoftValue<K,V> soft_ref = hash.get(key);
		if (soft_ref != null) {
			// From the SoftReference we get the value, which can be
			// null if it was not in the map,  or if it was cleared by the garbage collector.
			result = soft_ref.get();
			if (result == null) {
				hash.remove(key);
			} else {
				hardCacheLock.lock();
				try{
					hardCache.add(result);
					if (hardCache.size() > HARD_SIZE) {
						hardCache.removeFirst();
					}
				}finally{
					hardCacheLock.unlock();
				}
			}
		}
		if(result!=null)
			return result.getValue();
		else
			return null;
	}

	public V put(K key, V value) {
		SoftValue<K,V> oldVal= hash.put(key, new SoftValue<K,V>(key,value));
		if(oldVal!=null)
			return oldVal.getValue();
		else 
			return null;

	}

	public V remove(Object key) {
		SoftValue<K,V> oldVal =  hash.remove(key);	
		if(oldVal!=null){
			ValueHolder<K,V> toRemove = oldVal.get();
			hardCacheLock.lock();
			try{
				ListIterator<ValueHolder<K,V>> li= hardCache.listIterator(0);
				while(li.hasNext()){
					if(li.next() == toRemove)
						li.remove();
				}
			}finally{
				hardCacheLock.unlock();
			}
			return oldVal.getValue();
		}else{ 
			return null;
		}
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

	public Set entrySet() {
		//Not implemented because this method is not used.
		throw new UnsupportedOperationException();
	}

	/*
	 * This class is a wrapper for value which contains not only the 
	 * value but also the key.  The strong reference to the key prevents
	 * it from being garbage collected. Once the value is cleaned up, there 
	 * is no strong reference to the key and so it is cleaned too (Because we use a WeakHashMap)
	 */

	private static class ValueHolder<K,V> {
		private final K key;
		private final V val;
		private ValueHolder(K key, V value) {                       
			this.key = key;
			this.val = value;
		}

		public K getKey() {
			return this.key;
		}

		public V getValue() {
			return this.val;
		}

	}      

	private static final class SoftValue<K,V> extends SoftReference<ValueHolder<K,V>>  {
		private SoftValue(K key,V value) {
			super(new ValueHolder<K,V>(key,value));
		}

		public K getKey() {
			ValueHolder<K,V> valueHolder=this.get();
			if(valueHolder!=null)
				return valueHolder.getKey();
			else
				return null;

		}

		public V getValue() {
			ValueHolder<K,V> valueHolder=this.get();
			if(valueHolder!=null)
				return valueHolder.getValue();
			else
				return null;               
		}
	}
}
