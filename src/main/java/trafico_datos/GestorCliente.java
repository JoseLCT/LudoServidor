package trafico_datos;

import diccionario.DiccionarioTablaHash;
import juego.Ficha;
import org.json.JSONObject;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.List;

// Falta verificar por que cuando termina una partida y el jugador vuelve a crear o unirse a una el servidor le devuelve
// 2 respuestas
public class GestorCliente implements Runnable {

    private Socket socket;
    private BufferedReader bufferedReader;
    public BufferedWriter bufferedWriter;
    private DiccionarioTablaHash<String, Partida> partidas;
    private List<GestorCliente> clientesConectados;

    private String color;
    private String nombre;
    private String skin;
    private DiccionarioTablaHash<Integer, Ficha> fichas;

    private PropertyChangeSupport notificador;

    public GestorCliente(Socket socket, DiccionarioTablaHash<String, Partida> partidas, List<GestorCliente> clientes) throws Exception {
        this.socket = socket;
        bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        this.partidas = partidas;
        this.clientesConectados = clientes;
        fichas = new DiccionarioTablaHash<>();
        this.notificador = new PropertyChangeSupport(this);
    }

    @Override
    public void run() {
        while (!socket.isClosed()) {
            try {
                String txtDatos = bufferedReader.readLine();
                JSONObject solicitud = new JSONObject(txtDatos);

                //se verifica el tipo y se realiza la acción correspondiente
                String tipo = solicitud.getString("tipo");

                if (tipo.equals("crear")) {
                    nombre = solicitud.getString("jugador");
                    skin = solicitud.getString("skin");

                    String partida_id = generarId();

                    JSONObject respuesta = new JSONObject();
                    respuesta.put("tipo", "respuesta_crear");
                    respuesta.put("partida_id", partida_id);

                    bufferedWriter.write(respuesta.toString());
                    bufferedWriter.newLine();
                    bufferedWriter.flush();

                    initFichas();

                    Partida nuevaPartida = new Partida();
                    nuevaPartida.setId(partida_id);
                    nuevaPartida.addJugador(this);
                    partidas.insertar(partida_id, nuevaPartida);
                    initFichas();
                } else if (tipo.equals("unirse")) {
                    JSONObject respuesta = new JSONObject();
                    String partida_id = solicitud.getString("partida_id");
                    if (partidas.contieneLlave(partida_id)) {
                        if (partidas.obtener(partida_id).sePuedeUnir()) {

                            respuesta.put("tipo", "respuesta_unirse");
                            respuesta.put("partida_id", partida_id);

                            bufferedWriter.write(respuesta.toString());
                            bufferedWriter.newLine();
                            bufferedWriter.flush();

                            nombre = solicitud.getString("jugador");
                            skin = solicitud.getString("skin");

                            partidas.obtener(partida_id).addJugador(this);
                        } else {
                            respuesta.put("tipo", "solicitud_denegada");
                            respuesta.put("mensaje", "La partida ya comenzó");
                        }
                    } else {
                        respuesta.put("tipo", "solicitud_denegada");
                        respuesta.put("mensaje", "No existe una partida con el ID " + partida_id);
                    }

                    bufferedWriter.write(respuesta.toString());
                    bufferedWriter.newLine();
                    bufferedWriter.flush();

                } else {
                    solicitud.put("color", color);
                    notificador.firePropertyChange(tipo, null, solicitud);
                }
            } catch (Exception e) {
                System.out.println("Cliente desconectado");
                cerrarConexiones();
            }
        }
    }

    public void addObserver(Partida partida) {
        notificador.addPropertyChangeListener(partida);
    }

    public void removeObserver(Partida partida) {
        notificador.removePropertyChangeListener(partida);
    }

    public void cerrarConexiones() {
        try {
            socket.close();
            bufferedReader.close();
            bufferedWriter.close();

            clientesConectados.remove(this);
        } catch (Exception e) {
            System.out.println("Conexiones cerradas");
        }
    }

    private String generarId() {
        StringBuilder id = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            int numero = (int) (Math.random() * 10);
            if (i == 3) {
                id.append("-");
            }
            id.append(numero);
        }
        if (partidas.contieneLlave(id.toString())) {
            return generarId();
        }
        return id.toString();
    }

    public void initFichas() {
        for (int i = 1; i <= 4; i++) {
            Ficha ficha = new Ficha(i, color);
            fichas.insertar(i, ficha);
        }
    }

    public void partidaFinalizada(String id) {
        partidas.eliminar(id);
    }

    public Socket getSocket() {
        return socket;
    }

    public void setSocket(Socket socket) {
        this.socket = socket;
    }

    public BufferedReader getBufferedReader() {
        return bufferedReader;
    }

    public void setBufferedReader(BufferedReader bufferedReader) {
        this.bufferedReader = bufferedReader;
    }

    public BufferedWriter getBufferedWriter() {
        return bufferedWriter;
    }

    public void setBufferedWriter(BufferedWriter bufferedWriter) {
        this.bufferedWriter = bufferedWriter;
    }

    public DiccionarioTablaHash<String, Partida> getPartidas() {
        return partidas;
    }

    public void setPartidas(DiccionarioTablaHash<String, Partida> partidas) {
        this.partidas = partidas;
    }

    public List<GestorCliente> getClientesConectados() {
        return clientesConectados;
    }

    public void setClientesConectados(List<GestorCliente> clientesConectados) {
        this.clientesConectados = clientesConectados;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getSkin() {
        return skin;
    }

    public void setSkin(String skin) {
        this.skin = skin;
    }

    public DiccionarioTablaHash<Integer, Ficha> getFichas() {
        return fichas;
    }

    public void setFichas(DiccionarioTablaHash<Integer, Ficha> fichas) {
        this.fichas = fichas;
    }
}
