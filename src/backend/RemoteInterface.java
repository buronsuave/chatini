package backend;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RemoteInterface extends Remote {
    boolean receiveMessage(String jsonStringMessage) throws RemoteException;
    boolean receiveBroadcastMessage(String jsonStringMessage) throws RemoteException;
}
