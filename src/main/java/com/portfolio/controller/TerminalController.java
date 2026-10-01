package com.portfolio.controller;

import com.portfolio.model.CommandRequest;
import com.portfolio.model.CommandResponse;
import com.portfolio.service.CommandService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/terminal")
@RequiredArgsConstructor
public class TerminalController {

    private final CommandService commandService;

    /**
     * POST /api/terminal
     * Body: { "command": "help" }
     * Response: { "output": "...", "action": null, "appId": null }
     */
    @PostMapping
    public CommandResponse execute(@RequestBody CommandRequest request) {
        log.debug("Terminal command received: {}", request.getCommand());
        return commandService.execute(request.getCommand());
    }
}
