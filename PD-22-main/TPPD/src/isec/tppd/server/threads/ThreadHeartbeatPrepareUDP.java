package isec.tppd.server.threads;

import isec.tppd.server.classesaux.HeartBeat;
import isec.tppd.server.classesaux.ServerList;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.net.SocketTimeoutException;

public class ThreadHeartbeatPrepareUDP extends Thread{
    private HeartBeat heartBeat;
    private int total_servidores;
    private Thread threadEnviaHeartBeats;

    public ThreadHeartbeatPrepareUDP(HeartBeat heartBeat, int total_servidores, Thread threadEnviaHeartBeats) throws SocketException {
        this.heartBeat = heartBeat;
        this.total_servidores = total_servidores;
        this.threadEnviaHeartBeats = threadEnviaHeartBeats;
    }

    @Override
    public void run(){
        int aux = 0;
        String confirma;

        try {
            DatagramSocket ds = new DatagramSocket(heartBeat.getPortUDPAuto());
            ds.setBroadcast(true);
            do{
                DatagramPacket dpRec = new DatagramPacket(new byte[1024], 1024);
                try {
                    ds.receive(dpRec);

                    ObjectInputStream iis = new ObjectInputStream(new ByteArrayInputStream(dpRec.getData(),0,dpRec.getLength()));
                    confirma = (String) iis.readObject();
                    if(confirma.equalsIgnoreCase("confirma")) {
                        aux++;
                    }
                } catch (ClassNotFoundException e) {
                    throw new RuntimeException(e);
                }catch (SocketTimeoutException e){
                    e.printStackTrace();
                    synchronized (heartBeat){
                        heartBeat.setPrepareConfirmed(false);
                        heartBeat.setStatus("Abort");
                        heartBeat.setQuery_sql("");
                        heartBeat.setPrepareConfirm(""); //Só para confirmar
                    }
                    //System.out.println("Abort" + heartBeat.getStatus());
                    threadEnviaHeartBeats.interrupt(); //Para interromper o sleep da Thread
                }catch (IOException e){
                    e.printStackTrace();
                }
            }while(aux < total_servidores);
            ds.close();
        } catch (SocketException e) {
            //e.printStackTrace();
            System.out.println("Erro ao comunicar com os outros servidores");
            try {
                currentThread().join(); //Fecha a thread caso esta já esteja criada
            } catch (InterruptedException ex) {
                throw new RuntimeException(ex);
            }
        }
        if(aux == total_servidores){
            System.out.println("Recebi todas as confirmações - Comando: Commit");
            synchronized (heartBeat){
                heartBeat.setPrepareConfirmed(true);
                heartBeat.setStatus("Commit");
                heartBeat.setVersao_bd(heartBeat.getVersao_bd() + 1); //Incrementa base de dados
            }
            threadEnviaHeartBeats.interrupt(); //Para interromper o sleep da Thread
        }
    }
}
