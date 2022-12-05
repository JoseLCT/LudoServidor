package diccionario;

public class Par<K, V> {

    private K key;
    private V value;

    Par(K key, V value) {
        this.key = key;
        this.value = value;
    }

    public K getKey() {
        return key;
    }

    public V getValue() {
        return value;
    }
}