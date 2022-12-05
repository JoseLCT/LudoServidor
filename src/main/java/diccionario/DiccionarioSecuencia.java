package diccionario;

import comparadores.Comparador;

import java.util.List;

public class DiccionarioSecuencia<K, V> extends Diccionario<K, V> {

    private Nodo<K, V> primero;
    private Nodo<K, V> ultimo;

    public DiccionarioSecuencia(Comparador comparador) {
        super(comparador);
    }

    public DiccionarioSecuencia() {
        super();
    }

    @Override
    public void insertar(K key, V value) {
        Nodo<K, V> nodo = getNodo(key);

        if(nodo != null){
            nodo.setValue(value);
            return;
        }

        nodo = new Nodo<>(key, value);
        if (primero == null) {
            primero = nodo;
            ultimo = nodo;
        } else {
            ultimo.setSiguiente(nodo);
            nodo.setAnterior(ultimo);
            ultimo = nodo;
        }
        cantidadElementos++;
    }

    @Override
    public V obtener(K key) {
        Nodo<K, V> nodo = getNodo(key);
        if (nodo != null) {
            return nodo.getValue();
        }
        return null;
    }

    @Override
    public V eliminar(K key) {
        Nodo<K, V> nodo = getNodo(key);

        if(nodo == null){
            return null;
        }

        //Cuando solo hay un elemento
        if(primero == ultimo && primero == nodo) {
            primero = null;
            ultimo = null;
            cantidadElementos--;
            return nodo.getValue();
        }

        //Cuando al menos hay dos elementos

        // [X] <-> ... <->  []
        //El que quiero eliminar es el primero
        if (nodo == primero) {
            primero = primero.getSiguiente();
            primero.setAnterior(null);
            nodo.setSiguiente(null);
            cantidadElementos--;
            return nodo.getValue();
        }
        // [] <-> ... <->  [X]
        //El que quiero eliminar es el ultimo
        if (nodo == ultimo) {
            ultimo = ultimo.getAnterior();
            ultimo.setSiguiente(null);
            nodo.setAnterior(null);
            cantidadElementos--;
            return nodo.getValue();
        }

        // [] <-> .. <-> [X] <-> ..<->  []
        Nodo<K,V> anterior = nodo.getAnterior();
        Nodo<K,V> siguiente = nodo.getSiguiente();

        anterior.setSiguiente(siguiente);
        siguiente.setAnterior(anterior);

        nodo.setAnterior(null);
        nodo.setSiguiente(null);

        cantidadElementos--;
        return nodo.getValue();
    }

    @Override
    public boolean contieneLlave(K key) {
        Nodo<K, V> nodo = getNodo(key);

        return nodo != null;
    }

    @Override
    public List<K> getLlaves() {
        List<K> llaves = new java.util.ArrayList<>();
        Nodo<K, V> actual  = primero;
        while(actual != null){
            llaves.add(actual.getKey());
            actual = actual.getSiguiente();
        }

        return llaves;
    }

    @Override
    public List<V> getValores() {
        List<V> valores = new java.util.ArrayList<>();
        Nodo<K, V> actual  = primero;
        while(actual != null){
            valores.add(actual.getValue());
            actual = actual.getSiguiente();
        }

        return valores;
    }

    @Override
    public List<Par<K, V>> getEntradas() {
        List<Par<K, V>> list = new java.util.ArrayList<>();
        Nodo<K, V> actual  = primero;
        while(actual != null){
            Par<K, V> par = new Par<>(actual.getKey(), actual.getValue());
            list.add(par);
            actual = actual.getSiguiente();
        }

        return list;
    }

    private Nodo<K, V> getNodo(K key) {
        Nodo<K, V> actual  = primero;
        while(actual != null){

            if(comparador.esIgual(actual.getKey(), key)){
                return actual;
            }

            actual = actual.getSiguiente();
        }
        return null;
    }

    public String toString(){
        StringBuilder sb = new StringBuilder("[");
        Nodo<K, V> actual  = primero;
        String sepatador = "";
        while(actual != null){
            sb.append(sepatador).append(actual.toString());
            sepatador = ", ";
            actual = actual.getSiguiente();
        }
        sb.append("]");
        return sb.toString();
    }
}














