package infrastructure.persistence.mapper;

import domain.messaging.News;
import domain.shared.Username;
import infrastructure.persistence.json.JsonObjectBuilder;
import infrastructure.persistence.json.JsonValue;
import infrastructure.persistence.orm.EntityMapper;
import infrastructure.persistence.orm.MapperHelpers;

public final class NewsMapper implements EntityMapper<News, Integer> {

    @Override public Integer idOf(News n) { return n.id(); }
    @Override public String idAsString(Integer id) { return id.toString(); }

    @Override public JsonValue toJson(News n) {
        return JsonObjectBuilder.create()
                .put("_id", Integer.toString(n.id()))
                .put("id", n.id())
                .put("title", n.title())
                .put("body", n.body())
                .put("author", n.author().value())
                .putStrings("comments", n.comments())
                .build();
    }

    @Override public News fromJson(JsonValue json) {
        JsonValue.JsonObject o = (JsonValue.JsonObject) json;
        News n = new News(MapperHelpers.readInt(o, "id"),
                MapperHelpers.readString(o, "title"),
                MapperHelpers.readString(o, "body"),
                new Username(MapperHelpers.readString(o, "author")));
        for (String c : MapperHelpers.readStrings(o, "comments")) n.addComment(c);
        return n;
    }
}
