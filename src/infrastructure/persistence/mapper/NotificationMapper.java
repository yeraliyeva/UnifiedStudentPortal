package infrastructure.persistence.mapper;

import domain.messaging.Notification;
import domain.shared.Username;
import infrastructure.persistence.json.JsonObjectBuilder;
import infrastructure.persistence.json.JsonValue;
import infrastructure.persistence.orm.EntityMapper;
import infrastructure.persistence.orm.MapperHelpers;

import java.time.LocalDateTime;
import java.util.UUID;

public final class NotificationMapper implements EntityMapper<Notification, String> {

    @Override public String idOf(Notification n) {
        return n.recipient().value() + "|" + n.at() + "|" + UUID.nameUUIDFromBytes(n.text().getBytes());
    }
    @Override public String idAsString(String id) { return id; }

    @Override public JsonValue toJson(Notification n) {
        return JsonObjectBuilder.create()
                .put("_id", idOf(n))
                .put("recipient", n.recipient().value())
                .put("text", n.text())
                .put("at", n.at().toString())
                .build();
    }

    @Override public Notification fromJson(JsonValue json) {
        JsonValue.JsonObject o = (JsonValue.JsonObject) json;
        return new Notification(new Username(MapperHelpers.readString(o, "recipient")),
                MapperHelpers.readString(o, "text"),
                LocalDateTime.parse(MapperHelpers.readString(o, "at")));
    }
}
