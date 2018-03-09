package com.segment.proxy.cache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * LRU Implementation of the [[Cache]] interface. Values in this implementation are stored as [[CacheRecord]] which provides
 * capability to set ttl for a given entry.
 * Size of the cache is decided at initialization and LRU entries are evicted cache reaches capacity.
 * This implementation also starts a eviction thread if the ttl set in the configs is > 0. This thread checks for expired cache entries
 * and evicts them.
 * Cache methods are synchronized using the synchronized block. This ensures only 1 thread can access the Cache at a given time.
 * Cache cannot be made Volatile here instead since the "get", "set" are not atomic operations and involve get-update-set transactions. Volatile will not guarantee thread safety here.
 * @param <K> The type of the Key
 * @param <V> The type of the Value. Stored as CacheRecord(V val, long ttl)
 */
public class LRUCacheImpl<K, V> implements Cache<K, CacheRecord<V>> {
    private static final Logger LOGGER = LoggerFactory.getLogger(LRUCacheImpl.class);
    private int capacity;
    private int ttl;
    private LinkedHashMap<K, CacheRecord<V>> cache;

    /**
     * Constructor to create Cache. Sets capacity and ttl configs.
     * @param capacity The max size of the cache after which to start evicting LRU entries
     * @param ttlSecs The time in seconds after which an entry is evicted.
     */
    public LRUCacheImpl(int capacity, int ttlSecs) {
        this.capacity = capacity;
        this.ttl = ttlSecs * 1000;
        cache = new LinkedHashMap<>(capacity);
        LOGGER.info("Created LRU cache of size : "+capacity+" and TTL : "+ttlSecs);

        if(this.ttl > 0)
            startCacheEvicter();
    }

    @Override
    public  CacheRecord<V> get(K key) {
        synchronized (cache) {
            if(cache.containsKey(key)) {
                CacheRecord<V> value = cache.get(key);
                cache.remove(key);
                cache.put(key, value);
                return value;
            }
            return null;
        }
    }

    @Override
    public void set(K key, CacheRecord<V> val) {
        synchronized (cache) {
            if(cache.containsKey(key)){
                cache.remove(key);
                cache.put(key, val);
            } else {
                if(cache.size() == capacity){
                    K keyToRemove = cache.entrySet().iterator().next().getKey();
                    cache.remove(keyToRemove);
                    LOGGER.debug("Removed key as per LRU : "+ keyToRemove);
                }
                cache.put(key, val);
            }
        }
    }

    @Override
    public int getSize() {
        synchronized (cache){
            return cache.size();
        }
    }

    @Override
    public boolean contains(K key) {
        synchronized (cache) {
            return cache.containsKey(key);
        }
    }

    @Override
    public void clear() {
        synchronized (cache) {
            cache.clear();
        }
    }

    /**
     * Checks for all keys in the cache and evicts all entries older than the set ttl.
     */
    public void evictOldEntries() {
        long currentTime = System.currentTimeMillis();
        List<K> keysToEvict = new ArrayList<K>();
        synchronized (cache) {
            Iterator<K> iter = cache.keySet().iterator();
            while (iter.hasNext()) {
                K key = iter.next();
                if (currentTime - cache.get(key).getLastAccessed() > ttl)
                    keysToEvict.add(key);
            }

            for (K key : keysToEvict) {
                LOGGER.debug("Evicted key : " + key);
                cache.remove(key);
            }
        }
    }


    /**
     * Starts a thread to evict expired entries in the Cache ie entries older than ttl seconds.
     */
    private void startCacheEvicter() {
        Thread evicter = new Thread() {
            public void run() {
                while(true) {
                    evictOldEntries();
                    try {
                        Thread.sleep(ttl / 2);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        evicter.start();
        LOGGER.info("Started Cache eviction thread. Will remove entries older than : "+ttl+" milliseconds");
    }

}
