package com.portfolio.model;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * One skill. {@code level} (0-100) is optional: the terminal draws a bar when it is set,
 * and just lists the name when it is null.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record Skill(String name, String description, Integer level) {}
