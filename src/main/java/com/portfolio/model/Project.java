package com.portfolio.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

/** A portfolio project, stored in projects.json. */
@Data
// tagsRaw is derived from tags: still sent to the frontend (as before), never read from the file.
@JsonIgnoreProperties(value = "tagsRaw", allowGetters = true)
public class Project {

    private Long id;
    private String name;
    private String description;
    private List<String> tags = List.of();
    private String githubUrl;
    private String demoUrl;
    /** Used by the Projects app's filter tabs, e.g. "Full Stack", "AI / ML". */
    private String category;

    /** Kept so the API response shape is unchanged: "Python,Networking,CTF". */
    public String getTagsRaw() {
        return tags == null ? "" : String.join(",", tags);
    }
}
