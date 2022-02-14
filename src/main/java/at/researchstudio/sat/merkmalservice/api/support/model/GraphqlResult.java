package at.researchstudio.sat.merkmalservice.api.support.model;

import at.researchstudio.sat.merkmalservice.model.Project;
import at.researchstudio.sat.merkmalservice.model.mapping.Mapping;
import java.util.List;

public class GraphqlResult {
    private List<Project> projects;
    private Project project;
    private List<Mapping> mappings;
    private Mapping mapping;

    public GraphqlResult() {}

    public List<Project> getProjects() {
        return projects;
    }

    public List<Mapping> getMappings() {
        return mappings;
    }

    public Mapping getMapping() {
        return mapping;
    }

    public Project getProject() {
        return project;
    }
}
