import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Scanner;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

public class Sender {
    // Socket de conexão com o Receiver
    static DatagramSocket socket;
    
    // Buffer principal do Sender, onde os IDs dos pacotes já enviados são armezenados junto a um Timer, quando houver timeout, reenvia o pacote com o ID correspondente 
    // ConcurrentHashMap foi escolhido para evitar erros de concorrência entre as Threads, já que a Thread principal adiciona timers aqui e o Listener remove quando necessário
    static Map<Integer, Timer> timers = new ConcurrentHashMap<>();

    // Thread que irá ficar esperando os ACKs correspondentes
    static class Listener extends Thread {
        public void run() {
            byte[] inputBuffer = new byte[2048];
            
            try {
                while (true) {
                    DatagramPacket packet = new DatagramPacket(inputBuffer, inputBuffer.length);
                    socket.receive(packet); // Espera um pacote

                    SegmentoConfiavel ack = SegmentoConfiavel.fromBytes(packet.getData()); // Reconstrói o SegmentoConfiável
                
                    // Caso seja um ACK, imprime na tela que foi recebida e remove do buffer com timers
                    if(ack.isAck()) {
                        System.out.println("\nMensagem id " + ack.getID() + " recebida pelo receiver\n");
                    
                        Timer timer = timers.remove(ack.getID());
                        if(timer != null){ 
                            timer.cancel();
                        }
                    }
                }
            } catch (Exception e) {}
        };
    }

    // Classe criada para simular o cenário de envio lento
    static class SlowSender extends Thread {
        private DatagramPacket latePacket;

        public SlowSender(DatagramPacket packet) {
            this.latePacket = packet;
        }

        public void run() {
            try {
                Thread.sleep(10000); // Vai enviar o pacote somente após 10 segundos
                socket.send(latePacket);
            } catch(Exception e) {}
        }
    }

    // Classe TimerTask responsável por retransmitir um pacote caso seu timer associado se esgote
    static class RetransmissionTimer extends TimerTask {
        private DatagramPacket packetStore;
        private int seqNumStore;

        public RetransmissionTimer(DatagramPacket packet, int seq) {
            this.packetStore = packet;
            this.seqNumStore = seq;
        }

        public void run() {
            System.out.println("\nMensagem id " + seqNumStore + " deu timeout, reenviando.");

            try {
                socket.send(packetStore);
            } catch (IOException e) {}
        }
    }

    // Método utilizado para iniciar o timer de um pacote 'packet' com id 'id'
    static void startTimer(DatagramPacket packet, int id) {
        Timer timer = new Timer(); // Cria um objeto Timer
        RetransmissionTimer task = new RetransmissionTimer(packet, id); // Cria um objeto que será a Task executada caso o timer se esgote
        timers.put(id, timer); // Adiciona o timer no buffer principal
        timer.schedule(task, 5000); // Programa a task caso o timer de 5 segundos se esgote
    }

