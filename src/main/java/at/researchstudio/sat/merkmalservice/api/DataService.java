package at.researchstudio.sat.merkmalservice.api;

import at.researchstudio.sat.merkmalservice.model.Project;
import at.researchstudio.sat.merkmalservice.model.mapping.Mapping;
import java.util.List;

public interface DataService {
    List<Project> getProjectsWithFeatureSets();

    List<Mapping> getMappings(List<String> mappingIds);
}
