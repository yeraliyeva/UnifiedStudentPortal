package presentation.rest.controller;

import application.Result;
import bootstrap.AppContext;
import domain.enums.HelpType;
import domain.enums.RequestStatus;
import domain.enums.UrgencyLevel;
import domain.messaging.Comment;
import domain.messaging.Message;
import domain.messaging.News;
import domain.messaging.Order;
import domain.messaging.Request;
import domain.shared.Username;
import domain.user.Dean;
import domain.user.Employee;
import domain.user.Manager;
import domain.user.User;
import infrastructure.persistence.json.JsonObjectBuilder;
import infrastructure.persistence.json.JsonValue;
import presentation.rest.auth.RequestContext;
import presentation.rest.http.HttpRequest;
import presentation.rest.http.HttpResponse;

import java.util.ArrayList;
import java.util.List;

import static presentation.rest.controller.ControllerUtils.*;

/** Handles messages, news, help requests, and IT order endpoints. */
public final class MessagingController {
    private final AppContext ctx;

    public MessagingController(AppContext ctx) {
        this.ctx = ctx;
    }


    /** GET /api/messages/inbox */
    public HttpResponse inbox(HttpRequest request) {
        User user = RequestContext.current();
        List<Message> msgs = ctx.messageRepository.inboxOf(user.username());
        List<JsonValue> arr = new ArrayList<>();
        for (Message m : msgs) arr.add(messageToJson(m));
        return HttpResponse.ok(new JsonValue.JsonArray(arr));
    }

    /** POST /api/messages */
    public HttpResponse sendMessage(HttpRequest request) {
        User user = RequestContext.current();
        JsonValue.JsonObject body = request.body();
        String recipient = str(body, "recipient");
        String subject   = str(body, "subject");
        String msgBody   = str(body, "body");
        String urgency   = str(body, "urgency");

        try {
            UrgencyLevel level = UrgencyLevel.valueOf(urgency.toUpperCase());
            Result result = ctx.sendMessage.execute(user.username(), new Username(recipient),
                    subject, msgBody, level);
            return resultToResponse(result);
        } catch (IllegalArgumentException e) {
            return HttpResponse.badRequest("Invalid urgency. Use: LOW, MEDIUM, HIGH");
        }
    }

    /** GET /api/news */
    public HttpResponse listNews(HttpRequest request) {
        List<News> all = ctx.newsRepository.findAllSorted();
        List<JsonValue> arr = new ArrayList<>();
        for (News n : all) arr.add(newsToJson(n));
        return HttpResponse.ok(new JsonValue.JsonArray(arr));
    }

    /** POST /api/news — Employee only */
    public HttpResponse publishNews(HttpRequest request) {
        Employee author = (Employee) RequestContext.current();
        JsonValue.JsonObject body = request.body();
        String title   = str(body, "title");
        String content = str(body, "body");
        if (title.isBlank()) return HttpResponse.badRequest("'title' is required.");
        Result result = ctx.publishNews.execute(author.username(), title, content);
        return resultToResponse(result);
    }

    /** POST /api/news/{id}/comment */
    public HttpResponse commentOnNews(HttpRequest request) {
        User   user    = RequestContext.current();
        String idStr   = request.pathSegment(2).orElse("0");
        String comment = str(request.body(), "comment");
        Result result  = ctx.commentOnNews.execute(user, Integer.parseInt(idStr), comment);
        return resultToResponse(result);
    }


    /** GET /api/requests — requesters see their own; Managers/Deans see all. */
    public HttpResponse listRequests(HttpRequest request) {
        User user = RequestContext.current();
        List<Request> visible = canProcessRequests(user)
                ? ctx.requestRepository.findAll()
                : ctx.requestRepository.findByRequester(user.username());
        List<JsonValue> arr = new ArrayList<>();
        for (Request r : visible) arr.add(requestToJson(r));
        return HttpResponse.ok(new JsonValue.JsonArray(arr));
    }

    /** PUT /api/requests/{id} — Manager/Dean only; body: {"status": "APPROVED" | "REJECTED" | ...}. */
    public HttpResponse processRequest(HttpRequest request) {
        User actor = RequestContext.current();
        if (!canProcessRequests(actor)) return HttpResponse.forbidden();

        int id;
        try {
            id = Integer.parseInt(request.pathSegment(2).orElse(""));
        } catch (NumberFormatException e) {
            return HttpResponse.badRequest("Invalid request id.");
        }

        String statusRaw = str(request.body(), "status");
        try {
            RequestStatus status = RequestStatus.valueOf(statusRaw.toUpperCase());
            Result result = ctx.processRequest.execute(actor.username(), id, status);
            return resultToResponse(result);
        } catch (IllegalArgumentException e) {
            return HttpResponse.badRequest("Invalid status. Allowed: "
                    + java.util.Arrays.toString(RequestStatus.values()));
        }
    }

    private static boolean canProcessRequests(User user) {
        return user instanceof Manager || user instanceof Dean;
    }

