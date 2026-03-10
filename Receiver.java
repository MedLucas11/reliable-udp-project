import java.net.DatagramSocket;
import java.net.SocketException;
import java.io.IOException;
import java.net.DatagramPacket;


public class Receiver {
    public static void main(String[] args) throws SocketException, IOException, ClassNotFoundException {
        
        int rcvPort=12345;

        DatagramSocket socket = new DatagramSocket(rcvPort);
        byte[] inputBuffer = new byte[2048];

        System.out.println("Receiver iniciado na porta " + rcvPort + ". Aguardando mensagens...");

        while(true) {

            DatagramPacket packet = new DatagramPacket(inputBuffer, inputBuffer.length);
            socket.receive(packet);

            SegmentoConfiavel segmento = SegmentoConfiavel.fromBytes(packet.getData());

            System.out.println("Mensagem id " + segmento.getSeqNum() + " recebida na ordem, entregando para a camada de aplicação");

            SegmentoConfiavel segmentoResposta = new SegmentoConfiavel(segmento.getSeqNum());
            byte[] dados = segmentoResposta.toBytes();
            DatagramPacket resPacket = new DatagramPacket(dados, dados.length, packet.getAddress(), packet.getPort()); 
            socket.send(resPacket);

            if(segmento.getMensagem().equals("quit")){
                System.out.println("Fechando conexão");
                socket.close();
                System.exit(0);
            }
        }
    }  
}