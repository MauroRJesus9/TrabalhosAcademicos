package isec.tppd.server.threads;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;

public class ThreadTransfBDOwner extends Thread{ //Thread para transferir a versão mais recente da BD a um novo server
    private Socket svSocket;
    private ArrayList<String> comandosSQL;
    private int versao_BD;

    public ThreadTransfBDOwner(Socket svSocket,ArrayList<String> comandosSQL,int versao_BD){
        this.svSocket = svSocket;
        this.comandosSQL = comandosSQL;
        this.versao_BD = versao_BD;
    }

    @Override
    public void run(){
        System.out.println("A enviar dados da BD ao novo servidor");
        try {
            ObjectOutputStream oos = new ObjectOutputStream(svSocket.getOutputStream());
            oos.writeUnshared(versao_BD);
            oos.flush();
            for(int i= 0;i<comandosSQL.size();i++){
                oos.writeUnshared(comandosSQL.get(i));
                oos.flush();
            }
            oos.writeUnshared("acabou");
            oos.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
