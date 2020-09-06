package server;

public class HttpRequest {
    private final String entryPointRoute;
    private final RequestHandler requestHandler;

    public HttpRequest(String entryPointRoute, RequestHandler requestHandler) {
        this.entryPointRoute = entryPointRoute;
        this.requestHandler = requestHandler;
    }

    public RequestHandler getRequestHandler() {
        return requestHandler;
    }

    public String getEntryPointRoute() {
        return entryPointRoute;
    }
}
