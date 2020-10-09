package letscode;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeoutException;

class Server {
    private final static int BUFFER_SIZE = 256;
    private AsynchronousServerSocketChannel server;

    private final HttpHandler handler;

    Server(HttpHandler handler) {
        this.handler = handler;
    }

    public void bootstrap() {
        try {
            server = AsynchronousServerSocketChannel.open();
            server.bind(new InetSocketAddress("127.0.0.1", 8088));

            while (true) {
                Future<AsynchronousSocketChannel> future = server.accept();
                handleClient(future);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }
    }

    private void handleClient(Future<AsynchronousSocketChannel> future)
            throws InterruptedException, ExecutionException, TimeoutException, IOException {
        System.out.println("new client connection");

        AsynchronousSocketChannel clientChannel = future.get();

        while (clientChannel != null && clientChannel.isOpen()) {
            ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);
            StringBuilder builder = new StringBuilder();
            boolean keepReading = true;

            while (keepReading) {
                int readResult = clientChannel.read(buffer).get();

                keepReading = readResult == BUFFER_SIZE;
                buffer.flip();
                CharBuffer charBuffer = StandardCharsets.UTF_8.decode(buffer);
                builder.append(charBuffer);

                buffer.clear();
            }

            HttpRequest request = new HttpRequest(builder.toString());
            HttpResponse response = new HttpResponse();

            if (handler != null) {
                try {
                    String body = this.handler.handle(request, response);

                    if (body != null && !body.isBlank()) {
                        if (response.getHeaders().get("Content-Type") == null) {
                            response.addHeader("Content-Type", "text/html; charset=utf-8");
                        }

                        response.setBody(body);
                    }
                } catch (Exception e) {
                    e.printStackTrace();

                    response.setStatusCode(500);
                    response.setStatus("Internal server error");
                    response.addHeader("Content-Type", "text/html; charset=utf-8");
                    response.setBody("<html><body><h1>Error happens</h1></body></html>");
                }
            } else {
                response.setStatusCode(404);
                response.setStatus("Not found");
                response.addHeader("Content-Type", "text/html; charset=utf-8");
                response.setBody("<html><body><h1>Resource not found</h1></body></html>");
            }

            ByteBuffer resp = ByteBuffer.wrap(response.getBytes());
            clientChannel.write(resp);

            clientChannel.close();
        }
    }
}
