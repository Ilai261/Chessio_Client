package org.chessio.chessio_client.Services;

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.concurrent.Task;
import org.chessio.chessio_client.Configurations.AppConfig;

public class ClientHttpSender {
    private final String baseUrl;
    private final java.net.http.HttpClient client;
    private final ObjectMapper objectMapper;
    private final ExecutorService executorService;


    public ClientHttpSender()
    {
        this.baseUrl = String.format("http://%s:%d",
                AppConfig.getServerAddress(),
                AppConfig.getServerPort());
        System.out.println("baseServerUrl: " + baseUrl);
        this.client = java.net.http.HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper();
        final int NUM_THREADS = 5;
        this.executorService = Executors.newFixedThreadPool(NUM_THREADS); // Adjust pool size as needed

    }

    public <T> HttpResponse<String> post(String endpoint, T body) throws Exception
    {
        String json = objectMapper.writeValueAsString(body);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + endpoint))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    public <T> Task<HttpResponse<String>> postAsync(String endpoint, T body)
    {
        return new Task<>() {
            @Override
            protected HttpResponse<String> call() throws Exception {
                return post(endpoint, body);
            }
        };
    }

    public void shutdown() {
        executorService.shutdown();
    }
}
