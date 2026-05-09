package infrastructure.persistence.database;

import infrastructure.persistence.json.JsonReader;
import infrastructure.persistence.json.JsonValue;
import infrastructure.persistence.json.JsonWriter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class JsonFileDatabase implements Database {
    private final Path root;

    public JsonFileDatabase(Path root) {
        this.root = root;
        try { Files.createDirectories(root); }
        catch (IOException e) { throw new RuntimeException("Cannot create db dir " + root, e); }
    }

    @Override public List<JsonValue> readTable(String table) {
        Path file = root.resolve(table + ".json");
        if (!Files.exists(file)) return new ArrayList<>();
        try {
            String text = Files.readString(file);
            if (text.isBlank()) return new ArrayList<>();
            JsonValue v = JsonReader.parse(text);
            if (v.isNull()) return new ArrayList<>();
            return new ArrayList<>(v.asArray());
        } catch (IOException e) {
            throw new RuntimeException("Cannot read table " + table, e);
        }
    }

    @Override public void writeTable(String table, List<JsonValue> rows) {
        Path file = root.resolve(table + ".json");
        String text = JsonWriter.write(new JsonValue.JsonArray(rows));
        try { Files.writeString(file, text); }
        catch (IOException e) { throw new RuntimeException("Cannot write table " + table, e); }
    }
}
