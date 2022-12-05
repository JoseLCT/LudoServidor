package comparadores;

public interface Comparador<K> {

    boolean esIgual(K key1, K key2);

    int getHashCode(K key);
}