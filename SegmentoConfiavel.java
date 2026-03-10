import java.io.Serializable;
import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.io.IOException;

public class SegmentoConfiavel implements Serializable {

    private int seqNum;
    private String mensagem;
    private boolean ACK;

    public SegmentoConfiavel(int seqNum, String mensagem) {
        this.seqNum = seqNum;
        this.mensagem = mensagem;
        this.ACK = false;
    }

    public SegmentoConfiavel(int seqNum) {
        this.seqNum = seqNum;
        this.ACK = true;
    }

    public int getSeqNum() {
        return this.seqNum;
    }

    public String getMensagem() {
        return this.mensagem;
    }

    public boolean isAck() {
        return this.ACK;
    }

    public byte[] toBytes() throws IOException {
        ByteArrayOutputStream byteOutput = new ByteArrayOutputStream();
        ObjectOutputStream objectOutput = new ObjectOutputStream(byteOutput);
        objectOutput.writeObject(this);
        return byteOutput.toByteArray();
    }

    public static SegmentoConfiavel fromBytes(byte[] bytes) throws IOException, ClassNotFoundException {
        ByteArrayInputStream byteInput = new ByteArrayInputStream(bytes);
        ObjectInputStream objectInput = new ObjectInputStream(byteInput);
        return (SegmentoConfiavel) objectInput.readObject();
    }
}