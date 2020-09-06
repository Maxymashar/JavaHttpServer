package server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.HashMap;

public class HttpServer {
    private static HttpServer httpServer = null;
    private final ServerSocketChannel mServerSocketChannel = ServerSocketChannel.open();
    private final ArrayList<HttpRequest> httpRequestList = new ArrayList<>();
    private final int port;

    private HttpServer(int port) throws IOException {
        this.port = port;
    }

    public static HttpServer getHttpServer(int port) throws IOException {
        if (httpServer == null) {
            httpServer = new HttpServer(port);
        }
        return httpServer;
    }

    public void start() throws IOException {
        try {
            mServerSocketChannel.bind(new InetSocketAddress(port));
            System.out.println("Server Started Listening At Port " + port);
        } catch (IOException e) {
            System.err.println("Error binding to the port " + port);
            return;
        }
        /*The Server runs forever until stopped*/
        while (true) {
            /*Connect to a Socket*/
            SocketChannel socketChannel = mServerSocketChannel.accept();
            System.out.println("Server Connected To Socket At Address " + socketChannel.getRemoteAddress());
            new Thread(() -> {
                try {
                    /*Get the Http Exchange Object*/
                    HttpExchange httpExchange = getRequestHttpExchange(socketChannel);
                    /*Handle the request*/
                    handleRequest(httpExchange);
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }).start();

        }
    }

    private void handleRequest(HttpExchange httpExchange) {
        String[] requestPath = httpExchange.getRequestPath().trim().split("/");
        if (requestPath.length == 0) {
            RequestHandler rootHandler = getRegisteredRoute("/");
            if (rootHandler == null) {
                throw new RuntimeException("No Registered Route /");
            } else {
                rootHandler.handleRequest(httpExchange);
            }
        } else {
            String route = "/" + requestPath[1];
            RequestHandler handler = getRegisteredRoute(route);
            if (handler == null) {
                RequestHandler rootHandler = getRegisteredRoute("/");
                if (rootHandler == null) {
                    throw new RuntimeException("No Registered Route /");
                } else {
                    rootHandler.handleRequest(httpExchange);
                }
            } else {
                handler.handleRequest(httpExchange);
            }
        }
    }

    private RequestHandler getRegisteredRoute(String route) {
        for (HttpRequest httpRequest : httpRequestList) {
            if (httpRequest.getEntryPointRoute().equals(route)) {
                return httpRequest.getRequestHandler();
            }
        }
        return null;
    }

    /*Registers a route*/
    public void handleRouteRequest(String entryPointRoute, RequestHandler requestHandler) {
        httpRequestList.add(new HttpRequest(entryPointRoute, requestHandler));
    }

    /*Gets the Http Exchange*/
    private HttpExchange getRequestHttpExchange(SocketChannel socketChannel) throws IOException {
        HttpExchange httpExchange = new HttpExchange();
        httpExchange.setSocketChannel(socketChannel);
        HashMap<String, String> requestHeaders = httpExchange.getRequestHeaders();

        while (true) {
            String line = readLine(socketChannel);
            if (line != null) {
                if (line.trim().length() == 0) {
                    return httpExchange;
                }
                /*Get the Header Line*/
                if (line.startsWith("GET") || line.startsWith("POST")) {
                    String path = line.split(" ")[1].trim();
                    httpExchange.setRequestPath(path);
                } else {
                    /*Get the Http Request Headers*/
                    int indexOfColon = line.indexOf(":");
                    if (indexOfColon == -1) {
                        System.err.println("No Colon Found In The Header Line : " + line);
                        continue;
                    }
                    String key = line.substring(0, indexOfColon).trim();
                    String value = line.substring(indexOfColon + 1).trim();
                    requestHeaders.put(key, value);
                }

            }
        }
    }

    /*Reads only one line from the SocketChannel at a time*/
    private String readLine(SocketChannel socketChannel) throws IOException {
        boolean isFirstLine = true;
        ByteBuffer buffer = ByteBuffer.allocate(1);
        StringBuilder sb = new StringBuilder();

        while (true) {
            /*Read the data from the SocketChannel to the buffer {OneByte}*/
            int readBytes = socketChannel.read(buffer);
            /*Return null if there is no more data to be read from the SocketChannel*/
            if (readBytes == -1) {
                if (isFirstLine) {
                    return null;
                } else {
                    return sb.toString();
                }
            }
            buffer.flip();
            int readByte = buffer.get();
            if (readByte == 10) {
                return sb.toString();
            }
            sb.append((char) readByte);
            buffer.clear();
            isFirstLine = false;
        }
    }

    public static void main(String[] args) throws IOException {
        HttpServer httpServer = HttpServer.getHttpServer(8080);
        WebServer webServer = new WebServer();
        httpServer.handleRouteRequest("/", httpExchange -> {
            try {
                webServer.sendRequestedFile(httpExchange);
                httpExchange.getSocketChannel().close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        httpServer.handleRouteRequest("/events", httpExchange -> System.out.println("EVENTS"));
        httpServer.start();
    }

}
