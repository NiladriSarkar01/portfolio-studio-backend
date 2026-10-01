package com.portfolio.model;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

/** resume.json — served as-is by GET /api/resume. */
public record Resume(
        List<Experience> experience,
        List<Education> education,
        List<Certification> certifications) {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record Experience(String title, String company, String period, String type, List<String> points) {}

    public record Education(String degree, String institution, String period, String notes) {}

    public record Certification(String name, String issuer, String year) {}
}
