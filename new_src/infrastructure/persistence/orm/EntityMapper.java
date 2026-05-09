package infrastructure.persistence.orm;

import infrastructure.persistence.json.JsonValue;

public interface EntityMapper<T, ID> {
    JsonValue toJson(T entity);
    T fromJson(JsonValue json);
    ID idOf(T entity);
    String idAsString(ID id);
}
