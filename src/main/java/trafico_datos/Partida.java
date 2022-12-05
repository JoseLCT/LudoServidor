package trafico_datos;

import juego.Casilla;
import juego.Ficha;
import org.json.JSONObject;

import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

//Memes

public class Partida implements PropertyChangeListener {

    private String id;
    private boolean partidaIniciada;
    private List<GestorCliente> jugadores;

    private Casilla[] recorridoGeneral;
    private Casilla[] recorridoAzul;
    private Casilla[] recorridoAmarillo;
    private Casilla[] recorridoVerde;
    private Casilla[] recorridoRojo;

    public Partida() {
        initRecorridos();
        jugadores = new ArrayList<>();
        partidaIniciada = false;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        JSONObject solicitud = new JSONObject(evt.getNewValue().toString());

        String tipoDeSolicitud = solicitud.getString("tipo");
        String color_jugador = solicitud.getString("color");

        if (tipoDeSolicitud.equals("iniciar_partida")) {

            JSONObject respuesta = new JSONObject();

            if (jugadores.size() == 1) {

                // La partida solo tiene 1 jugador dentro

                respuesta.put("tipo", "solicitud_denegada");
                respuesta.put("mensaje", "Debe haber minimo 2 jugadores en la sala");

            } else {

                partidaIniciada = true;
                respuesta.put("tipo", "partida_iniciada");
                respuesta.put("turno", "azul");

                String mensaje = "Partida iniciada";
                respuesta.put("mensaje", mensaje);

            }

            notificarJugadores(respuesta);

        } else if (tipoDeSolicitud.equals("tirar_dados")) {

            int dado = tirarDados();

            JSONObject respuesta = new JSONObject();
            respuesta.put("tipo", "respuesta_tirar_dados");
            respuesta.put("color", color_jugador);
            respuesta.put("dado", dado);

            GestorCliente jugador = getJugador(color_jugador);
            String color_nombre = getCodigoColor(color_jugador);
            String mensaje = "<html>" + color_nombre + jugador.getNombre() + "</font> sacó " + dado + "</html>";
            respuesta.put("mensaje", mensaje);

            notificarJugadores(respuesta);
            if (todasLasFichasEnCasa(color_jugador)) {
                if (dado != 1) {
                    cambioDeTurno(color_jugador);
                }
            } else if (!sePuedeMover(getJugador(color_jugador), dado)) {
                cambioDeTurno(color_jugador);
            }

        } else if (tipoDeSolicitud.equals("salir_casa")) {

            // COMPLETO
            // Si intenta salir de casa se verifica si existe una ficha en la posición de salida, si existe
            // una ficha entonces se verifica si es una ficha del mismo jugador o no, si es del mismo jugador
            // se deniega la solicitud, si no, se come la ficha del otro jugador
            int dado = solicitud.getInt("dado");
            int ficha_id = solicitud.getInt("ficha");
            String color_ficha = solicitud.getString("color");
            Ficha ficha = getFicha(color_ficha, ficha_id);

            if (dado == 1) {
                salirDeCasa(ficha);
            } else {
                JSONObject respuesta = new JSONObject();

                respuesta.put("tipo", "solicitud_denegada");
                respuesta.put("mensaje", "No puedes sacar esta ficha");
                respuesta.put("color", color_ficha);
                notificarJugador(respuesta);
            }

        } else if (tipoDeSolicitud.equals("avanzar")) {

            // COMPLETO
            // Primero se verifica si la Ficha se sobrepasa del final (si llega a más del recorrido final, si
            // se sobrepasó entonces se deniega la solicitud.

            JSONObject respuesta = new JSONObject();

            int dado = solicitud.getInt("dado");
            int ficha_id = solicitud.getInt("ficha");

            Ficha ficha_elegida = getFicha(color_jugador, ficha_id);

            if (fueraDeRango(ficha_elegida, dado)) {
                respuesta.put("tipo", "solicitud_denegada");
                respuesta.put("mensaje", "Esta ficha sale del límite de casillas");
                respuesta.put("color", ficha_elegida.getColor());

                notificarJugador(respuesta);
            } else {
                avanzarCasilla(ficha_elegida, dado);
            }
            // Luego se verifica si a la casilla a la que se quiere dirigir el jugador se encuentra una Ficha,
            // si no se encuentra ninguna entonces avanza con normalidad, si no, se verifica si la Ficha que se
            // encuentra en esa Casilla está en su casilla segura, si no lo está entonces la comerá, si no,
            // se deniega la solicitud

        } else if (tipoDeSolicitud.equals("mensaje")) {

            String nuevo_mensaje = solicitud.getString("contenido");

            JSONObject notificacion = new JSONObject();
            notificacion.put("tipo", "nuevo_mensaje");

            GestorCliente jugador = getJugador(color_jugador);
            String mensaje = jugador.getColor().toUpperCase() + ": " + nuevo_mensaje;
            notificacion.put("contenido", mensaje);

            notificarJugadores(notificacion, jugador);
        } else if (tipoDeSolicitud.equals("meme")) {

            String tipo_meme = solicitud.getString("meme");

            JSONObject notificacion = new JSONObject();
            notificacion.put("tipo", "meme");
            notificacion.put("color", color_jugador);
            notificacion.put("meme", tipo_meme);

            notificarJugadores(notificacion);
        }
    }

