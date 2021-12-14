package at.researchstudio.sat.merkmalservice.api;

import at.researchstudio.sat.merkmalservice.api.support.exception.MMSGraphQLClientException;
import at.researchstudio.sat.merkmalservice.api.support.model.GraphqlResult;
import at.researchstudio.sat.merkmalservice.model.Project;
import at.researchstudio.sat.merkmalservice.model.mapping.Mapping;
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
import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.keycloak.exceptions.TokenVerificationException;
import org.keycloak.representations.JsonWebToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class DataService {
    private static final Logger logger =
            LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final String apiEndpoint;
    private WebClientGraphQLClient graphQLClient;
    @Autowired ResourceLoader resourceLoader;
    private ConcurrentHashMap<String, String> graphqlQueries = new ConcurrentHashMap<>();

    public DataService(
            @Value("${mms.desktop.api.mms:https://merkmalservice.at/backend/graphql}")
                    String apiEndpoint) {
        this.apiEndpoint = apiEndpoint;
    }

    private WebClientGraphQLClient getGraphQLClient(String token) {
        if (graphQLClient == null) {
            synchronized (this) {
                if (graphQLClient == null) {
                    WebClient webClient = WebClient.create(apiEndpoint);
                    graphQLClient =
                            MonoGraphQLClient.createWithWebClient(
                                    webClient,
                                    headers ->
                                            headers.add(
                                                    HttpHeaders.AUTHORIZATION, "Bearer " + token));
                }
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

    public String callGraphQlEndpoint(String queryString, String idTokenString)
            throws TokenVerificationException {
        Objects.requireNonNull(queryString);
        Objects.requireNonNull(idTokenString);
        HttpPost post = new HttpPost(apiEndpoint);
        Header[] headers = {
            new BasicHeader("Content-type", "application/json"),
            new BasicHeader("Accept", "application/json"),
            new BasicHeader(HttpHeaders.AUTHORIZATION, "Bearer " + idTokenString)
        };
        try {
            post.setHeaders(headers);
            post.setEntity(new StringEntity(queryString));
            HttpClient client = HttpClients.custom().build();
            HttpResponse response = client.execute(post);
            String result;
            result = IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);
            response.getEntity().getContent().close();
            if (response.getStatusLine().getStatusCode() == 200) {
                return result;
            } else if (response.getStatusLine().getStatusCode() == 403) {
                logger.info("Start Refresh Token process");
                throw new TokenVerificationException(new JsonWebToken());
            } else {
                String message =
                        String.format(
                                "Unexpected response status %d while trying to fetch data from api endpoint %s",
                                response.getStatusLine().getStatusCode(), apiEndpoint);
                throw new MMSGraphQLClientException(message);
            }
        } catch (IOException e) {
            throw new MMSGraphQLClientException(
                    String.format(
                            "Error while trying to fetch data from api endpoint %s", apiEndpoint),
                    e);
        }
    }

    public List<Project> getProjectsWithFeatureSets(String idTokenString) {
        String queryString = getGraphQlQuery("classpath:graphql/query-projects.gql");
        GraphQLResponse response =
                getGraphQLClient(idTokenString).reactiveExecuteQuery(queryString).block();
        if (response == null) {
            throw new NullPointerException("Empty Response for project query");
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
                                throw new NullPointerException("Empty Response for mappings query");
                            }
                            return response.dataAsObject(GraphqlResult.class).getMapping();
                        })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
}
