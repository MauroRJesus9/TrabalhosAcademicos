package isec.tppd.server.javarmi;

import java.io.IOException;
import java.rmi.Remote;

public interface ServerRemoteInterface extends Remote {
    void registerForCallback(String name,ClientRemoteInterface client) throws IOException;
    void unregisterForCallback(String name) throws IOException;
    void askToCloseService(String name,ClientRemoteInterface client) throws IOException;
}