    public void addJugador(GestorCliente jugador) {

        Thread hilo = new Thread(jugador);
        hilo.start();
        asignarColor(jugador);
        jugador.addObserver(this);
        jugadores.add(jugador);

        jugador.initFichas();

        JSONObject respuesta = new JSONObject();
        respuesta.put("tipo", "asignacion_de_color");
        respuesta.put("color", jugador.getColor());

        notificarJugador(respuesta);
        notificarNuevoJugador();

    }

    /**
     * Notifica a todos los jugadores que se acaba de unir un nuevo jugador
     */
    private void notificarNuevoJugador() {

        JSONObject respuesta = new JSONObject();
        respuesta.put("tipo", "actualizar_jugadores");

        int cantidadJugadores = jugadores.size();
        respuesta.put("cantidad_jugadores", cantidadJugadores);

        for (int i = 0; i < jugadores.size(); i++) {

            GestorCliente jugador = jugadores.get(i);
            respuesta.put("jugador_" + i, jugador.getNombre());
            respuesta.put("color_" + i, jugador.getColor());
            respuesta.put("skin_" + i, jugador.getSkin());
        }

        notificarJugadores(respuesta);

    }

    private void asignarColor(GestorCliente jugador) {

        int cantidadJugadores = jugadores.size();

        if (cantidadJugadores == 0) {
            jugador.setColor("azul");

        } else if (cantidadJugadores == 1) {
            jugador.setColor("amarillo");

        } else if (cantidadJugadores == 2) {
            jugador.setColor("verde");

        } else if (cantidadJugadores == 3) {
            jugador.setColor("rojo");
        }
    }

    public boolean sePuedeUnir() {
        return jugadores.size() < 4 && !partidaIniciada;
    }

    /**
     * Verifica si por lo menos el jugador puede mover 1 ficha
     */
    private boolean sePuedeMover(GestorCliente jugador, int dado) {

        for (Ficha ficha : jugador.getFichas().getValores()) {

            if (ficha.enCasa() && dado == 1) return true;
            if (!ficha.enCasa() && puedeAvanzar(ficha, dado)) return true;

        }
        return false;

    }

