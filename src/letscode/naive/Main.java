package letscode.naive;

import letscode.naive.server.NaiveServer;

public class Main {

    public static void main(String[] args) throws InterruptedException {
        NaiveServer server = NaiveServer.Builder.get()
                .setBufferSize(1024)
                .setServerAddress("127.0.0.1")
                .setServerPort(8080)
                .start();

        Thread.sleep(30000);
        server.stop();
    }

}
