package presentation.rest.serializer;

import domain.user.*;
import infrastructure.persistence.json.JsonObjectBuilder;
import infrastructure.persistence.json.JsonValue;

/**
 * Converts domain {@link User} subtypes to JSON for API responses.
 *
 * <p>Adapter pattern: keeps serialisation logic out of domain objects (SRP).
 * Uses Java 17 pattern matching switch for clean, exhaustive dispatch (OCP-friendly).
 */
public final class UserSerializer {

    private UserSerializer() {}

    public static JsonValue toJson(User user) {
        JsonObjectBuilder builder = JsonObjectBuilder.create()
                .put("username",   user.username().value())
                .put("firstName",  user.name().first())
                .put("lastName",   user.name().last())
                .put("email",      user.email().address())
                .put("faculty",    user.faculty().name())
                .put("gender",     user.gender().name())
                .put("language",   user.language().name())
                .put("role",       user.getClass().getSimpleName());

        // Subtype-specific extras
        if (user instanceof Student s) {
            builder.put("degreeType", s.degreeType().name())
                   .put("studyYear",  s.studyYear())
                   .put("availableCredits", s.availableCredits().value())
                   .put("failCount",  s.failCount())
                   .put("isResearcher", s.isResearcher());
        } else if (user instanceof Teacher t) {
            builder.put("degree",    t.degree())
                   .put("position",  t.position().name())
                   .put("avgRating", t.averageRating())
                   .put("isResearcher", t.isResearcher());
        } else if (user instanceof EmployeeResearcher er) {
            builder.put("researchField", er.researcherProfile().field());
        } else if (user instanceof Manager m) {
            builder.put("position", m.position().name());
        }

        return builder.build();
    }
}