    /**
     * Verifica si la ficha que se le manda por parámetro tiene la posibilidad de avanzar
     * (copia descarada de los métodos avanzarCasilla y verificarAvance)
     */
    private boolean puedeAvanzar(Ficha ficha, int cantidad) {
        if (ficha.recorridoAcabado()) {
            return false;
        } else if (ficha.enRecorridoFinal() && ficha.getPosicionActual() + cantidad == 5) {
            return true;

        } else if (!ficha.enRecorridoFinal() && ficha.getPosicionActual() == getLimiteGeneral(ficha.getColor()) && cantidad == 6) {
            return true;

        } else {

            int posicionActual = ficha.getPosicionActual();
            boolean enRecorridoFinal = false;

            Casilla casilla_actual;

            if (ficha.enRecorridoFinal()) {

                if (posicionActual + cantidad > 5) {
                    return false;
                }

                Casilla[] recorridoFinal = getRecorridoFinal(ficha.getColor());
                posicionActual += cantidad;
                casilla_actual = recorridoFinal[posicionActual];

            } else {

                int cantidad_casillas = recorridoGeneral.length;
                int limite = getLimiteGeneral(ficha.getColor());

                for (int i = 0; i < cantidad; i++) {
                    posicionActual++;
                    if (posicionActual == limite + 1) {
                        posicionActual = 0;
                        enRecorridoFinal = true;
                    } else if (posicionActual > cantidad_casillas - 1) {
                        posicionActual = 0;
                    }
                }

                if (enRecorridoFinal) {
                    casilla_actual = getRecorridoFinal(ficha.getColor())[posicionActual];
                } else {
                    casilla_actual = recorridoGeneral[posicionActual];
                }
            }
            if (casilla_actual.estaVacia()) {
                return true;

            } else if (casilla_actual.getFicha().getColor().equals(ficha.getColor())) {
                return false;

            } else return !estaEnCasillaSegura(casilla_actual);
        }
    }

    /**
     * Se gestionan los turnos
     */
    private void cambioDeTurno(String color_jugador_anterior) {

        JSONObject respuesta = new JSONObject();
        respuesta.put("tipo", "cambio_de_turno");

        int cantidadJugadores = jugadores.size();
        String jugador_actual = "";

        if (cantidadJugadores == 2) {

            switch (color_jugador_anterior) {
                case "azul":
                    respuesta.put("jugador_anterior", color_jugador_anterior);
                    respuesta.put("jugador_actual", "amarillo");
                    jugador_actual = "amarillo";
                    break;
                case "amarillo":
                    respuesta.put("jugador_anterior", color_jugador_anterior);
                    respuesta.put("jugador_actual", "azul");
                    jugador_actual = "azul";
                    break;
            }

        } else if (cantidadJugadores == 3) {

            switch (color_jugador_anterior) {
                case "azul":
                    respuesta.put("jugador_anterior", color_jugador_anterior);
                    respuesta.put("jugador_actual", "amarillo");
                    jugador_actual = "amarillo";
                    break;
                case "amarillo":
                    respuesta.put("jugador_anterior", color_jugador_anterior);
                    respuesta.put("jugador_actual", "verde");
                    jugador_actual = "verde";
                    break;
                case "verde":
                    respuesta.put("jugador_anterior", color_jugador_anterior);
                    respuesta.put("jugador_actual", "azul");
                    jugador_actual = "azul";
                    break;
            }

        } else if (cantidadJugadores == 4) {

            switch (color_jugador_anterior) {
                case "azul":
                    respuesta.put("jugador_anterior", color_jugador_anterior);
                    respuesta.put("jugador_actual", "amarillo");
                    jugador_actual = "amarillo";
                    break;
                case "amarillo":
                    respuesta.put("jugador_anterior", color_jugador_anterior);
                    respuesta.put("jugador_actual", "verde");
                    jugador_actual = "verde";
                    break;
                case "verde":
                    respuesta.put("jugador_anterior", color_jugador_anterior);
                    respuesta.put("jugador_actual", "rojo");
                    jugador_actual = "rojo";
                    break;
                case "rojo":
                    respuesta.put("jugador_anterior", color_jugador_anterior);
                    respuesta.put("jugador_actual", "azul");
                    jugador_actual = "azul";
                    break;
            }

        }
        GestorCliente jugador = getJugador(jugador_actual);
        String color_nombre = getCodigoColor(jugador_actual);
        String mensaje = "<html>Turno de " + color_nombre + jugador.getNombre() + "</font></html>";
        respuesta.put("mensaje", mensaje);
        notificarJugadores(respuesta);
    }