    public static void main(String[] args) throws UnknownHostException, SocketException, IOException, InterruptedException {
        Scanner scan = new Scanner(System.in);
        
        System.out.print("Digite o IP do Receiver [127.0.0.1]:");
        socket = new DatagramSocket();

        // Aqui é solicitado o endereço IP da máquina que estamos nos comunicando
        String ipStr = scan.nextLine();
        InetAddress ipDestino = InetAddress.getByName(ipStr.isEmpty() ? "127.0.0.1" : ipStr); // Com o operador ternário, caso deixado em branco, o default é o localhost 127.0.0.1
        
        // Porta de destino para comunicação definida aqui. Para o host Sender que inicia a comunicação preferi manter uma porta de destino fixa,
        // sem solicitar ao usuário sempre que rodasse o código. 
        int destPort = 12345; 
        
        // A mensagem inicial tem pacote com ID=0, sendo incrementado a cada novo pacote
        int idAtual = 0;

        // Thread que irá esperar os ACKs do host Receiver é iniciada aqui
        Thread thread = new Listener();
        thread.start();
        
        // Estrutura de dados HashMap criada para auxiliar na simulação de envio de pacotes fora de ordem
        // Ela armezena os pacotes que foram "enviados" (sem rodar o socket.send()), para que quando um pacote normal for enviado possamos esvaziar essa lista enviando corretamente
        // HashMap utilizado para poder armazenar o ID que será utilizado no Timer dos pacotes enviados posteriormente
        Map<Integer, DatagramPacket> pacotesCriados = new HashMap<>();
        
        while(true) {
            // Digitar a mensagem que deseja enviar, não coloquei um print de auxílio pois o comportamento assíncrono com as Threads bagunça o terminal
            String mensagem = scan.nextLine();

            System.out.println("\nDigite o tipo de envio (normal, duplicada, lento, perda ou fora de ordem):\n");
            
            String tipoEnvio = scan.nextLine();
            
            // Constrói o segmento confiável para que possa ser enviado via UDP com o DatagramPacket
            SegmentoConfiavel segmento = new SegmentoConfiavel(idAtual, mensagem);
            byte[] dados = segmento.toBytes();
            DatagramPacket packet = new DatagramPacket(dados, dados.length, ipDestino, destPort);
            

            switch (tipoEnvio.toLowerCase()) {
                case "normal": // Normal: envio ocorre normalmente
                    socket.send(packet);
                    startTimer(packet, segmento.getID()); // Sempre que envio um pacote com socket.send(), inicio o timer logo em seguida
                    System.out.println("\nMensagem \"" + mensagem + "\" enviada como [normal] com id " + segmento.getID());

                    // Verifica os pacotes armazenados, para o caso de teste do "Fora de Ordem"
                    if (!pacotesCriados.isEmpty()) {
                        for(Map.Entry<Integer, DatagramPacket> p : pacotesCriados.entrySet()) {
                            socket.send(p.getValue());
                            startTimer(p.getValue(), p.getKey());
                            Thread.sleep(500);
                        }
                        pacotesCriados.clear();
                    }
                    break;
            
                case "duplicada": // Duplicada: envia dois pacotes 
                    socket.send(packet);
                    startTimer(packet, segmento.getID()); // Apenas um timer pois o segundo pacote enviado é apenas para simular o receiver recebendo duplicado
                    socket.send(packet);
                    
                    System.out.println("\nMensagem \"" + mensagem + "\" enviada como [duplicada] com id " + segmento.getID());
                    break;
                
                case "lento": // Lento: pacote é enviado lentamente, simula o caso de "Timeout Prematuro"
                    startTimer(packet, segmento.getID()); // Inicio um Timer para o pacote atual, quando der timeout o RetransmissionTimer vai enviar novamente
                    // Inicio o SlowSender, que só vai enviar o pacote depois de 10 segundos
                    new SlowSender(packet).start();
                    
                    System.out.println("\nMensagem \"" + mensagem + "\" enviada como [lento] com id " + segmento.getID());
                    break;
                
                case "perda": // Com perda: o pacote não chega e estoura o timer
                    startTimer(packet, segmento.getID()); // Utilizo apenas o método com startTimer para que ocorra o timeout, sem enviar com socket.send()
                    
                    System.out.println("\nMensagem \"" + mensagem + "\" enviada como [perda] com id " + segmento.getID());
                    break;
                
                case "fora de ordem": // Fora de Ordem: simula o caso de pacotes fora de ordem
                    pacotesCriados.put(segmento.getID(), packet); // Apenas adiciono os pacotes no HashMap auxiliar e não os envio, o HashMap é esvaziado assim que um pacote normal é enviado
                    
                    System.out.println("\nMensagem \"" + mensagem + "\" enviada como [fora de ordem] com id " + segmento.getID());
                    break;
            }
            
            idAtual++;
         
            // Fecha a conexão
            if(mensagem.equals("quit")) {
                System.out.println("Fechando conexão");
                scan.close();
                socket.close();
                System.exit(0);
            }
        }
    }
}
