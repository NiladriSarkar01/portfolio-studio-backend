package com.portfolio.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class ApiProxyService {

    private final RestTemplate restTemplate = new RestTemplate();

    public Object execute(Map<String, Object> request) {

        String url = (String) request.get("url");
        String method = (String) request.getOrDefault("method", "GET");

        HttpHeaders headers = new HttpHeaders();

        HttpEntity<?> entity = new HttpEntity<>(headers);

        HttpMethod httpMethod = HttpMethod.valueOf(method.toUpperCase());

        ResponseEntity<String> response =
                restTemplate.exchange(
                        url,
                        httpMethod,
                        entity,
                        String.class
                );

        return Map.of(
                "status", response.getStatusCode().value(),
                "headers", response.getHeaders(),
                "body", response.getBody()
        );
    }
}