package trafico_datos;


import diccionario.DiccionarioTablaHash;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Servidor {

    private ServerSocket serverSocket;
    private DiccionarioTablaHash<String, Partida> partidas;
    private List<GestorCliente> clientesConectados;

    public Servidor() throws Exception {
        this.serverSocket = new ServerSocket(12345);
        this.partidas = new DiccionarioTablaHash<>();
        clientesConectados = new ArrayList<>();
    }

    public void iniciarServidor() throws Exception {
        System.out.println("Server en ejecucion");
        while (true) {
            //entrada de datos
            Socket socket = serverSocket.accept();

            GestorCliente nuevoCliente = new GestorCliente(socket, partidas, clientesConectados);
            Thread hilo = new Thread(nuevoCliente);
            clientesConectados.add(nuevoCliente);
            hilo.start();
        }
    }
}
