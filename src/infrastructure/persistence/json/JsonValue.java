package infrastructure.persistence.json;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public sealed interface JsonValue {

    final class JsonNull implements JsonValue {
        public static final JsonNull INSTANCE = new JsonNull();
        private JsonNull() {}
    }
    record JsonString(String value) implements JsonValue {}
    record JsonNumber(double value) implements JsonValue {}
    record JsonBool(boolean value) implements JsonValue {}
    record JsonArray(List<JsonValue> items) implements JsonValue {}
    record JsonObject(Map<String, JsonValue> fields) implements JsonValue {}

    static JsonValue of(String s) { return s == null ? JsonNull.INSTANCE : new JsonString(s); }
    static JsonValue of(int n) { return new JsonNumber(n); }
    static JsonValue of(long n) { return new JsonNumber(n); }
    static JsonValue of(double n) { return new JsonNumber(n); }
    static JsonValue of(boolean b) { return new JsonBool(b); }
    static JsonValue ofNull() { return JsonNull.INSTANCE; }

    static JsonObject obj() { return new JsonObject(new LinkedHashMap<>()); }
    static JsonArray arr() { return new JsonArray(new ArrayList<>()); }

    default String asString() { return ((JsonString) this).value(); }
    default int asInt() { return (int) ((JsonNumber) this).value(); }
    default long asLong() { return (long) ((JsonNumber) this).value(); }
    default double asDouble() { return ((JsonNumber) this).value(); }
    default boolean asBool() { return ((JsonBool) this).value(); }
    default List<JsonValue> asArray() { return ((JsonArray) this).items(); }
    default Map<String, JsonValue> asObject() { return ((JsonObject) this).fields(); }
    default boolean isNull() { return this instanceof JsonNull; }
}
