package infrastructure.persistence.mapper;

import domain.research.JournalName;
import domain.research.PaperId;
import domain.research.ResearchPaper;
import domain.shared.Username;
import infrastructure.persistence.json.JsonObjectBuilder;
import infrastructure.persistence.json.JsonValue;
import infrastructure.persistence.orm.EntityMapper;
import infrastructure.persistence.orm.MapperHelpers;

public final class ResearchPaperMapper implements EntityMapper<ResearchPaper, PaperId> {

    @Override public PaperId idOf(ResearchPaper p) { return p.id(); }
    @Override public String idAsString(PaperId id) { return Integer.toString(id.value()); }

    @Override public JsonValue toJson(ResearchPaper p) {
        return JsonObjectBuilder.create()
                .put("_id", Integer.toString(p.id().value()))
                .put("id", p.id().value())
                .put("title", p.title())
                .put("author", p.author().value())
                .put("journal", p.journal().value())
                .put("abstract", p.abstractText())
                .put("pages", p.pages())
                .put("doi", p.doi())
                .put("citations", p.citations())
                .build();
    }

    @Override public ResearchPaper fromJson(JsonValue json) {
        JsonValue.JsonObject o = (JsonValue.JsonObject) json;
        ResearchPaper p = new ResearchPaper(new PaperId(MapperHelpers.readInt(o, "id")),
                MapperHelpers.readString(o, "title"),
                new Username(MapperHelpers.readString(o, "author")),
                new JournalName(MapperHelpers.readString(o, "journal")),
                MapperHelpers.readString(o, "abstract"),
                MapperHelpers.readIntOr(o, "pages", 0),
                MapperHelpers.readString(o, "doi"));
        int cites = MapperHelpers.readIntOr(o, "citations", 0);
        for (int i = 0; i < cites; i++) p.cite();
        return p;
    }
}
