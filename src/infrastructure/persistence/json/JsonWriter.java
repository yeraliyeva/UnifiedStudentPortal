package infrastructure.persistence.json;

import java.util.List;
import java.util.Map;

public final class JsonWriter {

    public static String write(JsonValue value) {
        StringBuilder sb = new StringBuilder();
        write(value, sb, 0);
        return sb.toString();
    }

    private static void write(JsonValue v, StringBuilder sb, int indent) {
        if (v instanceof JsonValue.JsonNull) sb.append("null");
        else if (v instanceof JsonValue.JsonString s) sb.append('"').append(escape(s.value())).append('"');
        else if (v instanceof JsonValue.JsonNumber n) appendNumber(n.value(), sb);
        else if (v instanceof JsonValue.JsonBool b) sb.append(b.value());
        else if (v instanceof JsonValue.JsonArray a) writeArray(a.items(), sb, indent);
        else if (v instanceof JsonValue.JsonObject o) writeObject(o.fields(), sb, indent);
    }

    private static void writeArray(List<JsonValue> items, StringBuilder sb, int indent) {
        if (items.isEmpty()) { sb.append("[]"); return; }
        sb.append("[\n");
        for (int i = 0; i < items.size(); i++) {
            indent(sb, indent + 1);
            write(items.get(i), sb, indent + 1);
            if (i < items.size() - 1) sb.append(',');
            sb.append('\n');
        }
        indent(sb, indent);
        sb.append(']');
    }

    private static void writeObject(Map<String, JsonValue> fields, StringBuilder sb, int indent) {
        if (fields.isEmpty()) { sb.append("{}"); return; }
        sb.append("{\n");
        int i = 0;
        for (Map.Entry<String, JsonValue> e : fields.entrySet()) {
            indent(sb, indent + 1);
            sb.append('"').append(escape(e.getKey())).append("\": ");
            write(e.getValue(), sb, indent + 1);
            if (i < fields.size() - 1) sb.append(',');
            sb.append('\n');
            i++;
        }
        indent(sb, indent);
        sb.append('}');
    }

    private static void indent(StringBuilder sb, int n) { for (int i = 0; i < n; i++) sb.append("  "); }

    private static void appendNumber(double n, StringBuilder sb) {
        if (n == Math.floor(n) && !Double.isInfinite(n)) sb.append((long) n);
        else sb.append(n);
    }

    private static String escape(String s) {
        StringBuilder sb = new StringBuilder(s.length() + 8);
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '"' -> sb.append("\\\"");
                case '\\' -> sb.append("\\\\");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                case '\b' -> sb.append("\\b");
                case '\f' -> sb.append("\\f");
                default -> {
                    if (c < 0x20) sb.append(String.format("\\u%04x", (int) c));
                    else sb.append(c);
                }
            }
        }
        return sb.toString();
    }
}
