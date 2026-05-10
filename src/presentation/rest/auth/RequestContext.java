package presentation.rest.auth;

import domain.user.User;

/** Holds the authenticated user for the current request thread so controllers don't need to re-fetch it. */
public final class RequestContext {
    private static final ThreadLocal<User> holder = new ThreadLocal<>();

    private RequestContext() {}

    public static void set(User user)  { holder.set(user); }
    public static User  current()      { return holder.get(); }
    public static void  clear()        { holder.remove(); }
}
