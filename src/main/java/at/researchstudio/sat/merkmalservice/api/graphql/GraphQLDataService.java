package at.researchstudio.sat.merkmalservice.api.graphql;

import at.researchstudio.sat.merkmalservice.api.support.model.GraphqlResult;
import at.researchstudio.sat.merkmalservice.api.userdata.UserDataDirService;
import at.researchstudio.sat.merkmalservice.model.Project;
import at.researchstudio.sat.merkmalservice.model.Standard;
import at.researchstudio.sat.merkmalservice.model.mapping.Mapping;
import at.researchstudio.sat.merkmalservice.support.exception.NoGraphQlResponseException;
import com.netflix.graphql.dgs.client.GraphQLResponse;
import com.netflix.graphql.dgs.client.MonoGraphQLClient;
import com.netflix.graphql.dgs.client.WebClientGraphQLClient;
import java.io.*;
import java.lang.invoke.MethodHandles;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import org.apache.http.HttpHeaders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;
import org.springframework.web.reactive.function.client.WebClient;

@Component
@DependsOn("userDataDirService")
public class GraphQLDataService implements InitializingBean {
    private static final Logger logger =
            LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final String apiEndpoint;
    private WebClientGraphQLClient graphQLClient;
    private int currentTokenHash;
    private UserDataDirService userDataDirService;
    @Autowired ResourceLoader resourceLoader;
    private ConcurrentHashMap<String, String> graphqlQueries = new ConcurrentHashMap<>();
    private static final String GRAPHQL_LOGFILE = "graphql-session-log-";
    private static final String GRAPHQL_LOGFILE_EXTENSION = ".txt";
    private PrintWriter logWriter;
    private File logFile;

    public GraphQLDataService(
            @Value("${mms.desktop.api.mms:https://merkmalservice.at/backend/graphql}")
                    String apiEndpoint,
            @Autowired UserDataDirService userDataDirService) {
        this.apiEndpoint = apiEndpoint;
        this.userDataDirService = userDataDirService;
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
                    try (InputStream fileInputStream =
                            resourceLoader.getResource(filename).getInputStream()) {
                        return StreamUtils.copyToString(fileInputStream, StandardCharsets.UTF_8);
                    } catch (IOException e) {
                        logger.warn("could not read json operation from file {}", filename, e);
                    }
                    return null;
                });
    }

    public List<Project> getProjectsWithFeatureSets(String idTokenString) {
        String queryString = getGraphQlQuery("classpath:graphql/query-projects.gql");
        GraphQLResponse response =
                getGraphQLClient(idTokenString)
                        .reactiveExecuteQuery(queryString)
                        .doOnError(t -> logError(t))
                        .block();
        logResponse(response);
        if (response == null) {
            throw new NoGraphQlResponseException("Empty Response for project query");
        }
        return response.dataAsObject(GraphqlResult.class).getProjects();
    }

    public List<Standard> getFeatureSetsOfProjectWithPropertySets(
            String projectId, String idTokenString) {
        String queryString =
                getGraphQlQuery("classpath:graphql/query-standards-with-propertysets.gql");
        GraphQLResponse response =
                getGraphQLClient(idTokenString)
                        .reactiveExecuteQuery(
                                queryString,
                                Map.of("projectId", projectId),
                                "projectWithStandardsWithPropertySets")
                        .doOnError(t -> logError(t))
                        .block();
        logResponse(response);
        if (response == null) {
            throw new NoGraphQlResponseException("Empty Response for standards query");
        }
        return response.dataAsObject(GraphqlResult.class).getProject().getStandards();
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
                                            .doOnError(t -> logError(t))
                                            .block();
                            logResponse(response);
                            if (response == null) {
                                throw new NoGraphQlResponseException(
                                        "Empty Response for mappings query");
                            }
                            return response.dataAsObject(GraphqlResult.class).getMapping();
                        })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private void logResponse(GraphQLResponse response) {
        if (response == null) {
            writeToLog("graphql response is null");
        } else {
            writeToLog("GraphQL Response: " + response.getJson().replaceAll("\r?\n", " "));
        }
    }

    private void logError(Throwable throwable) {
        writeToLog(
                String.format(
                        "GraphQL Excption: %s: %s",
                        throwable.getClass().getName(), throwable.getMessage()));
    }

    private void writeToLog(String logMessage) {
        SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss ");
        try {
            logWriter.println(
                    String.format(
                            "%s [%s] %s",
                            format.format(new Date()),
                            Thread.currentThread().getName(),
                            logMessage));
            logWriter.flush();
        } catch (Exception e) {
            logger.warn("Error writing to graphql log file {}", logFile, e);
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        this.logFile =
                new File(
                        userDataDirService.getUserDataDir(),
                        GRAPHQL_LOGFILE
                                + new SimpleDateFormat("yyyyMMdd_HHmmssZ").format(new Date())
                                + GRAPHQL_LOGFILE_EXTENSION);
        if (!logFile.canWrite()) {
            logger.warn("Cannot write graphql session log file {}", logFile);
        }
        FileOutputStream out = new FileOutputStream(logFile);
        this.logWriter = new PrintWriter(out);
    }
}
