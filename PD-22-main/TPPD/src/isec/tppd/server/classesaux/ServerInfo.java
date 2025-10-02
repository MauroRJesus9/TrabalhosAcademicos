package isec.tppd.server.classesaux;

import java.io.Serializable;
import java.net.InetAddress;

public class ServerInfo implements Serializable {
    private static final long serialVersionUID = 000003L;
    private InetAddress ip;
    private int port;
    private int quant; //Carga


    public ServerInfo(InetAddress ip, int port) {
        this.ip = ip;
        this.port = port;
    }

    public InetAddress getIp() {
        return ip;
    }

    public void setIp(InetAddress ip) {
        this.ip = ip;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getQuant() {
        return quant;
    }

    public void setQuant(int quant) {
        this.quant = quant;
    }

    @Override
    public String toString() {
        return "ServerInfo{" +
                "ip=" + ip +
                ", port=" + port +
                ", quant=" + quant +
                '}';
    }

}
