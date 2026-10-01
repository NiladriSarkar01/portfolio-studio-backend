package com.portfolio.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.portfolio.model.Profile;
import com.portfolio.model.SkillsData;
import com.portfolio.storage.JsonFileStore;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/** Read access to profile.json and skills.json. */
@Service
@RequiredArgsConstructor
public class PortfolioDataService {

    private final JsonFileStore store;

    public Profile profile() {
        return store.read("profile.json", new TypeReference<Profile>() {});
    }

    public SkillsData skills() {
        return store.read("skills.json", new TypeReference<SkillsData>() {});
    }
}
