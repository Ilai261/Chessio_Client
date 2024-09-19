// Written by Ilai Azaria and Eitan Feldsherovich, 2024
// This class defines the client's HTTP request sender, that sends HTTP a-synchronized requests to the server
// (register, login and retrieve game history)

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
    // base url of the server
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
        this.executorService = Executors.newFixedThreadPool(NUM_THREADS); // adjust pool size as needed
    }

    // post request
    public <T> HttpResponse<String> post(String endpoint, T body) throws Exception
    {
        // parses to JSON, builds the request and sends it as a post
        String json = objectMapper.writeValueAsString(body);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + endpoint))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    // a-synchronized post request, calls the post function inside a new Task object
    public <T> Task<HttpResponse<String>> postAsync(String endpoint, T body)
    {
        return new Task<>() {
            @Override
            protected HttpResponse<String> call() throws Exception {
                return post(endpoint, body);
            }
        };
    }

    // shuts down the executor service
    public void shutdown() {
        executorService.shutdown();
    }
}
