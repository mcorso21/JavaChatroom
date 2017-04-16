package ServerPack;

import java.nio.*;
import java.nio.channels.*;
import java.nio.charset.Charset;

class Client {

    private SelectionKey clientKey;
    private SocketChannel socketChannel;
    private ByteBuffer buf;
    private int bufferSize;
    private CharBuffer cbuf;
    private Server server;
    private String data;
    private String username;
    private boolean disconnected = false;

    protected Client(Server server, SelectionKey selectionKey,
                     SocketChannel socketChannel, int bufferSize) throws Throwable {
        this.username = "Guest";
        this.bufferSize = bufferSize;
        this.server = server;
        this.clientKey = selectionKey;
        this.socketChannel = (SocketChannel) socketChannel.configureBlocking(false);
        this.buf = ByteBuffer.allocateDirect(this.bufferSize);
        this.cbuf = CharBuffer.allocate(this.bufferSize);
    }

    protected void closeConnection() {
        if (this.disconnected) return;
//        server.receivedData(this, String.format("[disconnect] %s", this.username));
        server.clientList.remove(this);
        server.updateClientList();
        server.clientHashMap.remove(this.clientKey);
        try {
            if (this.clientKey != null) this.clientKey.cancel();
            if (this.socketChannel == null) return;
            this.socketChannel.close();
        } catch (Throwable t) {
            ServerMain.catcher("Client closeConnection threw: %s", t);
        }
        this.disconnected = true;
    }

    protected void gotData() throws Throwable {
        if (this.readData()) this.parseData();
        this.forwardData();
    }

    private boolean readData() {
        try {
            int bytesRead = -1;
            try {
                this.buf.clear();
                bytesRead = socketChannel.read(this.buf);
            } catch (Throwable t) {
                ServerMain.catcher("Client readData1 threw: %s", t);
                server.receivedData(this, String.format("[disconnect] %s", this.username));
                return false;
            }
            if (bytesRead == -1) this.closeConnection();
            if (bytesRead == 0) return false;
        } catch (Throwable t) {
            this.closeConnection();
            ServerMain.catcher("Client readData2 threw: %s", t);
        }
        return true;
    }

    private void parseData() {
        try {
            this.data = "";
            this.buf.flip();
            this.cbuf = Charset.forName("UTF-8").decode(buf);
            this.data = cbuf.toString();
        } catch (Throwable t) {
            ServerMain.catcher("Client parseData threw: %s", t);
        }
    }

    private void forwardData() {
        try {
            server.receivedData(this, this.data);
            this.data = "";
        } catch (Throwable t) {
            ServerMain.catcher("Client forwardData threw: %s", t);
        }
    }

    protected void sendData(String data) {
        try {
            ByteBuffer sendBuff = ByteBuffer.allocate(this.bufferSize);
            sendBuff.clear();
            sendBuff.put(data.getBytes());
            sendBuff.flip();
            this.socketChannel.write(sendBuff);
        } catch (Throwable t) {
            ServerMain.catcher("Client sendData threw: %s", t);
        }
    }

    protected String getUsername() {
        return this.username;
    }

    protected void setUserName(String username) {
        this.username = username;
    }

    @Override
    public String toString() {
        return this.username;
    }
}