package vistas;

import trafico_datos.Servidor;

import javax.swing.*;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

public class Eventos extends JPanel implements PropertyChangeListener {

    private Servidor servidor;
    private JTextArea txta_eventos;

    public Eventos() {
        init();
    }

    private void init() {
        /*setLayout(new BorderLayout());

        servidor = new Servidor();
        servidor.addObserver(this);

        //se crea el hilo y se pone en ejecucíon para que el servidor siempre esté a la escucha
        Thread hilo = new Thread(servidor);
        hilo.start();

        txta_eventos = new JTextArea();
        txta_eventos.setEditable(false);
        txta_eventos.setBackground(Color.DARK_GRAY);
        add(txta_eventos);*/
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        txta_eventos.append(evt.getNewValue().toString());
        txta_eventos.append("\n");
    }
}
