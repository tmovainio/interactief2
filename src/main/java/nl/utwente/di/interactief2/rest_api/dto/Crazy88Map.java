package nl.utwente.di.interactief2.rest_api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Crazy88Map {

    @JsonProperty("problem_name")
    public String problemName;
    public String description;
    public int score;
}
