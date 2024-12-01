package server;

import backend.RemoteInterface;

import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;

public class ChatiniServer extends UnicastRemoteObject implements ServerRemoteInterface  {
    private final ArrayList<String> contacts;
    private static final String SERVER_NAME = "192.168.1.73";

    public ChatiniServer() throws RemoteException {
        contacts = new ArrayList<>();
    }

    public boolean sendBroadcast(String jsonStringMessage) throws RemoteException {
        System.out.println("Request to send broadcast: " + jsonStringMessage);
        for (String contact : contacts) {
            try {
                RemoteInterface ri = (RemoteInterface) Naming.lookup("//" + contact + ":3091/RemoteController");
                if (ri.receiveBroadcastMessage(jsonStringMessage)) {
                    System.out.println("Message successfully sent to " + contact);
                }
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
        return true;
    }

    public boolean registerContact(String ip) throws RemoteException {
        contacts.add(ip);
        System.out.println("Contact " + ip + " registered successfully");
        return true;
    }

    public static void main(String[] args) {
        try {
            LocateRegistry.createRegistry(3092);
            System.setProperty("java.rmi.server.hostname", SERVER_NAME);
            ServerRemoteInterface sri = new ChatiniServer();
            System.out.println("Listening in //" + SERVER_NAME + ":3092/RemoteServer");
            java.rmi.Naming.rebind("//" + SERVER_NAME + ":3092/RemoteServer", sri);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}
