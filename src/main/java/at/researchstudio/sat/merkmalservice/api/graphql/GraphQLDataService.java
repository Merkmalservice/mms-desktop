package at.researchstudio.sat.merkmalservice.api.graphql;

import at.researchstudio.sat.merkmalservice.api.support.model.GraphqlResult;
import at.researchstudio.sat.merkmalservice.model.Project;
import at.researchstudio.sat.merkmalservice.model.mapping.Mapping;
import at.researchstudio.sat.merkmalservice.support.exception.NoGraphQlResponseException;
import com.netflix.graphql.dgs.client.GraphQLResponse;
import com.netflix.graphql.dgs.client.MonoGraphQLClient;
import com.netflix.graphql.dgs.client.WebClientGraphQLClient;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.invoke.MethodHandles;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import org.apache.http.HttpHeaders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class GraphQLDataService {
    private static final Logger logger =
            LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final String apiEndpoint;
    private WebClientGraphQLClient graphQLClient;
    private int currentTokenHash;
    @Autowired ResourceLoader resourceLoader;
    private ConcurrentHashMap<String, String> graphqlQueries = new ConcurrentHashMap<>();

    public GraphQLDataService(
            @Value("${mms.desktop.api.mms:https://merkmalservice.at/backend/graphql}")
                    String apiEndpoint) {
        this.apiEndpoint = apiEndpoint;
    }

    private WebClientGraphQLClient getGraphQLClient(String token) {
        if (graphQLClient == null || token.hashCode() != currentTokenHash) {
            currentTokenHash = token.hashCode();
            synchronized (this) {
                WebClient webClient = WebClient.create(apiEndpoint);
                graphQLClient =
                        MonoGraphQLClient.createWithWebClient(
                                webClient,
                                headers ->
                                        headers.add(HttpHeaders.AUTHORIZATION, "Bearer " + token));
            }
        }
        return graphQLClient;
    }

    private String getGraphQlQuery(String filename) {
        return graphqlQueries.computeIfAbsent(
                filename,
                n -> {
                    Resource jsonFile = resourceLoader.getResource(filename);
                    try {
                        return Files.readString(Path.of(jsonFile.getURI()), StandardCharsets.UTF_8);
                    } catch (IOException e) {
                        logger.warn("could not read json operation from file {}", filename, e);
                    }
                    return null;
                });
    }

    public List<Project> getProjectsWithFeatureSets(String idTokenString) {
        String queryString = getGraphQlQuery("classpath:graphql/query-projects.gql");
        GraphQLResponse response =
                getGraphQLClient(idTokenString).reactiveExecuteQuery(queryString).block();
        if (response == null) {
            throw new NoGraphQlResponseException("Empty Response for project query");
        }
        return response.dataAsObject(GraphqlResult.class).getProjects();
    }

    public List<Mapping> getMappings(List<String> mappingIds, String idTokenString) {
        String queryString = getGraphQlQuery("classpath:graphql/query-mappings.gql");
        return mappingIds.stream()
                .map(
                        id -> {
                            GraphQLResponse response =
                                    getGraphQLClient(idTokenString)
                                            .reactiveExecuteQuery(
                                                    queryString, Map.of("mappingId", id), "mapping")
                                            .doOnError(
                                                    t ->
                                                            t.printStackTrace(
                                                                    new PrintWriter(System.err)))
                                            .block();
                            if (response == null) {
                                throw new NoGraphQlResponseException(
                                        "Empty Response for mappings query");
                            }
                            return response.dataAsObject(GraphqlResult.class).getMapping();
                        })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
}
