package letscode.naive.server;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;

import static letscode.naive.server.Utils.log;
import static letscode.naive.server.Utils.safelyClose;

class NaiveRequestHandler implements Runnable {

    private final AsynchronousSocketChannel clientChannel;
    private final int bufferSize;


    public NaiveRequestHandler(AsynchronousSocketChannel clientChannel, int bufferSize) {
        this.clientChannel = clientChannel;
        this.bufferSize = bufferSize;
    }


    @Override
    public void run() {
        log("request handler: thread started");
        try {
            if (clientChannel != null && clientChannel.isOpen()) {
                String request = getRequestAsString();
                log("request handler: request: \n".concat(request));

                String response = getResponseAsString();
                ByteBuffer resp = ByteBuffer.wrap(response.getBytes());
                clientChannel.write(resp);
            }
        } finally {
            safelyClose(clientChannel);
        }

        log("request handler: thread stopped\n");
    }


    private static final String HEADERS = "" +
            "HTTP/1.1 200 OK\n" +
            "Server: naive\n" +
            "Content-Type: text/html\n" +
            "Content-Length: %s\n" +
            "Connection: close\n\n";

    private static final String BODY = "<html><body><h1>Hello, naive</h1></body></html>";


    private String getResponseAsString() {
        return String.format(HEADERS, BODY.length()) + BODY;
    }


    private String getRequestAsString() {
        ByteBuffer buffer = ByteBuffer.allocate(bufferSize);
        StringBuilder builder = new StringBuilder();

        boolean keepReading = true;
        while (keepReading) {
            try {
                clientChannel.read(buffer).get();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }

            int position = buffer.position();
            keepReading = position == bufferSize;

            byte[] array = keepReading
                    ? buffer.array()
                    : Arrays.copyOfRange(buffer.array(), 0, position);

            builder.append(new String(array));
            buffer.clear();
        }

        return builder.toString();
    }

}
