package isec.tppd.server.threads;

import isec.tppd.server.Servidor;
import isec.tppd.server.classesaux.HeartBeat;
import isec.tppd.server.classesaux.ServerInfo;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLException;
import java.util.ArrayList;

public class ThreadComTCP extends Thread{
    private ServerSocket serverSocket;
    ArrayList<Thread> allThreads = new ArrayList<>();
    private HeartBeat heartBeat;
    private boolean keepGoing;
    private String bd_url;
    private ServerInfo dadosServer;
    private ArrayList<ServerInfo> serverList;
    private ArrayList<String> comandosSQL;
    private ThreadEnviaHeartBeat threadEnviaHeartBeat;

    private Servidor servidor;

    public ThreadComTCP(ServerSocket serverSocket, boolean keepGoing, String bd_url, HeartBeat heartBeat, ServerInfo dadosServer
            , ArrayList<ServerInfo> serverList,ArrayList<String> comandosSQL, ThreadEnviaHeartBeat threadEnviaHeartBeat, Servidor servidor){
        this.serverSocket = serverSocket;
        this.keepGoing = keepGoing;
        this.bd_url = bd_url;
        this.heartBeat = heartBeat;
        this.dadosServer = dadosServer;
        this.serverList = serverList;
        this.comandosSQL = comandosSQL;
        this.threadEnviaHeartBeat = threadEnviaHeartBeat;
        this.servidor = servidor;
    }


    @Override
    public void run(){
        while(keepGoing){
            try {
                Socket cliSocket = serverSocket.accept();
                ObjectInputStream ois = new ObjectInputStream(cliSocket.getInputStream());
                try{
                    String info = (String) ois.readObject();
                    System.out.println("Info:" + info);

                    //Verificar se é um servidor ou um cliente
                    if(info.equalsIgnoreCase("servidor")){ //Servidor - para pedir base de dados atualizada
                        System.out.println(comandosSQL.toString());
                        ThreadTransfBDOwner ttbd = new ThreadTransfBDOwner(cliSocket,comandosSQL,heartBeat.getVersao_bd());
                        ttbd.start();
                    }else if(info.equalsIgnoreCase("cliente")){ //Cliente - Comunicação utilizador
                        servidor.sendUpdateCallbackRMI("Nova ligação via TCP : IP: " + cliSocket.getInetAddress().getHostAddress() + " PORT TCP: " + cliSocket.getPort());
                        ThreadPrivCliente tc = new ThreadPrivCliente(cliSocket,bd_url,heartBeat,dadosServer, comandosSQL, threadEnviaHeartBeat, servidor);
                        tc.start();
                        allThreads.add(tc);
                    }
                }catch (ClassNotFoundException e){
                    break;
                }
            } catch (IOException | SQLException e) {
                throw new RuntimeException(e);
            }
        }

        for(Thread t : allThreads){
            try {
                t.join();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        try {
            serverSocket.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
