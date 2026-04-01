package edu.univ.erp.access;

import edu.univ.erp.data.DataStore;
import edu.univ.erp.domain.User;

public final class AccessGuard {
    private static final DataStore ds = DataStore.getInstance();

    private AccessGuard() {}

    public static boolean isMaintenanceMode() {
        return ds.isMaintenanceMode();
    }

    public static boolean canAccess(User user) {
        if (user == null) return false;
        // All users can log in and view, even during maintenance mode
        // Only write operations are blocked for non-admins
        return true;
    }

    public static boolean canStudentsTransact() {
        return !isMaintenanceMode();
    }

    public static boolean canInstructorsTransact() {
        return !isMaintenanceMode();
    }
}

