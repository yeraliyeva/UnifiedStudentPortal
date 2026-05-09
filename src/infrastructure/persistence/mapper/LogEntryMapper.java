package infrastructure.persistence.mapper;

import domain.logging.LogEntry;
import domain.shared.Username;
import infrastructure.persistence.json.JsonObjectBuilder;
import infrastructure.persistence.json.JsonValue;
import infrastructure.persistence.orm.EntityMapper;
import infrastructure.persistence.orm.MapperHelpers;

import java.time.LocalDateTime;
import java.util.UUID;

public final class LogEntryMapper implements EntityMapper<LogEntry, String> {

    @Override public String idOf(LogEntry e) {
        return e.actor().value() + "|" + e.at() + "|" + UUID.nameUUIDFromBytes(e.action().getBytes());
    }
    @Override public String idAsString(String id) { return id; }

    @Override public JsonValue toJson(LogEntry e) {
        return JsonObjectBuilder.create()
                .put("_id", idOf(e))
                .put("at", e.at().toString())
                .put("actor", e.actor().value())
                .put("action", e.action())
                .build();
    }

    @Override public LogEntry fromJson(JsonValue json) {
        JsonValue.JsonObject o = (JsonValue.JsonObject) json;
        return new LogEntry(LocalDateTime.parse(MapperHelpers.readString(o, "at")),
                new Username(MapperHelpers.readString(o, "actor")),
                MapperHelpers.readString(o, "action"));
    }
}
