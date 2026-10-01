package com.portfolio.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.portfolio.model.Resume;
import com.portfolio.storage.JsonFileStore;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ResumeService {

    private final JsonFileStore store;

    public Resume get() {
        return store.read("resume.json", new TypeReference<Resume>() {});
    }
}
