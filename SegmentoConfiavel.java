import java.io.Serializable;
import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.io.IOException;

public class SegmentoConfiavel implements Serializable {

    private int id;
    private String mensagem;
    private boolean ehAck;
    private String tipoEnvio;

    public SegmentoConfiavel(int id, String mensagem, String tipoEnvio) {
        this.id = id;
        this.mensagem = mensagem;
        this.ehAck = false;
        this.tipoEnvio = tipoEnvio;
    }

    public SegmentoConfiavel(int id) {
        this.id = id;
        this.ehAck = true;
    }

    public int getID() {
        return this.id;
    }

    public String getMensagem() {
        return this.mensagem;
    }

    public boolean isAck() {
        return this.ehAck;
    }

    public String tipoEnvio() {
        return this.tipoEnvio;
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