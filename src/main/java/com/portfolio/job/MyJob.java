package com.portfolio.job;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClient;

import java.util.Map;
import java.util.Objects;

class PostClient{
    private String status;
    private String message;

    // MANDATORY getters and setters for Jackson
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}


@Slf4j
@Component
public class MyJob {

    private final RestClient restClient;

    MyJob (RestClient restClient){
        this.restClient = restClient;
    }

    @Scheduled(fixedDelay = 10000*60)
    public void callMe(){
        PostClient res = restClient.get()
                .uri("https://portfolio-studio-backend.onrender.com/health")
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, (request, response) -> {
                    // Handle 4xx errors (e.g., 404, 401)
                    throw new HttpClientErrorException(response.getStatusCode());
                })
                .onStatus(HttpStatusCode::is5xxServerError, (request, response) -> {
                    // Handle 5xx errors (e.g., 500, 503)
                    throw new HttpServerErrorException(response.getStatusCode());
                })
                .body(PostClient.class);
        assert res != null;
        log.info(res.getMessage());
    }
}
