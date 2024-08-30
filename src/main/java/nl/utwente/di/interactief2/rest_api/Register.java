package nl.utwente.di.interactief2.rest_api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import nl.utwente.di.interactief2.JDBC.SecurityClasses.Sanitizer;
import nl.utwente.di.interactief2.JDBC.attribute_queries.PersonAttributes;
import nl.utwente.di.interactief2.JDBC.attribute_queries.SubmissionAttributes;
import nl.utwente.di.interactief2.JDBC.attribute_queries.TeamAttributes;
import nl.utwente.di.interactief2.rest_api.authentication.Authenticator;
import nl.utwente.di.interactief2.rest_api.dto.Person;

import java.net.http.HttpHeaders;

@Path("/register")
public class Register {
    private static final String TOKEN = "authorization";

    Authenticator authenticator;

    public Register() {;
        authenticator = new Authenticator();
    }

    /**
     * Servlet to register a new account
     * @param body The account information to be registered
     * @param headers The HTTP headers containing additional context information
     * @return An appropriate HTTP response
     */
    @POST
    @Produces("application/json")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response registerNewAccount(String body, @Context HttpHeaders headers) {
        Person person;
        ObjectMapper mapper = new ObjectMapper();
        //One by one sanitize all vars included
        try {
            person = mapper.readValue(body, Person.class);
        } catch (JsonProcessingException e) {
            return Response.status(Response.Status.BAD_REQUEST).type(MediaType.APPLICATION_JSON_TYPE).build();
        }
        if (!Sanitizer.studentNumberFormat(person.s_numb)) {
            return Response.status(Response.Status.BAD_REQUEST).type(MediaType.APPLICATION_JSON_TYPE)
                    .entity(" { message: 'Your student number is incorrect, please try again!' } ").build();
        }
        if (!Sanitizer.passwordCheck(person.password)) {
            return Response.status(Response.Status.BAD_REQUEST).type(MediaType.APPLICATION_JSON_TYPE)
                    .entity(" { message: 'Your password does not meet the criteria, please try again!' } ").build();
        }
        if (!Sanitizer.checkStudentName(person.name)) {
            return Response.status(Response.Status.BAD_REQUEST).type(MediaType.APPLICATION_JSON_TYPE)
                    .entity(" { message: 'Your name does not meet the criteria, please try again!' } ").build();
        }
        if (!Sanitizer.phoneNumberCheck(person.phone_numb)) {
            return Response.status(Response.Status.BAD_REQUEST).type(MediaType.APPLICATION_JSON_TYPE)
                    .entity(" { message: 'Your phone number is incorrect, please try again!' } ").build();
        }
        //then insert the new person, welcome
        PersonAttributes.insertPerson(person.s_numb, person.name, person.phone_numb, person.password);
        return Response.status(Response.Status.OK).type(MediaType.APPLICATION_JSON_TYPE).build();
    }
}
