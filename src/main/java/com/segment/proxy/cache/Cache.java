package com.segment.proxy.cache;

/**
 * Cache interface which classes can implement. Enables implementing multiple types of Caches based on requirements.
 * Cache stores Kev Value entries.
 * @param <K> Key stored in the Cache
 * @param <V> Value stored in the cache
 */
public interface Cache<K, V> {

    /**
     *  Get entry from Cache based on given key
     * @param key Key to look up
     * @return Value for the corresponding Key
     */
    V get(K key);

    /**
     * Sets given Key Value pair in the cache
     * @param key Key to set
     * @param val Value to set
     */
    void set(K key, V val);

    /**
     * Get Number of entries in the cache
     * @return the size of the cache
     */
    int getSize();

    /**
     * Check if cache contains given key
     * @param key Key to look up
     * @return true if cache contains key, false otherwise
     */
    boolean contains(K key);

    /**
     * Empties cache
     */
    void clear();
}
