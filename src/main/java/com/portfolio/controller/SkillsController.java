package com.portfolio.controller;

import com.portfolio.model.SkillsData;
import com.portfolio.service.PortfolioDataService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/** GET /api/skills — { skills: {category: [...]}, tools: [...], environment: {...} } (skills.json). */
@RestController
@RequestMapping("/api/skills")
@RequiredArgsConstructor
public class SkillsController {

    private final PortfolioDataService data;

    @GetMapping
    public SkillsData get() {
        return data.skills();
    }
}
