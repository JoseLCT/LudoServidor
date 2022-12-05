package diccionario;

class Nodo<K, V> {

    private K key;
    private V value;

    private Nodo<K, V> siguiente;
    private Nodo<K, V> anterior;

    public Nodo(K key, V value) {
        this.key = key;
        this.value = value;
    }

    public K getKey() {
        return key;
    }

    public V getValue() {
        return value;
    }

    public void setValue(V value) {
        this.value = value;
    }

    public Nodo<K, V> getSiguiente() {
        return siguiente;
    }

    public void setSiguiente(Nodo<K, V> siguiente) {
        this.siguiente = siguiente;
    }

    public Nodo<K, V> getAnterior() {
        return anterior;
    }

    public void setAnterior(Nodo<K, V> anterior) {
        this.anterior = anterior;
    }

    @Override
    public String toString() {
        return "{" +
                key.toString() +
                ", " + value.toString() +
                '}';
    }
}