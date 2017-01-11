package test.java;

import main.java.HashTable;
import org.junit.Test;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

import static org.junit.Assert.*;

/**
 * Created by xiaochen on 12/31/16.
 */
public class CacheTest {
    @Test
    public void cacheTest1() {
        HashTable<Integer, String> c1 = new HashTable<>();
        c1.set(0, "Hello");
        c1.set(1, "World");
        assertTrue(c1.get(0).equals("Hello"));
        assertTrue(c1.get(1).equals("World"));
    }

    @Test
    public void cacheTest2() {
        int testNum = 2000000;
        HashTable<Integer, Integer> c2 = new HashTable<>();
        Map<Integer, Integer> dict = new java.util.HashMap();

        for (int i = 0; i < testNum; i++) {
            int randVal = ThreadLocalRandom.current().nextInt();
            dict.put(i, randVal);
            c2.set(i, randVal);
        }

        for (int i = 0; i < testNum; i++) {
            assertTrue(dict.get(i).equals(c2.get(i)));
        }

        assertEquals(testNum, c2.getSize());
    }

    @Test
    public void cacheTestNull() {
        HashTable<Integer, String> c3 = new HashTable<>();
        assertNull(c3.get(ThreadLocalRandom.current().nextInt()));
    }

    @Test
    public void cacheTestMultiThread() throws InterruptedException {
        int testNum = 200000; // number of test writes
        int numThreads = 20; Thread[] tPool = new Thread[numThreads]; // what the fuck is that

        HashTable<Integer, Integer> c4 = new HashTable<>();
        Map<Integer, Integer> dict = new java.util.HashMap();
        List<Map<Integer, Integer>> parts = new ArrayList<>();

        for (int i = 0; i < numThreads; i++) {
            parts.add(new java.util.HashMap());
        }

        int val;
        for (int i = 0; i < testNum; i++) { // filling parts
            val = ThreadLocalRandom.current().nextInt(0, Integer.MAX_VALUE);
            dict.put(i, val);
            parts.get(i % 10).put(i, val);
        }

        for (int i = 0; i < numThreads; i++) {
            tPool[i] = new Thread(new cachePutter(parts.get(i), c4));
        }

        for (int i = 0; i < numThreads / 2; i++) {
            tPool[i].start();
        }
        for (int i = 0; i < numThreads / 2; i++) {
            tPool[i].join();
        }

        List<Map<Integer, Integer>> containers = new ArrayList<>();
        for (int i = 0; i < numThreads / 2; i++) {
            containers.add(new java.util.HashMap());
            tPool[i] = new Thread(new cacheGetter(containers.get(i), c4, parts.get(i).keySet()));
        }

        for (int i = 0; i < numThreads; i++) {
            tPool[i].start();
        }

        for (int i = 0; i < numThreads; i++) {
            tPool[i].join();
            System.out.println("Thread " + i + " finished");
        }

        System.out.println("Cap: " + c4.getCapacity());

        for (Map<Integer, Integer> container : containers) {
            for (int key : container.keySet()) {
                assertEquals(dict.get(key), container.get(key));
            }
        }

        for (int key : dict.keySet()) {
            assertEquals(dict.get(key), c4.get(key));
        }
    }

    class cachePutter implements Runnable {

        Map<Integer, Integer> content;
        HashTable<Integer, Integer> cache;

        public cachePutter(Map<Integer, Integer> content, HashTable cache) {
            this.content = content;
            this.cache = cache;
        }

        @Override
        public void run() {
            List<Integer> keys = new ArrayList<>(content.keySet());
            long seed = System.nanoTime();
            Collections.shuffle(keys, new Random(seed));

            for (int k : keys) {
                cache.set(k, content.get(k));
            }
        }
    }

    class cacheGetter implements Runnable {
            Map<Integer, Integer> container;
            HashTable<Integer, Integer> cache;
            Collection<Integer> queries;

            public cacheGetter(Map<Integer, Integer> container, HashTable<Integer, Integer> cache, Collection<Integer> queries) {
                this.container = container;
                this.cache = cache;
                this.queries = queries;
            }

        @Override
        public void run() {
            for (int k : queries) {
                if (cache.get(k) == null) {
                    container.put(k, -1);
                }
                else {
                    container.put(k, cache.get(k));
                }
            }
        }
    }

}