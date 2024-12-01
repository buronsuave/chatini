package backend;

import java.rmi.server.UnicastRemoteObject;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;

import app.ChatiniApp;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

public class RemoteController extends UnicastRemoteObject implements RemoteInterface {
    private final ChatiniApp context;

    public RemoteController(ChatiniApp context) throws RemoteException {
        this.context = context;
    }

    public boolean receiveMessage(String jsonStringMessage) throws RemoteException {
        JsonObject message = JsonParser.parseString(jsonStringMessage).getAsJsonObject();
        try {
            context.receiveMessage(message);
        } catch (JsonParseException e) {
            System.out.println(e.getMessage());
            return false;
        }

        return true;
    }

    public boolean receiveBroadcastMessage(String jsonStringMessage) throws RemoteException {
        JsonObject message = JsonParser.parseString(jsonStringMessage).getAsJsonObject();
        try {
            System.out.println("Received broadcast message: " + message.toString());
            context.receiveBroadcastMessage(message);
        } catch (JsonParseException e) {
            System.out.println(e.getMessage());
            return false;
        }

        return true;
    }

    public void listen() {
        try {
            LocateRegistry.createRegistry(3091);
            System.setProperty("java.rmi.server.hostname", context.getHostname());
            RemoteInterface ri = new RemoteController(context);
            System.out.println("Listening in //" + context.getHostname() + ":3091/RemoteController");
            java.rmi.Naming.rebind("//" + context.getHostname() + ":3091/RemoteController", ri);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}
