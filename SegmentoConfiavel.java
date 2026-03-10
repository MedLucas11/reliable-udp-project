import java.io.Serializable;
import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.io.IOException;

public class SegmentoConfiavel implements Serializable {

    private int id;
    private String mensagem;
    private boolean ACK;

    public SegmentoConfiavel(int id, String mensagem) {
        this.id = id;
        this.mensagem = mensagem;
        this.ACK = false;
    }

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