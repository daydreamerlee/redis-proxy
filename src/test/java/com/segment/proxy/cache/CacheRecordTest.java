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
    public void getValue1() throws Exception {
        //Test Cache Record constructor requiring only message and not status
        String value = "TestVal";
        CacheRecord<String> record = new CacheRecord<>(value);
        assertEquals(record.getValue(), value);
        assertEquals(record.getLastAccessed(), -1);
    }

    @Test
    public void getLastAccessed() throws Exception {
        String value = "TestVal";
        long lastAccessed = System.currentTimeMillis();
        CacheRecord<String> record = new CacheRecord<>(value, lastAccessed);
        assertEquals(record.getLastAccessed(), lastAccessed);
    }

    @Test
    public void setLastAccessed() throws Exception {
        String value = "TestVal";
        long lastAccessed = System.currentTimeMillis();
        long newLastAccessed = System.currentTimeMillis() + 10;

        CacheRecord<String> record = new CacheRecord<>(value, lastAccessed);
        assertEquals(record.getLastAccessed(), lastAccessed);

        record.setLastAccessed(newLastAccessed);
        assertEquals(record.getLastAccessed(), newLastAccessed);
    }

}