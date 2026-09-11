package dragon;

import java.util.UUID;

// The userId of the current authenticated user is going to be needed for
// most service/repository operations. We keep it as a static variable that
// gets filled in on login as convenience. This should get set to null if
// the user logs out.
public class AuthenticatedAccountContext {
    private static UUID authenticatedUserId = null;

    public static UUID getAuthenticatedUserId() {
        return authenticatedUserId;
    }

    public static void setAuthenticatedUserId(UUID authenticatedUserId) {
        AuthenticatedAccountContext.authenticatedUserId = authenticatedUserId;
    }
}
