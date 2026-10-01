package com.portfolio.controller;

import com.portfolio.model.ContactRequest;
import com.portfolio.service.ContactService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/contact")
@RequiredArgsConstructor
public class ContactController {

    private final ContactService contactService;

    /**
     * POST /api/contact
     * Body: { name, email, subject, message }
     * Returns: { status: "sent" } or 400/500
     */
    @PostMapping
    public ResponseEntity<Map<String, String>> send(@Valid @RequestBody ContactRequest request) {
        log.info("Contact form submission from {}", request.getEmail());
        contactService.send(request);
        return ResponseEntity.ok(Map.of("status", "sent", "message", "Message received! I'll get back to you soon."));
    }
}
