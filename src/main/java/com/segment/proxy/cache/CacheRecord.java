package com.segment.proxy.cache;

/**
 * Generic class to store various types of values in the cache.
 * Also provides another optional field to store the last Accessed time for that entry. Helps to make eviction decision based on Cache implementation.
 */
public class CacheRecord<V> {
    private V value;
    private long lastAccessed;

    /**
     * Creates new CacheRecord object with provided value and lastAccessed time.
     * @param value The value of the Cache Entry
     * @param lastAccessed Last accessed time of the Entry
     */
    public CacheRecord(V value, long lastAccessed) {
        this.value = value;
        this.lastAccessed = lastAccessed;
    }

    /**
     * Creates new CacheRecord object with provided value. Last Accessed is set to -1 to indicate this field is not used.
     * @param value
     */
    public CacheRecord(V value) {
        this(value, -1);
    }

    /**
     * Returns the value field stored by the object.
     * @return value stored
     */
    public V getValue() {
        return value;
    }

    /**
     * Returns the last accessed time
     * @return last accessed time
     */
    public long getLastAccessed() {
        return lastAccessed;
    }

    public void setLastAccessed(long lastAccessed) {
        this.lastAccessed = lastAccessed;
    }
}
