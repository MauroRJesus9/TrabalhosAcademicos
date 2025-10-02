package isec.tppd.server;

import isec.tppd.server.classesaux.HeartBeat;
import isec.tppd.server.classesaux.ServerInfo;
import isec.tppd.server.javarmi.ClientRemoteInterface;
import isec.tppd.server.javarmi.InfoSvRMI;
import isec.tppd.server.javarmi.JavaRmiInterface;
import isec.tppd.server.javarmi.ServerRemoteInterface;
import isec.tppd.server.threads.ThreadComTCP;
import isec.tppd.server.threads.ThreadConectaCliente;
import isec.tppd.server.threads.ThreadEnviaHeartBeat;

import java.io.IOException;
import java.net.*;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Servidor extends UnicastRemoteObject implements JavaRmiInterface, ServerRemoteInterface {
    public static final String localhost = "127.0.0.1";
    public static ThreadEnviaHeartBeat threadEnviaHeartBeat;

    private Map<String,ClientRemoteInterface> callbacks = new ConcurrentHashMap<>();

    @Override
    public ArrayList<InfoSvRMI> getServer() throws IOException {
        return threadEnviaHeartBeat.getServer();
    }

    @Override
    public void registerForCallback(String name, ClientRemoteInterface client) throws IOException {
        callbacks.put(name,client);
    }

    @Override
    public void unregisterForCallback(String name) throws IOException {
        callbacks.remove(name);
    }

    @Override
    public void askToCloseService(String name,ClientRemoteInterface client) throws IOException {
        callbacks.remove(name); //Caso não tenha sido removido dos map dos callbacks
        client.closeRMIService();
    }

    public void sendUpdateCallbackRMI(String message){
        for(ClientRemoteInterface callback : callbacks.values()){
            try{
                callback.callback(message);
            }catch (IOException e){
                e.printStackTrace();
            }
        }
    }

    public Servidor() throws RemoteException{ }

    public static void main(String [] args) throws IOException, InterruptedException {
        final int port_serv = Integer.parseInt(args[0]); //Porto do servidor
        InetAddress hostAddress = InetAddress.getByName(localhost);
        final String bd_url = args[1]; //Endereço da base de dados
        boolean keepGoing = true;
        ArrayList<Thread> allThreadsSv = new ArrayList<>();
        ArrayList<ServerInfo> listaServerDados = new ArrayList<>();
        ArrayList<String> comandosSQL = new ArrayList<>(); //Guarda todos os comandos SQL executados

        //Java RMI
        String rmiservice = "SHOW_SERVICE_" + port_serv;
        Servidor serv = new Servidor();
        Registry registry;
        try{
            registry = LocateRegistry.createRegistry(Registry.REGISTRY_PORT);
        }catch (RemoteException e){
            registry = LocateRegistry.getRegistry(Registry.REGISTRY_PORT); //Caso o Serviço RMI ja tenha sido lançado p outro server
        }
        registry.rebind(rmiservice,serv);

        ServerSocket serverSocket = new ServerSocket(0); //TCP
        int portTCP = serverSocket.getLocalPort();

        //Dados do servidor (Clientes & Heartbeat)
        ServerInfo dadosServer = new ServerInfo(hostAddress, portTCP); //Dados a ser fornecido ao cliente após 1º conexão
        listaServerDados.add(dadosServer);
        HeartBeat heartBeat = new HeartBeat(hostAddress,portTCP,port_serv,true);

        //HeartBeat
        threadEnviaHeartBeat = new ThreadEnviaHeartBeat(serverSocket,listaServerDados, heartBeat,bd_url,comandosSQL, serv);
        threadEnviaHeartBeat.start(); //Inicia o canal Multicast / HeartBeat
        allThreadsSv.add(threadEnviaHeartBeat);

        //UDP
        DatagramSocket socketUDP = new DatagramSocket(port_serv);
        socketUDP.setSoTimeout(10000);

        //Rececao do Cliente
        ThreadConectaCliente threadConectaCliente = new ThreadConectaCliente(socketUDP, listaServerDados, heartBeat,keepGoing, serv);
        threadConectaCliente.start();
        allThreadsSv.add(threadConectaCliente);

        //Comunicação com o Cliente TCP
        ThreadComTCP threadComTCP = new ThreadComTCP(serverSocket,keepGoing,bd_url, heartBeat, dadosServer, listaServerDados,comandosSQL,threadEnviaHeartBeat, serv);
        threadComTCP.start();
        allThreadsSv.add(threadComTCP);

        while(keepGoing){
            
        }

        for(Thread t : allThreadsSv){
            t.join();
        }

        serverSocket.close();
        socketUDP.close();
    }

}
