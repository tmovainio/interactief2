package nl.utwente.di.interactief2.rest_api.annotations;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.Response;
import nl.utwente.di.interactief2.JDBC.attribute_queries.PersonAttributes;
import nl.utwente.di.interactief2.rest_api.authentication.Authenticator;

import java.io.IOException;

@Participant
public class ParticipantFilter implements ContainerRequestFilter {

    static PersonAttributes personAttributes = new PersonAttributes();

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        if (requestContext.getHeaders().containsKey("Authorization")) {
            Authenticator authenticator = new Authenticator();
            String token = requestContext.getHeaders().getFirst("Authorization");
            try {
                int sid = authenticator.decodeJWToken(token);
                if (personAttributes.getParticipantByID(sid) == null) {
                    requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED).build());
                }
                requestContext.setProperty("sid", sid);
            } catch (Exception e) {
                requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED).build());
            }
        } else {
            requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED).entity("test123").build());
        }
    }


}
