import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Scanner;

public class Cliente {
    private static final String HOST_SERVIDOR = "localhost";
    private static final int PUERTO_SERVIDOR = 6000;

    public static void main(String[] args) {
        try (DatagramSocket socket = new DatagramSocket(); Scanner scanner = new Scanner(System.in)) {
            InetAddress direccionServidor = InetAddress.getByName(HOST_SERVIDOR);

            String inicial = "conectar";
            byte[] bufferSalida = inicial.getBytes();
            DatagramPacket paqueteSalida = new DatagramPacket(bufferSalida, bufferSalida.length, direccionServidor, PUERTO_SERVIDOR);
            socket.send(paqueteSalida);

            while (true) {
                byte[] bufferEntrada = new byte[1024];
                DatagramPacket paqueteEntrada = new DatagramPacket(bufferEntrada, bufferEntrada.length);
                socket.receive(paqueteEntrada);

                String mensajeRecibido = new String(paqueteEntrada.getData(), 0, paqueteEntrada.getLength());
                System.out.println("Servidor: " + mensajeRecibido);

                if (mensajeRecibido.contains("Finalizo la encuesta")) break;

                System.out.print("Escribe tu respuesta: ");
                String respuesta = scanner.nextLine();
                
                bufferSalida = respuesta.getBytes();
                paqueteSalida = new DatagramPacket(bufferSalida, bufferSalida.length, direccionServidor, PUERTO_SERVIDOR);
                socket.send(paqueteSalida);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
