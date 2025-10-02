package isec.tppd.server.threads;

import isec.tppd.server.Servidor;
import isec.tppd.server.classesaux.HeartBeat;
import isec.tppd.server.classesaux.ServerInfo;
import isec.tppd.server.classesaux.ServerList;

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.util.ArrayList;

public class ThreadConectaCliente extends Thread{
    private DatagramSocket socketUDP;
    private ArrayList<ServerInfo> serverList;
    private boolean keepGoing;
    private HeartBeat heartBeat;
    private Servidor servidor;

    public ThreadConectaCliente(DatagramSocket socketUDP, ArrayList<ServerInfo> serverList, HeartBeat heartBeat, boolean keepGoing, Servidor servidor){
        this.socketUDP = socketUDP;
        this.serverList = serverList;
        this.keepGoing = keepGoing;
        this.heartBeat = heartBeat;
        this.servidor = servidor;
    }

    @Override
    public void run(){
        DatagramPacket dpRec = new DatagramPacket(new byte[256], 256);
        while(keepGoing){
            try{
                socketUDP.receive(dpRec);
                ObjectInputStream iis = new ObjectInputStream(new ByteArrayInputStream(dpRec.getData(),0,dpRec.getLength()));
                String msgRec = (String) iis.readObject();

                if(msgRec.equalsIgnoreCase("conecta")){
                    ByteArrayOutputStream bOut = new ByteArrayOutputStream();
                    ObjectOutputStream oos = new ObjectOutputStream(bOut);
                    InetAddress ipClient = dpRec.getAddress();
                    int portClient = dpRec.getPort();
                    servidor.sendUpdateCallbackRMI("Novo cliente ligado via UDP : IP: " + ipClient.getHostAddress() + " PORT UDP: " + portClient);
                    //Envia ArrayList ordenado por ordem de carga com Ip:port
                    oos.writeObject(new ServerList(serverList));
                    oos.flush();
                    DatagramPacket dpSend = new DatagramPacket(bOut.toByteArray(), bOut.size(),ipClient, portClient);
                    socketUDP.send(dpSend);
                }
            }catch (SocketTimeoutException e) {
                // timeout exception.
                //System.out.println("Timeout reached!!! " + e);
            } catch (ClassNotFoundException | IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
