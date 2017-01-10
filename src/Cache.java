import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Created by xiaochen on 12/31/16.
 */
public class Cache<K, V> {

    class Node<K, V> {
        K key;
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

    public Cache() {
        this(DEFAULT_CAPACITY);
    }

    public Cache(int initSize) {
        capacity = initSize;
        store = new Node[capacity];
        locks = new ReadWriteLock[capacity];

        for (int i = 0; i < capacity; i++) {
            locks[i] = new ReentrantReadWriteLock();
        }
    }

    public V get(K key) {
        int h;
        synchronized (this) {
            h = hash(key);
            locks[h].readLock().lock();
        }
        try {
            Node n = store[hash(key)];
            while (n != null && !key.equals(n.key)) {
                n = n.next;
            }
            return n == null ? null : (V) n.val;
        } finally {
            locks[h].readLock().unlock();
        }

    }

    public boolean set(K key, V val) {
        return set(key, val, false);
    }

    private boolean set(K key, V val, boolean isResizing) {

        boolean success = true; int h;

        synchronized (this) {
            if (!isResizing) checkAndResize();
            h = hash(key);
            locks[h].writeLock().lock();
        }

        try {
            Node n = store[h];
            if (n == null) {
                store[h] = new Node(key, val);
            }
            else {
                while (!key.equals(n.key) && n.next != null) {
                    n = n.next;
                }
                if (key.equals(n.key)) {
                    n.val = val;
                    success = false;
                }
                n.next = new Node(key, val);
            }
            return success;
        } finally {
            locks[h].writeLock().unlock();
        }
    }

    public int getSize() {
        return size;
    }

    public int getCapacity() {
        return capacity;
    }

    private void checkAndResize() {
        if ((++size) * 1.0 / capacity >= loadFactor ) { resize(); }
    }

    private int hash(K key) {
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
                    set((K) n.key, (V) n.val, true);
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

