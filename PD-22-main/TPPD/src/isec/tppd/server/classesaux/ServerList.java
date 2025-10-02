package isec.tppd.server.classesaux;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class ServerList implements Serializable {
    private static final long serialVersionUID = 000001L;
    private ArrayList<ServerInfo> serverList;

    public ServerList(ArrayList<ServerInfo> serverList) {
        this.serverList = serverList;
    }

    public ArrayList<ServerInfo> getServerList() {
        return serverList;
    }

    public void setServerList(ArrayList<ServerInfo> serverList) {
        this.serverList = serverList;
    }

    public void organizaLista(){
        Collections.sort(serverList, (Comparator< ServerInfo >) (o1, o2) -> Integer.compare(o1.getQuant(), o2.getQuant()));
    }

    @Override
    public String toString() {
        return "ServerList{" +
                "serverList=" + serverList +
                '}';
    }
}
