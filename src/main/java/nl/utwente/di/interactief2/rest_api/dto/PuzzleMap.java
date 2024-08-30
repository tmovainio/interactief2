package nl.utwente.di.interactief2.rest_api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PuzzleMap {

    @JsonProperty("problem_name")
    public String problemName;
    @JsonProperty("location_id")
    public int locationID;
}
