package app;

import backend.RemoteController;
import backend.RemoteInterface;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import frontend.GraphicApp;
import server.ServerRemoteInterface;

import java.net.*;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.util.Enumeration;

public class ChatiniApp {
    private static final String SERVER_NAME = "192.168.0.10";

    private RemoteController back = null;
    private final GraphicApp front;
    private final String hostname;
    private final JsonArray messages;

    public ChatiniApp() throws RuntimeException {
        hostname = getIP();
        if (hostname.isEmpty()) throw new RuntimeException();

        // Create message list and add new one for broadcast messages
        messages = new JsonArray();
        JsonObject broadcastChat = new JsonObject();
        broadcastChat.addProperty("contact", "broadcast");
        broadcastChat.add("record", new JsonArray());
        messages.add(broadcastChat);

        try {
            back = new RemoteController(this);
        } catch (RemoteException e) {
            System.out.println(e.getMessage());
        }
        front = new GraphicApp(this);
    }

    public String getHostname() {
        return hostname;
    }

    public JsonArray getMessages() {
        return messages;
    }

    public void runBackend() {
        back.listen();
    }

    public void runFrontend() {
        front.run();
    }

    public void receiveBroadcastMessage(JsonObject message) throws JsonParseException {
        JsonArray broadcastRecord = messages.get(0).getAsJsonObject().get("record").getAsJsonArray();
        broadcastRecord.add(message);
        front.refreshMessages();
    }

    public void receiveMessage(JsonObject message) throws JsonParseException {
        String contact = message.get("sender").getAsString();

        // i = 0 is broadcast chat
        for (int i = 1; i < messages.size(); ++i) {
            // Conversation already exists
            if (messages.get(i).getAsJsonObject().get("contact").getAsString().equals(contact)) {
                messages.get(i).getAsJsonObject().get("record").getAsJsonArray().add(message);
            }
        }

        // No conversation with sender reported so far. Create a new one
        JsonObject newChat = new JsonObject();
        newChat.addProperty("contact", contact);
        JsonArray newRecord = new JsonArray();
        newRecord.add(message);
        newChat.add("record", newRecord);
        messages.add(newChat);

        front.refreshMessages();
    }

    public void register() {
        try {
            ServerRemoteInterface sri = (ServerRemoteInterface) Naming.lookup("//" + SERVER_NAME + ":3092/RemoteServer");
            sri.registerContact(hostname);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public static void main(String[] args) {
        ChatiniApp app  = new ChatiniApp();
        app.register();

        Thread backThread = new Thread(app::runBackend);
        Thread frontThread = new Thread(app::runFrontend);
        backThread.start();
        frontThread.start();

        // Join both threads to main thread to keep execution alive
        try {
            backThread.join();
            frontThread.join();
        } catch (InterruptedException e) {
            System.out.println("App interrupted: " + e.getMessage());
        }
    }

    public int findChat(String sender) {
        // i = 0 is broadcast chat
        for (int i = 0; i < messages.size(); ++i) {
            // Conversation already exists
            if (messages.get(i).getAsJsonObject().get("contact").getAsString().equals(sender)) {
                return i;
            }
        }

        // No chat with that sender so far
        return -1;
    }

    // For dynamic assignment of hostnames
    private String getIP() {
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface iface = interfaces.nextElement();
                if (iface.isLoopback() || !iface.isUp() || iface.isVirtual() || iface.isPointToPoint())
                    continue;

                Enumeration<InetAddress> addresses = iface.getInetAddresses();
                while(addresses.hasMoreElements()) {
                    InetAddress addr = addresses.nextElement();
                    final String ip = addr.getHostAddress();
                    if(Inet4Address.class == addr.getClass() && (ip.contains("192.168.0.")
                            || ip.contains("192.168.84.") || ip.contains("192.168.1.")))
                        return ip;
                }
            }
        } catch (SocketException e) {
            throw new RuntimeException(e);
        }

        return "";
    }

    public void sendBroadcastMessage(String content) {
        System.out.println("Trying to send " + content + " from " + hostname + " to all known hosts ");
        JsonObject message = new JsonObject();
        message.addProperty("sender", hostname);
        message.addProperty("content", content);
        try {
            ServerRemoteInterface sri = (ServerRemoteInterface) Naming.lookup("//" + SERVER_NAME + ":3092/RemoteServer");
            sri.sendBroadcast(message.toString());
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public void sendMessage(String destination, String content) {
        System.out.println("Trying to send " + content + " from " + hostname + " to " + destination);
        JsonObject message = new JsonObject();
        message.addProperty("sender", hostname);
        message.addProperty("content", content);

        try {
            RemoteInterface ri = (RemoteInterface) Naming.lookup("//" + destination + ":3091/RemoteController");
            if (ri.receiveMessage(message.toString())) {
                // Receive the message sent as well to update list
                if (!destination.equals(hostname)) {
                    // i = 0 is broadcast chat
                    for (int i = 1; i < messages.size(); ++i) {
                        // Conversation already exists
                        if (messages.get(i).getAsJsonObject().get("contact").getAsString().equals(destination)) {
                            messages.get(i).getAsJsonObject().get("record").getAsJsonArray().add(message);
                        }
                    }

                    // No conversation with sender reported so far. Create a new one
                    JsonObject newChat = new JsonObject();
                    newChat.addProperty("contact", destination);
                    JsonArray newRecord = new JsonArray();
                    newRecord.add(message);
                    newChat.add("record", newRecord);
                    messages.add(newChat);
                }

                front.refreshMessages();
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}
