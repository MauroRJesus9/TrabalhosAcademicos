package isec.tppd.server.javarmi;

import java.io.Serial;
import java.io.Serializable;
import java.net.InetAddress;

public class InfoSvRMI implements Serializable {
    @Serial
    private final static long serialVersionUID = 4L;
    private InetAddress ip;
    private int port_tcp;
    private int port_udp;
    private int carga;
    private int tempo_dec_ult_heartbeat; //tempo decorrido desde a receção do último heartbeat

    public InfoSvRMI(InetAddress ip, int port_tcp, int port_udp, int carga, int tempo_dec_ult_heartbeat) {
        this.ip = ip;
        this.port_tcp = port_tcp;
        this.port_udp = port_udp;
        this.carga = carga;
        this.tempo_dec_ult_heartbeat = tempo_dec_ult_heartbeat;
    }

    public InetAddress getIp() {
        return ip;
    }

    public void setIp(InetAddress ip) {
        this.ip = ip;
    }

    public int getPort_tcp() {
        return port_tcp;
    }

    public void setPort_tcp(int port_tcp) {
        this.port_tcp = port_tcp;
    }

    public int getPort_udp() {
        return port_udp;
    }

    public void setPort_udp(int port_udp) {
        this.port_udp = port_udp;
    }

    public int getCarga() {
        return carga;
    }

    public void setCarga(int carga) {
        this.carga = carga;
    }

    public int getTempo_dec_ult_heartbeat() {
        return tempo_dec_ult_heartbeat;
    }

    public void setTempo_dec_ult_heartbeat(int tempo_dec_ult_heartbeat) {
        this.tempo_dec_ult_heartbeat = tempo_dec_ult_heartbeat;
    }

    @Override
    public String toString() {
        return "Servidor: \n Endereço IP: " + ip
                +"\n Porto UDP: " +port_udp + " Porto TCP: " + port_tcp
                + "\n Clientes Ativos: " + carga + " \n Tempo decorrido desde o último heartbeat: "
                + tempo_dec_ult_heartbeat + " segundos.";
    }
}
