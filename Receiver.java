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
        
        int rcvPort=12345; // Como a porta de destino 12345 foi definida no Sender, também defino assim aqui no Receiver

        DatagramSocket socket = new DatagramSocket(rcvPort);
        byte[] inputBuffer = new byte[2048];
        
        int idEsperado = 0; // O primeiro ID que o Receiver espera será sempre o 0

        // Estrutura de Dados HashSet, utilizada para armazenar os IDs dos pacotes já recebidos, para tratar duplicação 
        // Utilizo HashSet para não ter IDs duplicados aqui
        Set<Integer> recebidos = new HashSet<>();

        // Buffer principal do Receiver, aqui armazena os pacotes recebidos fora de ordem, para que quando o próximo esperado chegar ele poder 
        // entregar para camada de aplicação os que já chegaram. Utilizo HashMap para poder associar um pacote à um ID de forma mais rápida e fácil (sem precisar checar o ID individualmente) 
        Map<Integer, SegmentoConfiavel> packetBuffer = new HashMap<>();
        
        System.out.println("Receiver iniciado na porta " + rcvPort + ". Aguardando mensagens...");

        while(true) {

            DatagramPacket packet = new DatagramPacket(inputBuffer, inputBuffer.length);
            socket.receive(packet); // Espera a chegada de pacotes

            SegmentoConfiavel segmento = SegmentoConfiavel.fromBytes(packet.getData()); // Reconstrói o SegmentoConfiável
            
            // Primeiro tratamento, se o ID recebido já existir no HashSet, indica que é um pacote duplicado
            if(recebidos.contains(segmento.getID())){
                System.out.println("Mensagem id " + segmento.getID() + " recebida de forma duplicada");
            }


            // Caso que o pacote recebido é o que o Receiver está esperando, simplemente adiciono o ID ao HashSet e incremento o idEsperado
            else if(segmento.getID() == idEsperado) {
                recebidos.add(segmento.getID());
                System.out.println("Mensagem id " + segmento.getID() + " recebida na ordem, entregando para a camada de aplicação");
                idEsperado++;

                // Após receber o pacote esperado, verifica se existem os próximos no buffer
                while(packetBuffer.containsKey(idEsperado)){
                    System.out.println("Mensagem id " + idEsperado + " recebida na ordem, entregando para a camada de aplicação");
                    recebidos.add(idEsperado);
                    packetBuffer.remove(idEsperado);
                    idEsperado++;
                }
            }

            // Tratamento: caso o ID seja maior que o esperado, indica que os pacotes chegaram fora de ordem
            else if(segmento.getID() > idEsperado) {
                packetBuffer.put(segmento.getID(), segmento); // Guarda o pacote recebido no buffer principal
                recebidos.add(segmento.getID()); // Guarda o ID no HashSet de recebidos

                // Lógica para fazer a impressão correta dos IDs faltantes
                boolean first = true;
                System.out.print("Mensagem id " + segmento.getID() + " recebida fora de ordem, ainda não recebidos os identificadores [");
                for(int i = idEsperado; i < segmento.getID(); i++) {
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

            // Independente do caso, pelo funcionamento da Repetição Seletiva, envio o ACK do pacote que foi recebido
            SegmentoConfiavel segmentoResposta = new SegmentoConfiavel(segmento.getID());
            byte[] dados = segmentoResposta.toBytes();
            DatagramPacket resPacket = new DatagramPacket(dados, dados.length, packet.getAddress(), packet.getPort()); 
            socket.send(resPacket);

            
            // Fecho a conexão
            if(segmento.getMensagem().equals("quit")){
                System.out.println("Fechando conexão");
                socket.close();
                System.exit(0);
            }
        }
    }  
}