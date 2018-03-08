package com.segment.proxy.cache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Created by umehta on 3/2/18.
 */
public class LRUCacheImpl<K, V> implements Cache<K, CacheRecord<V>> {
    private static final Logger LOGGER = LoggerFactory.getLogger(LRUCacheImpl.class);
    private int capacity;
    private int ttl;
    private LinkedHashMap<K, CacheRecord<V>> cache;

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
