import java.net.DatagramSocket;
import java.net.SocketException;
import java.io.IOException;
import java.net.DatagramPacket;
import java.util.Set;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Map;


public class Receiver {
    public static void main(String[] args) throws SocketException, IOException, ClassNotFoundException {
        
        int rcvPort=12345;

        DatagramSocket socket = new DatagramSocket(rcvPort);
        byte[] inputBuffer = new byte[2048];
        int seqEsperado = 0;

        Set<Integer> recebidos = new HashSet<>();
        Map<Integer, SegmentoConfiavel> packetBuffer = new HashMap<>();
        
        System.out.println("Receiver iniciado na porta " + rcvPort + ". Aguardando mensagens...");

        while(true) {

            DatagramPacket packet = new DatagramPacket(inputBuffer, inputBuffer.length);
            socket.receive(packet);

            SegmentoConfiavel segmento = SegmentoConfiavel.fromBytes(packet.getData());
            
            if(recebidos.contains(segmento.getSeqNum())){
                System.out.println("Mensagem id " + segmento.getSeqNum() + " recebida de forma duplicada");
            }

            else if(segmento.getSeqNum() == seqEsperado) {
                recebidos.add(segmento.getSeqNum());
                System.out.println("Mensagem id " + segmento.getSeqNum() + " recebida na ordem, entregando para a camada de aplicação");
                seqEsperado++;

                // Após receber o pacote esperado, verificar se existem os próximos no buffer
                while(packetBuffer.containsKey(seqEsperado)){
                    System.out.println("Mensagem id " + seqEsperado + " recebida na ordem, entregando para a camada de aplicação");
                    recebidos.add(seqEsperado);
                    packetBuffer.remove(seqEsperado);
                    seqEsperado++;
                }
            }

            else if(segmento.getSeqNum() > seqEsperado) {
                packetBuffer.put(segmento.getSeqNum(), segmento);
                recebidos.add(segmento.getSeqNum());

                boolean first = true;
                System.out.println("Mensagem id " + segmento.getSeqNum() + " recebida fora de ordem, ainda não recebidos os identificadores [");
                for(int i = seqEsperado; i < segmento.getSeqNum(); i++) {
                    if(!recebidos.contains(i)) {
                        if(!first) {
                            System.out.print(", ");
                        }
                        System.out.print(i);
                        first = false;
                    }
                }
                System.out.println("]");
            }

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