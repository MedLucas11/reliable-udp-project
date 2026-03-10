import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Scanner;
import java.util.HashMap;
import java.util.Map;

public class Sender {
    public static void main(String[] args) throws UnknownHostException, SocketException, IOException {
        Scanner scan = new Scanner(System.in);
        Map<Integer, String> tiposEnvio = new HashMap<>();
        tiposEnvio.put(1, "normal");
        tiposEnvio.put(2, "duplicada");
        tiposEnvio.put(3, "lento");
        tiposEnvio.put(4, "perda");
        tiposEnvio.put(5, "fora de ordem");
        
        System.out.print("Digite o IP do Receiver [127.0.0.1]:");
        String ipStr = scan.nextLine();
        InetAddress ipDestino = InetAddress.getByName(ipStr.isEmpty() ? "127.0.0.1" : ipStr);
        int destPort = 12345;

        DatagramSocket socket = new DatagramSocket();

        int idAtual = 0;

        while(true) {

            System.err.print("Digite a mensagem que deseja enviar: ");
            String mensagem = scan.nextLine();

            System.out.println("Escolha o tipo de envio (digite o número entre []):");
            System.out.println("[1] - Normal\n[2] - Duplicada\n[3] - Lento\n[4] - Perda\n[5] - Fora de Ordem");
            
            int tipoEnvio = scan.nextInt();
            scan.nextLine();

            SegmentoConfiavel segmento = new SegmentoConfiavel(idAtual, mensagem);
            byte[] dados = segmento.toBytes();


            DatagramPacket packet = new DatagramPacket(dados, dados.length, ipDestino, destPort);

            socket.send(packet);

            System.out.println("Mensagem \"" + mensagem + "\" enviada como [" + tiposEnvio.get(tipoEnvio) +"] com id " + segmento.getID());
            
            idAtual++;
            
            if(mensagem.equals("quit")) {
                System.out.println("Fechando conexão");
                scan.close();
                socket.close();
                System.exit(0);
            }
        }
    }
}
