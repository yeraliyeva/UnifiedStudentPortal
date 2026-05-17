package infrastructure.persistence.mapper;

import domain.messaging.Comment;
import domain.messaging.News;
import domain.shared.Username;
import infrastructure.persistence.json.JsonObjectBuilder;
import infrastructure.persistence.json.JsonValue;
import infrastructure.persistence.orm.EntityMapper;
import infrastructure.persistence.orm.MapperHelpers;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public final class NewsMapper implements EntityMapper<News, Integer> {

    @Override public Integer idOf(News n) { return n.id(); }
    @Override public String idAsString(Integer id) { return id.toString(); }

    @Override public JsonValue toJson(News n) {
        List<JsonValue> commentsJson = new ArrayList<>();
        for (Comment c : n.comments()) {
            commentsJson.add(JsonObjectBuilder.create()
                    .put("author", c.author().value())
                    .put("text", c.text())
                    .put("postedAt", c.postedAt().toString())
                    .build());
        }
        return JsonObjectBuilder.create()
                .put("_id", Integer.toString(n.id()))
                .put("id", n.id())
                .put("title", n.title())
                .put("body", n.body())
                .put("author", n.author().value())
                .putObjects("comments", commentsJson)
                .build();
    }

    @Override public News fromJson(JsonValue json) {
        JsonValue.JsonObject o = (JsonValue.JsonObject) json;
        News n = new News(MapperHelpers.readInt(o, "id"),
                MapperHelpers.readString(o, "title"),
                MapperHelpers.readString(o, "body"),
                new Username(MapperHelpers.readString(o, "author")));
        JsonValue raw = o.fields().get("comments");
        if (raw instanceof JsonValue.JsonArray arr) {
            for (JsonValue item : arr.items()) {
                Comment c = readComment(item);
                if (c != null) n.addComment(c);
            }
        }
        return n;
    }

    private static Comment readComment(JsonValue value) {
        if (value instanceof JsonValue.JsonObject obj) {
            String author = MapperHelpers.readString(obj, "author");
            String text = MapperHelpers.readString(obj, "text");
            String posted = MapperHelpers.readString(obj, "postedAt");
            if (author == null || text == null) return null;
            LocalDateTime when = (posted == null || posted.isBlank())
                    ? LocalDateTime.now()
                    : LocalDateTime.parse(posted);
            return new Comment(new Username(author), text, when);
        }
        if (value instanceof JsonValue.JsonString s) {
            String raw = s.value();
            int idx = raw.indexOf(": ");
            String author = idx > 0 ? raw.substring(0, idx) : "unknown";
            String text = idx > 0 ? raw.substring(idx + 2) : raw;
            return new Comment(new Username(author), text, LocalDateTime.now());
        }
        return null;
    }
}
