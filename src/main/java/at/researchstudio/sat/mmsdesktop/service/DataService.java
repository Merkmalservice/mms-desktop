package at.researchstudio.sat.mmsdesktop.service;

import org.keycloak.representations.AccessToken;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

public class DataService {
    private AuthService authService;

    public DataService(AuthService authService) {
        this.authService = authService;
    }

    public static void callGraphQlEndpoint(String query, AccessToken token) {
        Client client = ClientBuilder.newClient();
        WebTarget resource =
                        client.target("https://mms.researchstudio.at/backend/graphql")
                                        .property(HttpHeaders.AUTHORIZATION, "Bearer " + token.getSubject());
        Invocation.Builder request = resource.request();
        request.accept(MediaType.APPLICATION_JSON);
        Response response = request.get();
        if (response.getStatusInfo().getFamily() == Response.Status.Family.SUCCESSFUL) {
            System.out.println("Success! " + response.getStatus());
            System.out.println(response.getEntity());
        } else {
            System.out.println("ERROR! " + response.getStatus());
            System.out.println(response.getEntity());
        }
    }
}
