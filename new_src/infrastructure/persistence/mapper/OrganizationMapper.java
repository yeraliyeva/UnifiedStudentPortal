package infrastructure.persistence.mapper;

import domain.organization.Organization;
import domain.shared.Username;
import infrastructure.persistence.json.JsonObjectBuilder;
import infrastructure.persistence.json.JsonValue;
import infrastructure.persistence.orm.EntityMapper;
import infrastructure.persistence.orm.MapperHelpers;

public final class OrganizationMapper implements EntityMapper<Organization, String> {

    @Override public String idOf(Organization o) { return o.name().toLowerCase(); }
    @Override public String idAsString(String id) { return id; }

    @Override public JsonValue toJson(Organization o) {
        return JsonObjectBuilder.create()
                .put("_id", o.name().toLowerCase())
                .put("name", o.name())
                .put("head", o.head().value())
                .putStrings("members", o.members().stream().map(Username::value).toList())
                .build();
    }

    @Override public Organization fromJson(JsonValue json) {
        JsonValue.JsonObject o = (JsonValue.JsonObject) json;
        Organization org = new Organization(MapperHelpers.readString(o, "name"),
                new Username(MapperHelpers.readString(o, "head")));
        for (String m : MapperHelpers.readStrings(o, "members")) org.addMember(new Username(m));
        return org;
    }
}
