import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Scanner;

public class Sender {
    public static void main(String[] args) throws UnknownHostException, SocketException, IOException {
        Scanner scan = new Scanner(System.in);

        System.out.print("Digite o IP do Receiver [127.0.0.1]:");
        String ipStr = scan.nextLine();
        InetAddress ipDestino = InetAddress.getByName(ipStr.isEmpty() ? "127.0.0.1" : ipStr);
        int destPort = 12345;

        DatagramSocket socket = new DatagramSocket();

        while(true) {

            System.err.print("Digite a mensagem que deseja enviar: ");
            String mensagem = scan.nextLine();

            SegmentoConfiavel segmento = new SegmentoConfiavel(0, mensagem, "normal");
            byte[] dados = segmento.toBytes();

            DatagramPacket packet = new DatagramPacket(dados, dados.length, ipDestino, destPort);

            socket.send(packet);

            System.out.println("Mensagem \"" + mensagem + "\" enviada como [normal] com id " + segmento.getID());

            if(mensagem.equals("quit")) {
                System.out.println("Fechando conexão");
                scan.close();
                socket.close();
                System.exit(0);
            }
        }
    }
}
