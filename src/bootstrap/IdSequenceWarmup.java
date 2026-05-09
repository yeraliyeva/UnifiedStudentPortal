package bootstrap;

import domain.course.CourseId;

public final class IdSequenceWarmup {
    private IdSequenceWarmup() {}

    public static void warm(AppContext ctx) {
        ctx.courseRepository.findAll().forEach(c -> ctx.courseIds.seedAtLeast(numericTail(c.id()) + 1));
        ctx.bookRepository.findAll().forEach(b -> ctx.bookIds.seedAtLeast(b.id().value() + 1));
        ctx.newsRepository.findAllSorted().forEach(n -> ctx.newsIds.seedAtLeast(n.id() + 1));
        ctx.requestRepository.findAll().forEach(r -> ctx.requestIds.seedAtLeast(r.id() + 1));
        ctx.orderRepository.findAll().forEach(o -> ctx.orderIds.seedAtLeast(o.id() + 1));
        ctx.paperRepository.findAll().forEach(p -> ctx.paperIds.seedAtLeast(p.id().value() + 1));
        ctx.projectRepository.findAll().forEach(p -> ctx.projectIds.seedAtLeast(p.id() + 1));
        ctx.userRepository.findAll().forEach(u ->
                ctx.messageRepository.inboxOf(u.username()).forEach(m -> ctx.messageIds.seedAtLeast(m.id() + 1)));
    }

    private static int numericTail(CourseId id) {
        String s = id.value();
        int dash = s.lastIndexOf('-');
        try { return dash >= 0 ? Integer.parseInt(s.substring(dash + 1)) : 0; }
        catch (NumberFormatException e) { return 0; }
    }
}
