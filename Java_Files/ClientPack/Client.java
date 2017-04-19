package ClientPack;

import ClientPack.JavaFX.LoginController;
import ClientPack.JavaFX.RunningController;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Client {

    private SocketChannel socketChannel;
    private String username = "";
    private String hostIP = "";
    private int hostPort = 0;
    private ByteBuffer writeBuf;
    private ByteBuffer readBuf;
    private int bufferSize;
    private CharBuffer cbuf;
    private String data;
    private LoginController loginController;
    private RunningController runningController;
    private ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private ExecutorService executor = Executors.newSingleThreadExecutor();

    public Client (int bufferSize, LoginController loginController) {
        try {
            this.loginController = loginController;
            this.bufferSize = bufferSize;
            this.socketChannel = SocketChannel.open();
//            this.socketChannel.configureBlocking(false);
            this.writeBuf = ByteBuffer.allocateDirect(this.bufferSize);
            this.readBuf = ByteBuffer.allocateDirect(this.bufferSize);
            this.cbuf = CharBuffer.allocate(this.bufferSize);
        }
        catch (Throwable t) {
            ClientMain.catcher("Client run threw", t);
        }
    }

    Runnable running = new Runnable() {
        public void run() {
            while (true) {
                try {
                    if (!socketChannel.isConnected()) break;
                    if (readData()) parseData();
                } catch (Throwable t) {
                    ClientMain.catcher("Client run threw", t);
                }
            }
            closeConnection();
        }
    };

    public void initConnection() {
        try {
            if (hostIP.equals("") || hostPort == 0) throw new Throwable("No IP/Port given for host");
            // Connect to server
            this.socketChannel.connect(new InetSocketAddress(hostIP, hostPort));
            TimeUnit.MILLISECONDS.sleep(500);
            // Send username
            this.sendData(String.format("498 %s", username));
            // Run listener
            executor.execute(running);
        }
        catch (Throwable t) {
            ClientMain.catcher("Client initConnection threw", t);
        }
    }

    private boolean readData() {
        try {
            this.readBuf = ByteBuffer.allocate(this.bufferSize);
            int bytesRead = -1;
            try {
                this.readBuf.clear();
                bytesRead = socketChannel.read(this.readBuf);
            } catch (Throwable t) {
                ClientMain.catcher("Client readData-inner threw: %s", t);
                this.closeConnection();
                return false;
            }
            if (bytesRead == -1) this.closeConnection();
            if (bytesRead == 0) return false;
        } catch (Throwable t) {
            this.closeConnection();
            ClientMain.catcher("Client readData threw: %s", t);
            return false;
        }
        return true;
    }

    private void parseData() {
        try {
            // Get first three chars
            this.readBuf.flip();
            byte[] bytes = new byte[3];
            String codeAsString = "";
            this.readBuf.get(bytes, 0,3);
            for (byte b : bytes) codeAsString += ((char) b);
            int code = Integer.parseInt(codeAsString);

            if (code < 500) { // Some type of string will be created
                this.readBuf.position(3);
                this.cbuf = CharBuffer.allocate(this.bufferSize);
                this.data = "";
                this.cbuf = Charset.forName("UTF-8").decode(this.readBuf);
                this.data = this.cbuf.toString();

                if (code == 0) runningController.updateChatWindow(this.data);               // general chat
                else if (code == 499) runningController.updateClientListView(this.data);    // update client list
            }
            else if (code < 750) {
                // Something else is created? Image?
            }
            else {
                // Something else is created? Sound?
            }

        } catch (Throwable t) {
            ClientMain.catcher("Client parseData threw: %s", t);
        }
    }

    public void sendData(String data) {
        System.out.println("Sending " + data);
        try {
            this.writeBuf = ByteBuffer.allocate(this.bufferSize);
            this.writeBuf.clear();
            this.writeBuf.put(data.getBytes());
            this.writeBuf.flip();
            while(this.writeBuf.hasRemaining()) {
                socketChannel.write(this.writeBuf);
            }
        } catch (Throwable t) {
            ClientMain.catcher("Client sendData threw: %s", t);
        }
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUsername() {
        return this.username;
    }

    public void setHostIP(String hostIP) {
        this.hostIP = hostIP;
    }

    public void setHostPort(int hostPort) {
        this.hostPort = hostPort;
    }

    public void setRunningController(RunningController rc) {
        this.runningController = rc;
        this.runningController.setClient(this);
    }

    public void closeConnection() {
        try {
            if (!this.executor.isShutdown()) this.executor.shutdownNow();
            if (socketChannel.isConnected()) {
                sendData(String.format("[disconnect] %s", username));
                TimeUnit.MILLISECONDS.sleep(250);
                this.socketChannel.close();
            }
        }
        catch (Throwable t) {
            ClientMain.catcher("Client closeConnection threw", t);
        }
    }
}