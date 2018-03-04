package com.segment.proxy.cache;

import com.segment.proxy.configs.ProxyConfigs;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Created by umehta on 3/2/18.
 */
public class LRUCacheImpl<K, V> implements Cache<K, CacheRecord<V>> {
    private int capacity;
    private int ttl;
    private LinkedHashMap<K, CacheRecord<V>> cache;

    public LRUCacheImpl(int capacity, int ttlSecs) {
        this.capacity = capacity;
        this.ttl = ttlSecs * 1000;
        cache = new LinkedHashMap<>(capacity);
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
                    System.out.println("Removed old key : "+(String)keyToRemove);
                }
                cache.put(key, val);
            }
        }
    }

    @Override
    public int getCapacity() {
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

    private void evictOldEntries() {
        long currentTime = System.currentTimeMillis();
        List<K> keysToEvict = new ArrayList<K>();
        synchronized (cache) {
            Iterator<K> iter = cache.keySet().iterator();
            while (iter.hasNext()) {
                K key = iter.next();
                if (currentTime - cache.get(key).getLastAccessed() > ttl)
                    keysToEvict.add(key);
            }
        }
        for(K key: keysToEvict){
            System.out.println("Removed : "+key);
            cache.remove(key);
        }
    }

    private void startCacheEvicter() {
        Thread evicter = new Thread() {
            public void run() {
                while(true) {
                    evictOldEntries();
                    try {
                        Thread.sleep(ttl);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        evicter.start();
        System.out.println("Started eviction thread : "+ttl);
    }

}