    private int tirarDados() {
        return (int) ((Math.random() * 6) + 1);
    }

    private Casilla[] getCasillasSeguras(String color) {
        Casilla[] casillasSeguras = new Casilla[7];
        switch (color) {
            case "azul":
                casillasSeguras[0] = recorridoGeneral[3];
                casillasSeguras[1] = recorridoGeneral[9];

                casillasSeguras[2] = recorridoAzul[0];
                casillasSeguras[3] = recorridoAzul[1];
                casillasSeguras[4] = recorridoAzul[2];
                casillasSeguras[5] = recorridoAzul[3];
                casillasSeguras[6] = recorridoAzul[4];
                break;
            case "amarillo":
                casillasSeguras[0] = recorridoGeneral[16];
                casillasSeguras[1] = recorridoGeneral[22];

                casillasSeguras[2] = recorridoAmarillo[0];
                casillasSeguras[3] = recorridoAmarillo[1];
                casillasSeguras[4] = recorridoAmarillo[2];
                casillasSeguras[5] = recorridoAmarillo[3];
                casillasSeguras[6] = recorridoAmarillo[4];
                break;
            case "verde":
                casillasSeguras[0] = recorridoGeneral[29];
                casillasSeguras[1] = recorridoGeneral[35];

                casillasSeguras[2] = recorridoVerde[0];
                casillasSeguras[3] = recorridoVerde[1];
                casillasSeguras[4] = recorridoVerde[2];
                casillasSeguras[5] = recorridoVerde[3];
                casillasSeguras[6] = recorridoVerde[4];
                break;
            case "rojo":
                casillasSeguras[0] = recorridoGeneral[42];
                casillasSeguras[1] = recorridoGeneral[48];

                casillasSeguras[2] = recorridoRojo[0];
                casillasSeguras[3] = recorridoRojo[1];
                casillasSeguras[4] = recorridoRojo[2];
                casillasSeguras[5] = recorridoRojo[3];
                casillasSeguras[6] = recorridoRojo[4];
                break;
        }
        return casillasSeguras;
    }

    /**
     * Verifica si la casilla de salida está ocupada por otra ficha y si puede ser eliminada
     */
    private void salirDeCasa(Ficha ficha) {
        JSONObject respuesta = new JSONObject();

        Casilla casilla = getCasillasSeguras(ficha.getColor())[0];

        if (casilla.estaVacia()) {

            // La casilla está vacia

            casilla.setFicha(ficha);
            ficha.setPosicionActual(casilla.getNumeroCasilla());
            ficha.setEnCasa(false);

            respuesta.put("tipo", "respuesta_salir_de_casa");
            respuesta.put("color", ficha.getColor());
            respuesta.put("ficha", ficha.getId());
            respuesta.put("x", casilla.getPosicion().x);
            respuesta.put("y", casilla.getPosicion().y);

            notificarJugadores(respuesta);
            cambioDeTurno(ficha.getColor());

        } else if (!casilla.getFicha().getColor().equals(ficha.getColor())) {

            // La casilla está ocupada por otra ficha de otro color

            Ficha ficha_eliminada = casilla.getFicha();
            ficha_eliminada.setPosicionActual(-1);
            ficha_eliminada.setEnCasa(true);

            casilla.setFicha(ficha);
            ficha.setEnCasa(false);
            ficha.setPosicionActual(casilla.getNumeroCasilla());

            notificarFichaEliminada(ficha_eliminada, ficha, casilla);
            //cambioDeTurno(ficha.getColor()); ------------------------------------

        } else {

            // La casilla está ocupada por otra ficha del mismo color

            respuesta.put("tipo", "solicitud_denegada");
            respuesta.put("mensaje", "Tu casilla de salida está ocupada");
            respuesta.put("color", ficha.getColor());
            notificarJugador(respuesta);
        }
    }

