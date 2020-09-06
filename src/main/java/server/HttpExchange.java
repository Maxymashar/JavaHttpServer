package server;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.HashMap;

public class HttpExchange {
    private String requestPath;
    private SocketChannel socketChannel;
    private final HashMap<String, String> requestHeaders = new HashMap<>();
    private final HashMap<String, String> responseHeaders = new HashMap<>();

    public void setRequestPath(String requestPath) {
        this.requestPath = requestPath;
    }

    public String getRequestPath() {
        return requestPath;
    }

    public void setSocketChannel(SocketChannel socketChannel) {
        this.socketChannel = socketChannel;
    }

    public SocketChannel getSocketChannel() {
        return socketChannel;
    }

    public HashMap<String, String> getRequestHeaders() {
        return requestHeaders;
    }

    public HashMap<String, String> getResponseHeaders() {
        return responseHeaders;
    }

    /*Send the Http Response Headers*/
    public void sendResponseHeaders(int contentLength, String responseCode) throws IOException {
        StringBuilder sb = new StringBuilder();
        responseHeaders.put("Content-Length", String.valueOf(contentLength));
        sb.append(responseCode).append("\n");
        responseHeaders.forEach((key, value) -> sb.append(key).append(":").append(value).append("\n"));
        sb.append("\n");

        byte[] res = sb.toString().getBytes();
        ByteBuffer buffer = ByteBuffer.allocate(res.length);
        buffer.put(res);
        buffer.flip();
        socketChannel.write(buffer);
    }
}
