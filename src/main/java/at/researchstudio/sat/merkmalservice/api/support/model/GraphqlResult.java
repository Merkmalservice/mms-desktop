package at.researchstudio.sat.merkmalservice.api.support.model;

import at.researchstudio.sat.merkmalservice.model.Project;
import at.researchstudio.sat.merkmalservice.model.mapping.Mapping;
import java.util.List;

public class GraphqlResult {
    private List<Project> projects;
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
}
