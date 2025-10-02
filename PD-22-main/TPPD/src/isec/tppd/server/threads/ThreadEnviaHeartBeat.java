package isec.tppd.server.threads;

import isec.tppd.server.Servidor;
import isec.tppd.server.classesaux.HeartBeat;
import isec.tppd.server.classesaux.ServerInfo;
import isec.tppd.server.javarmi.ClientRemoteInterface;
import isec.tppd.server.javarmi.InfoSvRMI;
import isec.tppd.server.javarmi.JavaRmiInterface;

import java.io.*;
import java.net.*;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class ThreadEnviaHeartBeat extends Thread implements JavaRmiInterface{
    private final static String network_interface = "eth0"; //wlan0 ou eth0
    private MulticastSocket ms;
    private InetAddress ipGroup;
    private SocketAddress sa;
    private NetworkInterface ni;
    private final int port = 4004;
    private ArrayList<ServerInfo> serverList;
    private ServerSocket serverSocket;
    private HeartBeat heartBeat;
    private ArrayList<HeartBeat> lstHeartbeats;
    private String bd_url;
    private ArrayList<String> comandosSQL;
    private Servidor servidor;
    private ThreadRecebeHeartbeat recebeHeartbeat;

    public ThreadEnviaHeartBeat(ServerSocket serverSocket, ArrayList<ServerInfo> serverList, HeartBeat heartBeat, String bd_url,ArrayList<String> comandosSQL, Servidor servidor) throws IOException, RemoteException {
        ms = new MulticastSocket(4004);
        ipGroup = InetAddress.getByName("239.39.39.39");
        sa = new InetSocketAddress(ipGroup,4004);
        ni = NetworkInterface.getByName(network_interface);

        ms.joinGroup(sa,ni);
        this.serverList = serverList;
        this.serverSocket = serverSocket;
        this.heartBeat = heartBeat;
        lstHeartbeats = new ArrayList<>();
        this.bd_url = bd_url;
        this.comandosSQL = comandosSQL;
        this.servidor = servidor;
    }

    @Override
    public ArrayList<InfoSvRMI> getServer() throws IOException {
        ArrayList<InfoSvRMI> infoSvRMIS = new ArrayList<>();
        synchronized (heartBeat) {
            infoSvRMIS.add(new InfoSvRMI(heartBeat.getIp(), heartBeat.getPort_tcp(), heartBeat.getPort_udp(), heartBeat.getTotal_ligacoes(), 0));
        }
        infoSvRMIS.addAll(recebeHeartbeat.getServer());

        return infoSvRMIS;
    }


    @Override
    public void run(){
        DatagramPacket dp;
        recebeHeartbeat = new ThreadRecebeHeartbeat(ms,serverList, lstHeartbeats,heartBeat, bd_url,comandosSQL);
        recebeHeartbeat.start();
        try {
            Thread.sleep(30*1000); //Espera 30 segundos no arranque para receber heartbeats de outros servers ativos
        } catch (InterruptedException e) {
            System.out.println("Sleep de 30 segs. da thread interrompida ");
            //throw new RuntimeException(e);
        }
        if(heartBeat.getVersao_bd() == 0 && lstHeartbeats.size() > 0){
            verificaBDVersao();
        }
        if(lstHeartbeats.size() == 0){ //Caso não tenha recebido nenhum heartbeat
            heartBeat.setVersao_bd(1);
        }
        System.out.println("Inicio de envio de Heartbeats.");
        try{
            while(true) {
                try {
                    Thread.sleep(10 * 1000); //A cada 10 segundos envia um heartbeat
                } catch (InterruptedException e) {
                    System.out.println("Sleep Thread interrompida devido a uma nova alteração");
                }

                //Aciona callback de eventos
                //servidor.checkForEvents();

                ByteArrayOutputStream bOut = new ByteArrayOutputStream();
                ObjectOutputStream oos = new ObjectOutputStream(bOut);

                synchronized (heartBeat){
                    Calendar cal = Calendar.getInstance();
                    heartBeat.setUltimo_heartbeat(cal.get(Calendar.HOUR)+":"+cal.get(Calendar.MINUTE)+":"+cal.get(Calendar.SECOND)); //Atualiza o valor do último heartbeat enviado
                    oos.writeUnshared(heartBeat); //Envia o seu heartbeat para serem adicionados à lista de servidores e que tbm é aproveitado para a lista de servers para os cliente
                    oos.flush();
                    dp = new DatagramPacket(bOut.toByteArray(), bOut.size(),ipGroup, port);
                }

                ms.send(dp); //Não precisa de synchronized pq a DatagramPacket é ThreadSafe


                synchronized (heartBeat){
                    if(heartBeat.getStatus().equals("Prepare")){
                        //System.out.println("A criar a thread +  port: " + heartBeat.getPortUDPAuto());
                        if(lstHeartbeats.size() > 0){ //Apenas cria a thread para esperar confirmações caso haja mais servidores no cluster
                            ThreadHeartbeatPrepareUDP threadHeartbeatPrepareUDP = new ThreadHeartbeatPrepareUDP(heartBeat,lstHeartbeats.size(),currentThread());
                            threadHeartbeatPrepareUDP.start();
                        }
                    }else if(heartBeat.getStatus().equals("Abort")){
                        heartBeat.setStatus("null"); //Elimina os status do abort após enviar o cmd Abort
                    }
                }
            }
        }catch (IOException e){
            //Acaba a thread porque deixou de receber mensagens do socket Multicast
            System.out.println("Impossibilidade de aceder ao conteudo da mensagem recebida! " + e);
        }

        try {
            ms.leaveGroup(sa,ni);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        ms.close();
        try {
            recebeHeartbeat.join(); //Fecha a thread para receber o HeartBeat
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void verificaBDVersao(){ //Pedir a versão da base de dados mais recente
        ArrayList<HeartBeat> lstHBversao = new ArrayList<>(); //Lista de heartbeats com maior versão
        ArrayList<Integer> versoesBD = new ArrayList<>();
        ArrayList<Integer> quantClientes = new ArrayList<>();
        for(HeartBeat ht : lstHeartbeats){
            versoesBD.add(ht.getVersao_bd());
        }
        int versao = Collections.max(versoesBD); //Guardar a maior versão da BD atual
        for(HeartBeat ht: lstHeartbeats){
            if(ht.getVersao_bd() == versao){
                lstHBversao.add(ht);
            }
        }
        for(HeartBeat ht : lstHBversao){
            quantClientes.add(ht.getTotal_ligacoes());
        }
        int minClientes = Collections.min(quantClientes);
        HeartBeat heartBeat1 = null;
        for(HeartBeat ht: lstHBversao){
            if(ht.getVersao_bd() == versao && ht.getTotal_ligacoes() == minClientes){ //Guardar o servidor que tem a maior versão e o menor nr de clientes ligados
                heartBeat1 = ht;
                break;
            }
        }

        //Request BD - Via TCP
        try{
            Socket serverSocket = new Socket(InetAddress.getByName("127.0.0.1"),heartBeat1.getPort_tcp());
            System.out.println("Pedido de dados de BD + Port:" + heartBeat1.getPort_tcp());
            ObjectOutputStream oos = new ObjectOutputStream(serverSocket.getOutputStream());
            oos.writeObject("servidor"); //Informando o servidor que se trata de um servidor que precisa da versão atual da base de dados
            oos.flush();
            Thread.sleep(500);
            String comandoSQL;
            ObjectInputStream iis = new ObjectInputStream(serverSocket.getInputStream());
            int versao_bd = (int) iis.readObject();
            heartBeat.setVersao_bd(versao_bd);
            do{
                comandoSQL = (String) iis.readObject();
                if(!comandoSQL.equalsIgnoreCase("acabou")){
                    comandosSQL.add(comandoSQL);
                }
            }while(!comandoSQL.equalsIgnoreCase("acabou"));
            serverSocket.close();
        }catch (IOException e){
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        //Executa queries SQL
        try{
            Connection dbConn = DriverManager.getConnection(bd_url);
            Statement statement = dbConn.createStatement();
            //System.out.println(comandosSQL.toString());
            for(String querySql : comandosSQL){
                statement.executeUpdate(querySql);
            }
            statement.close();
            dbConn.close();
        }catch (SQLException e){
            e.printStackTrace();
            System.out.println("Erro a executar a query sql.");
        }
    }


}
