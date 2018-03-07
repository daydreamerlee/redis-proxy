package com.segment.proxy.cache;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by umehta on 3/5/18.
 */
public class LRUCacheImplTest {
    @Test
    public void get() throws Exception {
        int capacity = 3;
        int ttl = -1;
        long now = System.currentTimeMillis();
        Cache<String, CacheRecord<String>> cache = new LRUCacheImpl<>(capacity, ttl);
        cache.set("foo", new CacheRecord<>("fooval", now));
        cache.set("abc", new CacheRecord<>("abcval", now));
        cache.set("bar", new CacheRecord<>("barval", now));
        cache.set("xyz", new CacheRecord<>("xyzval", now));

        assertEquals("Cache should return stored entry", "abcval", cache.get("abc").getValue());
        assertNull("Key should be removed as cache is at capacity", cache.get("foo"));
        assertNull("Key not present in cache", cache.get("invalid"));
        assertNull("Check for Null key", cache.get(null));

        //Check if right key is evicted on reaching cache capacity. Order of operations is important
        Cache<String, CacheRecord<String>> cache1 = new LRUCacheImpl<>(capacity, ttl);
        cache1.set("foo", new CacheRecord<>("fooval", now));
        cache1.set("abc", new CacheRecord<>("abcval", now));
        cache1.get("foo");
        cache1.set("bar", new CacheRecord<>("barval", now));
        cache1.get("foo");
        cache1.set("xyz", new CacheRecord<>("xyzval", now));
        cache1.set("pqr", new CacheRecord<>("pqrval", now));

        assertNull("Correct key should be evicted for get requests following set", cache1.get("abc")); //Cache should have foo, xyz, pqr
        assertNull("Correct key should be evicted for get requests following set", cache1.get("bar"));

        //Check for cache eviction based on ttl
        int ttl2 = 1;
        Cache<String, CacheRecord<String>> cache2 = new LRUCacheImpl<>(capacity, ttl2);
        cache2.set("foo", new CacheRecord<>("fooval", now));
        Thread.sleep(1100);
        assertFalse("Key should be evicted", cache2.contains("foo"));
        assertEquals(cache2.getSize(), 0);


    }

    @Test
    public void set() throws Exception {
        int capacity = 3;
        int ttl = -1;
        long now = System.currentTimeMillis();
        Cache<String, CacheRecord<String>> cache = new LRUCacheImpl<>(capacity, ttl);
        cache.set("foo", new CacheRecord<>("fooval", now));
        assertTrue("Check should contain key after set", cache.contains("foo"));

        cache.set("foo", new CacheRecord<>("fooval1", now));
        cache.set("bar", new CacheRecord<>("barval", now));

        assertEquals("set on same key twice should return latest value", cache.get("foo").getValue(), "fooval1");
        assertEquals("Check cache size after set operation", cache.getSize(), 2);
    }

    @Test
    public void getSize() throws Exception {
        int capacity = 3;
        int ttl = -1;
        long now = System.currentTimeMillis();
        Cache<String, CacheRecord<String>> cache = new LRUCacheImpl<>(capacity, ttl);

        cache.set("foo", new CacheRecord<>("fooval", now));
        assertEquals("Check cache size", cache.getSize(), 1);

        cache.set("foo", new CacheRecord<>("fooval1", now));
        cache.set("bar", new CacheRecord<>("barval", now));

        assertEquals("Check cache size", cache.getSize(), 2);

        cache.set("abc", new CacheRecord<>("abcval", now));
        cache.set("xyz", new CacheRecord<>("xyzval", now));

        assertEquals("Check cache size", cache.getSize(), 3);
    }

    @Test
    public void contains() throws Exception {
        int capacity = 3;
        int ttl = -1;
        long now = System.currentTimeMillis();
        Cache<String, CacheRecord<String>> cache = new LRUCacheImpl<>(capacity, ttl);

        cache.set("foo", new CacheRecord<>("fooval", now));
        assertTrue("Check should contain key", cache.contains("foo"));
        assertFalse("Cache should return false for invalid key", cache.contains("invalid"));

        cache.set("abc", new CacheRecord<>("abcval", now));
        cache.set("bar", new CacheRecord<>("barval", now));
        cache.set("xyz", new CacheRecord<>("xyzval", now));
        assertFalse("Cache should return false for key evicted on reaching capacity", cache.contains("foo"));
    }

    @Test
    public void clear() throws Exception {
        int capacity = 3;
        int ttl = -1;
        long now = System.currentTimeMillis();
        Cache<String, CacheRecord<String>> cache = new LRUCacheImpl<>(capacity, ttl);

        cache.set("foo", new CacheRecord<>("fooval", now));
        cache.clear();
        assertEquals("Cache size should be 0 after clear", cache.getSize(), 0);
    }

    @Test
    public void evictOldEntries() throws Exception {
        int capacity = 3;
        int ttl = 2;
        long now = System.currentTimeMillis();
        LRUCacheImpl<String, String> cache = new LRUCacheImpl<>(capacity, ttl);
        cache.set("abc", new CacheRecord<>("abcval", now - 5000));
        cache.set("bar", new CacheRecord<>("barval", now - 1000));
        cache.set("xyz", new CacheRecord<>("xyzval", now));

        //Entries older than ttl secs should be deleted
        cache.evictOldEntries();
        assertEquals("Entry older than ttl secs should be evicted", cache.getSize(), 2);
        assertTrue("New entries should not be evicted", cache.contains("bar"));
        assertFalse("Old entries should not evicted", cache.contains("abc"));
    }

}