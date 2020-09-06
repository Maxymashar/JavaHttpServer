package server;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SocketChannel;

public class WebServer {
    private int getFileSize(String filename) {
        return (int) new File(filename).length();
    }

    private String getRequestedFilename(String requestPath) {
        if (requestPath.equals("/")) {
            return "src/main/web/index.html";
        } else {
            return "src/main/web" + requestPath;
        }
    }

    private String getMimeType(String filename) {
        if (filename.endsWith(".html")) {
            return "text/html";
        } else if (filename.endsWith(".js")) {
            return "text/javascript";
        } else if (filename.endsWith(".css")) {
            return "text/css";
        }
        return "text/html";
    }

    public void sendRequestedFile(HttpExchange httpExchange) throws IOException {
        /*Send the response Headers*/
        String requestedFilePath = getRequestedFilename(httpExchange.getRequestPath());
        httpExchange.getResponseHeaders().put("Content-Type", getMimeType(requestedFilePath));
        int fileSize = getFileSize(requestedFilePath);
        httpExchange.sendResponseHeaders(fileSize,ResponseCodes.RES_200);

        System.out.println("Requested File :: " + requestedFilePath);
        RandomAccessFile randomAccessFile = new RandomAccessFile(requestedFilePath, "rw");
        FileChannel fileChannel = randomAccessFile.getChannel();


        ByteBuffer buffer = ByteBuffer.allocate(fileSize);
        fileChannel.read(buffer);
        buffer.flip();
        fileChannel.close();

        SocketChannel socketChannel = httpExchange.getSocketChannel();
        socketChannel.write(buffer);
        buffer.clear();
    }

    public static class ResponseCodes {
        public static final String RES_200 = "HTTP/1.1 200 OK";
    }
}
