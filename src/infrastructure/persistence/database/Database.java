package infrastructure.persistence.database;

import infrastructure.persistence.json.JsonValue;

import java.util.List;

public interface Database {
    List<JsonValue> readTable(String table);
    void writeTable(String table, List<JsonValue> rows);
}
