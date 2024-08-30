package nl.utwente.di.interactief2.rest_api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Credentials {
    @JsonProperty("student_number")
    public int studentNumber;
    public String password;
}
