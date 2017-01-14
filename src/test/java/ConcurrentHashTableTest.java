package test.java;

import main.java.ConcurrentHashTable;
import org.junit.Test;
import java.util.*;
import java.util.concurrent.*;

import static org.junit.Assert.*;

/**
 * Created by xiaochen on 12/31/16.
 */
public class ConcurrentHashTableTest {
    @Test
    public void basicWriteReadTest() {
        ConcurrentHashTable<Integer, String> c1 = new ConcurrentHashTable<>();
        c1.put(0, "Hello");
        c1.put(1, "World");
        assertTrue(c1.get(0).equals("Hello"));
        assertTrue(c1.get(1).equals("World"));
    }

    @Test
    public void largeVolWriteReadTest() {
        int numTestWrites = 2000000;
        ConcurrentHashTable<Integer, Integer> c2 = new ConcurrentHashTable<>();
        Map<Integer, Integer> dict = new HashMap();

        for (int i = 0; i < numTestWrites; i++) {
            int randVal = ThreadLocalRandom.current().nextInt();
            dict.put(i, randVal);
            c2.put(i, randVal);
        }

        for (int i = 0; i < numTestWrites; i++) {
            assertTrue(dict.get(i).equals(c2.get(i)));
        }

        assertEquals(numTestWrites, c2.size());
    }

    @Test
    public void nullTest() {
        ConcurrentHashTable<Integer, String> c3 = new ConcurrentHashTable<>();
        assertNull(c3.get(ThreadLocalRandom.current().nextInt()));
    }

    @Test
    public void multiThreadTest() throws InterruptedException {
        int numTestWrites = 200000; // number of test writes
        int numThreadsFirstRound = 10;
        int numThreadsSecondRound = 20;
        ExecutorService es = Executors.newFixedThreadPool(numThreadsFirstRound);

        ConcurrentHashTable<Integer, Integer> c4 = new ConcurrentHashTable<>();
        Map<Integer, Integer> dict = new HashMap();
        Map<Integer, Integer>[] parts = new Map[numThreadsSecondRound];

        for (int i = 0; i < numThreadsSecondRound; i++) { parts[i] = new HashMap<>(); }

        for (int i = 0; i < numTestWrites; i++) { // filling parts
            int val = ThreadLocalRandom.current().nextInt(0, Integer.MAX_VALUE);
            dict.put(i, val);
            parts[i % numThreadsSecondRound].put(i, val);
        }

        for (int i = 0; i < numThreadsFirstRound; i++) { es.execute(new HashTablePutter(parts[i], c4)); }

        es.shutdown();
        es.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        es = Executors.newFixedThreadPool(numThreadsSecondRound);

        Map<Integer, Integer>[] getResults = new Map[numThreadsFirstRound];
        for (int i = 0; i < numThreadsSecondRound; i++) {
            if (i <  numThreadsFirstRound) {
                es.execute(new HashTableGetter(getResults[i] = new HashMap<>(), c4, parts[i].keySet()));
            }
            else { es.execute(new HashTablePutter(parts[i], c4)); }
        }


        es.shutdown();
        es.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);

        assertEquals(numTestWrites, c4.size());
        assertTrue(numTestWrites < c4.capacity());

        for (Map<Integer, Integer> container : getResults) {
            for (int key : container.keySet()) { assertEquals(dict.get(key), container.get(key)); }
        }

        for (int key : dict.keySet()) { assertEquals(dict.get(key), c4.get(key)); }
    }

    @Test
    public void removeTest() {
        ConcurrentHashTable<Integer, String> c5 = new ConcurrentHashTable<>();

        assertNull(c5.remove(0));

        c5.put(1, "One");
        assertEquals("One", c5.get(1));

        assertTrue(c5.remove(1).equals("One"));
        assertNull(c5.get(1));
        assertNull(c5.remove(1));
    }

    @Test
    public void largeVolRemoveTest() {
        int numTestWrites = 2000000;
        ConcurrentHashTable<Integer, Integer> c6 = new ConcurrentHashTable<>();

        Map<Integer, Integer> dict = new HashMap();

        for (int i = 0; i < numTestWrites; i++) {
            int randVal = ThreadLocalRandom.current().nextInt();
            dict.put(i, randVal);
            c6.put(i, randVal);
            assertEquals(randVal, (int) c6.get(i));
        }

        for (int i = 0; i < numTestWrites; i++) {
            assertEquals(c6.get(i), c6.remove(i));
            assertNull(c6.get(i));
        }

        assertEquals(numTestWrites, c6.size());
    }
}