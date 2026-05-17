package presentation.rest.controller;

import bootstrap.AppContext;
import domain.user.User;
import infrastructure.i18n.PropertiesTranslator;
import infrastructure.persistence.json.JsonObjectBuilder;
import infrastructure.persistence.json.JsonValue;
import presentation.rest.http.HttpRequest;
import presentation.rest.http.HttpResponse;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/** Cross-cutting endpoints: i18n bundle delivery and the lightweight user directory. */
public final class SystemController {
    private final AppContext ctx;

    public SystemController(AppContext ctx) {
        this.ctx = ctx;
    }

    /** GET /api/system/messages — full translation bundle for the active language. */
    public HttpResponse getMessages(HttpRequest request) {
        Map<String, String> allMessages = PropertiesTranslator.INSTANCE.getAll();
        JsonObjectBuilder builder = JsonObjectBuilder.create();
        for (Map.Entry<String, String> entry : allMessages.entrySet()) {
            builder.put(entry.getKey(), entry.getValue());
        }
        return HttpResponse.ok(builder.build());
    }

    /** GET /api/users/directory — minimal {username, fullName, role} entries for autocomplete. */
    public HttpResponse getDirectory(HttpRequest request) {
        Collection<User> users = ctx.userRepository.findAll();
        List<JsonValue> arr = new ArrayList<>();
        for (User u : users) {
            arr.add(JsonObjectBuilder.create()
                    .put("username", u.username().value())
                    .put("fullName", u.name().first() + " " + u.name().last())
                    .put("role",     u.getClass().getSimpleName())
                    .build());
        }
        return HttpResponse.ok(new JsonValue.JsonArray(arr));
    }
}
