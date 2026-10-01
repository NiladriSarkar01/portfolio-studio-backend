package com.portfolio.storage;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.portfolio.model.ContactMessage;
import com.portfolio.model.Profile;
import com.portfolio.model.Project;
import com.portfolio.model.Resume;
import com.portfolio.model.SkillsData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JsonFileStoreTest {

    @TempDir Path dir;
    JsonFileStore store;

    @BeforeEach
    void setUp() { store = new JsonFileStore(new ObjectMapper(), dir.toString()); }

    @Test
    void missingFileIsCreatedFromBundledDefault() {
        List<Project> projects = store.read("projects.json", new TypeReference<List<Project>>() {});
        assertThat(projects).hasSize(11);
        assertThat(projects.get(0).getTags()).containsExactly("Java", "JavaFX", "Spring Boot", "Linux", "PTY4J", "Desktop");
        assertThat(projects.get(0).getTagsRaw()).isEqualTo("Java,JavaFX,Spring Boot,Linux,PTY4J,Desktop");
        assertThat(projects.get(0).getCategory()).isEqualTo("Developer Tools");
        assertThat(dir.resolve("projects.json")).exists();
    }

    @Test
    void everyBundledDefaultParsesIntoItsModel() {
        Profile profile = store.read("profile.json", new TypeReference<Profile>() {});
        SkillsData skills = store.read("skills.json", new TypeReference<SkillsData>() {});
        Resume resume = store.read("resume.json", new TypeReference<Resume>() {});
        List<ContactMessage> messages = store.read("messages.json", new TypeReference<List<ContactMessage>>() {});

        assertThat(profile.name()).isNotBlank();
        assertThat(profile.stats().location()).isNotBlank();
        assertThat(skills.skills()).containsKeys("languages", "frontend", "backend", "databases", "devops");
        assertThat(skills.tools()).isNotEmpty();
        assertThat(skills.environment()).containsKey("runtime");
        assertThat(resume.experience()).isNotEmpty();
        assertThat(resume.experience().get(0).points()).isNotEmpty();
        assertThat(messages).isEmpty();
    }

    @Test
    void existingFileIsNotOverwritten() throws Exception {
        Files.writeString(dir.resolve("projects.json"),
                "[{\"id\":1,\"name\":\"Mine\",\"tags\":[\"x\"]}]");
        List<Project> projects = store.read("projects.json", new TypeReference<List<Project>>() {});
        assertThat(projects).extracting(Project::getName).containsExactly("Mine");
    }

    @Test
    void updateAppendsAndPersists() {
        TypeReference<List<ContactMessage>> type = new TypeReference<List<ContactMessage>>() {};
        store.update("messages.json", type, cur -> {
            List<ContactMessage> n = new ArrayList<>(cur);
            n.add(new ContactMessage("1", "Ann", "a@b.c", "Hi", "Hello", "2026-10-01T00:00:00Z"));
            return n;
        });
        // a brand-new store instance sees it => it really hit the disk
        List<ContactMessage> again = new JsonFileStore(new ObjectMapper(), dir.toString()).read("messages.json", type);
        assertThat(again).extracting(ContactMessage::name).containsExactly("Ann");
    }

    @Test
    void unknownFileWithNoDefaultFails() {
        assertThatThrownBy(() -> store.read("nope.json", new TypeReference<List<String>>() {}))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void pathTraversalIsRejected() {
        assertThatThrownBy(() -> store.read("../evil.json", new TypeReference<List<String>>() {}))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
