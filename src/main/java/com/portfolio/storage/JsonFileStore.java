package com.portfolio.storage;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.function.UnaryOperator;

/**
 * Tiny JSON-file "database".
 *
 * Every file lives in {@code portfolio.data-dir}. If a file is missing it is created
 * from the default bundled at {@code classpath:/data/<file>} the first time it is used,
 * so a fresh deploy works out of the box and you can edit the files by hand afterwards.
 *
 * Writes go to a temp file first and are then moved into place, so a crash mid-write
 * can never leave a half-written JSON file behind.
 */
@Slf4j
@Component
public class JsonFileStore {

    private final ObjectMapper mapper;
    private final Path dataDir;

    public JsonFileStore(ObjectMapper mapper,
                         @Value("${portfolio.data-dir:./data}") String dataDir) {
        this.mapper = mapper.copy()
                .enable(SerializationFeature.INDENT_OUTPUT)
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        this.dataDir = Path.of(dataDir).toAbsolutePath().normalize();
        log.info("JSON data directory: {}", this.dataDir);
    }

    /** Read and parse a JSON file. */
    public synchronized <T> T read(String file, TypeReference<T> type) {
        Path path = ensureExists(file);
        try {
            return mapper.readValue(path.toFile(), type);
        } catch (IOException e) {
            log.error("Cannot read {}", path, e);
            throw new IllegalStateException("Data storage error");
        }
    }

    /**
     * Atomic read-modify-write: reads the file, applies {@code change}, writes the result
     * back. Use this for anything that appends/edits so concurrent requests can't lose updates.
     */
    public synchronized <T> T update(String file, TypeReference<T> type, UnaryOperator<T> change) {
        Path path = ensureExists(file);
        T next = change.apply(read(file, type));
        write(path, next);
        return next;
    }

    // ── internals ─────────────────────────────────────────────────────────

    private Path ensureExists(String file) {
        Path path = dataDir.resolve(file).normalize();
        if (!path.startsWith(dataDir)) {
            throw new IllegalArgumentException("Invalid data file name");
        }
        if (Files.exists(path)) return path;

        try (InputStream in = new ClassPathResource("data/" + file).getInputStream()) {
            Files.createDirectories(dataDir);
            Files.copy(in, path);
            log.info("Created {} from bundled default", path);
            return path;
        } catch (IOException e) {
            log.error("{} is missing and there is no bundled default", path, e);
            throw new IllegalStateException("Data storage error");
        }
    }

    private void write(Path path, Object value) {
        Path tmp = path.resolveSibling(path.getFileName() + ".tmp");
        try {
            mapper.writeValue(tmp.toFile(), value);
            try {
                Files.move(tmp, path, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException e) {
                Files.move(tmp, path, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            log.error("Cannot write {}", path, e);
            throw new IllegalStateException("Data storage error");
        }
    }
}
