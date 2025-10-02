package isec.tppd.server.javarmi;

import java.io.IOException;
import java.rmi.Remote;
import java.util.ArrayList;

public interface JavaRmiInterface extends Remote {
    ArrayList<InfoSvRMI> getServer() throws IOException;
}
