package test.java;

import main.java.ConcurrentHashTable;
import java.util.*;

/**
 * Created by xiaochen on 1/13/17.
 */
public class HashTablePutter implements Runnable {

    Map<Integer, Integer> content;
    ConcurrentHashTable<Integer, Integer> hashTable;

    public HashTablePutter(Map<Integer, Integer> content, ConcurrentHashTable hashTable) {
        this.content = content;
        this.hashTable = hashTable;
    }

    @Override
    public void run() {
        List<Integer> keys = new ArrayList<>(content.keySet());
        long seed = System.nanoTime();
        Collections.shuffle(keys, new Random(seed));

        for (int k : keys) {
            hashTable.put(k, content.get(k));
        }
    }
}
