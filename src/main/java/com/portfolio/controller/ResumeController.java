package com.portfolio.controller;

import com.portfolio.model.Resume;
import com.portfolio.service.ResumeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/** GET /api/resume — experience / education / certifications (resume.json). */
@RestController
@RequestMapping("/api/resume")
@RequiredArgsConstructor
public class ResumeController {

    private final ResumeService resumeService;

    @GetMapping
    public Resume getResume() {
        return resumeService.get();
    }
}
