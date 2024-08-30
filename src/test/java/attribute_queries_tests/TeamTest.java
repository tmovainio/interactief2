package attribute_queries_tests;

import attribute_queries_tests.PersonTest;
import nl.utwente.di.interactief2.JDBC.attribute_queries.PersonAttributes;
import nl.utwente.di.interactief2.JDBC.attribute_queries.TeamAttributes;
import org.checkerframework.checker.fenum.qual.SwingTextOrientation;
import org.json.JSONObject;
import org.junit.jupiter.api.*;

import java.sql.SQLException;
import java.util.Objects;

public class TeamTest {


    static final String testTeam = "testTeam";
    static int indent = 0;

    @BeforeEach
    void setUp() {
        //Make sure a new test team is created.
        PersonTest.insertTestPerson();
        Assertions.assertTrue(createTeam(PersonTest.testStudentNumber));
    }

    @AfterEach
    void reset() {
        //Make sure the test team is removed.
        Assertions.assertTrue(removeTestTeam());
        for(int i = 1; i <= indent; i++) {
            PersonTest.deleteTestTeamMember(i);
        }
        indent = 1;
        PersonTest.deleteTestPerson();
    }

    @Test
    void testUnapprovedTeams() {
        System.out.println(TeamAttributes.getAllUnapprovedTeams().toString());
    }

    @Test
    void approveTeamTest() {
        //Make sure there is a team test in the db.
        JSONObject team = TeamAttributes.getTeamByName(testTeam, true);
        if(!Objects.isNull(team)) {
            //Make sure that there is no invite link inside the returned team object (and then by definition isn't approved).
            Assertions.assertFalse(team.getJSONObject(TeamAttributes.TEAM_COLUMN).has(TeamAttributes.INVITE_LINK));

            //Approve the test team.
            Assertions.assertTrue(approveTeam());

            //Make sure that there is an invitation link inside the returned team object (and then by definition is approved).
            JSONObject teamTemp = TeamAttributes.getTeamByName(testTeam, true).getJSONObject(TeamAttributes.TEAM_COLUMN);
            Assertions.assertTrue(teamTemp.has(TeamAttributes.INVITE_LINK));
        }
    }

    @Test
    void joinTeamTest() {
        //Make sure there is now a new person in the db.
        indent = 4;
        for(int i = 1; i < indent; i++) {
            PersonTest.insertTestTeamMember(i);
            Assertions.assertNotNull(PersonAttributes.getPersonByID(PersonTest.testStudentNumber + i));
            Assertions.assertNull(PersonAttributes.getParticipantByID(PersonTest.testStudentNumber + i));
        }
        Assertions.assertEquals(PersonTest.testName, TeamAttributes.getTeamCaptainByTeamName(testTeam).getJSONObject(PersonAttributes.PARTICIPANT_OBJECT_NAME).getString(PersonAttributes.NAME));

        //Make sure the team is join-able.
        approveTeamTest();

        String inviteLink = TeamAttributes.getTeamByName(testTeam, true).getJSONObject(TeamAttributes.TEAM_COLUMN).getString(TeamAttributes.INVITE_LINK);

        //Make sure that the new team member has joined the test team.
        for(int i = 1; i < indent; i++) {
            Assertions.assertEquals(testTeam, TeamAttributes.joinTeamByInviteLink(PersonTest.testStudentNumber + i, inviteLink));
            Assertions.assertNotNull(PersonAttributes.getParticipantByID(PersonTest.testStudentNumber + i));

            //Make sure the is_full boolean works.
            boolean check = TeamAttributes.getTeamByName(testTeam, false).getJSONObject(TeamAttributes.TEAM_COLUMN).getBoolean(TeamAttributes.IS_FULL_BOOLEAN);
            if(i != indent - 1) {
                Assertions.assertFalse(check);
            } else {
                Assertions.assertTrue(check);
            }
        }

        //Make sure no-one can join the team now that is full.
        Assertions.assertNull(TeamAttributes.joinTeamByInviteLink(PersonTest.testStudentNumber + indent, inviteLink));
        Assertions.assertNull(PersonAttributes.getParticipantByID(PersonTest.testStudentNumber + indent));

        Assertions.assertTrue(TeamAttributes.getTeamByName(testTeam, false).getJSONObject(TeamAttributes.TEAM_COLUMN).getBoolean(TeamAttributes.IS_FULL_BOOLEAN));

        //Make sure the is_full boolean updates correctly when someone leaves the team.
        boolean check = true;
        try{
            PersonAttributes.leaveTeam(PersonTest.testStudentNumber + indent - 1);
        } catch (SQLException e) {
            check = false;
        }
        Assertions.assertTrue(check);
        Assertions.assertFalse( TeamAttributes.getTeamByName(testTeam, false).getJSONObject(TeamAttributes.TEAM_COLUMN).getBoolean(TeamAttributes.IS_FULL_BOOLEAN));
    }

    public static synchronized boolean createTeam(int sNumb) {
        Assertions.assertNull(TeamAttributes.getTeamByName(testTeam, false));

        return TeamAttributes.createTeam(testTeam, sNumb);
    }

    public static synchronized boolean approveTeam() {
        if(!Objects.isNull(TeamAttributes.getTeamByName(testTeam, true))) {
            return TeamAttributes.approveTeam(testTeam, true);
        } else {
            return false;
        }
    }

    public static synchronized boolean removeTestTeam() {
        if(!Objects.isNull(TeamAttributes.getTeamByName(testTeam, true))) {
            return TeamAttributes.deleteTeamByTeamName(testTeam);
        } else {
            return false;
        }
    }


}
