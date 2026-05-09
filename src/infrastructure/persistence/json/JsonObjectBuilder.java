package infrastructure.persistence.json;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class JsonObjectBuilder {
    private final Map<String, JsonValue> fields = new LinkedHashMap<>();

    public static JsonObjectBuilder create() { return new JsonObjectBuilder(); }

    public JsonObjectBuilder put(String key, String value) { fields.put(key, JsonValue.of(value)); return this; }
    public JsonObjectBuilder put(String key, int value) { fields.put(key, JsonValue.of(value)); return this; }
    public JsonObjectBuilder put(String key, long value) { fields.put(key, JsonValue.of(value)); return this; }
    public JsonObjectBuilder put(String key, double value) { fields.put(key, JsonValue.of(value)); return this; }
    public JsonObjectBuilder put(String key, boolean value) { fields.put(key, JsonValue.of(value)); return this; }
    public JsonObjectBuilder put(String key, JsonValue value) { fields.put(key, value); return this; }
    public JsonObjectBuilder putStrings(String key, Iterable<String> values) {
        List<JsonValue> arr = new ArrayList<>();
        for (String v : values) arr.add(JsonValue.of(v));
        fields.put(key, new JsonValue.JsonArray(arr));
        return this;
    }
    public JsonObjectBuilder putObjects(String key, List<JsonValue> objects) {
        fields.put(key, new JsonValue.JsonArray(objects));
        return this;
    }

    public JsonValue.JsonObject build() { return new JsonValue.JsonObject(fields); }
}
