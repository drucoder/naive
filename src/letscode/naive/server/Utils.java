package letscode.naive.server;

class Utils {

    static void safelyClose(AutoCloseable resource) {
        if (resource == null) return;
        try {
            resource.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    static void log(String message) {
        Thread thread = Thread.currentThread();
        System.out.printf(
                "[%s[%d]] Naive Server: %s\n",
                thread.getName(),
                thread.getId(),
                message
        );
    }

}
