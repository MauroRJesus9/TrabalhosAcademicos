package isec.tppd.client.threads;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ThreadMostraLugaresDisp extends Thread{

    private Socket cliSocket;
    private String comando;
    private String msgRec;
    private ObjectOutputStream oos;
    private ObjectInputStream ois;

    public ThreadMostraLugaresDisp(Socket cliSocket,String comando, String msgRec,ObjectOutputStream oos,ObjectInputStream ois) {
        this.cliSocket=cliSocket;
        this.comando = comando;
        this.msgRec = msgRec;
        this.ois = ois;
        this.oos = oos;
    }

    @Override
    public void run(){
        do{
            try {
                oos.writeUnshared(comando);
                oos.flush();
                synchronized (msgRec) {
                    msgRec = (String) ois.readObject();
                }
                System.out.println(msgRec);
                try {
                    Thread.sleep(5000);
                }catch (InterruptedException e){
                    break;
                }
            } catch (IOException | ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }while (true);
    }
}
