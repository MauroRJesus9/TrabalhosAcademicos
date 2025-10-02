package isec.tppd.server.classesaux;

import java.io.Serializable;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Calendar;

public class HeartBeat implements Serializable {
    private static final long serialVersionUID = 000002L;
    private InetAddress ip;
    private int versao_bd;
    private int port_tcp;
    private int port_udp;
    private boolean disponivel;
    private int total_ligacoes;
    private String status; //Mensagem de estado: Prepare, Commit ou Abort
    private String query_sql;
    private int portUDPAuto; //Port automático UDP para receber confirmações
    private String prepareConfirm; //Confirmação enviada pelos servidores que recebem o comando Prepare
    private boolean prepareConfirmed; //Confirmação enviada pelo servidor que emitiu o comando Prepare
    private String ultimo_heartbeat; //Regista HH:MM:ss do ultimo heartbeat enviado

    public HeartBeat(InetAddress ip, int port_tcp,int port_udp, boolean disponivel) {
        this.ip = ip;
        this.port_tcp = port_tcp;
        this.disponivel = disponivel;
        this.port_udp = port_udp;
        prepareConfirm = "";
        prepareConfirmed = false;
        status = "null";
        query_sql = "";
        versao_bd = 0;
        try{
            DatagramSocket ds = new DatagramSocket(0);
            ds.setBroadcast(true);
            portUDPAuto = ds.getLocalPort();
            ds.close();
        }catch (SocketException e){
            e.printStackTrace();
        }
    }

    public InetAddress getIp() {
        return ip;
    }

    public void setIp(InetAddress ip) {
        this.ip = ip;
    }

    public int getVersao_bd() {
        return versao_bd;
    }

    public void setVersao_bd(int versao_bd) {
        this.versao_bd = versao_bd;
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

    public boolean isDisponivel() {
        return disponivel;
    }

    public void setDisponivel(boolean disponivel) {
        this.disponivel = disponivel;
    }

    public int getTotal_ligacoes() {
        return total_ligacoes;
    }

    public void setTotal_ligacoes(int total_ligacoes) {
        this.total_ligacoes = total_ligacoes;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getQuery_sql() {
        return query_sql;
    }

    public void setQuery_sql(String query_sql) {
        this.query_sql = query_sql;
    }

    public int getPortUDPAuto() {
        return portUDPAuto;
    }

    public void setPortUDPAuto(int portUDPAuto) {
        this.portUDPAuto = portUDPAuto;
    }

    public boolean isPrepareConfirmed() {
        return prepareConfirmed;
    }

    public void setPrepareConfirmed(boolean prepareConfirmed) {
        this.prepareConfirmed = prepareConfirmed;
    }

    public String getPrepareConfirm() {
        return prepareConfirm;
    }

    public void setPrepareConfirm(String prepareConfirm) {
        this.prepareConfirm = prepareConfirm;
    }

    public String getUltimo_heartbeat() {
        return ultimo_heartbeat;
    }

    public void setUltimo_heartbeat(String ultimo_heartbeat) {
        this.ultimo_heartbeat = ultimo_heartbeat;
    }
}
