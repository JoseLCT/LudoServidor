package juego;

public class Ficha {

    private int id;
    private String color;
    private int posicionActual;
    private boolean enCasa;
    private boolean enRecorridoFinal;
    private boolean recorridoAcabado;

    public Ficha(int id, String color){
        this.id = id;
        this.color = color;
        posicionActual = -1;
        enCasa = true;
        enRecorridoFinal = false;
    }

    public int getId() {
        return id;
    }

    public String getColor() {
        return color;
    }

    public int getPosicionActual() {
        return posicionActual;
    }

    public void setPosicionActual(int posicionActual) {
        this.posicionActual = posicionActual;
    }

    public boolean enCasa() {
        return enCasa;
    }

    public void setEnCasa(boolean enCasa) {
        this.enCasa = enCasa;
    }

    public boolean enRecorridoFinal() {
        return enRecorridoFinal;
    }

    public void setEnRecorridoFinal(boolean enRecorridoFinal) {
        this.enRecorridoFinal = enRecorridoFinal;
    }

    public boolean recorridoAcabado() {
        return recorridoAcabado;
    }

    public void setRecorridoAcabado(boolean recorridoAcabado) {
        this.recorridoAcabado = recorridoAcabado;
    }
}
