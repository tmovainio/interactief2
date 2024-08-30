package nl.utwente.di.interactief2.rest_api;

import com.auth0.jwt.exceptions.JWTVerificationException;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import nl.utwente.di.interactief2.JDBC.SecurityClasses.Sanitizer;
import nl.utwente.di.interactief2.JDBC.attribute_queries.LocationAttributes;
import nl.utwente.di.interactief2.JDBC.attribute_queries.PersonAttributes;
import nl.utwente.di.interactief2.JDBC.attribute_queries.TeamAttributes;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;
import java.util.Objects;

import static nl.utwente.di.interactief2.rest_api.LeaderBoard.*;

@Path("/locations")
public class Locations {

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response addLocation(@HeaderParam("Authorization") String token, String body) {
        // uses the token to verify the user is logged in
        int studentNumber = -1;
        try {
            studentNumber = authenticator.decodeJWToken(token);
        } catch (JWTVerificationException e) {
            return Response.status(Response.Status.UNAUTHORIZED).type(MediaType.APPLICATION_JSON_TYPE).build();
        }
        // check if the user is an admin
        if (PersonAttributes.checkIfAdmin(studentNumber)) {
            JSONObject json = new JSONObject(body);
            // check if the new file has a location name
            if (json.has(LocationAttributes.LOCATION_NAME)) {
                // add the new location
                LocationAttributes.addLocation(json.get(LocationAttributes.LOCATION_NAME).toString());
                return Response.status(Response.Status.OK).type(MediaType.APPLICATION_JSON_TYPE).build();
            } else {
                return Response.status(Response.Status.BAD_REQUEST).type(MediaType.APPLICATION_JSON_TYPE).build();
            }
        }
        return Response.status(Response.Status.FORBIDDEN).type(MediaType.APPLICATION_JSON_TYPE).build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response getAllLocations(@HeaderParam("Authorization") String token) {
        int sNumb;
        try {
            sNumb = authenticator.decodeJWToken(token);
        } catch (JWTVerificationException e) {
            return Response.status(Response.Status.UNAUTHORIZED).type(MediaType.APPLICATION_JSON_TYPE).build();
        }

        if (PersonAttributes.checkIfAdmin(sNumb)) {
            List<JSONObject> result = LocationAttributes.getAllLocations();
            return Response.status(Response.Status.OK).type(MediaType.APPLICATION_JSON_TYPE)
                    .entity(new JSONArray(result).toString()).build();
        } else {
            return Response.status(Response.Status.BAD_REQUEST).type(MediaType.APPLICATION_JSON_TYPE).build();
        }
    }

    @GET
    @Path("/{location_id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response getSpecificLocation(@HeaderParam("Authorization") String token,
            @PathParam("location_id") int locationID) {
        try {
            authenticator.decodeJWToken(token);
        } catch (JWTVerificationException e) {
            return Response.status(Response.Status.UNAUTHORIZED).type(MediaType.APPLICATION_JSON_TYPE).build();
        }

        JSONObject result = LocationAttributes.getLocationByID(locationID);

        if (!Objects.isNull(result)) {
            return Response.status(Response.Status.OK).type(MediaType.APPLICATION_JSON_TYPE)
                    .entity(new JSONArray(result).toString()).build();
        } else {
            return Response.status(Response.Status.BAD_REQUEST).type(MediaType.APPLICATION_JSON_TYPE).build();
        }
    }

    @GET
    @Path("/team/{team_name}")
    @Produces("application/json")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response getUnlockedLocationsFromTeam(@HeaderParam("Authorization") String token,
            @PathParam("team_name") String teamName) {
        int sNumb;
        try {
            sNumb = authenticator.decodeJWToken(token);

            if (Objects.equals(PersonAttributes.getTeamNameByParticipant(sNumb), teamName)) {
                if (Objects.isNull(TeamAttributes.getTeamByName(teamName, false))) {
                    return Response.status(Response.Status.BAD_REQUEST).type(MediaType.APPLICATION_JSON_TYPE).build();
                } else {
                    List<JSONObject> result = LocationAttributes.getUnlockedLocationsByTeam(teamName);
                    return Response.status(Response.Status.OK).type(MediaType.APPLICATION_JSON_TYPE)
                            .entity(new JSONArray(result).toString()).build();
                }
            } else {
                throw new JWTVerificationException("Not part of team specified");
            }
        } catch (JWTVerificationException e) {
            return Response.status(Response.Status.UNAUTHORIZED).type(MediaType.APPLICATION_JSON_TYPE).build();
        }
    }

    @PATCH
    @Path("/{location_id}")
    @Produces("application/json")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response alterLocation(@HeaderParam("Authorization") String token, @PathParam("location_id") int locationID,
            String body) {
        int sNumb;
        try {
            sNumb = authenticator.decodeJWToken(token);
        } catch (JWTVerificationException e) {
            return Response.status(Response.Status.UNAUTHORIZED).type(MediaType.APPLICATION_JSON_TYPE).build();
        }
        try {
            if (PersonAttributes.checkIfAdmin(sNumb)) {
                JSONObject json = new JSONObject(body);

                if (json.has(LocationAttributes.LOCATION_NAME)) {
                    String locationName = json.get(LocationAttributes.LOCATION_NAME).toString();

                    if (!Objects.isNull(locationName) && !Objects.equals("", locationName)
                            && Sanitizer.generalStringCheck(locationName)) {
                        LocationAttributes.updateLocation(locationName, locationID);

                        return Response.status(Response.Status.OK).type(MediaType.APPLICATION_JSON_TYPE).build();
                    } else {
                        throw new Exception();
                    }
                } else {
                    throw new Exception();
                }
            } else {
                throw new Exception();
            }
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST).type(MediaType.APPLICATION_JSON_TYPE).build();
        }
    }

    /**
     * Servlet to delete a location.
     * @param token The token to check if the deleting request comes from an admin.
     * @param locationID The ID of the location that is requested to be deleted.
     * @return OK if successful, UNAUTHORIZED if not an admin and BAD_REQUEST for malformed input.
     */
    @DELETE
    @Path("/{location_id}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response deleteLocation(@HeaderParam("Authorization") String token, @PathParam("location_id") int locationID) {
        int sNumb;
        try {
            sNumb = authenticator.decodeJWToken(token);
        } catch (JWTVerificationException e) {
            return Response.status(Response.Status.UNAUTHORIZED).type(MediaType.APPLICATION_JSON_TYPE).build();
        }

        if (PersonAttributes.checkIfAdmin(sNumb)) {
            if(LocationAttributes.deleteLocation(locationID)) {
                return Response.status(Response.Status.OK).build();
            } else {
                return Response.status(Response.Status.BAD_REQUEST).build();
            }
        } else {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
    }

}
