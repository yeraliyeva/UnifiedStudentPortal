package presentation.rest.auth;

import domain.user.User;

/**
 * Thread-local holder for the authenticated user of the current request.
 *
 * <p>Set by {@link SecurityFilter} before the controller runs; cleared after.
 * Controllers call {@link #current()} to get the user without re-fetching from the DB.
 *
 * <p>GRASP: Information Expert — the thread context owns "who is executing this request".
 */
public final class RequestContext {
    private static final ThreadLocal<User> holder = new ThreadLocal<>();

    private RequestContext() {}

    public static void set(User user)  { holder.set(user); }
    public static User  current()      { return holder.get(); }
    public static void  clear()        { holder.remove(); }
}
