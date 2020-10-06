package letscode.naive.server;

import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static letscode.naive.server.Utils.log;

class NaiveServerLoop implements Runnable {

    private final AsynchronousServerSocketChannel server;
    private final int bufferSize;

    NaiveServerLoop(AsynchronousServerSocketChannel server, int bufferSize) {
        this.bufferSize = bufferSize;
        this.server = server;
    }

    @Override
    public void run() {
        log("server loop: started");
        boolean runServer = true;
        while (runServer) {
            try {
                Future<AsynchronousSocketChannel> future = server.accept();
                log("server loop: listening for connection");

                AsynchronousSocketChannel clientChannel = future.get();
                log("server loop: new connection recieved");

                NaiveRequestHandler handler = new NaiveRequestHandler(clientChannel, bufferSize);
                new Thread(handler).start();

            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                runServer = false;
            }
        }
        log("server loop: stopped");
    }

}
