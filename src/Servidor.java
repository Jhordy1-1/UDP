import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

public class Servidor {
    private static final int PUERTO = 6000;
    private static final String[] PREGUNTAS = {
        "Capital de Ecuador?",
        "Islas famosas?",
        "Moneda oficial?",
        "Volcán activo?",
        "Animal emblemático?"
    };
    private static final String[] RESPUESTAS = { "quito", "galapagos", "dolar", "cotopaxi", "condor" };

    private static Map<String, Integer> progresoClientes = new HashMap<>();
    private static Map<String, Integer> puntuacionClientes = new HashMap<>();

    private static int contadorRespuestas = 1;

    public static void main(String[] args) {
        try (DatagramSocket socket = new DatagramSocket(PUERTO)) {
            System.out.println("Servidor UDP esperando conexiones...");

            while (true) {
                byte[] bufferEntrada = new byte[1024];
                DatagramPacket paqueteEntrada = new DatagramPacket(bufferEntrada, bufferEntrada.length);
                socket.receive(paqueteEntrada);

                String mensajeRecibido = new String(paqueteEntrada.getData(), 0, paqueteEntrada.getLength()).trim();
                InetAddress direccionCliente = paqueteEntrada.getAddress();
                int puertoCliente = paqueteEntrada.getPort();
                String idCliente = direccionCliente.toString() + ":" + puertoCliente;

                int numeroDePregunta = progresoClientes.getOrDefault(idCliente, 0);
                int total = puntuacionClientes.getOrDefault(idCliente, 0);

                if (numeroDePregunta > 0) {
                    boolean correcto = RESPUESTAS[numeroDePregunta - 1].equalsIgnoreCase(mensajeRecibido);
                    if (correcto) total++;
                    puntuacionClientes.put(idCliente, total);

                    registrarRespuesta(mensajeRecibido, direccionCliente.getHostAddress());
                }

                String mensajeRespuesta;
                if (numeroDePregunta == PREGUNTAS.length) {
                    mensajeRespuesta = "Finalizo la encuesta, tu puntuación es: " + total*4 + "/20";
                    progresoClientes.remove(idCliente);
                    puntuacionClientes.remove(idCliente);
                } else {
                    mensajeRespuesta = (numeroDePregunta > 0 ? "Respuesta " + (RESPUESTAS[numeroDePregunta - 1].equalsIgnoreCase(mensajeRecibido) ? "Correcta. " : "Incorrecta. La respuesta es: "+ RESPUESTAS[numeroDePregunta - 1]) : "") 
                                       + "\nPregunta: " + PREGUNTAS[numeroDePregunta];
                    progresoClientes.put(idCliente, numeroDePregunta + 1);
                }

                byte[] bufferSalida = mensajeRespuesta.getBytes();
                DatagramPacket paqueteSalida = new DatagramPacket(bufferSalida, bufferSalida.length, direccionCliente, puertoCliente);
                socket.send(paqueteSalida);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void registrarRespuesta(String respuesta, String ip) {
        try (PrintWriter writer = new PrintWriter(new FileWriter("respuestas.txt", true))) {
            String fechaHora = java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            String entrada = String.format("Respuesta #%d | Fecha y Hora: %s | IP: %s | Respuesta: %s",
                                            contadorRespuestas++, fechaHora, ip, respuesta);
            writer.println(entrada);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
