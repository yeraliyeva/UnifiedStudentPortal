package infrastructure.persistence.mapper;

import domain.enums.Faculty;
import domain.enums.HelpType;
import domain.enums.RequestStatus;
import domain.enums.UrgencyLevel;
import domain.messaging.Request;
import domain.shared.Username;
import infrastructure.persistence.json.JsonObjectBuilder;
import infrastructure.persistence.json.JsonValue;
import infrastructure.persistence.orm.EntityMapper;
import infrastructure.persistence.orm.MapperHelpers;

public final class RequestMapper implements EntityMapper<Request, Integer> {

    @Override public Integer idOf(Request r) { return r.id(); }
    @Override public String idAsString(Integer id) { return id.toString(); }

    @Override public JsonValue toJson(Request r) {
        return JsonObjectBuilder.create()
                .put("_id", Integer.toString(r.id()))
                .put("id", r.id())
                .put("requester", r.requester().value())
                .put("type", r.type().name())
                .put("faculty", r.faculty().name())
                .put("urgency", r.urgency().name())
                .put("additionalInfo", r.additionalInfo())
                .put("status", r.status().name())
                .build();
    }

    @Override public Request fromJson(JsonValue json) {
        JsonValue.JsonObject o = (JsonValue.JsonObject) json;
        Request r = new Request(MapperHelpers.readInt(o, "id"),
                new Username(MapperHelpers.readString(o, "requester")),
                Enum.valueOf(HelpType.class, MapperHelpers.readString(o, "type")),
                Enum.valueOf(Faculty.class, MapperHelpers.readString(o, "faculty")),
                Enum.valueOf(UrgencyLevel.class, MapperHelpers.readString(o, "urgency")),
                MapperHelpers.readString(o, "additionalInfo"));
        r.changeStatus(Enum.valueOf(RequestStatus.class, MapperHelpers.readString(o, "status")));
        return r;
    }
}
