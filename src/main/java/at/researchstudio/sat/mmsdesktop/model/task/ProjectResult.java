package at.researchstudio.sat.mmsdesktop.model.task;

import java.util.List;

public class ProjectResult {
    private String id;
    private String name;
    private List<User> users;
    private List<Standard> standards;

    public ProjectResult(String id, String name, List user, List standards) {
        this.id = id;
        this.name = name;
        this.users = user;
        this.standards = standards;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getUsers() {
        return users.size();
    }

    public void setUsers(List users) {
        this.users = users;
    }

    public int getUsersSize() {
        return this.users.size();
    }

    public List getStandards() {
        return standards;
    }

    public void setStandards(List standards) {
        this.standards = standards;
    }

    public int getStandardsSize() {
        return standards.size();
    }

    public int getMappingsSize() {
        return this.standards.stream().mapToInt(standard -> standard.mappings.size()).sum();
    }

    class Standard {
        String id;
        List mappings;
    }

    class User {
        String id;
        String name;
    }
}
