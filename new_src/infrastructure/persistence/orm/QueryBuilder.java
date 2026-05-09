package infrastructure.persistence.orm;

import infrastructure.persistence.database.Database;
import infrastructure.persistence.json.JsonValue;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;

public final class QueryBuilder<T, ID> {
    private final Database db;
    private final String tableName;
    private final EntityMapper<T, ID> mapper;
    private final List<Condition> conditions = new ArrayList<>();
    private Predicate<T> typedPredicate;
    private Comparator<T> ordering;
    private int limit = -1;
    private int offset = 0;

    QueryBuilder(Database db, String tableName, EntityMapper<T, ID> mapper) {
        this.db = db;
        this.tableName = tableName;
        this.mapper = mapper;
    }

    public QueryBuilder<T, ID> where(String field, Op op, Object value) {
        conditions.add(new Condition(field, op, value));
        return this;
    }
    public QueryBuilder<T, ID> whereEq(String field, Object value) { return where(field, Op.EQ, value); }
    public QueryBuilder<T, ID> whereContains(String field, String fragment) { return where(field, Op.CONTAINS, fragment); }
    public QueryBuilder<T, ID> whereGt(String field, Number value) { return where(field, Op.GT, value); }
    public QueryBuilder<T, ID> whereLt(String field, Number value) { return where(field, Op.LT, value); }

    public QueryBuilder<T, ID> whereMatch(Predicate<T> predicate) {
        this.typedPredicate = (this.typedPredicate == null) ? predicate : this.typedPredicate.and(predicate);
        return this;
    }

    public QueryBuilder<T, ID> orderBy(Comparator<T> comparator) { this.ordering = comparator; return this; }
    public QueryBuilder<T, ID> orderByDesc(Comparator<T> comparator) { this.ordering = comparator.reversed(); return this; }
    public QueryBuilder<T, ID> limit(int n) { this.limit = Math.max(0, n); return this; }
    public QueryBuilder<T, ID> offset(int n) { this.offset = Math.max(0, n); return this; }

    public List<T> list() {
        return collect();
    }

    public Optional<T> first() {
        List<T> rows = limit(1).collect();
        return rows.isEmpty() ? Optional.empty() : Optional.of(rows.get(0));
    }

    public int count() {
        return collect().size();
    }

    public boolean exists() {
        return first().isPresent();
    }

    public int deleteAll() {
        List<JsonValue> rows = db.readTable(tableName);
        List<JsonValue> kept = new ArrayList<>();
        int deleted = 0;
        for (JsonValue row : rows) {
            if (matchesJson(row) && (typedPredicate == null || typedPredicate.test(mapper.fromJson(row)))) deleted++;
            else kept.add(row);
        }
        db.writeTable(tableName, kept);
        return deleted;
    }

    public int updateEach(Consumer<T> updater) {
        List<JsonValue> rows = db.readTable(tableName);
        List<JsonValue> next = new ArrayList<>();
        int touched = 0;
        for (JsonValue row : rows) {
            if (matchesJson(row)) {
                T entity = mapper.fromJson(row);
                if (typedPredicate == null || typedPredicate.test(entity)) {
                    updater.accept(entity);
                    next.add(mapper.toJson(entity));
                    touched++;
                    continue;
                }
            }
            next.add(row);
        }
        db.writeTable(tableName, next);
        return touched;
    }

    private List<T> collect() {
        List<T> out = new ArrayList<>();
        for (JsonValue row : db.readTable(tableName)) {
            if (!matchesJson(row)) continue;
            T entity = mapper.fromJson(row);
            if (typedPredicate != null && !typedPredicate.test(entity)) continue;
            out.add(entity);
        }
        if (ordering != null) out.sort(ordering);
        if (offset > 0) out = (offset >= out.size()) ? List.of() : out.subList(offset, out.size());
        if (limit >= 0 && out.size() > limit) out = out.subList(0, limit);
        return new ArrayList<>(out);
    }

    private boolean matchesJson(JsonValue row) {
        JsonValue.JsonObject obj = (JsonValue.JsonObject) row;
        for (Condition c : conditions) if (!c.matches(obj)) return false;
        return true;
    }
}
