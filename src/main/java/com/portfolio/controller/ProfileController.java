package com.portfolio.controller;

import com.portfolio.model.Profile;
import com.portfolio.service.PortfolioDataService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/** GET /api/profile — name, bio, contact links, stats (profile.json). */
@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final PortfolioDataService data;

    @GetMapping
    public Profile get() {
        return data.profile();
    }
}
