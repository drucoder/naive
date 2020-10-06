package letscode.naive.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousServerSocketChannel;

public class NaiveServer {

    private final AsynchronousServerSocketChannel server;
    private final Thread serverLoopThread;


    private NaiveServer(int bufferSize, String serverAddress, int serverPort) {
        try {
            server = AsynchronousServerSocketChannel.open();
            server.bind(new InetSocketAddress(serverAddress, serverPort));

            NaiveServerLoop serverLoop = new NaiveServerLoop(server, bufferSize);
            serverLoopThread = new Thread(serverLoop);
            serverLoopThread.start();
        } catch (IOException e) {
            throw new RuntimeException("Server failed to start", e);
        }
    }


    public void stop() {
        serverLoopThread.interrupt();
    }


    public static class Builder {

        private int bufferSize = 256;
        private String serverAddress = "127.0.0.1";
        private int serverPort = 8080;

        public Builder setBufferSize(int bufferSize) {
            this.bufferSize = bufferSize;
            return this;
        }

        public Builder setServerAddress(String serverAddress) {
            this.serverAddress = serverAddress;
            return this;
        }

        public Builder setServerPort(int serverPort) {
            this.serverPort = serverPort;
            return this;
        }

        public NaiveServer start() {
            return new NaiveServer(bufferSize, serverAddress, serverPort);
        }

        public static Builder get() {
            return new Builder();
        }

    }

}
