package diccionario;

import comparadores.Comparador;
import comparadores.ComparadorGenerico;

import java.util.List;

public abstract class Diccionario<K,V> {

    protected int cantidadElementos;
    protected Comparador<K> comparador;

    public Diccionario(Comparador<K> comparador) {
        this.comparador = comparador;
    }

    public Diccionario(){
        this(new ComparadorGenerico());
    }


    public abstract void insertar(K key, V value);

    public abstract V obtener(K key);

    public abstract V eliminar(K key);

    public abstract boolean contieneLlave(K key);

    public boolean estaVacio(){
        return cantidadElementos == 0;
    }

    public int getCantidadElementos() {
        return cantidadElementos;
    }

    public abstract List<K> getLlaves();

    public abstract List<V> getValores();

    public abstract List<Par<K,V>> getEntradas();
}