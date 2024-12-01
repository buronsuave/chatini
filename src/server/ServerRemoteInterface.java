package server;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ServerRemoteInterface extends Remote {
    boolean sendBroadcast(String jsonStringMessage) throws RemoteException;
    boolean registerContact(String ip) throws RemoteException;
}
