package at.researchstudio.sat.mmsdesktop.service;

import java.nio.charset.StandardCharsets;
import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;

public class DataService {
    public static String callGraphQlEndpoint(String queryString, String idTokenString)
            throws Exception {
        HttpPost post = new HttpPost("https://merkmalservice.at/backend/graphql");
        Header headers[] = {
            new BasicHeader("Content-type", "application/json"),
            new BasicHeader("Accept", "application/json"),
            new BasicHeader(HttpHeaders.AUTHORIZATION, "Bearer " + idTokenString)
        };
        post.setHeaders(headers);
        post.setEntity(new StringEntity(queryString));
        HttpClient client = HttpClients.custom().build();
        HttpResponse response = client.execute(post);
        String result = IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);
        response.getEntity().getContent().close();
        if (response.getStatusLine().getStatusCode() == 200) {
            return result;
        } else {
            throw new Exception(response.getStatusLine().getReasonPhrase());
        }
    }
}
