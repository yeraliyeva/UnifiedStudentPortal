package infrastructure.persistence.orm;

import infrastructure.persistence.json.JsonObjectBuilder;
import infrastructure.persistence.json.JsonValue;

import java.util.ArrayList;
import java.util.List;

public final class MapperHelpers {
    private MapperHelpers() {}

    public static JsonValue.JsonObject withId(JsonObjectBuilder builder, String id) {
        return builder.put("_id", id).build();
    }

    public static List<String> readStrings(JsonValue.JsonObject obj, String key) {
        if (!obj.fields().containsKey(key)) return List.of();
        JsonValue v = obj.fields().get(key);
        if (v.isNull()) return List.of();
        List<String> out = new ArrayList<>();
        for (JsonValue item : v.asArray()) out.add(item.asString());
        return out;
    }

    public static String readString(JsonValue.JsonObject obj, String key) {
        JsonValue v = obj.fields().get(key);
        return (v == null || v.isNull()) ? null : v.asString();
    }

    public static int readInt(JsonValue.JsonObject obj, String key) {
        return obj.fields().get(key).asInt();
    }

    public static int readIntOr(JsonValue.JsonObject obj, String key, int defaultValue) {
        JsonValue v = obj.fields().get(key);
        return (v == null || v.isNull()) ? defaultValue : v.asInt();
    }

    public static double readDoubleOr(JsonValue.JsonObject obj, String key, double defaultValue) {
        JsonValue v = obj.fields().get(key);
        return (v == null || v.isNull()) ? defaultValue : v.asDouble();
    }

    public static boolean readBoolOr(JsonValue.JsonObject obj, String key, boolean defaultValue) {
        JsonValue v = obj.fields().get(key);
        return (v == null || v.isNull()) ? defaultValue : v.asBool();
    }
}
