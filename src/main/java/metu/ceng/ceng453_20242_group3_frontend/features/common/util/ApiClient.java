package metu.ceng.ceng453_20242_group3_frontend.features.common.util;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Client for making API calls to the backend.
 */
public class ApiClient {
    
    private static final String API_BASE_URL = "http://localhost:8080/api";
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    
    public ApiClient() {
        httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_2)
                .build();
        objectMapper = new ObjectMapper();
    }
    
    /**
     * Makes a GET request to the specified endpoint.
     * 
     * @param endpoint The API endpoint (e.g., "/users")
     * @param onSuccess Callback for successful response
     * @param onError Callback for error
     */
    public void get(String endpoint, Consumer<String> onSuccess, Consumer<String> onError) {
        String url = API_BASE_URL + endpoint;
        
        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .build();
        
        // Add authorization header if user is logged in
        if (SessionManager.getInstance().isLoggedIn()) {
            request = HttpRequest.newBuilder(request, (name, value) -> true)
                    .header("Authorization", "Bearer " + SessionManager.getInstance().getAuthToken())
                    .build();
        }
        
        httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenAccept(onSuccess)
                .exceptionally(e -> {
                    onError.accept(e.getMessage());
                    return null;
                });
    }
    
    /**
     * Makes a POST request to the specified endpoint.
     * 
     * @param endpoint The API endpoint (e.g., "/users")
     * @param requestBody The request body as JSON string
     * @param onSuccess Callback for successful response
     * @param onError Callback for error
     */
    public void post(String endpoint, String requestBody, Consumer<String> onSuccess, Consumer<String> onError) {
        String url = API_BASE_URL + endpoint;
        
        HttpRequest request = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .build();
        
        // Add authorization header if user is logged in
        if (SessionManager.getInstance().isLoggedIn()) {
            request = HttpRequest.newBuilder(request, (name, value) -> true)
                    .header("Authorization", "Bearer " + SessionManager.getInstance().getAuthToken())
                    .build();
        }
        
        httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenAccept(onSuccess)
                .exceptionally(e -> {
                    onError.accept(e.getMessage());
                    return null;
                });
    }
    
    /**
     * Makes a PUT request to the specified endpoint.
     * 
     * @param endpoint The API endpoint (e.g., "/users/123")
     * @param requestBody The request body as JSON string
     * @param onSuccess Callback for successful response
     * @param onError Callback for error
     */
    public void put(String endpoint, String requestBody, Consumer<String> onSuccess, Consumer<String> onError) {
        String url = API_BASE_URL + endpoint;
        
        HttpRequest request = HttpRequest.newBuilder()
                .PUT(HttpRequest.BodyPublishers.ofString(requestBody))
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .build();
        
        // Add authorization header if user is logged in
        if (SessionManager.getInstance().isLoggedIn()) {
            request = HttpRequest.newBuilder(request, (name, value) -> true)
                    .header("Authorization", "Bearer " + SessionManager.getInstance().getAuthToken())
                    .build();
        }
        
        httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenAccept(onSuccess)
                .exceptionally(e -> {
                    onError.accept(e.getMessage());
                    return null;
                });
    }
    
    /**
     * Makes a DELETE request to the specified endpoint.
     * 
     * @param endpoint The API endpoint (e.g., "/users/123")
     * @param onSuccess Callback for successful response
     * @param onError Callback for error
     */
    public void delete(String endpoint, Consumer<String> onSuccess, Consumer<String> onError) {
        String url = API_BASE_URL + endpoint;
        
        HttpRequest request = HttpRequest.newBuilder()
                .DELETE()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .build();
        
        // Add authorization header if user is logged in
        if (SessionManager.getInstance().isLoggedIn()) {
            request = HttpRequest.newBuilder(request, (name, value) -> true)
                    .header("Authorization", "Bearer " + SessionManager.getInstance().getAuthToken())
                    .build();
        }
        
        httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenAccept(onSuccess)
                .exceptionally(e -> {
                    onError.accept(e.getMessage());
                    return null;
                });
    }
} 