package at.researchstudio.sat.mmsdesktop.model.task;

import java.util.List;

public class ProjectResult {
    private String id;
    private String name;
    private List<User> users;
    private List<Standard> standards;

    public ProjectResult(String id, String name, List<User> user, List<Standard> standards) {
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

    public void setUsers(List<User> users) {
        this.users = users;
    }

    public int getUsersSize() {
        return this.users.size();
    }

    public List<Standard> getStandards() {
        return standards;
    }

    public void setStandards(List<Standard> standards) {
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
        List<Mapping> mappings;
    }

    class User {
        String id;
        String name;
    }

    class Mapping {
        String id;
        Feature inputFeature;
        Feature outputFeature;
    }

    class Feature {
        String id;
        String name;
        String description;
        Organization organization;
    }

    class Organization {
        String id;
        String name;
    }
}