    private void avanzarCasilla(Ficha ficha, int cantidad) {
        if (ficha.enRecorridoFinal() && ficha.getPosicionActual() + cantidad == 5) {
            acaboElRecorrido(ficha);

        } else if (!ficha.enRecorridoFinal() && ficha.getPosicionActual() == getLimiteGeneral(ficha.getColor()) && cantidad == 6) {
            acaboElRecorrido(ficha);

        } else {

            int posicionActual = ficha.getPosicionActual();
            boolean enRecorridoFinal = false;

            Casilla casilla_anterior;
            Casilla casilla_actual;

            if (ficha.enRecorridoFinal()) {

                Casilla[] recorridoFinal = getRecorridoFinal(ficha.getColor());
                casilla_anterior = recorridoFinal[posicionActual];
                posicionActual += cantidad;
                casilla_actual = recorridoFinal[posicionActual];

            } else {

                int cantidad_casillas = recorridoGeneral.length;
                casilla_anterior = recorridoGeneral[posicionActual];
                int limite = getLimiteGeneral(ficha.getColor());

                for (int i = 0; i < cantidad; i++) {
                    posicionActual++;
                    if (posicionActual == limite + 1) {
                        posicionActual = 0;
                        enRecorridoFinal = true;
                    } else if (posicionActual > cantidad_casillas - 1) {
                        posicionActual = 0;
                    }
                }

                if (enRecorridoFinal) {
                    casilla_actual = getRecorridoFinal(ficha.getColor())[posicionActual];
                } else {
                    casilla_actual = recorridoGeneral[posicionActual];
                }
            }
            verificarAvance(casilla_anterior, casilla_actual, ficha, enRecorridoFinal);
        }
    }

    /**
     * Verifica si existe otra ficha en la casilla a la que se está dirigiendo la ficha actual y si puede avanzar
     */
    private void verificarAvance(Casilla casilla_anterior, Casilla casilla_actual, Ficha ficha, boolean enRecorridoFinal) {
        JSONObject respuesta = new JSONObject();

        if (casilla_actual.estaVacia()) {
            ficha.setPosicionActual(casilla_actual.getNumeroCasilla());
            casilla_actual.setFicha(ficha);

            if (enRecorridoFinal) {
                ficha.setEnRecorridoFinal(true);
            }

            respuesta.put("tipo", "avanzar");
            respuesta.put("color", ficha.getColor());
            respuesta.put("ficha", ficha.getId());
            respuesta.put("posicion", ficha.getPosicionActual());
            respuesta.put("x", casilla_actual.getPosicion().x);
            respuesta.put("y", casilla_actual.getPosicion().y);

            notificarJugadores(respuesta);
            cambioDeTurno(ficha.getColor());

            casilla_anterior.setFicha(null);

        } else if (casilla_actual.getFicha().getColor().equals(ficha.getColor())) {

            // Hay una ficha del mismo color en la casilla a la que se quiere dirigir la ficha actual

            respuesta.put("tipo", "solicitud_denegada");
            respuesta.put("mensaje", "Hay otra ficha del mismo color en la casilla a la que queres dirigirte");
            respuesta.put("color", ficha.getColor());

            notificarJugador(respuesta);

        } else if (!estaEnCasillaSegura(casilla_actual)) {

            // La ficha que está en la casilla a la que se quiere dirigir la ficha actual puede ser eliminada
            // ya que no está en alguna de sus casillas seguras

            Ficha ficha_eliminada = casilla_actual.getFicha();

            ficha_eliminada.setEnCasa(true);
            ficha_eliminada.setPosicionActual(-1);

            casilla_actual.setFicha(ficha);
            ficha.setPosicionActual(casilla_actual.getNumeroCasilla());
            casilla_anterior.setFicha(null);

            notificarFichaEliminada(ficha_eliminada, ficha, casilla_actual);
            //cambioDeTurno(ficha.getColor());

        } else {

            // Hay una ficha en la casilla que no puede ser eliminada, es una ficha del mismo jugador
            // o es otra que se encuentra en su casilla segura

            respuesta.put("tipo", "solicitud_denegada");
            respuesta.put("mensaje", "Existe otra ficha en la Casilla a la que te estás dirigiendo");
            respuesta.put("color", ficha.getColor());

            notificarJugador(respuesta);
        }
    }

