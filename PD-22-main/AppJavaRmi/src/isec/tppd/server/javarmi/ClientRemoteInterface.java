package isec.tppd.server.javarmi;

import java.io.IOException;
import java.rmi.Remote;

public interface ClientRemoteInterface extends Remote {
    void callback(String message) throws IOException;
    void closeRMIService() throws IOException;
}
