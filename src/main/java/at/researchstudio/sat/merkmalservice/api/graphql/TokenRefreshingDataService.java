package at.researchstudio.sat.merkmalservice.api.graphql;

import at.researchstudio.sat.merkmalservice.api.DataService;
import at.researchstudio.sat.merkmalservice.model.Project;
import at.researchstudio.sat.merkmalservice.model.mapping.Mapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.xml.crypto.Data;
import java.util.List;

@Component
public class TokenRefreshingDataService implements DataService {
    GraphQLDataService delegate;

    public TokenRefreshingDataService(@Autowired GraphQLDataService delegate) {
        this.delegate = delegate;
    }

    @Override public List<Project> getProjectsWithFeatureSets(
                    String idTokenString) {
        List<Project> projects = delegate.getProjectsWithFeatureSets(idTokenString);
        return projects;
    }

    @Override public List<Mapping> getMappings(
                    List<String> mappingIds, String idTokenString) {
        List<Mapping> mappings = delegate.getMappings(mappingIds, idTokenString);

        return mappings;
    }
}