    /**
     * Elimina la Ficha de la casilla en que se encuentra y obtiene las coordenada (X,Y) que le corresponden a la Ficha
     * para llegar a la meta
     */
    private void acaboElRecorrido(Ficha ficha) {
        Casilla casillaAnterior;
        String color_ficha = ficha.getColor();

        if (ficha.enRecorridoFinal()) {
            Casilla[] recorridoFinal = getRecorridoFinal(color_ficha);
            casillaAnterior = recorridoFinal[ficha.getPosicionActual()];
        } else {
            casillaAnterior = recorridoGeneral[ficha.getPosicionActual()];
        }
        casillaAnterior.setFicha(null);
        ficha.setRecorridoAcabado(true);

        int x = getRecorridoFinal(color_ficha)[4].getPosicion().x;
        int y = getRecorridoFinal(color_ficha)[4].getPosicion().y;
        Point posicion = new Point(x, y);

        switch (color_ficha) {
            case "azul":
                posicion.x += 40;
                break;
            case "amarillo":
                posicion.y -= 40;
                break;
            case "verde":
                posicion.x -= 40;
                break;
            case "rojo":
                posicion.y += 40;
                break;
        }
        ficha.setPosicionActual(-1);

        JSONObject respuesta = new JSONObject();

        respuesta.put("tipo", "ficha_fin_recorrido");
        respuesta.put("color", ficha.getColor());
        respuesta.put("ficha", ficha.getId());
        respuesta.put("posicion", ficha.getPosicionActual());
        respuesta.put("x", posicion.x);
        respuesta.put("y", posicion.y);

        GestorCliente jugador = getJugador(ficha.getColor());
        String color_nombre = getCodigoColor(ficha.getColor());
        String mensaje = "<html>Una ficha de " + color_nombre + jugador.getNombre() + "</font> llegó a la meta</html>";
        respuesta.put("mensaje", mensaje);

        notificarJugadores(respuesta);
        //cambioDeTurno(color_ficha); ------------------------------------

        if (esGanador(jugador)) {

            respuesta = new JSONObject();

            respuesta.put("tipo", "partida_finalizada");
            respuesta.put("color", jugador.getColor());

            mensaje = "<html>" + color_nombre + jugador.getNombre() + "</font> acaba de ganar</html>";
            respuesta.put("mensaje", mensaje);

            notificarJugadores(respuesta);
        }
    }


    /**
     * Verifica si la Ficha que se quiere avanzar no excede el número de Casillas
     */
    private boolean fueraDeRango(Ficha ficha, int cantidad) {
        if (ficha.enRecorridoFinal()) {
            int posicionFinal = ficha.getPosicionActual() + cantidad;
            return posicionFinal > 5;
        }
        return false;
    }

    private Casilla[] getRecorridoFinal(String color) {
        switch (color) {
            case "azul":
                return recorridoAzul;
            case "amarillo":
                return recorridoAmarillo;
            case "verde":
                return recorridoVerde;
            case "rojo":
                return recorridoRojo;
            default:
                return new Casilla[4];
        }
    }

    /**
     * Verifica si todas las fichas del jugador llegaron a la meta
     */
    private boolean esGanador(GestorCliente jugador) {
        for (Ficha ficha : jugador.getFichas().getValores()) {
            if (!ficha.recorridoAcabado()) {
                return false;
            }
        }
        return true;
    }

