package juego;

import java.awt.*;

public class Casilla {

    private int numeroCasilla;
    private Ficha ficha;
    private Point posicion;

    public Casilla(){

    }

    public boolean estaVacia(){
        return ficha == null;
    }

    public Ficha getFicha() {
        return ficha;
    }

    public void setFicha(Ficha ficha) {
        this.ficha = ficha;
    }

    public int getNumeroCasilla() {
        return numeroCasilla;
    }

    public void setNumeroCasilla(int numeroCasilla) {
        this.numeroCasilla = numeroCasilla;
    }

    public Point getPosicion() {
        return posicion;
    }

    public void setPosicion(Point posicion) {
        this.posicion = posicion;
    }
}
