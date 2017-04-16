package ServerPack;

import ServerPack.JavaFX.LaunchScreen.LaunchController;
import ServerPack.JavaFX.RunningScreen.RunningController;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.IOException;
import java.net.*;
import java.nio.channels.*;
import java.util.*;
import java.util.concurrent.*;

public class Server {

    private int bufferSize;
    private int hostPort;
    private ServerSocketChannel serverSocketChannel;
    private Selector selector;
    private SelectionKey serverAcceptKey;
    private ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    public LaunchController launchController;
    private RunningController runningController;
    protected HashMap<SelectionKey, Client> clientHashMap = new HashMap<>();
    protected ObservableList clientList = FXCollections.observableArrayList();

    public Server(int bufferSize) {
        this.bufferSize = bufferSize;
    }

    public void initServer() throws IOException{
        serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.configureBlocking(false);
        serverSocketChannel.bind(new InetSocketAddress("localhost", hostPort));
        selector = Selector.open();
        serverAcceptKey = serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

        this.serverMessage(String.format("Server listening at port %s.", this.serverSocketChannel.socket().getLocalPort()));
        Runnable running = new Runnable() {
            public void run() {
                checkIO();
            }
        };
        scheduler.scheduleWithFixedDelay(running, 0, 250, TimeUnit.MILLISECONDS);
    }

    private void checkIO() {
        try {
            selector.selectNow();
        } catch (Throwable t) {
            ServerMain.catcher("Server checkIO selector failed selectNow() threw: %s", t);
        }
        for (SelectionKey key : selector.selectedKeys()) {
            try {
                if (!key.isValid()) continue;
                if (key == serverAcceptKey) {
                    SocketChannel connection = serverSocketChannel.accept();
                    if (connection == null) continue;
                    connection.configureBlocking(false);
                    SelectionKey readKey = connection.register(selector, SelectionKey.OP_READ);
                    Client newClient = new Client(this, readKey, connection, this.bufferSize);
                    clientHashMap.put(readKey, newClient);
                    this.serverMessage(String.format("New client: %s. Total clients: %s.",
                            connection.getRemoteAddress(), clientHashMap.size()));
//                    clientList.add(newClient);
//                    this.updateClientList();
                }
                if (key.isReadable()) {
                    Client client = clientHashMap.get(key);
                    if (client == null) {
                        clientHashMap.remove(key);
                        continue;
                    }
                    client.gotData();
                }
            } catch (Throwable t) {
                ServerMain.catcher("Server checkIO threw: %s", t);
            }
        }
        selector.selectedKeys().clear();
    }

    public void receivedData(Client client, String data) {
        try {
            if (data.getBytes().length > this.bufferSize) {
                this.serverMessage(String.format("Error data size exceeds buffer size: '%s'", data));
            }
            else this.parseData(client, data);
        } catch (Throwable t) {
            ServerMain.catcher("Server receivedData threw: %s", t);
        }
    }

    private void parseData(Client client, String data) {
        try {
            // Client sent disconnect message
            if (data.split(" ")[0].equals("[disconnect]")) {
                try {
                    clientList.remove(client);
                } catch (Throwable t) {}
                client.closeConnection();
                data = String.format("%s disconnected.", data.split(" ", 2)[1]);
            }
            // Client updated username
            else if (data.split(" ")[0].equals("[username]")) {
                String username = data.split(" ", 2)[1];
                if (username.length() <= 15) {
                    client.setUserName(username);
                    data = String.format("server %s connected.", data.split(" ", 2)[1]);
                    this.clientList.add(client);
                    this.updateClientList();
                }
            }
            this.updateServerData(data);
            this.sendData_allClients(data);
        } catch (Throwable t) {
            ServerMain.catcher("Server parseData threw: %s", t);
        }
    }

    private void updateServerData(String data) {
        try {
            this.runningController.updateChatWindow("client", data);
        } catch (Throwable t) {
            ServerMain.catcher("Server updateServerData threw: %s", t);
        }
    }

    private void serverMessage(String data) {
        try {
            this.runningController.updateChatWindow("server", data);
        } catch (Throwable t) {
            ServerMain.catcher("Server serverMessage threw: %s", t);
        }
    }

    private void sendData_allClients(String data) {
        try {
            for (Client client : clientHashMap.values()) {
                client.sendData(data);
            }
        } catch (Throwable t) {
            ServerMain.catcher("Server sendData_allClients threw: %s", t);
        }
    }

    public void shutDownServer() {
        closeClientConnections();
        try {
            scheduler.shutdownNow();
            serverSocketChannel.close();
            selector.close();
            serverAcceptKey.cancel();
        } catch (Throwable t) {
            ServerMain.catcher("Server shutDownServer threw: %s", t);
        }
    }

    public void closeClientConnections() {
        try {
            for (Client client : clientHashMap.values()) {
                client.closeConnection();
            }
        } catch (Throwable t) {
            ServerMain.catcher("Server closeClientConnections threw: %s", t);
        }
    }

    protected void updateClientList() {
        try {
            this.runningController.updateClientListView(this.clientList);
        } catch (Throwable t) {
            ServerMain.catcher("Server updateClientList threw: %s", t);
        }
    }

    public void setHostPort(int hostPort) {
        this.hostPort = hostPort;
    }

    public void setRunningController(RunningController rsc) {
        this.runningController = rsc;
    }

    public void setLaunchController(LaunchController lsc) {
        this.launchController = lsc;
    }
}