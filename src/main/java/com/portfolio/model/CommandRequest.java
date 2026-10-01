package com.portfolio.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Incoming terminal command from React */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommandRequest {
    private String command;
}
