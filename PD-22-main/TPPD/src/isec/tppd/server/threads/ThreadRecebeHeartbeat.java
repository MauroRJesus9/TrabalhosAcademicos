package isec.tppd.server.threads;

import isec.tppd.server.classesaux.HeartBeat;
import isec.tppd.server.classesaux.ServerInfo;
import isec.tppd.server.javarmi.InfoSvRMI;
import isec.tppd.server.javarmi.JavaRmiInterface;

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class ThreadRecebeHeartbeat extends Thread implements JavaRmiInterface {
    private MulticastSocket ms;
    private ArrayList<ServerInfo> serverList;
    private ArrayList<HeartBeat> lstHeartbeats;
    private HeartBeat heartBeat;
    private String bd_url;
    private ArrayList<String> comandosSQL;


    public ThreadRecebeHeartbeat(MulticastSocket ms, ArrayList<ServerInfo> serverList, ArrayList<HeartBeat> lstHeartbeats,HeartBeat heartBeat, String bd_url,ArrayList<String> comandosSQL) {
        this.ms = ms;
        this.serverList = serverList;
        this.lstHeartbeats = lstHeartbeats;
        this.heartBeat = heartBeat;
        this.bd_url = bd_url;
        this.comandosSQL = comandosSQL;
    }

    @Override
    public ArrayList<InfoSvRMI> getServer() throws IOException {
        ArrayList<InfoSvRMI> infoSvRMIS = new ArrayList<>();
        synchronized (lstHeartbeats) {
            for (HeartBeat hearbeat : lstHeartbeats) {
                //Tempo passado após o envio do ultimo heartbeat
                Calendar cal = Calendar.getInstance();
                SimpleDateFormat sdf = new SimpleDateFormat("hh:mm:ss");
                if (heartBeat.getUltimo_heartbeat() != null) {
                    String aux = heartBeat.getUltimo_heartbeat();
                    Calendar cal2 = Calendar.getInstance();
                    try {
                        cal2.setTime(sdf.parse(aux));// all done
                    } catch (ParseException e) {
                        return null;
                    }
                    long ult_heartbeat_segundos = cal2.get(Calendar.HOUR) * 3600 + cal2.get(Calendar.MINUTE) * 60 + cal2.get(Calendar.SECOND);
                    long hora_atual_segundos = cal.get(Calendar.HOUR) * 3600 + cal.get(Calendar.MINUTE) * 60 + cal.get(Calendar.SECOND);
                    infoSvRMIS.add(new InfoSvRMI(hearbeat.getIp(), hearbeat.getPort_tcp(), hearbeat.getPort_udp(), hearbeat.getTotal_ligacoes(), (int) (hora_atual_segundos - ult_heartbeat_segundos)));
                } else {
                    infoSvRMIS.add(new InfoSvRMI(hearbeat.getIp(), hearbeat.getPort_tcp(), hearbeat.getPort_udp(), hearbeat.getTotal_ligacoes(), 0));
                }
            }
        }
        return infoSvRMIS;
    }

    @Override
    public void run(){
        DatagramPacket dp;
        Object obj;
        HeartBeat heartBeatAux = null;

        while(true){
            dp = new DatagramPacket(new byte[1024],1024);
            try {
                ms.receive(dp);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            try(ObjectInputStream iis = new ObjectInputStream(new ByteArrayInputStream(dp.getData(),0,dp.getLength()))){

                obj = iis.readObject();

                if(obj instanceof HeartBeat){
                    heartBeatAux = (HeartBeat) obj;
                }

                if(heartBeatAux != null && heartBeatAux.getStatus().equals("Prepare") && lstHeartbeats.size() == 0){ //Não há servidores ativos para além do original
                    System.out.println("Estou sozinho no cluster, confirmei as alterações sozinho.");
                    heartBeat.setPrepareConfirmed(true);
                    heartBeat.setStatus("Commit");
                    heartBeat.setVersao_bd(heartBeat.getVersao_bd() + 1); //Incrementa base de dados
                }
                verificaHeartbeat();
                if(heartBeatAux != null && heartBeatAux.getPort_tcp() != serverList.get(0).getPort()){
                    //Verifica estado de todos os heartbeats recebidos (ultimo heartbeat recebido à -35segs)

                    //Adiciona às listas ou atualiza
                    atualizaLstHeartbeat(lstHeartbeats,heartBeatAux);
                    ServerInfo serverInfoAux = new ServerInfo(dp.getAddress(),heartBeatAux.getPort_tcp());
                    atualizaLstServers(serverList,serverInfoAux);
                    System.out.println("Recebi um heartbeat " + heartBeatAux.getPort_tcp() + " / "+ dp.getAddress() + ": Clientes ativos:" + heartBeatAux.getTotal_ligacoes()
                                            + " : Status:" + heartBeatAux.getStatus() + " Versão BD: " + heartBeatAux.getVersao_bd());

                    //Verifica heartbeat recebido e toma ações se necessário
                    if(heartBeatAux.getStatus().equals("Prepare")){ //Recebe um heartbeat com o estado prepare
                        //Envia confirmação ao port UDP recebido
                        Thread.sleep(1000);
                        DatagramSocket ds = new DatagramSocket(heartBeat.getPortUDPAuto());
                        ByteArrayOutputStream bOut = new ByteArrayOutputStream();
                        ObjectOutputStream oos = new ObjectOutputStream(bOut);
                        synchronized (heartBeat){
                            heartBeat.setPrepareConfirm("confirma");
                            oos.writeObject(heartBeat.getPrepareConfirm()); //Confirmação de pedido de Prepare
                            oos.flush();
                        }
                        DatagramPacket dpSend = new DatagramPacket(bOut.toByteArray(), bOut.size(), InetAddress.getByName("localhost"), heartBeatAux.getPortUDPAuto());
                        ds.send(dpSend);
                        ds.close();
                        //System.out.println("Enviei confirmação + port:" + heartBeatAux.getPortUDPAuto());
                    }else if(heartBeatAux.getStatus().equals("Commit") && heartBeatAux.isPrepareConfirmed()){ //Faz alterações na base de dados
                        synchronized (heartBeat){
                            heartBeat.setPrepareConfirm(""); //Volta a deixar a confirmação a false
                            executaSqlQueries(heartBeatAux.getQuery_sql()); //Efectua os updates
                            heartBeat.setVersao_bd(heartBeatAux.getVersao_bd()); //Atualiza a versao da bd
                        }
                    }else if(heartBeatAux.getStatus().equals("Abort")){
                        synchronized (heartBeat){
                            heartBeat.setPrepareConfirm(""); //Volta a deixar a confirmação a false
                            heartBeat.setQuery_sql("null");
                        }
                    }
                }

            }catch (ClassNotFoundException e){
                System.out.println("Erro ao receber dados");
                break;
            }catch (IOException e){
                e.printStackTrace();
                System.out.println("Erro a receber dados.");
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void atualizaLstHeartbeat(ArrayList<HeartBeat> lstHeartbeats, HeartBeat heartBeatAux){
        boolean aux = false;
        if(heartBeatAux != null){
            for(HeartBeat hb : lstHeartbeats){
                if(hb.getPort_tcp() == heartBeatAux.getPort_tcp()){ //O servidor já estava na lista de heartbeats por isso atualiza dados
                    aux = true;
                    hb.setTotal_ligacoes(heartBeatAux.getTotal_ligacoes());
                    hb.setDisponivel(heartBeatAux.isDisponivel());
                    hb.setStatus(heartBeatAux.getStatus());
                    hb.setVersao_bd(heartBeatAux.getVersao_bd());
                    hb.setUltimo_heartbeat(heartBeatAux.getUltimo_heartbeat());
                }
            }
            if(!aux){
                lstHeartbeats.add(heartBeatAux);
            }
        }
    }

    public void atualizaLstServers(ArrayList<ServerInfo> serverList,ServerInfo serverInfoAux){
        boolean aux = false;
        if(serverInfoAux != null){
            for(ServerInfo serverInfo: serverList){
                if(serverInfo.getPort() == serverInfoAux.getPort()){ //O servidor já estava na lista de servidores por isso atualiza dados
                    aux = true;
                    serverInfo.setQuant(serverInfo.getQuant());
                }
            }
        }
        if(!aux){
            serverList.add(serverInfoAux);
        }
    }

    public void executaSqlQueries(String querySql)  {
        //System.out.println(querySql);
        try{
            Connection dbConn = DriverManager.getConnection(bd_url);
            Statement statement = dbConn.createStatement();
            statement.executeUpdate(querySql);
            statement.close();
            dbConn.close();
            synchronized (comandosSQL){
                comandosSQL.add(querySql);
            }
        }catch (SQLException e){
            System.out.println("Erro a executar a query sql.");
        }
    }

    private void verificaHeartbeat(){ //Verificar se o último heartbeat recebido dos outros servidores não foram há mais de 35segs
        Calendar cal = Calendar.getInstance(); //Hora atual
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm:ss");
        long hora_atual_segundos= cal.get(Calendar.HOUR)*3600 + cal.get(Calendar.MINUTE) * 60 + cal.get(Calendar.SECOND);
        long hora_atual_ult; //Segundos do ultimo heartbeat recebido
        synchronized (lstHeartbeats) {
            for (HeartBeat ht : lstHeartbeats) {
                String aux = ht.getUltimo_heartbeat();
                Calendar cal2 = Calendar.getInstance();
                try {
                    cal2.setTime(sdf.parse(aux));// all done
                } catch (ParseException e) {
                    return;
                }
                hora_atual_ult = cal2.get(Calendar.HOUR)*3600 + cal2.get(Calendar.MINUTE) * 60 + cal2.get(Calendar.SECOND) + 35;
                if (hora_atual_segundos > hora_atual_ult) { //O último heartbeat enviado foi há mais de 35 segundos (-1)
                    for (ServerInfo sv : serverList) {
                        if (sv.getPort() == ht.getPort_tcp()) {
                            serverList.remove(sv); //Remove da lista de servidores disponíveis
                            break;
                        }
                    }
                    System.out.println("O servidor port: " + ht.getPort_tcp() + " foi removido por estar inativo há mais de 35 segundos.");
                    lstHeartbeats.remove(ht); //Remove da lista dos heartsbeats
                    break;
                }
            }
        }
    }


}
