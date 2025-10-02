package isec.tppd.client.threads;

import isec.tppd.client.ui.UICliente;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Scanner;

public class ThreadComServidor extends Thread{ //Thread conexão inicial
    private Socket cliSocket;
    private Scanner scanner;
    private boolean autenticado;
    private boolean admin;
    private UICliente uiCliente;
    private int id_user;
    private Thread threadCliente;

    public ThreadComServidor(Socket cliSocket, Thread threadCliente){
        this.cliSocket = cliSocket;
        scanner = new Scanner(System.in);
        autenticado = false;
        admin = false;
        uiCliente = new UICliente(cliSocket);
        this.threadCliente = threadCliente;
    }

    @Override
    public void run(){
        String msg;
        String msgRec = null;
        ObjectOutputStream oos = null;
        ObjectInputStream ois = null;
        try {
            oos = new ObjectOutputStream(cliSocket.getOutputStream());
            ois = new ObjectInputStream(cliSocket.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
        uiCliente.setOis(ois);
        uiCliente.setOos(oos);
        while(true){
            try{
                if(!autenticado){ //Se o user ainda n estiver logado
                    msg = uiCliente.UIautenticacao();
                    oos.writeUnshared(msg);
                    oos.flush();
                    msgRec = (String)ois.readObject();
                    //System.out.println("Comando Servidor: " +  msgRec);
                    if(msgRec != null){
                        if(!msgRec.equalsIgnoreCase("saida_confirmada")){
                            verificaEstadoAutenticacao(msgRec);
                        }else{
                            break;
                        }
                    }else{
                        break;
                    }
                }else{
                    //System.out.println("Comando Servidor: " + msgRec);
                    if(msgRec != null){
                        verificaEstadoAutenticacao(msgRec);
                        oos.writeUnshared(uiCliente.verificaComandosServidor(msgRec));
                        oos.flush();
                        if(!msgRec.equalsIgnoreCase("logout_sucesso")){
                            msgRec = (String)ois.readObject();
                        }
                    }else{
                        msg = uiCliente.UImenu(admin,id_user);
                        oos.writeUnshared(msg);
                        oos.flush();
                        msgRec = (String)ois.readObject();
                    }
                }
            }catch (IOException | ClassNotFoundException e){
                e.printStackTrace();
                System.out.println("A fechar a ligação com o servidor atual...");
                break;
            }
        }
        try {
            cliSocket.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        threadCliente.interrupt(); //Para interromper o sleep da Thread Main

    }

    private void verificaEstadoAutenticacao(String msgRec){ //Verificacao inicial
        String [] dados = msgRec.split(":",2);
        if(msgRec.contains("Sucesso na autenticacao-Admin")){ //:id_user
            id_user=Integer.parseInt(dados[1]);
            autenticado = true;
            admin = true;
        }else if(msgRec.contains("Sucesso na autenticacao")){
            id_user=Integer.parseInt(dados[1]);
            autenticado = true;
        }else if(msgRec.contains("Sucesso no registo")){
            id_user=Integer.parseInt(dados[1]);
            autenticado = true;
        }else if(msgRec.contains("logout_sucesso")){
            autenticado = false;
        }else if(msgRec.equalsIgnoreCase("Erro no registo")){
            System.out.println(msgRec);
        }else if(msgRec.equalsIgnoreCase("Erro na autenticacao")){
            System.out.println(msgRec);
        }
    }


}
