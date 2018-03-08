package com.segment.proxy.cache;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by umehta on 3/4/18.
 */
public class CacheRecordTest {
    @Test
    public void getValue() throws Exception {
        String value = "TestVal";
        long lastAccessed = System.currentTimeMillis();
        CacheRecord<String> record = new CacheRecord<>(value, lastAccessed);
        assertEquals(record.getValue(), value);

        Integer value1 = 1;
        CacheRecord<Integer> record1 = new CacheRecord<>(value1, lastAccessed);
        assertEquals(record1.getValue(), value1);
    }

    @Test
    public void getLastAccessed() throws Exception {
        String value = "TestVal";
        long lastAccessed = System.currentTimeMillis();
        CacheRecord<String> record = new CacheRecord<>(value, lastAccessed);
        assertEquals(record.getLastAccessed(), lastAccessed);
    }

}