    private void notificarFichaEliminada(Ficha ficha_1, Ficha ficha_2, Casilla casilla) {
        JSONObject respuesta = new JSONObject();

        // La ficha y color 1 pertenece a la ficha que se acaba de eliminar
        // La ficha y color 2 pertenece a la ficha que eliminó a la ficha 1
        respuesta.put("tipo", "ficha_eliminada");
        respuesta.put("ficha_1", ficha_1.getId());
        respuesta.put("color_1", ficha_1.getColor());

        respuesta.put("ficha_2", ficha_2.getId());
        respuesta.put("color_2", ficha_2.getColor());
        respuesta.put("x", casilla.getPosicion().x);
        respuesta.put("y", casilla.getPosicion().y);

        GestorCliente jugador_1 = getJugador(ficha_1.getColor());
        GestorCliente jugador_2 = getJugador(ficha_2.getColor());

        String color_nombre_1 = getCodigoColor(ficha_1.getColor());
        String color_nombre_2 = getCodigoColor(ficha_2.getColor());

        String mensaje = "<html>" + color_nombre_2 + jugador_2.getNombre() + "</font> eliminó una ficha de " + color_nombre_1 + jugador_1.getNombre() + "</font></html>";
        respuesta.put("mensaje", mensaje);

        notificarJugadores(respuesta);
    }

