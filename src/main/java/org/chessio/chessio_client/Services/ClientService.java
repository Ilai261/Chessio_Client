package org.chessio.chessio_client.Services;

import org.chessio.chessio_client.Models.Game;
import org.chessio.chessio_client.Models.LoginRequest;
import org.chessio.chessio_client.Models.RegisterRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
public class ClientService
{

    @Autowired
    private RestTemplate restTemplate;

    @Value("${server.ip}")
    private String serverAddress;

    @Value("${server.port}")
    private int serverPort;

    public ResponseEntity<String> register(RegisterRequest registerRequest)
    {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<RegisterRequest> request = new HttpEntity<>(registerRequest, headers);

        String baseUrl = "https://" + serverAddress + ":" + serverPort;
        String endpoint = "/register";
        String fullUrl = baseUrl + endpoint;

        return restTemplate.postForEntity(fullUrl, request, String.class); // Adjust the endpoint as needed
    }

    public ResponseEntity<String> login(LoginRequest loginRequest)
    {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<LoginRequest> request = new HttpEntity<>(loginRequest, headers);

        String baseUrl = "https://" + serverAddress + ":" + serverPort;
        String endpoint = "/login";
        String fullUrl = baseUrl + endpoint;

        return restTemplate.postForEntity(fullUrl, request, String.class); // Adjust the endpoint as needed
    }

    // relevant for when we implement the game history request
//    public ResponseEntity<List> getPlayerHistory(String username)
//    {
//        HttpHeaders headers = new HttpHeaders();
//        headers.setContentType(MediaType.APPLICATION_JSON);
//
//        // Assuming you have a PlayerHistoryRequest class to encapsulate the username
//        PlayerHistoryRequest requestBody = new PlayerHistoryRequest(username);
//        HttpEntity<PlayerHistoryRequest> request = new HttpEntity<>(requestBody, headers);
//
//        return restTemplate.postForEntity("/api/player-history", request, List.class); // Adjust the endpoint as needed
//    }
}