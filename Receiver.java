import java.net.DatagramSocket;
import java.net.SocketException;
import java.io.IOException;
import java.net.DatagramPacket;


public class Receiver {
    public static void main(String[] args) throws SocketException, IOException, ClassNotFoundException {
        
        int port=12345;

        DatagramSocket socket = new DatagramSocket(port);
        byte[] inputBuffer = new byte[2048];

        System.out.println("Receiver iniciado na porta " + port + ". Aguardando mensagens...");

        while(true) {

            DatagramPacket packet = new DatagramPacket(inputBuffer, inputBuffer.length);
            socket.receive(packet);

            SegmentoConfiavel segmento = SegmentoConfiavel.fromBytes(packet.getData());

            System.out.println("Mensagem id: " + segmento.getID() + " recebida!");
            System.out.println("Conteúdo: " + segmento.getMensagem());

            if(segmento.getMensagem().equals("quit")){
                System.out.println("Fechando conexão");
                socket.close();
                System.exit(0);
            }
        }
    }  
}