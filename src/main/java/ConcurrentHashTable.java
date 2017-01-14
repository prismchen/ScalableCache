package main.java;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Created by xiaochen on 12/31/16.
 */
public class ConcurrentHashTable<K, V> implements Map<K, V> {

    class Node<K, V> {
        final K key;
        V val;
        Node next;

        public Node(K key, V val) {
            this.key = key;
            this.val = val;
        }
    }

    private static final int DEFAULT_CAPACITY = 16;
    private int capacity;
    private int size = 0;
    private double loadFactor = 0.75;
    private Node[] store;
    private ReadWriteLock[] locks;

    public ConcurrentHashTable() {
        this(DEFAULT_CAPACITY);
    }

    public ConcurrentHashTable(int initSize) {
        capacity = initSize;
        store = new Node[capacity];
        locks = new ReadWriteLock[capacity];

        for (int i = 0; i < capacity; i++) {
            locks[i] = new ReentrantReadWriteLock();
        }
    }

    public V get(Object key) {
        int h; Node n;
        synchronized (this) {
            h = hash(key);
            locks[h].readLock().lock();
        }
        try {
            n = store[hash(key)];
            while (n != null && !key.equals(n.key)) { n = n.next; }
            return n == null ? null : (V) n.val;
        } finally {
            locks[h].readLock().unlock();
        }

    }

    public V put(K key, V value) {
        return put(key, value, false);

    }

    private V put(K key, V val, boolean isResizing) {
        int h; Node n;
        synchronized (this) {
            if (!isResizing) checkAndResize();
            h = hash(key);
            locks[h].writeLock().lock();
        }

        try {
            if ((n = store[h]) == null) {
                store[h] = new Node(key, val);
            }
            else {
                while (!key.equals(n.key) && n.next != null) { n = n.next; }
                if (key.equals(n.key)) {
                    n.val = val;
                }
                n.next = new Node(key, val);
            }
            return val;
        } finally {
            locks[h].writeLock().unlock();
        }
    }

    public V remove(Object key) {
        int h; Node n, prev = null;
        synchronized (this) {
            h = hash(key);
            locks[h].writeLock().lock();
        }

        try {
            if ((n = store[h]) == null) {
                return null;
            }
            else {
                while (!key.equals(n.key) && n.next != null) {
                    prev = n;
                    n = n.next;
                }
                if (key.equals(n.key)) {
                    if (n == store[h]) {
                        store[h] = null;
                    }
                    else {
                        prev.next = n.next;
                    }
                    n.next = null;
                    return (V) n.val;
                }
                return null;
            }
        } finally {
            locks[h].writeLock().unlock();
        }
    }

    public boolean isEmpty() { return size == 0; }

    @Override
    public boolean containsKey(Object key) {
        return false;
    }

    @Override
    public boolean containsValue(Object value) {
        return false;
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {

    }

    @Override
    public void clear() {

    }

    @Override
    public Set<K> keySet() {
        return null;
    }

    @Override
    public Collection<V> values() {
        return null;
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        return null;
    }

    public int size() {
        return size;
    }

    public int capacity() {
        return capacity;
    }

    private void checkAndResize() {
        if ((++size) * 1.0 / capacity >= loadFactor ) { resize(); }
    }

    private int hash(Object key) {
        int hash = key.hashCode();
        return hash < 0 ? (hash + Integer.MAX_VALUE) % capacity : hash % capacity;
    }

    private void resize() {

        Node[] oldStore = store;
        ReadWriteLock[] oldLocks = locks;

        for (ReadWriteLock l : oldLocks) {
            l.writeLock().lock();
        }

        try {
            capacity *= 2;
            store = new Node[capacity];
            locks = new ReadWriteLock[capacity];

            for (int i = 0; i < capacity; i++) {
                locks[i] = new ReentrantReadWriteLock();
            }

            for (Node n : oldStore) {
                if (n == null) { continue; }
                while (n != null) {
                    put((K) n.key, (V) n.val, true);
                    n = n.next;
                }
            }
            oldStore = null;

        } finally {
            for (ReadWriteLock l : oldLocks) {
                l.writeLock().unlock();
                oldLocks = null;
            }
        }

    }
}

