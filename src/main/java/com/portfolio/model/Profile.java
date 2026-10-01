package com.portfolio.model;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * profile.json — served as-is by GET /api/profile.
 *
 * {@code github} / {@code linkedin} may be full URLs or bare "host/path". {@code blog} is optional.
 * {@code stats} is rendered key-by-key by the About app, so keep it to the items you
 * want shown there.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record Profile(
        String username,
        String name,
        String speciality,
        String bio,
        String college,
        String openTo,
        String email,
        String linkedin,
        String github,
        String blog,
        Stats stats) {

    public record Stats(String location, String status, String focus, String education) {}
}
