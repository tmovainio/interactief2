package nl.utwente.di.interactief2.objects;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Person {
    @JsonProperty("student_number")
    public final int sNumb;
    public final String name;
    @JsonProperty("phone_number")
    public final String phoneNumb;
    @JsonProperty("team_name")
    public final String teamName;

    public Person(int sNumb, String name, String phoneNumb, String teamName) {
        this.sNumb = sNumb;
        this.name = name;
        this.phoneNumb = phoneNumb;
        this.teamName = teamName;
    }

    public int getsNumb() {
        return this.sNumb;
    }

    public String getName() {
        return this.name;
    }

    public String getPhoneNumb() {
        return this.phoneNumb;
    }

    public String getTeamName() {
        return this.teamName;
    }
}
