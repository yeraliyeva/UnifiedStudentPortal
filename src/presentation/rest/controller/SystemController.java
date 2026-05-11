package presentation.rest.controller;

import infrastructure.i18n.PropertiesTranslator;
import infrastructure.persistence.json.JsonObjectBuilder;
import infrastructure.persistence.json.JsonValue;
import presentation.rest.http.HttpRequest;
import presentation.rest.http.HttpResponse;

import java.util.Map;

/** Handles system-level endpoints such as providing translations to the frontend. */
public final class SystemController {

    public SystemController() {
    }

    /** GET /api/system/messages */
    public HttpResponse getMessages(HttpRequest request) {
        Map<String, String> allMessages = PropertiesTranslator.INSTANCE.getAll();
        JsonObjectBuilder builder = JsonObjectBuilder.create();
        for (Map.Entry<String, String> entry : allMessages.entrySet()) {
            builder.put(entry.getKey(), entry.getValue());
        }
        return HttpResponse.ok(builder.build());
    }
}
