package infrastructure.persistence.orm;

import infrastructure.persistence.json.JsonValue;

import java.util.Collection;

public record Condition(String field, Op op, Object value) {

    public boolean matches(JsonValue.JsonObject row) {
        JsonValue cell = row.fields().get(field);
        if (cell == null || cell.isNull()) {
            return op == Op.NEQ && value != null;
        }
        return switch (op) {
            case EQ          -> equalsValue(cell, value);
            case NEQ         -> !equalsValue(cell, value);
            case GT          -> compare(cell) > 0;
            case LT          -> compare(cell) < 0;
            case GTE         -> compare(cell) >= 0;
            case LTE         -> compare(cell) <= 0;
            case CONTAINS    -> stringOf(cell).contains(String.valueOf(value));
            case STARTS_WITH -> stringOf(cell).startsWith(String.valueOf(value));
            case ENDS_WITH   -> stringOf(cell).endsWith(String.valueOf(value));
            case IN          -> isIn(cell, value);
        };
    }

    private boolean equalsValue(JsonValue cell, Object expected) {
        if (expected == null) return cell.isNull();
        if (cell instanceof JsonValue.JsonString s) return s.value().equals(String.valueOf(expected));
        if (cell instanceof JsonValue.JsonNumber n) return n.value() == toDouble(expected);
        if (cell instanceof JsonValue.JsonBool b) return b.value() == Boolean.parseBoolean(String.valueOf(expected));
        return false;
    }

    private int compare(JsonValue cell) {
        if (cell instanceof JsonValue.JsonNumber n) return Double.compare(n.value(), toDouble(value));
        return stringOf(cell).compareTo(String.valueOf(value));
    }

    private String stringOf(JsonValue cell) {
        if (cell instanceof JsonValue.JsonString s) return s.value();
        if (cell instanceof JsonValue.JsonNumber n) return String.valueOf(n.value());
        if (cell instanceof JsonValue.JsonBool b) return String.valueOf(b.value());
        return "";
    }

    private double toDouble(Object v) {
        if (v instanceof Number n) return n.doubleValue();
        return Double.parseDouble(String.valueOf(v));
    }

    private boolean isIn(JsonValue cell, Object value) {
        if (!(value instanceof Collection<?> coll)) return false;
        for (Object candidate : coll) if (equalsValue(cell, candidate)) return true;
        return false;
    }
}
