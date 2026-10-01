package com.portfolio.controller;

import com.portfolio.model.Project;
import com.portfolio.service.ProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    /** GET /api/projects */
    @GetMapping
    public List<Project> getAll() {
        return projectService.getAll();
    }
}
