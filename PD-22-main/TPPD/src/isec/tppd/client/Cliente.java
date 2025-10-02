package isec.tppd.client;

import isec.tppd.client.threads.ThreadComServidor;
import isec.tppd.server.classesaux.ServerList;

import java.io.*;
import java.net.*;

public class Cliente {
    public static void main(String []args) throws IOException, InterruptedException {
        Socket cliSocket = null;
        ObjectOutputStream oos = null;

        DatagramSocket ds = new DatagramSocket(0);
        ds.setBroadcast(true);
        //Servidor predefinido
        InetAddress ipServer = InetAddress.getByName(args[0]);
        int portServer = Integer.parseInt(args[1]);
        ds.setSoTimeout(3000); //Definir timeout para se tentar ligar aos servidores
        //System.out.println("Connected to " + cliSocket.getInetAddress() + " Port: " + cliSocket.getPort() + " Client ip: " + cliSocket.getLocalAddress());


        //Pedido de Conexão UDP e receber lista
        ServerList lstServidores = null;
        lstServidores = pedirListaServidores(ds,ipServer,portServer,oos,lstServidores);
        if(lstServidores != null) {
            System.out.println(lstServidores.toString());

            lstServidores.organizaLista(); //Organiza a lista de servidores
            ThreadComServidor threadComServidor = null;
            for (int i = 0; i < lstServidores.getServerList().size(); i++) {
                try {
                    lstServidores.organizaLista(); //Organiza a lista de servidores
                    cliSocket = new Socket(lstServidores.getServerList().get(i).getIp(), lstServidores.getServerList().get(i).getPort());
                    cliSocket.setSoTimeout(10000); //Assim que se conectar a um servidor passa para um timeout de espera default:10 segs
                    oos = new ObjectOutputStream(cliSocket.getOutputStream());
                    oos.writeObject("Cliente"); //Informando o servidor que se trata de um cliente
                    oos.flush();
                    threadComServidor = new ThreadComServidor(cliSocket, Thread.currentThread());
                    threadComServidor.start();
                    try {
                        Thread.sleep(Long.MAX_VALUE);
                    } catch (InterruptedException e) {
                        threadComServidor.join();
                    }
                } catch (InterruptedException | IOException e) {
                    try {
                        cliSocket.close();
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                }
                //Volta a pedir a lista de servidores caso esta se altere
                lstServidores = pedirListaServidores(ds, ipServer, portServer, oos, lstServidores);
                lstServidores.organizaLista(); //Organiza a lista de servidores
            }
        }
        //Fechar socket UDP
        ds.close();

        System.out.println("Cliente a fechar...");
    }

    private static ServerList pedirListaServidores(DatagramSocket ds, InetAddress ipServer, int portServer, ObjectOutputStream oos, ServerList lstServidores){
        ByteArrayOutputStream bOut = null;
        ObjectInputStream iis = null;
        DatagramPacket dpSend = null;
        DatagramPacket dpRec = null;

        try { //Voltar a pedir a lista caso esta se altere
            bOut = new ByteArrayOutputStream();
            oos = new ObjectOutputStream(bOut);
            oos.writeObject("Conecta"); //Pedido de conexão ao servidor
            oos.flush();

            dpSend = new DatagramPacket(bOut.toByteArray(), bOut.size(),ipServer,portServer);
            ds.send(dpSend);

            //Receber mensagem do servidor
            dpRec = new DatagramPacket(new byte[1024], 1024);
            ds.receive(dpRec);

            iis = new ObjectInputStream(new ByteArrayInputStream(dpRec.getData(),0,dpRec.getLength()));
            lstServidores = (ServerList) iis.readObject();
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Erro ao ligar ao servidor principal via UDP");
            //throw new RuntimeException(e);
        }
        return lstServidores;
    }
}
