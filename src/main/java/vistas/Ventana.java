package vistas;

import javax.swing.*;
import java.awt.*;

public class Ventana extends JFrame {

    private Eventos panel_eventos;

    public Ventana() {
        init();
    }

    private void init(){
        setLayout(new BorderLayout());
        setSize(500,700);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        panel_eventos = new Eventos();
        add(panel_eventos);
    }
}
