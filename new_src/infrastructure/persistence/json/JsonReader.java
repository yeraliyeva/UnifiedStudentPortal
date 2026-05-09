package infrastructure.persistence.json;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class JsonReader {

    public static JsonValue parse(String text) {
        JsonReader r = new JsonReader(text);
        r.skipWs();
        if (r.eof()) return JsonValue.ofNull();
        JsonValue v = r.readValue();
        r.skipWs();
        if (!r.eof()) throw new IllegalStateException("Trailing data at pos " + r.pos);
        return v;
    }

    private final String src;
    private int pos;

    private JsonReader(String src) { this.src = src; }

    private boolean eof() { return pos >= src.length(); }
    private char peek() { return src.charAt(pos); }
    private char take() { return src.charAt(pos++); }

    private void skipWs() {
        while (!eof() && Character.isWhitespace(peek())) pos++;
    }

    private void expect(char expected) {
        if (eof() || peek() != expected) throw fail("Expected '" + expected + "'");
        pos++;
    }

    private JsonValue readValue() {
        skipWs();
        char c = peek();
        return switch (c) {
            case '{' -> readObject();
            case '[' -> readArray();
            case '"' -> readString();
            case 't', 'f' -> readBool();
            case 'n' -> readNull();
            default -> readNumber();
        };
    }

    private JsonValue.JsonObject readObject() {
        expect('{'); skipWs();
        Map<String, JsonValue> fields = new LinkedHashMap<>();
        if (!eof() && peek() == '}') { pos++; return new JsonValue.JsonObject(fields); }
        while (true) {
            skipWs();
            String key = readString().asString();
            skipWs(); expect(':'); skipWs();
            fields.put(key, readValue());
            skipWs();
            if (!eof() && peek() == ',') { pos++; continue; }
            expect('}'); return new JsonValue.JsonObject(fields);
        }
    }

    private JsonValue.JsonArray readArray() {
        expect('['); skipWs();
        List<JsonValue> items = new ArrayList<>();
        if (!eof() && peek() == ']') { pos++; return new JsonValue.JsonArray(items); }
        while (true) {
            skipWs();
            items.add(readValue());
            skipWs();
            if (!eof() && peek() == ',') { pos++; continue; }
            expect(']'); return new JsonValue.JsonArray(items);
        }
    }

    private JsonValue.JsonString readString() {
        expect('"');
        StringBuilder sb = new StringBuilder();
        while (true) {
            if (eof()) throw fail("Unterminated string");
            char c = take();
            if (c == '"') return new JsonValue.JsonString(sb.toString());
            if (c == '\\') {
                if (eof()) throw fail("Bad escape");
                char esc = take();
                switch (esc) {
                    case '"' -> sb.append('"');
                    case '\\' -> sb.append('\\');
                    case '/' -> sb.append('/');
                    case 'n' -> sb.append('\n');
                    case 'r' -> sb.append('\r');
                    case 't' -> sb.append('\t');
                    case 'b' -> sb.append('\b');
                    case 'f' -> sb.append('\f');
                    case 'u' -> {
                        if (pos + 4 > src.length()) throw fail("Bad unicode escape");
                        sb.append((char) Integer.parseInt(src.substring(pos, pos + 4), 16));
                        pos += 4;
                    }
                    default -> throw fail("Unknown escape \\" + esc);
                }
            } else {
                sb.append(c);
            }
        }
    }

    private JsonValue readBool() {
        if (src.startsWith("true", pos)) { pos += 4; return JsonValue.of(true); }
        if (src.startsWith("false", pos)) { pos += 5; return JsonValue.of(false); }
        throw fail("Expected true/false");
    }

    private JsonValue readNull() {
        if (src.startsWith("null", pos)) { pos += 4; return JsonValue.ofNull(); }
        throw fail("Expected null");
    }

    private JsonValue readNumber() {
        int start = pos;
        if (!eof() && peek() == '-') pos++;
        while (!eof() && (Character.isDigit(peek()) || peek() == '.'
                || peek() == 'e' || peek() == 'E' || peek() == '+' || peek() == '-')) pos++;
        if (start == pos) throw fail("Expected number");
        return JsonValue.of(Double.parseDouble(src.substring(start, pos)));
    }

    private RuntimeException fail(String msg) { return new IllegalStateException(msg + " at pos " + pos); }
}
