package com.portfolio.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.portfolio.model.CommandRequest;
import com.portfolio.service.CommandService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.portfolio.model.CommandResponse;

@WebMvcTest(TerminalController.class)
class TerminalControllerTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;
    @MockBean  CommandService commandService;

    @Test
    void helpCommandReturnsOutput() throws Exception {
        when(commandService.execute("help"))
            .thenReturn(new CommandResponse("Available Commands\n..."));

        mvc.perform(post("/api/terminal")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(new CommandRequest("help"))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.output").isNotEmpty());
    }

    @Test
    void unknownCommandReturnsNotFound() throws Exception {
        when(commandService.execute("foobar"))
            .thenReturn(new CommandResponse("bash: foobar: command not found"));

        mvc.perform(post("/api/terminal")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(new CommandRequest("foobar"))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.output").value(org.hamcrest.Matchers.containsString("command not found")));
    }

    @Test
    void openProjectsReturnsAction() throws Exception {
        when(commandService.execute("open projects"))
            .thenReturn(CommandResponse.openApp("Opening projects...", "projects"));

        mvc.perform(post("/api/terminal")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(new CommandRequest("open projects"))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.action").value("openApp"))
            .andExpect(jsonPath("$.appId").value("projects"));
    }
}
