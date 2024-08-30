package nl.utwente.di.interactief2.rest_api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ChallengeMap {

    @JsonProperty("problem_name")
    public String problemName;
    @JsonProperty("location_id")
    public int locationID;
    public int score;
    public String description;
}
