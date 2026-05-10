package presentation.rest.controller;

import infrastructure.persistence.json.JsonValue;

import java.util.Map;

/**
 * Shared helper utilities for controllers.
 *
 * <p>Keeps repetitive field-extraction code out of individual controllers (DRY principle).
 * Package-private — only accessible within the controller package.
 */
final class ControllerUtils {

    private ControllerUtils() {}

    static String str(JsonValue.JsonObject body, String key) {
        JsonValue val = body.fields().get(key);
        return (val instanceof JsonValue.JsonString s) ? s.value() : "";
    }

    static int intVal(JsonValue.JsonObject body, String key) {
        JsonValue val = body.fields().get(key);
        return (val instanceof JsonValue.JsonNumber n) ? (int) n.value() : 0;
    }

    static boolean boolVal(JsonValue.JsonObject body, String key) {
        JsonValue val = body.fields().get(key);
        return (val instanceof JsonValue.JsonBool b) && b.value();
    }

    static boolean hasField(JsonValue.JsonObject body, String key) {
        return body.fields().containsKey(key);
    }
}
