package com.portfolio.model;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Terminal command response sent back to React.
 *
 * Fields:
 *   output  — ANSI-escaped string to write to xterm.js
 *   action  — optional: "openApp" tells React to open a window
 *   appId   — which app to open: "projects" | "resume" | "contact" | "skills"
 */
@Data
@NoArgsConstructor
public class CommandResponse {

    private String output;
    private String action;   // nullable
    private String appId;    // nullable

    public CommandResponse(String output) {
        this.output = output;
    }

    public static CommandResponse openApp(String output, String appId) {
        CommandResponse r = new CommandResponse(output);
        r.action = "openApp";
        r.appId  = appId;
        return r;
    }
}
