package infrastructure.persistence.mapper;

import domain.enums.MessageStatus;
import domain.enums.UrgencyLevel;
import domain.messaging.Message;
import domain.shared.Username;
import infrastructure.persistence.json.JsonObjectBuilder;
import infrastructure.persistence.json.JsonValue;
import infrastructure.persistence.orm.EntityMapper;
import infrastructure.persistence.orm.MapperHelpers;

public final class MessageMapper implements EntityMapper<Message, Integer> {

    @Override public Integer idOf(Message m) { return m.id(); }
    @Override public String idAsString(Integer id) { return id.toString(); }

    @Override public JsonValue toJson(Message m) {
        return JsonObjectBuilder.create()
                .put("_id", Integer.toString(m.id()))
                .put("id", m.id())
                .put("sender", m.sender().value())
                .put("recipient", m.recipient().value())
                .put("subject", m.subject())
                .put("body", m.body())
                .put("urgency", m.urgency().name())
                .put("status", m.status().name())
                .put("sentAt", m.sentAt().toString())
                .build();
    }

    @Override public Message fromJson(JsonValue json) {
        JsonValue.JsonObject o = (JsonValue.JsonObject) json;
        Message m = new Message(MapperHelpers.readInt(o, "id"),
                new Username(MapperHelpers.readString(o, "sender")),
                new Username(MapperHelpers.readString(o, "recipient")),
                MapperHelpers.readString(o, "subject"),
                MapperHelpers.readString(o, "body"),
                Enum.valueOf(UrgencyLevel.class, MapperHelpers.readString(o, "urgency")));
        if ("READ".equals(MapperHelpers.readString(o, "status"))) m.markRead();
        return m;
    }
}
