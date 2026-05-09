package infrastructure.persistence.mapper;

import domain.enums.OrderStatus;
import domain.messaging.Order;
import domain.shared.Username;
import infrastructure.persistence.json.JsonObjectBuilder;
import infrastructure.persistence.json.JsonValue;
import infrastructure.persistence.orm.EntityMapper;
import infrastructure.persistence.orm.MapperHelpers;

public final class OrderMapper implements EntityMapper<Order, Integer> {

    @Override public Integer idOf(Order o) { return o.id(); }
    @Override public String idAsString(Integer id) { return id.toString(); }

    @Override public JsonValue toJson(Order o) {
        JsonObjectBuilder b = JsonObjectBuilder.create()
                .put("_id", Integer.toString(o.id()))
                .put("id", o.id())
                .put("requester", o.requester().value())
                .put("description", o.description())
                .put("status", o.status().name())
                .put("createdAt", o.createdAt().toString());
        o.executor().ifPresent(u -> b.put("executor", u.value()));
        return b.build();
    }

    @Override public Order fromJson(JsonValue json) {
        JsonValue.JsonObject o = (JsonValue.JsonObject) json;
        Order order = new Order(MapperHelpers.readInt(o, "id"),
                new Username(MapperHelpers.readString(o, "requester")),
                MapperHelpers.readString(o, "description"));
        OrderStatus status = Enum.valueOf(OrderStatus.class, MapperHelpers.readString(o, "status"));
        String exec = MapperHelpers.readString(o, "executor");
        if (exec != null && status != OrderStatus.NEW) {
            order.accept(new Username(exec));
            if (status == OrderStatus.DONE) order.complete();
            if (status == OrderStatus.REJECTED) order.reject();
        } else if (status == OrderStatus.REJECTED) {
            order.reject();
        }
        return order;
    }
}
