package com.portfolio.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.portfolio.model.ContactMessage;
import com.portfolio.model.ContactRequest;
import com.portfolio.storage.JsonFileStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ContactService {

    private static final String FILE = "messages.json";

    private final JavaMailSender mailSender;
    private final JsonFileStore store;

    @Value("${portfolio.contact.recipient}")
    private String recipient;

    public void send(ContactRequest req) {
        // 1. Save first — messages.json is the source of truth, so nothing is lost
        //    even if the SMTP server is down or not configured.
        ContactMessage saved = new ContactMessage(
                UUID.randomUUID().toString(),
                req.getName(), req.getEmail(), req.getSubject(), req.getMessage(),
                Instant.now().toString());

        store.update(FILE, new TypeReference<List<ContactMessage>>() {}, current -> {
            List<ContactMessage> next = new ArrayList<>(current);
            next.add(saved);
            return next;
        });
        log.info("Contact message {} saved to {}", saved.id(), FILE);

        // 2. Best-effort email notification.
//        try {
//            SimpleMailMessage msg = new SimpleMailMessage();
//            msg.setTo(recipient);
//            msg.setSubject("[Portfolio Contact] " + (req.getSubject() != null ? req.getSubject() : "No Subject"));
//            msg.setText("From: " + req.getName() + " <" + req.getEmail() + ">\n\n" + req.getMessage());
//            msg.setReplyTo(req.getEmail());
//            mailSender.send(msg);
//            log.info("Contact email sent from {}", req.getEmail());
//        } catch (Exception e) {
//            log.warn("Message {} saved, but email notification failed: {}", saved.id(), e.getMessage());
//        }
    }
}
