package com.segment.proxy.cache;

/**
 * Created by umehta on 3/2/18.
 */
public interface Cache<K, V> {

    public V get(K key);

    public void set(K key, V val);

    public int getCapacity();

    public boolean contains(K key);

    public void clear();
}
