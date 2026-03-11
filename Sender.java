import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Scanner;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Sender {
    static DatagramSocket socket;

    static class Listener extends Thread {
        public void run() {
            byte[] inputBuffer = new byte[2048];
            //System.out.println("\nListening ACKs through the Thread...");
            
            try {
                while (true) {
                    DatagramPacket packet = new DatagramPacket(inputBuffer, inputBuffer.length);
                    socket.receive(packet);

                    SegmentoConfiavel ack = SegmentoConfiavel.fromBytes(packet.getData());
                
                    if(ack.isAck()) {
                        System.out.println("\nMensagem id " + ack.getSeqNum() + " recebida pelo receiver");
                    }
                }
            } catch (Exception e) {}
        };
    }

    public static void main(String[] args) throws UnknownHostException, SocketException, IOException, InterruptedException {
        Scanner scan = new Scanner(System.in);
        Map<Integer, String> tiposEnvio = new HashMap<>();
        tiposEnvio.put(1, "normal");
        tiposEnvio.put(2, "duplicada");
        tiposEnvio.put(3, "lento");
        tiposEnvio.put(4, "perda");
        tiposEnvio.put(5, "fora de ordem");
        
        System.out.print("Digite o IP do Receiver [127.0.0.1]:");
        socket = new DatagramSocket();
        String ipStr = scan.nextLine();
        InetAddress ipDestino = InetAddress.getByName(ipStr.isEmpty() ? "127.0.0.1" : ipStr);
        int destPort = 12345;
        

        int idAtual = 0;
        Thread thread = new Listener();
        thread.start();
        
        List<DatagramPacket> pacotesCriados = new ArrayList<>();
        
        while(true) {

            //System.out.print("\nDigite a mensagem que deseja enviar: ");
            String mensagem = scan.nextLine();

            System.out.println("\nEscolha o tipo de envio (digite o número entre []):\n");
            System.out.println("[1] - Normal\n[2] - Duplicada\n[3] - Lento\n[4] - Perda\n[5] - Fora de Ordem");
            
            int tipoEnvio = scan.nextInt();
            scan.nextLine();
            
            SegmentoConfiavel segmento = new SegmentoConfiavel(idAtual, mensagem);
            byte[] dados = segmento.toBytes();
            DatagramPacket packet = new DatagramPacket(dados, dados.length, ipDestino, destPort);
            
            switch (tipoEnvio) {
                case 1: // Normal
                    socket.send(packet);
                    System.out.println("\nMensagem \"" + mensagem + "\" enviada como [" + tiposEnvio.get(tipoEnvio) +"] com id " + segmento.getSeqNum());

                    if (!pacotesCriados.isEmpty()) {
                        for(DatagramPacket p : pacotesCriados) {
                            socket.send(p);
                        }
                        pacotesCriados.clear();
                    }
                    break;
            
                case 2: // Duplicada
                    socket.send(packet);
                    socket.send(packet);
                    System.out.println("\nMensagem \"" + mensagem + "\" enviada como [" + tiposEnvio.get(tipoEnvio) +"] com id " + segmento.getSeqNum());
                    break;
                
                case 3: // Lento
                    Thread.sleep(3000);
                    socket.send(packet);
                    System.out.println("\nMensagem \"" + mensagem + "\" enviada como [" + tiposEnvio.get(tipoEnvio) +"] com id " + segmento.getSeqNum());
                    break;
                
                case 4: // Com perda
                    System.out.println("\nMensagem \"" + mensagem + "\" enviada como [" + tiposEnvio.get(tipoEnvio) +"] com id " + segmento.getSeqNum());
                    break;
                
                case 5: // Fora de Ordem
                    pacotesCriados.add(packet);
                    System.out.println("\nMensagem \"" + mensagem + "\" enviada como [" + tiposEnvio.get(tipoEnvio) +"] com id " + segmento.getSeqNum());
                    break;
            }
            
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