    /**
     * Verifica si la Ficha que se encuentra en esta Casilla puede ser eliminada (si se encuentra en su casilla
     * segura o no)
     */
    private boolean estaEnCasillaSegura(Casilla casilla) {
        Ficha ficha = casilla.getFicha();
        Casilla[] casillasSeguras = getCasillasSeguras(ficha.getColor());
        for (Casilla casillaSegura : casillasSeguras) {
            if (casillaSegura.getNumeroCasilla() == casilla.getNumeroCasilla()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Devuelve el número de casilla límite para entrar al recorrido final
     */
    private int getLimiteGeneral(String color) {
        switch (color) {
            case "azul":
                return 51;
            case "amarillo":
                return 12;
            case "verde":
                return 25;
            case "rojo":
                return 38;
        }
        return -1;
    }

    /**
     * Verifica si todas las fichas del jugador están en casa
     */
    private boolean todasLasFichasEnCasa(String color) {
        List<Ficha> fichas = new ArrayList<>();
        for (GestorCliente jugador : jugadores) {
            if (jugador.getColor().equals(color)) {
                fichas = jugador.getFichas().getValores();
            }
        }
        for (Ficha ficha : fichas) {
            if (!ficha.enCasa()) {
                return false;
            }
        }
        return true;
    }

    private Ficha getFicha(String color, int id) {
        GestorCliente jugador = getJugador(color);
        for (Ficha ficha : jugador.getFichas().getValores()) {
            if (ficha.getId() == id) {
                return ficha;
            }
        }
        return null;
    }

    private GestorCliente getJugador(String color) {
        for (GestorCliente jugador : jugadores) {
            if (jugador.getColor().equals(color)) {
                return jugador;
            }
        }
        return null;
    }

    private void notificarJugador(JSONObject mensaje) {
        String colorJugador = mensaje.getString("color");
        for (GestorCliente jugador : jugadores) {
            if (jugador.getColor().equals(colorJugador)) {
                try {
                    jugador.bufferedWriter.write(mensaje.toString());
                    jugador.bufferedWriter.newLine();
                    jugador.bufferedWriter.flush();
                } catch (Exception e) {
                    System.out.println("Error en el método notificarJugador");
                }
            }
        }
    }

    private void notificarJugadores(JSONObject mensaje) {
        for (GestorCliente jugador : jugadores) {
            try {
                jugador.bufferedWriter.write(mensaje.toString());
                jugador.bufferedWriter.newLine();
                jugador.bufferedWriter.flush();
                if (mensaje.getString("tipo").equals("partida_finalizada")) {
                    jugador.partidaFinalizada(id);
                    jugador.removeObserver(this);
                }
            } catch (Exception e) {
                System.out.println("Error en el método notificarJugadores");
            }
        }
    }

    private void notificarJugadores(JSONObject mensaje, GestorCliente jugador_no_notificar) {
        for (GestorCliente jugador : jugadores) {
            if (jugador.getColor().equals(jugador_no_notificar.getColor())) {
                continue;
            }
            try {
                jugador.bufferedWriter.write(mensaje.toString());
                jugador.bufferedWriter.newLine();
                jugador.bufferedWriter.flush();
            } catch (Exception e) {
                System.out.println("Error en el método notificarJugadores");
            }
        }
    }

    private void initRecorridos() {
        int x = 0;
        int y = 420;

        // General -> 0 - 51
        recorridoGeneral = new Casilla[52];
        for (int i = 0; i < recorridoGeneral.length; i++) {
            Point posicion = new Point(x, y);

            Casilla casilla = new Casilla();
            casilla.setNumeroCasilla(i);
            casilla.setPosicion(posicion);
            recorridoGeneral[i] = casilla;

            if (i >= 0 && i <= 4) {
                x += 40;
            } else if (i == 5) {
                x += 40;
                y += 40;
            } else if (i >= 6 && i <= 10) {
                y += 40;
            } else if (i >= 11 && i <= 12) {
                x += 40;
            } else if (i >= 13 && i <= 17) {
                y -= 40;
            } else if (i == 18) {
                x += 40;
                y -= 40;
            } else if (i >= 19 && i <= 23) {
                x += 40;
            } else if (i >= 24 && i <= 25) {
                y -= 40;
            } else if (i >= 26 && i <= 30) {
                x -= 40;
            } else if (i == 31) {
                x -= 40;
                y -= 40;
            } else if (i >= 32 && i <= 36) {
                y -= 40;
            } else if (i >= 37 && i <= 38) {
                x -= 40;
            } else if (i >= 39 && i <= 43) {
                y += 40;
            } else if (i == 44) {
                x -= 40;
                y += 40;
            } else if (i >= 45 && i <= 49) {
                x -= 40;
            } else if (i >= 50 && i <= 51) {
                y += 40;
            }
        }

        // Azul -> 0 - 4
        x = recorridoGeneral[51].getPosicion().x + 40;
        y = recorridoGeneral[51].getPosicion().y;

        recorridoAzul = new Casilla[5];
        for (int i = 0; i < recorridoAzul.length; i++) {
            Point posicion = new Point(x, y);

            Casilla casilla = new Casilla();
            casilla.setNumeroCasilla(i);
            casilla.setPosicion(posicion);
            recorridoAzul[i] = casilla;

            x += 40;
        }

        // Amarillo -> 0 - 4
        x = recorridoGeneral[12].getPosicion().x;
        y = recorridoGeneral[12].getPosicion().y - 40;

        recorridoAmarillo = new Casilla[5];
        for (int i = 0; i < recorridoAmarillo.length; i++) {
            Point posicion = new Point(x, y);

            Casilla casilla = new Casilla();
            casilla.setNumeroCasilla(i);
            casilla.setPosicion(posicion);
            recorridoAmarillo[i] = casilla;

            y -= 40;
        }

        // Verde -> 0 - 4
        x = recorridoGeneral[25].getPosicion().x - 40;
        y = recorridoGeneral[25].getPosicion().y;

        recorridoVerde = new Casilla[5];
        for (int i = 0; i < recorridoVerde.length; i++) {
            Point posicion = new Point(x, y);

            Casilla casilla = new Casilla();
            casilla.setNumeroCasilla(i);
            casilla.setPosicion(posicion);
            recorridoVerde[i] = casilla;

            x -= 40;
        }

        // Rojo -> 0 - 4
        x = recorridoGeneral[38].getPosicion().x;
        y = recorridoGeneral[38].getPosicion().y + 40;

        recorridoRojo = new Casilla[5];
        for (int i = 0; i < recorridoRojo.length; i++) {
            Point posicion = new Point(x, y);

            Casilla casilla = new Casilla();
            casilla.setNumeroCasilla(i);
            casilla.setPosicion(posicion);
            recorridoRojo[i] = casilla;

            y += 40;
        }
    }

    private String getCodigoColor(String color) {
        switch (color) {
            case "azul":
                return "<font color='blue'>";
            case "amarillo":
                return "<font color='yellow'>";
            case "verde":
                return "<font color='green'>";
            case "rojo":
                return "<font color='red'>";
        }
        return null;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
