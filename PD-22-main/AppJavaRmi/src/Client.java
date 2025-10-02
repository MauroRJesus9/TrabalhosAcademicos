import isec.tppd.server.javarmi.ClientRemoteInterface;
import isec.tppd.server.javarmi.InfoSvRMI;
import isec.tppd.server.javarmi.JavaRmiInterface;
import isec.tppd.server.javarmi.ServerRemoteInterface;

import java.io.IOException;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Scanner;

public class Client extends UnicastRemoteObject implements ClientRemoteInterface {

    protected Client() throws RemoteException{

    }


    public static void main(String[] args) throws IOException, NotBoundException {
        String server_name = "SHOW_SERVICE_" + args[1];
        Registry r = LocateRegistry.getRegistry(args[0],Registry.REGISTRY_PORT);
        String menu = "\nBem-vindo\n";
        menu += " 1- Receber informações dos servidores ativos.\n 2- Ativar o registo de callbacks.\n 3- Remover o registo de callbacks \n 4- Sair \n";
        int op;
        Scanner scanner = new Scanner(System.in);
        Remote remote = r.lookup(server_name);
        Client client = new Client();
        do{
            System.out.println(menu);
            op = scanner.nextInt();
            scanner.nextLine();
            switch (op){
                case 1 -> {
                    JavaRmiInterface jri = (JavaRmiInterface) remote;
                    for(InfoSvRMI server : jri.getServer()){
                        System.out.println("\n" + server.toString() + "\n");
                    }
                }
                case 2 -> {
                    ServerRemoteInterface remoteInterface = (ServerRemoteInterface)remote;
                    remoteInterface.registerForCallback(args[2],client);
                }
                case 3 ->{
                    ServerRemoteInterface remoteInterface = (ServerRemoteInterface)remote;
                    remoteInterface.unregisterForCallback(args[2]);
                }
            }
        }while(op != 4);
        ServerRemoteInterface remoteInterface = (ServerRemoteInterface)remote;
        remoteInterface.askToCloseService(args[2],client);
        /*String[] serverNames = r.list();

        for(String serverName : serverNames){
            if(serverName.contains("SHOW_SERVICE_")) {
                Remote remote = r.lookup(serverName);
                JavaRmiInterface jri = (JavaRmiInterface) remote;
                System.out.println(jri.getServer().toString());
                ServerRemoteInterface remoteInterface = (ServerRemoteInterface)remote;
                remoteInterface.registerForCallback("client",new Client());
            }
        }*/

    }


    @Override
    public void callback(String message) throws IOException {
        System.out.println(message + "\n");
    }

    @Override
    public void closeRMIService() throws IOException {
        UnicastRemoteObject.unexportObject(this, true); //Fechar o processo remoto ativo
    }
}