package com.segment.proxy.cache;

/**
 * Created by umehta on 3/2/18.
 */
public class CacheRecord<V> {
    private V value;
    private long lastAccessed;

    public CacheRecord(V value, long lastAccessed) {
        this.value = value;
        this.lastAccessed = lastAccessed;
    }
    public V getValue() {
        return value;
    }

    public long getLastAccessed() {
        return lastAccessed;
    }
}
