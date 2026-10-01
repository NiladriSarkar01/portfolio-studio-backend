package com.portfolio.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.portfolio.model.Project;
import com.portfolio.storage.JsonFileStore;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private static final String FILE = "projects.json";

    private final JsonFileStore store;

    /** Read on every call, so editing projects.json takes effect without a restart. */
    public List<Project> getAll() {
        return store.read(FILE, new TypeReference<List<Project>>() {});
    }
}
