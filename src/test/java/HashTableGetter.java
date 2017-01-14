package test.java;

import main.java.ConcurrentHashTable;
import java.util.*;

/**
 * Created by xiaochen on 1/13/17.
 */
public class HashTableGetter implements Runnable {
    Map<Integer, Integer> container;
    ConcurrentHashTable<Integer, Integer> hashTable;
    Collection<Integer> queries;

    public HashTableGetter(Map<Integer, Integer> container, ConcurrentHashTable<Integer, Integer> hashTable, Collection<Integer> queries) {
        this.container = container;
        this.hashTable = hashTable;
        this.queries = queries;
    }

    @Override
    public void run() {
        for (int k : queries) {
            if (hashTable.get(k) == null) {
                container.put(k, -1);
            }
            else {
                container.put(k, hashTable.get(k));
            }
        }
    }
}
