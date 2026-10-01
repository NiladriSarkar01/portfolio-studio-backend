package com.portfolio.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.portfolio.model.CommandResponse;
import com.portfolio.model.Project;
import com.portfolio.model.Resume;
import com.portfolio.storage.JsonFileStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class CommandServiceTest {

    @TempDir Path dir;
    PortfolioDataService data;
    ProjectService projects;
    ResumeService resumes;
    CommandService service;

    @BeforeEach
    void setUp() {
        // Real JSON defaults bundled in src/main/resources/data, copied into a temp dir.
        JsonFileStore store = new JsonFileStore(new ObjectMapper(), dir.toString());
        data = new PortfolioDataService(store);
        projects = new ProjectService(store);
        resumes = new ResumeService(store);
        service = new CommandService(data, projects, resumes);
    }

    private String out(String cmd) { return service.execute(cmd).getOutput(); }

    @Test void helpReturnsOutput()        { assertThat(out("help")).contains("Available Commands"); }
    @Test void whoamiReadsProfileJson()   { assertThat(out("whoami")).contains("Niladri Sarkar").contains("Backend / Full-Stack Developer"); }
    @Test void lsListsSections()          { assertThat(out("ls")).contains("projects"); }
    @Test void unknownCommandNotFound()   { assertThat(out("badcmd")).contains("command not found"); }
    @Test void clearReturnsMarker()       { assertThat(out("clear")).isEqualTo("__CLEAR__"); }
    @Test void blankCommandIsEmpty()      { assertThat(out("")).isEqualTo(""); }
    @Test void catMissingArgErrors()      { assertThat(out("cat")).contains("missing operand"); }
    @Test void openProjectsHasAction()    {
        CommandResponse r = service.execute("open projects");
        assertThat(r.getAction()).isEqualTo("openApp");
        assertThat(r.getAppId()).isEqualTo("projects");
    }

    @Test
    void catSkillsDrawsBarsFromLevels() {
        assertThat(out("cat skills"))
                .contains("DEVOPS").contains("Docker").contains("55%")
                .contains("Spring Boot").contains("78%");
    }

    @Test
    void skillWithoutLevelIsListedWithoutBar() throws Exception {
        out("cat skills");   // seed
        Files.writeString(dir.resolve("skills.json"),
                "{\"skills\":{\"languages\":[{\"name\":\"Rust\",\"description\":\"d\"}]},\"tools\":[],\"environment\":{}}");
        assertThat(out("cat skills")).contains("Rust").doesNotContain("%");
    }

    @Test
    void linkCommandsHandleFullUrlsWithoutDoublingTheScheme() {
        assertThat(out("github")).contains("Opening https://github.com/NiladriSarkar01").doesNotContain("https://https://");
        assertThat(out("linkedin")).doesNotContain("https://https://");
    }

    @Test
    void blogCommandSaysSoWhenNoBlogIsConfigured() {
        assertThat(out("blog")).contains("No blog configured");
    }

    // ── single source of truth: terminal output must match what the REST API serves ──

    @Test
    void terminalProjectsMatchApiProjects() {
        String terminal = out("cat projects");
        for (Project p : projects.getAll()) assertThat(terminal).contains(p.getName());
    }

    @Test
    void terminalResumeMatchesApiResume() {
        String terminal = out("cat resume");
        Resume r = resumes.get();
        for (Resume.Experience e : r.experience()) assertThat(terminal).contains(e.company());
        for (Resume.Education e : r.education()) assertThat(terminal).contains(e.institution());
        for (Resume.Certification c : r.certifications()) assertThat(terminal).contains(c.name());
    }

    @Test
    void terminalContactMatchesApiProfile() {
        String terminal = out("cat contact");
        assertThat(terminal).contains(data.profile().email()).contains(data.profile().github());
    }

    @Test
    void editingAJsonFileChangesTheTerminalWithoutRestart() throws Exception {
        out("cat projects");   // seeds projects.json into the temp dir
        Files.writeString(dir.resolve("projects.json"),
                "[{\"id\":1,\"name\":\"Brand New Project\",\"description\":\"d\",\"tags\":[\"x\"]}]");
        assertThat(out("cat projects")).contains("Brand New Project").doesNotContain("ArogyaGenie");
    }
}
