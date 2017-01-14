package test.java;

import main.java.ConcurrentHashTable;
import org.junit.Test;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import static org.junit.Assert.assertEquals;

/**
 * Created by xiaochen on 1/13/17.
 */
public class ConcurrentHashTablePerformanceTest {
    @Test
    public void testVsHashMapOnWrite() {
        int numTestWrites = 2000000;
        ConcurrentHashTable<Integer, Integer> c = new ConcurrentHashTable<>();
        Map<Integer, Integer> m = new HashMap<>();

        Map<Integer, Integer> toWrite = new HashMap<>();
        for (int i = 0; i < numTestWrites; i++) {
            toWrite.put(i, ThreadLocalRandom.current().nextInt());
        }

        long startTime = System.nanoTime();
        for (int i = 0; i < numTestWrites; i++) { c.put(i, toWrite.get(i)); }
        long endTime = System.nanoTime();

        assertEquals(numTestWrites, c.size());
        System.out.println(numTestWrites + " writes: ConcurrentHashTable takes " + (endTime - startTime) / 1000000 + " millisecs");


        startTime = System.nanoTime();
        for (int i = 0; i < numTestWrites; i++) { m.put(i, toWrite.get(i)); }
        endTime = System.nanoTime();

        System.out.println(numTestWrites + " writes: java.util.HashMap takes " + (endTime - startTime) / 1000000 + " millisecs");
    }

    @Test
    public void testVsHashTableOnRead() {
        int numTestWrites = 2000000;
        ConcurrentHashTable<Integer, Integer> c = new ConcurrentHashTable<>();
        Map<Integer, Integer> m = new HashMap<>();

        Map<Integer, Integer> toWrite = new HashMap<>();
        for (int i = 0; i < numTestWrites; i++) {
            int val = ThreadLocalRandom.current().nextInt();
            c.put(i, val);
            m.put(i, val);
        }

        long startTime = System.nanoTime();
        for (int i = 0; i < numTestWrites; i++) { c.get(i); }
        long endTime = System.nanoTime();

        assertEquals(numTestWrites, c.size());
        System.out.println(numTestWrites + " reads: ConcurrentHashTable takes " + (endTime - startTime) / 1000000 + " millisecs");


        startTime = System.nanoTime();
        for (int i = 0; i < numTestWrites; i++) { m.get(i); }
        endTime = System.nanoTime();

        System.out.println(numTestWrites + " reads: java.util.HashMap takes " + (endTime - startTime) / 1000000 + " millisecs");
    }

}