    /** POST /api/requests */
    public HttpResponse submitRequest(HttpRequest request) {
        User user = RequestContext.current();
        JsonValue.JsonObject body = request.body();
        String title = str(body, "title");
        String info  = str(body, "body");
        if (info.isBlank()) info = str(body, "info");
        String urgencyRaw = str(body, "urgency");
        if (urgencyRaw.isBlank()) urgencyRaw = "MEDIUM";
        if (title.isBlank()) return HttpResponse.badRequest("'title' is required.");
        try {
            HelpType     type    = HelpType.valueOf(str(body, "type").toUpperCase());
            UrgencyLevel urgency = UrgencyLevel.valueOf(urgencyRaw.toUpperCase());
            Result result = ctx.submitRequest.execute(user, title, type, urgency, info);
            return resultToResponse(result);
        } catch (IllegalArgumentException e) {
            return HttpResponse.badRequest("Invalid type or urgency. Allowed types: "
                    + java.util.Arrays.toString(HelpType.values())
                    + "; urgency: LOW, MEDIUM, HIGH.");
        }
    }


    /** GET /api/orders */
    public HttpResponse listOrders(HttpRequest request) {
        List<Order> all = ctx.orderRepository.findAll();
        List<JsonValue> arr = new ArrayList<>();
        for (Order o : all) arr.add(orderToJson(o));
        return HttpResponse.ok(new JsonValue.JsonArray(arr));
    }

    /** POST /api/orders */
    public HttpResponse createOrder(HttpRequest request) {
        User user = RequestContext.current();
        String desc = str(request.body(), "description");
        Result result = ctx.createOrder.execute(user, desc);
        return resultToResponse(result);
    }

    /** PUT /api/orders/{id}/accept — TechSupport only */
    public HttpResponse acceptOrder(HttpRequest request) {
        domain.user.TechSupport tech = (domain.user.TechSupport) RequestContext.current();
        int id = Integer.parseInt(request.pathSegment(2).orElse("0"));
        Result result = ctx.acceptOrder.execute(tech, id);
        return resultToResponse(result);
    }

    /** PUT /api/orders/{id}/complete — TechSupport only */
    public HttpResponse completeOrder(HttpRequest request) {
        domain.user.TechSupport tech = (domain.user.TechSupport) RequestContext.current();
        int id = Integer.parseInt(request.pathSegment(2).orElse("0"));
        Result result = ctx.completeOrder.execute(tech, id);
        return resultToResponse(result);
    }

    private static JsonValue messageToJson(Message m) {
        return JsonObjectBuilder.create()
                .put("id",       m.id())
                .put("sender",   m.sender().value())
                .put("subject",  m.subject())
                .put("urgency",  m.urgency().name())
                .put("status",   m.status().name())
                .put("sentAt",   m.sentAt().toString())
                .build();
    }

    private JsonValue newsToJson(News n) {
        List<JsonValue> commentsJson = new ArrayList<>();
        for (Comment c : n.comments()) {
            JsonObjectBuilder cb = JsonObjectBuilder.create()
                    .put("author",   c.author().value())
                    .put("text",     c.text())
                    .put("postedAt", c.postedAt().toString());
            ctx.userRepository.findByUsername(c.author())
                    .ifPresent(u -> cb.put("authorFullName", u.name().first() + " " + u.name().last()));
            commentsJson.add(cb.build());
        }
        JsonObjectBuilder b = JsonObjectBuilder.create()
                .put("id",          n.id())
                .put("title",       n.title())
                .put("body",        n.body())
                .put("author",      n.author().value())
                .put("publishedAt", n.publishedAt().toString())
                .put("pinned",      n.isPinned())
                .putObjects("comments", commentsJson);
        ctx.userRepository.findByUsername(n.author())
                .ifPresent(u -> b.put("authorFullName", u.name().first() + " " + u.name().last()));
        return b.build();
    }

    private JsonValue requestToJson(Request r) {
        JsonObjectBuilder b = JsonObjectBuilder.create()
                .put("id",        r.id())
                .put("requester", r.requester().value())
                .put("title",     r.title())
                .put("type",      r.type().name())
                .put("faculty",   r.faculty().name())
                .put("urgency",   r.urgency().name())
                .put("body",      r.additionalInfo())
                .put("createdAt", r.createdAt().toString())
                .put("status",    r.status().name());
        ctx.userRepository.findByUsername(r.requester())
                .ifPresent(u -> b.put("requesterFullName", u.name().first() + " " + u.name().last()));
        return b.build();
    }

    private static JsonValue orderToJson(Order o) {
        return JsonObjectBuilder.create()
                .put("id",          o.id())
                .put("requester",   o.requester().value())
                .put("description", o.description())
                .put("status",      o.status().name())
                .build();
    }

    private static HttpResponse resultToResponse(Result result) {
        if (result.success())
            return HttpResponse.ok(JsonObjectBuilder.create().put("message", result.message()).build());
        return HttpResponse.badRequest(result.message());
    }
}
