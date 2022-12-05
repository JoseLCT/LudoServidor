package diccionario;

import comparadores.Comparador;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DiccionarioTablaHash<K, V> extends Diccionario<K, V> {

    private DiccionarioSecuencia<K,V>[] elementos;
    private float factorCarga;
    private int limiteElementos;

    public DiccionarioTablaHash(Comparador<K> comparador) {
        super(comparador);
        init();
    }

    public DiccionarioTablaHash() {
        super();
        init();
    }

    @Override
    public void insertar(K key, V value) {
        int indice = getIndice(key);
        if(elementos[indice] == null){
            elementos[indice] = new DiccionarioSecuencia<>(comparador);
        }
        int antiguaCantidadElementos = elementos[indice].getCantidadElementos();
        elementos[indice].insertar(key, value);
        int actualCantidadElementos = elementos[indice].getCantidadElementos();
        if(antiguaCantidadElementos + 1 == actualCantidadElementos){
            cantidadElementos++;
        }
        if(cantidadElementos > limiteElementos){
            rehash();
        }
    }

    private void rehash() {
        int newSize = getNewSize();
        System.out.println(newSize);
        DiccionarioSecuencia<K,V>[] nuevo = new DiccionarioSecuencia[newSize];
        for (DiccionarioSecuencia<K, V> elemento : elementos) {
            if(elemento == null){
                continue;
            }
            for (Par<K,V> entry : elemento.getEntradas()) {

                int indice = getIndice(entry.getKey(), newSize);

                if (nuevo[indice] == null) {
                    nuevo[indice] = new DiccionarioSecuencia<>(comparador);
                }

                nuevo[indice].insertar(entry.getKey(), entry.getValue());
            }
        }
        HashMap<K,V> map = new HashMap<>();

        limiteElementos = (int) (newSize * factorCarga);
        elementos = nuevo;
    }

    private int getNewSize() {
        return elementos.length * 2 + 1;
    }

    private int getIndice(K key) {
        return getIndice(key, elementos.length);
    }

    private int getIndice(K key, int n) {

        return (this.comparador.getHashCode(key) & 0x7FFFFFFF ) % n;
    }

    @Override
    public V obtener(K key) {
        int indice = getIndice(key);
        if(elementos[indice] == null){
            return null;
        }

        return elementos[indice].obtener(key);
    }

    @Override
    public V eliminar(K key) {
        int posicion = getIndice(key);

        if (elementos[posicion] == null) {
            return null;
        }
        int antiguaCantidadElementos = elementos[posicion].getCantidadElementos();
        V valor = elementos[posicion].eliminar(key);
        int nuevaCantidadElementos = elementos[posicion].getCantidadElementos();
        if (antiguaCantidadElementos  != nuevaCantidadElementos ) {
            cantidadElementos--;
            return valor;
        }
        return null;
    }

    @Override
    public boolean contieneLlave(K key) {
        int indice = getIndice(key);
        if(elementos[indice] == null){
            return false;
        }

        return elementos[indice].contieneLlave(key);
    }

    @Override
    public List<K> getLlaves() {
        List<K> llaves = new ArrayList<>();
        for (DiccionarioSecuencia<K, V> diccionarioSecuencia : elementos) {
            if(diccionarioSecuencia != null){
                llaves.addAll(diccionarioSecuencia.getLlaves());
            }
        }

        return llaves;
    }

    @Override
    public List<V> getValores() {
        List<V> valores = new ArrayList<>();
        for (DiccionarioSecuencia<K, V> diccionarioSecuencia : elementos) {
            if(diccionarioSecuencia != null){
                valores.addAll(diccionarioSecuencia.getValores());
            }
        }

        return valores;
    }

    @Override
    public List<Par<K, V>> getEntradas() {
        List<Par<K, V>> entradas = new ArrayList<>();
        for (DiccionarioSecuencia<K, V> diccionarioSecuencia : elementos) {
            if(diccionarioSecuencia != null){
                entradas.addAll(diccionarioSecuencia.getEntradas());
            }
        }

        return entradas;
    }

    private void init(){
        elementos = new DiccionarioSecuencia[11];
        factorCarga = 0.75f;
        limiteElementos = (int) (elementos.length * factorCarga);
    }

    public String toString(){
        StringBuilder sb = new StringBuilder();
        String separator = "";
        for (DiccionarioSecuencia<K, V> diccionarioSecuencia : elementos) {
            sb.append(separator);
            if(diccionarioSecuencia != null){
                sb.append(diccionarioSecuencia.toString());
            }else{
                sb.append(" X ");
            }
            separator = " - ";
        }
        return sb.toString();
    }



}
