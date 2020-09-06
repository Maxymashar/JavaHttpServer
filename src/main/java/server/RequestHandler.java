package server;

public interface RequestHandler {
    void handleRequest(HttpExchange httpExchange);
}
