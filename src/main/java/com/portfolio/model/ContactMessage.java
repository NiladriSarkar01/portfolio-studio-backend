package com.portfolio.model;

/** A contact-form submission, appended to messages.json. */
public record ContactMessage(
        String id,
        String name,
        String email,
        String subject,
        String message,
        String receivedAt) {}
