package com.portfolio.model;

import java.util.List;
import java.util.Map;

/** skills.json — served as-is by GET /api/skills. */
public record SkillsData(
        Map<String, List<Skill>> skills,      // category -> skills (insertion order kept)
        List<String> tools,
        Map<String, String> environment) {}
