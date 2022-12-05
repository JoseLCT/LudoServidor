import trafico_datos.Servidor;
import vistas.Ventana;

import java.net.ServerSocket;
import java.net.Socket;

public class AppServer {
    public static void main(String[] args) {
        /*Ventana ventana = new Ventana();
        ventana.setVisible(true);*/
        try {
            Servidor servidor = new Servidor();
            servidor.iniciarServidor();
        } catch (Exception e){
            e.printStackTrace();
        }
    }
}
