package edu.univ.erp.service;

import edu.univ.erp.data.DataStore;
import edu.univ.erp.domain.Role;
import edu.univ.erp.domain.User;

import java.util.ArrayList;
import java.util.List;

public class AdminService {
    private final DataStore ds = DataStore.getInstance();

    public List<User> getInstructors() {
        List<User> list = new ArrayList<>();
        for (User u : ds.getUsers().values()) {
            if (u.getRole() == Role.INSTRUCTOR) {
                list.add(u);
            }
        }
        return list;
    }

    public boolean isMaintenanceMode() {
        return ds.isMaintenanceMode();
    }

    public void setMaintenanceMode(boolean mode) {
        ds.setMaintenanceMode(mode);
    }

    public User getAdminUser() {
        return ds.getUsers().get("admin");
    }
}

