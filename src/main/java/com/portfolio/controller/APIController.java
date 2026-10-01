package com.portfolio.controller;

import com.portfolio.service.ApiProxyService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api-requests")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class APIController {
    @Autowired
    private final ApiProxyService apiProxyService;

    @PostMapping
    public ResponseEntity<?> testApi(@RequestBody Map<String, Object> request) {
        return ResponseEntity.ok(
                apiProxyService.execute(request)
        );
    }
}
