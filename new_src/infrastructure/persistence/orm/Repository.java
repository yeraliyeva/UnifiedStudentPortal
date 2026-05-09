package infrastructure.persistence.orm;

import infrastructure.persistence.database.Database;
import infrastructure.persistence.json.JsonValue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public final class Repository<T, ID> {
    private final Database db;
    private final String tableName;
    private final EntityMapper<T, ID> mapper;

    public Repository(Database db, String tableName, EntityMapper<T, ID> mapper) {
        this.db = db;
        this.tableName = tableName;
        this.mapper = mapper;
    }

    public T save(T entity) {
        String id = mapper.idAsString(mapper.idOf(entity));
        List<JsonValue> rows = db.readTable(tableName);
        rows.removeIf(r -> idFromRow(r).equals(id));
        rows.add(mapper.toJson(entity));
        db.writeTable(tableName, rows);
        return entity;
    }

    public T create(T entity) { return save(entity); }
    public T update(T entity) { return save(entity); }

    public Optional<T> find(ID id) { return findById(id); }

    public Optional<T> findById(ID id) {
        String key = mapper.idAsString(id);
        return db.readTable(tableName).stream()
                .filter(r -> idFromRow(r).equals(key))
                .findFirst()
                .map(mapper::fromJson);
    }

    public List<T> findAll() {
        List<T> out = new ArrayList<>();
        for (JsonValue v : db.readTable(tableName)) out.add(mapper.fromJson(v));
        return Collections.unmodifiableList(out);
    }

    public Optional<T> findFirst(Predicate<T> filter) {
        return findAll().stream().filter(filter).findFirst();
    }

    public List<T> findAllMatching(Predicate<T> filter) {
        return findAll().stream().filter(filter).toList();
    }

    public boolean existsById(ID id) {
        String key = mapper.idAsString(id);
        return db.readTable(tableName).stream().anyMatch(r -> idFromRow(r).equals(key));
    }

    public boolean delete(T entity) { return deleteById(mapper.idOf(entity)); }

    public boolean deleteById(ID id) {
        String key = mapper.idAsString(id);
        List<JsonValue> rows = db.readTable(tableName);
        boolean removed = rows.removeIf(r -> idFromRow(r).equals(key));
        if (removed) db.writeTable(tableName, rows);
        return removed;
    }

    public int deleteAll() {
        int n = db.readTable(tableName).size();
        db.writeTable(tableName, new ArrayList<>());
        return n;
    }

    public int count() { return db.readTable(tableName).size(); }

    public QueryBuilder<T, ID> select() { return new QueryBuilder<>(db, tableName, mapper); }

    public QueryBuilder<T, ID> where(String field, Op op, Object value) {
        return select().where(field, op, value);
    }
    public QueryBuilder<T, ID> whereEq(String field, Object value) {
        return select().whereEq(field, value);
    }

    private String idFromRow(JsonValue row) {
        return row.asObject().get("_id").asString();
    }
}
