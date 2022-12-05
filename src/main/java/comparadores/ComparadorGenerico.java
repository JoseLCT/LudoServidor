package comparadores;

public class ComparadorGenerico<K> implements Comparador<K> {

    @Override
    public boolean esIgual(K key1, K key2) {
        return key1.equals(key2);
    }

    @Override
    public int getHashCode(K key) {
        return key.hashCode();
    }
}
