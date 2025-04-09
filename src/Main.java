import java.util.NoSuchElementException;
import java.util.concurrent.locks.ReentrantLock;

class myHashMap {
    public static class MyHashMap<K, V> {
        private static final int DEFAULT_CAPACITY = 16;
        private static final float LOAD_FACTOR = 0.75f;
        private Entry<K, V>[] buckets;
        private int count;
        private int capacity;

        private static class Entry<K, V> {
            final K key;
            V value;
            Entry<K, V> next;

            public Entry(K key, V value) {
                this.key = key;
                this.value = value;
                this.next = null;
            }
        }

        @SuppressWarnings("unchecked")
        public MyHashMap(){
            this.capacity = DEFAULT_CAPACITY;
            this.buckets = new Entry[this.capacity];
        }

        public void put(K key, V value){
            if (key == null) {
                throw new NullPointerException("Key cannot be null");
            }

            if(count >= capacity*LOAD_FACTOR){
                resize();
            }

            int index = getBucketIndex(key);
            Entry<K, V> entry = new Entry<>(key, value);

            if(buckets[index] == null){
                buckets[index] = entry;
            } else{
                Entry<K, V> current = buckets[index];
                while (current != null){
                    if(current.key.equals(key)){
                        current.value = value;
                        return;
                    }
                    if(current.next == null){
                        current.next = entry;
                        break;
                    }
                    current = current.next;
                }
            }
            count++;
        }

        private void resize(){
            int newCapacity = capacity *= 2;
            @SuppressWarnings("unchecked")
            Entry<K, V>[] newBuckets = (Entry<K, V>[]) new Entry[newCapacity];
            for (Entry<K, V> entry : buckets) {
                Entry<K, V> current = entry;
                while (current != null) {
                    Entry<K, V> next = current.next;
                    int newIndex = getBucketIndex(current.key);

                    current.next = newBuckets[newIndex];
                    newBuckets[newIndex] = current;

                    current = next;
                }
            }
            buckets = newBuckets;
            capacity = newCapacity;
        }

        public V get(K key){
            if (key == null) {
                throw new NullPointerException("Key cannot be null");
            }
            int index = getBucketIndex(key);
            Entry<K, V> current = buckets[index];

            while (current != null){
                if(current.key.equals(key)){
                    return current.value;
                }
                current = current.next;
            }
            throw new NoSuchElementException("Key '" + key + "' not found");

        }

        public boolean remove(K key){
            if (key == null) {
                throw new NullPointerException("Key cannot be null");
            }
            int index = getBucketIndex(key);
            Entry<K, V> current = buckets[index];
            Entry<K, V> previous = null;
            while (current != null){
                if(current.key.equals(key)){
                    if(previous == null){
                        buckets[index] = current.next;
                    } else {
                        previous.next = current.next;
                    }
                    current.value = null;
                    count--;
                    return true;
                }
                previous = current;
                current = current.next;
            }
            return false;
        }

        private int getBucketIndex(K key){
            int hash = key == null ? 0 : key.hashCode();
            return (hash & 0x7FFFFFFF) % capacity;
        }

        public static class ThreadSafeHashMap<K, V> {
            private final MyHashMap<K, V> map = new MyHashMap<>();
            private final ReentrantLock lock = new ReentrantLock();

            public void put(K key, V value) {
                lock.lock();
                try {
                    map.put(key, value);
                } finally {
                    lock.unlock();
                }
            }

            public V get(K key) {
                lock.lock();
                try {
                    return map.get(key);
                } finally {
                    lock.unlock();
                }
            }

            public boolean remove(K key) {
                lock.lock();
                try {
                    return map.remove(key);
                } finally {
                    lock.unlock();
                }
            }
        }
    }

    public static void main(String[] args) {
        // 1. Создание экземпляра MyHashMap
        MyHashMap<String, Integer> map = new MyHashMap<>();

        // 2. Добавление элементов
        map.put("one", 1);
        map.put("two", 2);
        map.put("three", 3);

        // 3. Получение элементов
        System.out.println("Get 'one': " + map.get("one"));
        System.out.println("Get 'two': " + map.get("two"));

        // 4. Обновление значения
        map.put("two", 22);
        System.out.println("Updated 'two': " + map.get("two"));

        // 5. Проверка удаления
        System.out.println("\nBefore remove:");
        System.out.println("Contains 'three': " + (map.get("three") != null));

        map.remove("three");
        System.out.println("\nAfter remove:");
        try {
            System.out.println("Contains 'three': " + map.get("three"));
        } catch (NoSuchElementException e) {
            System.out.println("'three' not found (as expected)");
        }

        // 6. Обработка несуществующего ключа
        try {
            System.out.println("Get 'five': " + map.get("five"));
        } catch (NoSuchElementException e) {
            System.out.println("Correctly caught exception for missing key: " + e.getMessage());
        }

        // 7. Демонстрация работы с null
        try {
            map.put(null, 0);
        } catch (NullPointerException e) {
            System.out.println("\nCorrectly rejected null key: " + e.getMessage());
        }

        // 8. Проверка коллизий
        System.out.println("\nTesting collisions:");
        MyHashMap<Integer, String> collisionMap = new MyHashMap<>();

        collisionMap.put(1, "Apple");
        collisionMap.put(17, "Orange");

        System.out.println("Key 1: " + collisionMap.get(1));
        System.out.println("Key 17: " + collisionMap.get(17));
        // Создание потокобезопасного экземляра HashMap
        MyHashMap.ThreadSafeHashMap<String, Integer> map1 = new MyHashMap.ThreadSafeHashMap<>();
        map1.put("one", 1);
        map1.put("two", 2);
        System.out.println("Get 'one': " + map1.get("one"));
        System.out.println("Get 'two': " + map1.get("two"));

    }
}
