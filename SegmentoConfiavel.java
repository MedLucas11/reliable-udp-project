import java.io.Serializable;
import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.io.IOException;

// Classe principal do projeto, utilizada para encapsulamento e envio efetivo de mensagens entre os hosts
public class SegmentoConfiavel implements Serializable {

    // Identificador único de cada segmento. Estritamente necessário para o tratamento de duplicados, fora de ordem, timeouts, confirmação de recebimento
    private int id;

    // Mensagem efetiva que é transportada e mostrada na tela/enviada para camada de aplicação
    private String mensagem; 
    
    // Flag para identificar se o pacote é do tipo ACK, para que as rotinas de tratamento da entrega confiável sejam implementadas
    private boolean ACK; 

    // Construtor do segmento com o payload de mensagem
    public SegmentoConfiavel(int id, String mensagem) {
        this.id = id;
        this.mensagem = mensagem;
        this.ACK = false;
    }

    // Construtor de segmento ACK
    public SegmentoConfiavel(int id) {
        this.id = id;
        this.ACK = true;
    }

    public int getID() {
        return this.id;
    }

    public String getMensagem() {
        return this.mensagem;
    }

    public boolean isAck() {
        return this.ACK;
    }

    // Método de serialização que tranforma o objeto SegmentoConfiável em um array de bytes, 
    // permitindo com que seja enviado pela rede com UDP
    public byte[] toBytes() throws IOException {
        ByteArrayOutputStream byteOutput = new ByteArrayOutputStream();
        ObjectOutputStream objectOutput = new ObjectOutputStream(byteOutput);
        objectOutput.writeObject(this);
        return byteOutput.toByteArray();
    }

    // Método de desserialização, reconstrói o SegmentoConfiável original a partir dos bytes recebidos pelo socket
    public static SegmentoConfiavel fromBytes(byte[] bytes) throws IOException, ClassNotFoundException {
        ByteArrayInputStream byteInput = new ByteArrayInputStream(bytes);
        ObjectInputStream objectInput = new ObjectInputStream(byteInput);
        return (SegmentoConfiavel) objectInput.readObject();
    }
}